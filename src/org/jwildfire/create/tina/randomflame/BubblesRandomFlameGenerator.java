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
package org.jwildfire.create.tina.randomflame;

import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.transform.XFormTransformService;
import org.jwildfire.create.tina.variation.VariationFuncList;

public class BubblesRandomFlameGenerator extends RandomFlameGenerator {

  @Override
  protected Flame createFlame() {
    Flame flame = new Flame();
    flame.setCentreX(0.0);
    flame.setCentreY(0.0);
    flame.setPixelsPerUnit(200);
    flame.setSpatialFilterRadius(1.0);
    flame.setFinalXForm(null);
    flame.getXForms().clear();
    int fncCount = ExperimentalSimpleRandomFlameGenerator.FNCLST_EXPERIMENTAL.length;
    // 1st xForm
    {
      XForm xForm = new XForm();
      flame.getXForms().add(xForm);
      xForm.setWeight(4.0 + Math.random() * 4);
      xForm.addVariation(2 + Math.random() * 4, VariationFuncList.getVariationFuncInstance("spherical", true));

      String fName;
      if (Math.random() < 0.2) {
        fName = ExperimentalSimpleRandomFlameGenerator.FNCLST_EXPERIMENTAL[(int) (Math.random() * fncCount)];
      }
      else {
        fName = Math.random() < 0.6 ? "eyefish" : "fisheye";
      }
      xForm.addVariation(0.05 + Math.random() * 0.5, VariationFuncList.getVariationFuncInstance(fName, true));
      xForm.setColorSymmetry(0.991 + Math.random() * 0.08);
      XFormTransformService.scale(xForm, 0.7 - Math.random() * 3, true, true);
      XFormTransformService.rotate(xForm, 90 - Math.random() * 180.0, true);
      XFormTransformService.localTranslate(xForm, 0.125 - 0.250 * Math.random(), 0.125 - 0.250 * Math.random(), true);
    }
    // 2nd xForm
    {
      XForm xForm = new XForm();
      flame.getXForms().add(xForm);
      xForm.setWeight(0.5 + Math.random() * 0.8);
      xForm.addVariation(0.3 + Math.random() * 0.3, VariationFuncList.getVariationFuncInstance("bubble", true));
      xForm.addVariation(4 + Math.random() * 2, VariationFuncList.getVariationFuncInstance("pre_blur", true));
      xForm.setColorSymmetry(-0.5);
      XFormTransformService.scale(xForm, 1.2 + Math.random(), true, true);
      XFormTransformService.localTranslate(xForm, 0.75 - 1.50 * Math.random(), 0.75 - 1.50 * Math.random(), true);
      XFormTransformService.rotate(xForm, 90 - Math.random() * 180.0, true);
    }

    // 3rd xForm
    {
      XForm xForm = new XForm();
      flame.getXForms().add(xForm);
      xForm.setWeight(0.5 + Math.random() * 1.5);
      String fName;
      if (Math.random() > 0.6) {
        fName = ExperimentalSimpleRandomFlameGenerator.FNCLST_EXPERIMENTAL[(int) (Math.random() * fncCount)];
      }
      else {
        fName = "linear3D";
      }
      xForm.addVariation(0.5, VariationFuncList.getVariationFuncInstance(fName, true));
      xForm.setColorSymmetry(-0.5);
      XFormTransformService.rotate(xForm, 90 - Math.random() * 180.0, true);
      XFormTransformService.localTranslate(xForm, 0.50 - 1.0 * Math.random(), 0.50 - 1.0 * Math.random(), true);
    }
    flame.distributeColors();
    return flame;
  }

  @Override
  public String getName() {
    return "Bubbles";
  }

}