/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2011 Andreas Maschke

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
package org.jwildfire.create.tina.variation;

import static org.jwildfire.base.mathlib.MathLib.M_PI_4;
import static org.jwildfire.base.mathlib.MathLib.cos;
import static org.jwildfire.base.mathlib.MathLib.sin;
import static org.jwildfire.create.tina.base.Constants.AVAILABILITY_CUDA;
import static org.jwildfire.create.tina.base.Constants.AVAILABILITY_JWILDFIRE;

import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

public class XHeartFunc extends VariationFunc {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_ANGLE = "angle";
  private static final String PARAM_RATIO = "ratio";
  private static final String[] paramNames = { PARAM_ANGLE, PARAM_RATIO };

  private double angle = 0.0;
  private double ratio = 0.0;
  // derived variables
  private double rat, cosa, sina;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // xheart by xyrus02, http://xyrus02.deviantart.com/art/XHeart-Plugin-139866412
    double r2_4 = pAffineTP.x * pAffineTP.x + pAffineTP.y * pAffineTP.y + 4;
    if (r2_4 == 0)
      r2_4 = 1;
    double bx = 4 / r2_4, by = rat / r2_4;
    double x = cosa * (bx * pAffineTP.x) - sina * (by * pAffineTP.y);
    double y = sina * (bx * pAffineTP.x) + cosa * (by * pAffineTP.y);

    if (x > 0) {
      pVarTP.x += pAmount * x;
      pVarTP.y += pAmount * y;
    }
    else {
      pVarTP.x += pAmount * x;
      pVarTP.y += -pAmount * y;
    }
    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[] { angle, ratio };
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_ANGLE.equalsIgnoreCase(pName))
      angle = pValue;
    else if (PARAM_RATIO.equalsIgnoreCase(pName))
      ratio = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "xheart";
  }

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    double ang = M_PI_4 + (0.5 * M_PI_4 * angle);
    sina = sin(ang);
    cosa = cos(ang);
    rat = 6 + 2 * ratio;
  }

  @Override
  public int getAvailability() {
    return AVAILABILITY_JWILDFIRE | AVAILABILITY_CUDA;
  }

}
