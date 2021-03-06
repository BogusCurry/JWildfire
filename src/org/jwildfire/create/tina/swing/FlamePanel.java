/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2012 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina.swing;

import static org.jwildfire.base.mathlib.MathLib.M_PI;
import static org.jwildfire.base.mathlib.MathLib.cos;
import static org.jwildfire.base.mathlib.MathLib.sin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jwildfire.base.Prefs;
import org.jwildfire.base.Tools;
import org.jwildfire.base.mathlib.MathLib;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import org.jwildfire.create.tina.random.RandomGeneratorFactory;
import org.jwildfire.create.tina.random.RandomGeneratorType;
import org.jwildfire.create.tina.render.FlameRenderer;
import org.jwildfire.create.tina.transform.XFormTransformService;
import org.jwildfire.create.tina.variation.FlameTransformationContext;
import org.jwildfire.create.tina.variation.Variation;
import org.jwildfire.image.SimpleImage;
import org.jwildfire.swing.ImagePanel;

public class FlamePanel extends ImagePanel {
  private final static int BORDER = 20;
  public static final Color XFORM_COLOR = new Color(217, 219, 223);
  private static final Color XFORM_POST_COLOR = new Color(255, 219, 160);
  public static final Color XFORM_COLOR_DARK = new Color(17, 19, 23);
  private static final Color XFORM_POST_COLOR_DARK = new Color(55, 19, 60);
  private static final Color BACKGROUND_COLOR = new Color(60, 60, 60);
  private static final Color VARIATION_COLOR = new Color(187, 189, 193);

  private static BasicStroke SELECTED_LINE = new BasicStroke(1.6f);
  private static BasicStroke NORMAL_LINE = new BasicStroke(1.0f);

  private static final long serialVersionUID = 1L;
  private FlameHolder flameHolder;
  private LayerHolder layerHolder;

  boolean darkTriangles = false;
  private boolean drawImage = true;
  private boolean drawTriangles = true;
  private boolean drawVariations = false;
  private boolean fineMovement = false;
  private XForm selectedXForm = null;
  private boolean allowScaleX = true;
  private boolean allowScaleY = true;
  private boolean showTransparency = false;

  private double triangleViewXScale, triangleViewYScale;
  private double triangleViewXTrans, triangleViewYTrans;
  private MouseDragOperation mouseDragOperation = MouseDragOperation.MOVE_TRIANGLE;
  private int xBeginDrag, yBeginDrag;
  private int xMouseClickPosition, yMouseClickPosition;
  private boolean editPostTransform = false;
  private double triangleZoom = 1.0;

  private int renderWidth;
  private int renderHeight;
  private double renderAspect = 1.0;

  private int selectedPoint = 1;

  private boolean redrawAfterMouseClick;
  private boolean reRender;
  private UndoManagerHolder<Flame> undoManagerHolder;
  private GradientOverlay gradientOverlay = new GradientOverlay(this);

  public FlamePanel(SimpleImage pSimpleImage, int pX, int pY, int pWidth, FlameHolder pFlameHolder, LayerHolder pLayerHolder) {
    super(pSimpleImage, pX, pY, pWidth);
    flameHolder = pFlameHolder;
    layerHolder = pLayerHolder;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    fillBackground(g);
    initTriangleView();
    if (drawImage) {
      drawImage(g);
    }
    if (drawVariations) {
      paintVariations((Graphics2D) g);
    }
    if (drawTriangles) {
      paintTriangles((Graphics2D) g);
    }
    if (mouseDragOperation == MouseDragOperation.GRADIENT && flameHolder.getFlame() != null && layerHolder != null) {
      gradientOverlay.paintGradient((Graphics2D) g, layerHolder.getLayer().getPalette(), this.getImageBounds());
    }
  }

  private void fillBackground(Graphics g) {
    Rectangle bounds = this.getBounds();
    if (showTransparency) {
      SimpleImage bgImg = getTransparencyImg(bounds.width, bounds.height);
      g.drawImage(bgImg.getBufferedImg(), 0, 0, bounds.width, bounds.height, this);
    }
    else {
      g.setColor(BACKGROUND_COLOR);
      g.fillRect(0, 0, bounds.width, bounds.height);
    }
  }

  private SimpleImage _transpBGImg = null;

