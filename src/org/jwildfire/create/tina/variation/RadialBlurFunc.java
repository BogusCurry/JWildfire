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

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

public class RadialBlurFunc extends VariationFunc {
  private final double gauss_rnd[] = new double[4];
  private int gauss_N;

  private static final String PARAM_ANGLE = "angle";
  private static final String[] paramNames = { PARAM_ANGLE };

  private double angle = 0.5;

  @Override
  public void transform(XFormTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double rndG = (gauss_rnd[0] + gauss_rnd[1] + gauss_rnd[2] + gauss_rnd[3] - 2);
    gauss_rnd[gauss_N] = pContext.random();
    gauss_N = (gauss_N + 1) & 3;

    double spin = pAmount * pContext.sin(angle * Math.PI * 0.5);
    double zoom = pAmount * pContext.cos(angle * Math.PI * 0.5);

    double ra = Math.sqrt(pAffineTP.x * pAffineTP.x + pAffineTP.y * pAffineTP.y);
    double alpha = Math.atan2(pAffineTP.y, pAffineTP.x) + spin * rndG;
    double sina = pContext.sin(alpha);
    double cosa = pContext.cos(alpha);
    double rz = zoom * rndG - 1;
    pVarTP.x += ra * cosa + rz * pAffineTP.x;
    pVarTP.y += ra * sina + rz * pAffineTP.y;
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[] { angle };
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_ANGLE.equalsIgnoreCase(pName))
      angle = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "radial_blur";
  }

  @Override
  public void init(FlameTransformationContext pContext, XForm pXForm) {
    gauss_rnd[0] = pContext.random();
    gauss_rnd[1] = pContext.random();
    gauss_rnd[2] = pContext.random();
    gauss_rnd[3] = pContext.random();
    gauss_N = 0;
  }

}
