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

#include "jwf_Variation.h"

class InflateZ_4Func: public Variation {
public:
	InflateZ_4Func() {
	}

	const char* getName() const {
		return "inflateZ_4";
	}

	void transform(FlameTransformationContext *pContext, XForm *pXForm, XYZPoint *pAffineTP, XYZPoint *pVarTP, JWF_FLOAT pAmount) {
		JWF_FLOAT ang1 = JWF_ATAN2(pAffineTP->y, pAffineTP->x);
		JWF_FLOAT rndm = pContext->randGen->random();

		JWF_FLOAT val1 = (M_PI_2 - ang1);
		if (rndm < 0.5) {
			val1 = -val1;
		}
		pVarTP->z += pAmount * val1 * 0.25;
	}

	InflateZ_4Func* makeCopy() {
		return new InflateZ_4Func(*this);
	}

};

