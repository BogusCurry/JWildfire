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

class Waves2Func: public Variation {
public:
	Waves2Func() {
		scalex = 0.25;
		scaley = 0.5;
		freqx = M_PI / 2.0;
		freqy = M_PI / 4.0;
		initParameterNames(4, "scalex", "scaley", "freqx", "freqy");
	}

	const char* getName() const {
		return "waves2";
	}

	void setParameter(char *pName, JWF_FLOAT pValue) {
		if (strcmp(pName, "scalex") == 0) {
			scalex = pValue;
		}
		else if (strcmp(pName, "scaley") == 0) {
			scaley = pValue;
		}
		else if (strcmp(pName, "freqx") == 0) {
			freqx = pValue;
		}
		else if (strcmp(pName, "freqy") == 0) {
			freqy = pValue;
		}
	}

	void transform(FlameTransformationContext *pContext, XForm *pXForm, XYZPoint *pAffineTP, XYZPoint *pVarTP, JWF_FLOAT pAmount) {
		pVarTP->x += pAmount * (pAffineTP->x + scalex * JWF_SIN(pAffineTP->y * freqx));
		pVarTP->y += pAmount * (pAffineTP->y + scaley * JWF_SIN(pAffineTP->x * freqy));
		if (pContext->isPreserveZCoordinate) {
			pVarTP->z += pAmount * pAffineTP->z;
		}
	}

	Waves2Func* makeCopy() {
		return new Waves2Func(*this);
	}

private:
	JWF_FLOAT scalex;
	JWF_FLOAT scaley;
	JWF_FLOAT freqx;
	JWF_FLOAT freqy;
};

