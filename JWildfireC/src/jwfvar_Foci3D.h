/*
 JWildfireC - an external C-based fractal-flame-renderer for JWildfire
 Copyright (C) 2012 Andreas Maschke

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

#ifndef JWFVAR_FOCI_3D_H_
#define JWFVAR_FOCI_3D_H_

#include "jwf_Variation.h"

class Foci3DFunc: public Variation {
public:
	Foci3DFunc() {
	}

	const char* getName() const {
		return "foci_3D";
	}

	void transform(FlameTransformationContext *pContext, XForm *pXForm, XYZPoint *pAffineTP, XYZPoint *pVarTP, float pAmount) {
    float expx = expf(pAffineTP->x) * 0.5f;
    float expnx = 0.25f / expx;
    float kikr, boot;
    boot = pAffineTP->z;
    kikr = atan2f(pAffineTP->y, pAffineTP->x);
    if (boot == 0.0) {
      boot = kikr;
    }
    float siny = sinf(pAffineTP->y);
    float cosy = cosf(pAffineTP->y);
    float sinz = sinf(boot);
    float cosz = cosf(boot);
    float tmp = pAmount / (expx + expnx - (cosy * cosz));

    pVarTP->x += (expx - expnx) * tmp;
    pVarTP->y += siny * tmp;
    pVarTP->z += sinz * tmp;
	}

	Foci3DFunc* makeCopy() {
		return new Foci3DFunc(*this);
	}

};

#endif // JWFVAR_FOCI_3D_H_