  private SimpleImage getTransparencyImg(int pWidth, int pHeight) {
    if (_transpBGImg != null && (_transpBGImg.getImageWidth() != pWidth || _transpBGImg.getImageHeight() != pHeight)) {
      _transpBGImg = null;
    }
    if (_transpBGImg == null) {
      _transpBGImg = new SimpleImage(pWidth, pHeight);
      final int SQUARE_SIZE = 8;
      final int BRIGHT_COLOR = 250;
      final int DARK_COLOR = 200;
      for (int i = 0; i < pHeight; i++) {
        for (int j = 0; j < pWidth; j++) {
          boolean hAlt = (i / SQUARE_SIZE) % 2 == 0;
          boolean wAlt = (j / SQUARE_SIZE) % 2 == 0;
          boolean alt = wAlt ^ hAlt;
          int color = alt ? BRIGHT_COLOR : DARK_COLOR;
          _transpBGImg.setARGB(j, i, 255, color, color, color);
        }
      }
    }
    return _transpBGImg;
  }

  private class Triangle {
    private final XForm xForm;
    double x[] = new double[3];
    double y[] = new double[3];
    int viewX[] = new int[3];
    int viewY[] = new int[3];

    public Triangle(XForm pXForm) {
      xForm = pXForm;
      // x
      x[0] = 1.0;
      y[0] = 0.0;
      // 0
      x[1] = 0.0;
      y[1] = 0.0;
      // y
      x[2] = 0.0;
      y[2] = 1.0;

      // transform the points (affine transform)
      for (int i = 0; i < x.length; i++) {
        //          
        // use the same layout as Apophysis
        double tx = affineTransformedX(x[i], y[i]);
        double ty = affineTransformedY(x[i], y[i]);
        viewX[i] = triangleXToView(tx);
        viewY[i] = triangleYToView(ty);
      }
    }

    public double getC00() {
      return editPostTransform ? xForm.getPostCoeff00() : xForm.getCoeff00();
    }

    public double getC01() {
      return editPostTransform ? xForm.getPostCoeff01() : xForm.getCoeff01();
    }

    public double getC10() {
      return editPostTransform ? xForm.getPostCoeff10() : xForm.getCoeff10();
    }

    public double getC11() {
      return editPostTransform ? xForm.getPostCoeff11() : xForm.getCoeff11();
    }

    public double getC20() {
      return editPostTransform ? xForm.getPostCoeff20() : xForm.getCoeff20();
    }

    public double getC21() {
      return editPostTransform ? xForm.getPostCoeff21() : xForm.getCoeff21();
    }

    public double affineTransformedX(double pX, double pY) {
      //      return pX * xForm.getCoeff00() + pY * xForm.getCoeff10() + xForm.getCoeff20();
      // use the same layout as Apophysis
      return pX * getC00() + (-pY * getC10()) + getC20();
    }

    public double affineTransformedY(double pX, double pY) {
      //      return pX * xForm.getCoeff01() + pY * xForm.getCoeff11() + xForm.getCoeff21();
      // use the same layout as Apophysis
      return (-pX * getC01()) + pY * getC11() + (-getC21());
    }
  }

  public Rectangle getImageBounds() {
    Rectangle bounds = this.getBounds();
    double aspect = (double) bounds.width / (double) bounds.height;
    int imageWidth, imageHeight;
    if (aspect <= renderAspect) {
      imageWidth = bounds.width;
      imageHeight = Tools.FTOI((double) imageWidth / renderAspect);
    }
    else {
      imageHeight = bounds.height;
      imageWidth = Tools.FTOI((double) imageHeight * renderAspect);
    }
    //    System.out.println(bounds.width + "x" + bounds.height + "->" + imageWidth + "x" + imageHeight);
    return new Rectangle(0, 0, imageWidth, imageHeight);
  }

  private boolean initViewFlag = false;

  private void initTriangleView() {
    if (initViewFlag) {
      return;
    }
    else {
      initViewFlag = true;
    }
    double viewXMin = -2.5;
    double viewXMax = 1.75;
    double viewYMin = -1.75;
    double viewYMax = 2.5;

    Rectangle bounds = this.getImageBounds();
    int width = bounds.width;
    int height = bounds.height;

    int areaLeft = BORDER;
    // unused:
    //      int areaRight = width - 1 - BORDER;
    //      int areaTop = BORDER;
    int areaBottom = height - 1 - BORDER;

    triangleViewXScale = (double) (width - 2 * BORDER) / (viewXMax - viewXMin);
    triangleViewYScale = (double) (height - 2 * BORDER) / (viewYMin - viewYMax);
    triangleViewXTrans = viewXMin * triangleViewXScale - areaLeft;
    triangleViewYTrans = viewYMin * triangleViewYScale - areaBottom;
    triangleViewXScale /= renderAspect;
  }

  private void paintTriangles(Graphics2D g) {
    if (layerHolder != null) {
      Layer layer = layerHolder.getLayer();
      if (layer != null) {
        g.setColor(editPostTransform ? (darkTriangles ? XFORM_POST_COLOR_DARK : XFORM_POST_COLOR) : (darkTriangles ? XFORM_COLOR_DARK : XFORM_COLOR));
        for (XForm xForm : layer.getXForms()) {
          drawXForm(g, xForm, false);
        }
        for (XForm xForm : layer.getFinalXForms()) {
          drawXForm(g, xForm, true);
        }
      }
    }
  }

  private void paintVariations(Graphics2D g) {
    if (selectedXForm != null && selectedXForm.getVariationCount() > 0) {
      try {
        selectedXForm.initTransform();
        for (Variation var : selectedXForm.getVariations()) {
          var.getFunc().init(getFlameTransformationContext(), layerHolder != null ? layerHolder.getLayer() : null, selectedXForm, var.getAmount());
        }

        // TODO
        double xMin = -1.0;
        double xMax = 1.0;
        int xSteps = 32;
        double yMin = -1.0;
        double yMax = 1.0;
        int ySteps = 32;
        //
        double xs = (xMax - xMin) / (double) (xSteps - 1.0);
        double ys = (yMax - yMin) / (double) (ySteps - 1.0);
        int xx[][] = new int[ySteps][xSteps];
        int yy[][] = new int[ySteps][xSteps];
        {
          double y = yMin;
          XYZPoint affineT = new XYZPoint(); // affine part of the transformation
          XYZPoint varT = new XYZPoint(); // complete transformation
          XYZPoint p = new XYZPoint();
          for (int i = 0; i < ySteps; i++) {
            double x = xMin;
            for (int j = 0; j < xSteps; j++) {
              p.x = x;
              p.y = y;

              p.z = 0.0;
              selectedXForm.transformPoint(getFlameTransformationContext(), affineT, varT, p, p);
              xx[i][j] = triangleXToView(p.x);
              yy[i][j] = triangleYToView(-p.y);
              x += xs;
            }
            y += ys;
          }
        }

        g.setColor(VARIATION_COLOR);
        g.setStroke(NORMAL_LINE);

        for (int y = 0; y < ySteps - 1; y++) {
          for (int x = 0; x < xSteps - 1; x++) {
            g.drawLine(xx[y][x], yy[y][x], xx[y][x + 1], yy[y][x + 1]);
            g.drawLine(xx[y][x], yy[y][x], xx[y + 1][x], yy[y + 1][x]);
            if (x == xSteps - 2) {
              g.drawLine(xx[y][x + 1], yy[y][x + 1], xx[y + 1][x + 1], yy[y + 1][x + 1]);
            }
            if (y == ySteps - 2) {
              g.drawLine(xx[y + 1][x], yy[y + 1][x], xx[y + 1][x + 1], yy[y + 1][x + 1]);
            }
          }
        }

      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private void drawXForm(Graphics2D g, XForm pXForm, boolean pIsFinal) {
    Triangle triangle = new Triangle(pXForm);
    boolean isSelected = (selectedXForm != null && selectedXForm == pXForm);
    g.setStroke(isSelected ? SELECTED_LINE : NORMAL_LINE);
    g.drawPolygon(triangle.viewX, triangle.viewY, 3);
    if (isSelected) {
      g.setStroke(NORMAL_LINE);
      // selected point
      {
        int radius = 10;
        g.fillOval(triangle.viewX[selectedPoint] - radius / 2, triangle.viewY[selectedPoint] - radius / 2, radius, radius);
      }
      // axes
      {
        int offset = 16;
        int cx = (triangle.viewX[0] + triangle.viewX[1] + triangle.viewX[2]) / 3;
        int cy = (triangle.viewY[0] + triangle.viewY[1] + triangle.viewY[2]) / 3;
        final String label = "xoy";
        for (int i = 0; i < triangle.viewX.length; i++) {
          double dx = triangle.viewX[i] - cx;
          double dy = triangle.viewY[i] - cy;
          double dr = MathLib.sqrt(dx * dx + dy * dy) + MathLib.EPSILON;
          dx /= dr;
          dy /= dr;
          g.drawString(String.valueOf(label.charAt(i)), triangle.viewX[i] + (int) (offset * dx), triangle.viewY[i] + (int) (offset * dy));
        }
      }
    }
  }

  private int triangleXToView(double pX) {
    return Tools.FTOI(triangleViewXScale * triangleZoom * pX - triangleViewXTrans);
  }

  private int triangleYToView(double pY) {
    return Tools.FTOI(triangleViewYScale * triangleZoom * pY - triangleViewYTrans);
  }

  private double triangleViewToX(int viewX) {
    return ((double) viewX + triangleViewXTrans) / (triangleViewXScale * triangleZoom);
  }

  private double triangleViewToY(int viewY) {
    return ((double) viewY + triangleViewYTrans) / (triangleViewYScale * triangleZoom);
  }

  public void setDrawImage(boolean drawImage) {
    this.drawImage = drawImage;
  }

  public void setDrawTriangles(boolean drawTriangles) {
    this.drawTriangles = drawTriangles;
  }

  public void setSelectedXForm(XForm selectedXForm) {
    this.selectedXForm = selectedXForm;
  }

  public boolean mouseDragged(int pX, int pY, boolean pLeftButton, boolean pRightButton, boolean pMiddleButton) {
    reRender = true;
    int viewDX = pX - xBeginDrag;
    int viewDY = pY - yBeginDrag;
    if (viewDX != 0 || viewDY != 0) {
      double dx = triangleViewToX(pX) - triangleViewToX(xBeginDrag);
      double dy = triangleViewToY(pY) - triangleViewToY(yBeginDrag);
      xBeginDrag = pX;
      yBeginDrag = pY;
      if (Math.abs(dx) > MathLib.EPSILON || Math.abs(dy) > MathLib.EPSILON) {
        switch (mouseDragOperation) {
          case MOVE_TRIANGLE: {
            if (selectedXForm == null) {
              return false;
            }
            // Move
            if (pLeftButton && !pRightButton) {
              if (fineMovement) {
                dx *= 0.25;
                dy *= 0.25;
              }
              // move
              if (editPostTransform) {
                selectedXForm.setPostCoeff20(selectedXForm.getPostCoeff20() + dx);
                selectedXForm.setPostCoeff21(selectedXForm.getPostCoeff21() - dy);
              }
              else {
                selectedXForm.setCoeff20(selectedXForm.getCoeff20() + dx);
                selectedXForm.setCoeff21(selectedXForm.getCoeff21() - dy);
              }
              return true;
            }
            // rotate
            else if (pRightButton) {
              if (fineMovement) {
                dx *= 0.1;
              }
              XFormTransformService.rotate(selectedXForm, dx * 30, editPostTransform);
              return true;
            }
            // zoom
            else if (pMiddleButton && !pLeftButton && !pRightButton) {
              if (fineMovement) {
                dx *= 0.1;
                dy *= 0.1;
              }
              Triangle triangle = new Triangle(selectedXForm);
              double v1x = triangle.x[0] - triangle.x[1];
              double v1y = triangle.y[0] - triangle.y[1];
              double v2x = v1x + dx;
              double v2y = v1y + dy;
              double dr1 = Math.sqrt(v1x * v1x + v1y * v1y);
              double dr2 = Math.sqrt(v2x * v2x + v2y * v2y);
              double scale = dr2 / dr1;
              if (editPostTransform) {
                if (allowScaleX) {
                  selectedXForm.setPostCoeff00(selectedXForm.getPostCoeff00() * scale);
                  selectedXForm.setPostCoeff01(selectedXForm.getPostCoeff01() * scale);
                }
                if (allowScaleY) {
                  selectedXForm.setPostCoeff10(selectedXForm.getPostCoeff10() * scale);
                  selectedXForm.setPostCoeff11(selectedXForm.getPostCoeff11() * scale);
                }
              }
              else {
                if (allowScaleX) {
                  selectedXForm.setCoeff00(selectedXForm.getCoeff00() * scale);
                  selectedXForm.setCoeff01(selectedXForm.getCoeff01() * scale);
                }
                if (allowScaleY) {
                  selectedXForm.setCoeff10(selectedXForm.getCoeff10() * scale);
                  selectedXForm.setCoeff11(selectedXForm.getCoeff11() * scale);
                }
              }
              return true;
            }
          }
          case ROTATE_TRIANGLE: {
            if (selectedXForm == null) {
              return false;
            }
            if (fineMovement) {
              dx *= 0.1;
            }
            XFormTransformService.rotate(selectedXForm, dx * 30, editPostTransform);
            return true;
          }
          case SCALE_TRIANGLE: {
            if (selectedXForm == null) {
              return false;
            }
            if (fineMovement) {
              dx *= 0.1;
              dy *= 0.1;
            }
            Triangle triangle = new Triangle(selectedXForm);
            double v1x = triangle.x[0] - triangle.x[1];
            double v1y = triangle.y[0] - triangle.y[1];
            double v2x = v1x + dx;
            double v2y = v1y + dy;
            double dr1 = Math.sqrt(v1x * v1x + v1y * v1y);
            double dr2 = Math.sqrt(v2x * v2x + v2y * v2y);
            double scale = dr2 / dr1;
            if (editPostTransform) {
              if (allowScaleX) {
                selectedXForm.setPostCoeff00(selectedXForm.getPostCoeff00() * scale);
                selectedXForm.setPostCoeff01(selectedXForm.getPostCoeff01() * scale);
              }
              if (allowScaleY) {
                selectedXForm.setPostCoeff10(selectedXForm.getPostCoeff10() * scale);
                selectedXForm.setPostCoeff11(selectedXForm.getPostCoeff11() * scale);
              }
            }
            else {
              if (allowScaleX) {
                selectedXForm.setCoeff00(selectedXForm.getCoeff00() * scale);
                selectedXForm.setCoeff01(selectedXForm.getCoeff01() * scale);
              }
              if (allowScaleY) {
                selectedXForm.setCoeff10(selectedXForm.getCoeff10() * scale);
                selectedXForm.setCoeff11(selectedXForm.getCoeff11() * scale);
              }
            }
            return true;
          }
          case POINTS: {
            if (selectedXForm == null) {
              return false;
            }
            if (fineMovement) {
              dx *= 0.25;
              dy *= 0.25;
            }
            switch (selectedPoint) {
              case 0:
                if (editPostTransform) {
                  selectedXForm.setPostCoeff00(selectedXForm.getPostCoeff00() + dx);
                  selectedXForm.setPostCoeff01(selectedXForm.getPostCoeff01() - dy);
                }
                else {
                  selectedXForm.setCoeff00(selectedXForm.getCoeff00() + dx);
                  selectedXForm.setCoeff01(selectedXForm.getCoeff01() - dy);
                }
                break;
              case 1:
                if (editPostTransform) {
                  selectedXForm.setPostCoeff20(selectedXForm.getPostCoeff20() + dx);
                  selectedXForm.setPostCoeff21(selectedXForm.getPostCoeff21() - dy);

                  selectedXForm.setPostCoeff10(selectedXForm.getPostCoeff10() + dx);
                  selectedXForm.setPostCoeff11(selectedXForm.getPostCoeff11() - dy);

                  selectedXForm.setPostCoeff00(selectedXForm.getPostCoeff00() - dx);
                  selectedXForm.setPostCoeff01(selectedXForm.getPostCoeff01() + dy);
                }
                else {
                  selectedXForm.setCoeff20(selectedXForm.getCoeff20() + dx);
                  selectedXForm.setCoeff21(selectedXForm.getCoeff21() - dy);

                  selectedXForm.setCoeff10(selectedXForm.getCoeff10() + dx);
                  selectedXForm.setCoeff11(selectedXForm.getCoeff11() - dy);

                  selectedXForm.setCoeff00(selectedXForm.getCoeff00() - dx);
                  selectedXForm.setCoeff01(selectedXForm.getCoeff01() + dy);
                }
                break;
              case 2:
                if (editPostTransform) {
                  selectedXForm.setPostCoeff10(selectedXForm.getPostCoeff10() - dx);
                  selectedXForm.setPostCoeff11(selectedXForm.getPostCoeff11() + dy);
                }
                else {
                  selectedXForm.setCoeff10(selectedXForm.getCoeff10() - dx);
                  selectedXForm.setCoeff11(selectedXForm.getCoeff11() + dy);
                }
                break;
            }
            return true;
          }
          case VIEW: {
            if (flameHolder != null && flameHolder.getFlame() != null) {
              Flame flame = flameHolder.getFlame();
              // Move
              if (pLeftButton && !pRightButton) {
                double cosa = cos(M_PI * (flame.getCamRoll()) / 180.0);
                double sina = sin(M_PI * (flame.getCamRoll()) / 180.0);
                double rcX = dx * cosa - dy * sina;
                double rcY = dy * cosa + dx * sina;

                if (fineMovement) {
                  rcX *= 0.1;
                  rcY *= 0.1;
                }
                flame.setCentreX(flame.getCentreX() - rcX * 0.3);
                flame.setCentreY(flame.getCentreY() + rcY * 0.3);
                return true;
              }
              // rotate
              else if (pRightButton) {
                if (fineMovement) {
                  dx *= 0.1;
                  dy *= 0.1;
                }
                if (pLeftButton) {
                  flame.setCamYaw(flame.getCamYaw() + 12 * dx);
                }
                else {
                  flame.setCamRoll(flame.getCamRoll() - 12 * dx);
                  //                  flame.setCamPitch(flame.getCamPitch() + 12 * dy);
                }
                return true;
              }
              // zoom
              else if (pMiddleButton && !pLeftButton && !pRightButton) {
                if (fineMovement) {
                  dx *= 0.1;
                  dy *= 0.1;
                }
                flame.setPixelsPerUnit(flame.getPixelsPerUnit() + 2.0 * dx);
                return true;
              }
            }
          }
          case FOCUS: {
            Flame flame = flameHolder.getFlame();
            double cosa = cos(M_PI * (flame.getCamRoll()) / 180.0);
            double sina = sin(M_PI * (flame.getCamRoll()) / 180.0);
            double rcX = dx * cosa - dy * sina;
            double rcY = dy * cosa + dx * sina;
            if (fineMovement) {
              rcX *= 0.1;
              rcY *= 0.1;
            }
            flame.setFocusX(flame.getFocusX() + rcX * 0.5);
            flame.setFocusY(flame.getFocusY() - rcY * 0.5);
            return true;
          }
          case GRADIENT: {
            Layer layer = layerHolder.getLayer();
            dx = viewDX * 0.25;
            if (fineMovement) {
              dx *= 0.25;
            }
            reRender = gradientOverlay.mouseDragged(dx, xBeginDrag, yBeginDrag, layer);
            return true;
          }
        }
      }
    }
    return false;
  }

  public void mousePressed(int x, int y) {
    if (selectedXForm != null || mouseDragOperation == MouseDragOperation.VIEW || mouseDragOperation == MouseDragOperation.GRADIENT) {
      xMouseClickPosition = xBeginDrag = x;
      yMouseClickPosition = yBeginDrag = y;
      gradientOverlay.beginDrag(xMouseClickPosition, yMouseClickPosition);
      if (undoManagerHolder != null) {
        undoManagerHolder.saveUndoPoint();
      }
    }
  }

  // Algorithm from http://www.blackpawn.com/texts/pointinpoly/default.html
  private boolean insideTriange(Triangle pTriangle, int viewX, int viewY) {
    // Compute vectors
    //    v0 = C - A
    //    v1 = B - A
    //    v2 = P - A
    // A: pTriangle,view[0]
    // B: pTriangle,view[1]
    // C: pTriangle,view[2]
    double v0x = pTriangle.viewX[2] - pTriangle.viewX[0];
    double v0y = pTriangle.viewY[2] - pTriangle.viewY[0];
    double v1x = pTriangle.viewX[1] - pTriangle.viewX[0];
    double v1y = pTriangle.viewY[1] - pTriangle.viewY[0];
    double v2x = viewX - pTriangle.viewX[0];
    double v2y = viewY - pTriangle.viewY[0];
    // Compute dot products
    double dot00 = v0x * v0x + v0y * v0y;
    double dot01 = v0x * v1x + v0y * v1y;
    double dot02 = v0x * v2x + v0y * v2y;
    double dot11 = v1x * v1x + v1y * v1y;
    double dot12 = v1x * v2x + v1y * v2y;

    // Compute barycentric coordinates
    double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
    double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
    double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

    // Check if point is in triangle
    return (u >= 0) && (v >= 0) && (u + v < 1);
  }

  private int selectNearestPoint(Triangle pTriangle, int viewX, int viewY) {
    double dx, dy;
    dx = pTriangle.viewX[0] - viewX;
    dy = pTriangle.viewY[0] - viewY;
    double dr0 = MathLib.sqrt(dx * dx + dy * dy);
    dx = pTriangle.viewX[1] - viewX;
    dy = pTriangle.viewY[1] - viewY;
    double dr1 = MathLib.sqrt(dx * dx + dy * dy);
    dx = pTriangle.viewX[2] - viewX;
    dy = pTriangle.viewY[2] - viewY;
    double dr2 = MathLib.sqrt(dx * dx + dy * dy);
    return dr0 < dr1 ? (dr0 < dr2 ? 0 : 2) : (dr1 < dr2 ? 1 : 2);
  }

  public XForm mouseClicked(int x, int y) {
    redrawAfterMouseClick = false;
    // select flame
    if (mouseDragOperation == MouseDragOperation.MOVE_TRIANGLE || mouseDragOperation == MouseDragOperation.ROTATE_TRIANGLE || mouseDragOperation == MouseDragOperation.SCALE_TRIANGLE) {
      Layer layer = layerHolder.getLayer();
      if (layer != null) {
        for (XForm xForm : layer.getXForms()) {
          Triangle triangle = new Triangle(xForm);
          if (insideTriange(triangle, x, y)) {
            if (mouseDragOperation == MouseDragOperation.POINTS) {
              selectedPoint = selectNearestPoint(triangle, x, y);
              redrawAfterMouseClick = true;
              reRender = true;
            }
            return xForm;
          }
        }
        for (XForm xForm : layer.getFinalXForms()) {
          Triangle triangle = new Triangle(xForm);
          if (insideTriange(triangle, x, y)) {
            if (mouseDragOperation == MouseDragOperation.POINTS) {
              selectedPoint = selectNearestPoint(triangle, x, y);
              redrawAfterMouseClick = true;
              reRender = true;
            }
            return xForm;
          }
        }
      }
    }
    // select nearest point
    else if (mouseDragOperation == MouseDragOperation.POINTS && selectedXForm != null) {
      Triangle triangle = new Triangle(selectedXForm);
      selectedPoint = selectNearestPoint(triangle, x, y);
      redrawAfterMouseClick = true;
      reRender = true;
      return selectedXForm;
    }
    else if (mouseDragOperation == MouseDragOperation.GRADIENT && flameHolder.getFlame() != null && layerHolder != null && layerHolder.getLayer() != null) {
      if (undoManagerHolder != null) {
        undoManagerHolder.saveUndoPoint();
      }
      redrawAfterMouseClick = gradientOverlay.mouseClicked(x, y, layerHolder.getLayer().getPalette());
      reRender = false;
    }
    return null;
  }

  public void setMouseDragOperation(MouseDragOperation mouseDragOperation) {
    this.mouseDragOperation = mouseDragOperation;
    selectedPoint = 1;
  }

  public void setEditPostTransform(boolean pEditPostTransform) {
    editPostTransform = pEditPostTransform;
  }

  public void zoomOut() {
    if (triangleZoom > 0.2) {
      triangleZoom -= 0.1;
    }
    else if (triangleZoom > 0.05) {
      triangleZoom -= 0.01;
    }
    else {
      triangleZoom -= 0.001;
    }
  }

  public void zoomIn() {
    triangleZoom += 0.1;
  }

  public void setRenderWidth(int pRenderWidth) {
    renderWidth = pRenderWidth;
    updateRenderAspect();
  }

  private void updateRenderAspect() {
    if (renderHeight > 0) {
      renderAspect = (double) renderWidth / (double) renderHeight;
    }
  }

  public void setRenderHeight(int pRenderHeight) {
    renderHeight = pRenderHeight;
    updateRenderAspect();
  }

  public void setFineMovement(boolean pFineMovement) {
    fineMovement = pFineMovement;
  }

  public void setDarkTriangles(boolean darkTriangles) {
    this.darkTriangles = darkTriangles;
  }

  public void setAllowScaleX(boolean allowScaleX) {
    this.allowScaleX = allowScaleX;
  }

  public void setAllowScaleY(boolean allowScaleY) {
    this.allowScaleY = allowScaleY;
  }

  public XForm getSelectedXForm() {
    return selectedXForm;
  }

  public void setDrawVariations(boolean drawVariations) {
    this.drawVariations = drawVariations;
  }

  private FlameRenderer flameRenderer = null;

  private FlameRenderer getFlameRenderer() {
    if (flameRenderer == null) {
      Flame flame = flameHolder.getFlame();
      if (flame == null) {
        throw new IllegalStateException();
      }
      Prefs prefs = new Prefs();
      try {
        prefs.loadFromFile();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
      flameRenderer = new FlameRenderer(flame, prefs, showTransparency, false);
    }
    return flameRenderer;
  }

  private FlameTransformationContext flameTransformationContext = null;

  private FlameTransformationContext getFlameTransformationContext() {
    if (flameTransformationContext == null) {
      flameTransformationContext = new FlameTransformationContext(getFlameRenderer(), RandomGeneratorFactory.getInstance(RandomGeneratorType.getDefaultValue()));
      flameTransformationContext.setPreview(getFlameRenderer().isPreview());
    }
    return flameTransformationContext;
  }

  public MouseDragOperation getMouseDragOperation() {
    return mouseDragOperation;
  }

  public boolean isRedrawAfterMouseClick() {
    return redrawAfterMouseClick;
  }

  public void setUndoManagerHolder(UndoManagerHolder<Flame> undoManagerHolder) {
    this.undoManagerHolder = undoManagerHolder;
  }

  public void setShowTransparency(boolean pShowTransparency) {
    showTransparency = pShowTransparency;
  }

  public boolean mouseWheelMoved(int pRotateAmount) {
    if (pRotateAmount != 0) {
      if (mouseDragOperation == MouseDragOperation.VIEW && flameHolder != null && flameHolder.getFlame() != null) {
        Flame flame = flameHolder.getFlame();
        double dx = pRotateAmount * 3.0;
        if (fineMovement) {
          dx *= 0.1;
        }
        flame.setPixelsPerUnit(flame.getPixelsPerUnit() - dx);
        return true;
      }
      else if (mouseDragOperation == MouseDragOperation.MOVE_TRIANGLE && selectedXForm != null) {
        double dx = -pRotateAmount * 0.1;
        double dy = dx;
        if (fineMovement) {
          dx *= 0.1;
          dy *= 0.1;
        }
        Triangle triangle = new Triangle(selectedXForm);
        double v1x = triangle.x[0] - triangle.x[1];
        double v1y = triangle.y[0] - triangle.y[1];
        double v2x = v1x + dx;
        double v2y = v1y + dy;
        double dr1 = Math.sqrt(v1x * v1x + v1y * v1y);
        double dr2 = Math.sqrt(v2x * v2x + v2y * v2y);
        double scale = dr2 / dr1;
        if (editPostTransform) {
          if (allowScaleX) {
            selectedXForm.setPostCoeff00(selectedXForm.getPostCoeff00() * scale);
            selectedXForm.setPostCoeff01(selectedXForm.getPostCoeff01() * scale);
          }
          if (allowScaleY) {
            selectedXForm.setPostCoeff10(selectedXForm.getPostCoeff10() * scale);
            selectedXForm.setPostCoeff11(selectedXForm.getPostCoeff11() * scale);
          }
        }
        else {
          if (allowScaleX) {
            selectedXForm.setCoeff00(selectedXForm.getCoeff00() * scale);
            selectedXForm.setCoeff01(selectedXForm.getCoeff01() * scale);
          }
          if (allowScaleY) {
            selectedXForm.setCoeff10(selectedXForm.getCoeff10() * scale);
            selectedXForm.setCoeff11(selectedXForm.getCoeff11() * scale);
          }
        }
        return true;
      }
      else if (mouseDragOperation == MouseDragOperation.FOCUS && flameHolder != null && flameHolder.getFlame() != null) {
        double dz = -pRotateAmount * 0.1;
        if (fineMovement) {
          dz *= 0.1;
        }
        Flame flame = flameHolder.getFlame();
        flame.setFocusZ(flame.getFocusZ() + dz);
        return true;
      }
      else if (mouseDragOperation == MouseDragOperation.GRADIENT && flameHolder != null && flameHolder.getFlame() != null) {
        double dz = -pRotateAmount * 0.1;
        if (fineMovement) {
          dz *= 0.1;
        }
        gradientOverlay.mouseWheelAction(dz);
        return true;
      }
    }
    return false;
  }

  public void setFlameHolder(FlameHolder pFlameHolder) {
    flameHolder = pFlameHolder;
  }

  public void importOptions(FlamePanel pFlamePanel) {
    if (pFlamePanel != null) {
      darkTriangles = pFlamePanel.darkTriangles;
      drawImage = pFlamePanel.drawImage;
      drawTriangles = pFlamePanel.drawTriangles;
      drawVariations = pFlamePanel.drawVariations;
      fineMovement = pFlamePanel.fineMovement;
      allowScaleX = pFlamePanel.allowScaleX;
      allowScaleY = pFlamePanel.allowScaleY;
      showTransparency = pFlamePanel.showTransparency;
      mouseDragOperation = pFlamePanel.mouseDragOperation;
      editPostTransform = pFlamePanel.editPostTransform;
    }
  }

  public void gradientFade() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.fadeRange(layerHolder.getLayer().getPalette());
    }
  }

  public void gradientInvert() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.invertRange(layerHolder.getLayer().getPalette());
    }
  }

  public void gradientReverse() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.reverseRange(layerHolder.getLayer().getPalette());
    }
  }

  public void gradientSort() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.sortRange(layerHolder.getLayer().getPalette());
    }
  }

  public void gradientSelectAll() {
    if (flameHolder.getFlame() != null) {
      gradientOverlay.selectAll();
    }
  }

  public void gradientMarker_move(int pIdx, int pDeltaPos) {
    gradientOverlay.gradientMarker_move(pIdx, pDeltaPos);
  }

  public boolean gradientMarker_selectColor(int pIdx) {
    if (layerHolder.getLayer() != null) {
      return gradientOverlay.gradientMarker_selectColor(pIdx, layerHolder.getLayer().getPalette());
    }
    else {
      return false;
    }
  }

  public int getGradientFrom() {
    return gradientOverlay.getFrom();
  }

  public int getGradientTo() {
    return gradientOverlay.getTo();
  }

  public void gradientCopyRange() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.copyRange(layerHolder.getLayer().getPalette());
    }
  }

  public void gradientPasteRange() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.pasteRange(layerHolder.getLayer().getPalette());
    }
  }

  public void gradientEraseRange() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.eraseRange(layerHolder.getLayer().getPalette());
    }
  }

  public void gradientMonochrome() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.monochrome(layerHolder.getLayer().getPalette());
    }
  }

  public boolean isReRender() {
    return reRender;
  }

  public void gradientFadeAll() {
    if (layerHolder.getLayer() != null) {
      gradientOverlay.fadeAll(layerHolder.getLayer().getPalette());
    }
  }

  public void applyBalancing() {
    if (layerHolder.getLayer() != null) {
      layerHolder.getLayer().getPalette().applyBalancing();
    }
  }

  public void applyTX() {
    if (layerHolder.getLayer() != null) {
      layerHolder.getLayer().getPalette().applyTX();
    }
  }
}
