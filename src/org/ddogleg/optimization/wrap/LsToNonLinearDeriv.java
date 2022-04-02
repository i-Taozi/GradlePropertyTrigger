/*
 * Copyright (c) 2012-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ejml.data.DMatrix;

/**
 * Convert the Jacobian of a least squares function into a nonlinear optimization gradient.
 *
 * G(x) = sum 2*f'_i(x)*f_i(x)
 *
 * @author Peter Abeles
 */
public class LsToNonLinearDeriv<S extends DMatrix> implements FunctionNtoN {

	FunctionNtoM func;
	FunctionNtoMxN<S> deriv;

	double funcOutput[];
	S J; // the jacobian

	public LsToNonLinearDeriv(FunctionNtoM func,FunctionNtoMxN<S> deriv) {
		this.func = func;
		this.deriv = deriv;
		funcOutput = new double[ deriv.getNumOfOutputsM() ];
		J = deriv.declareMatrixMxN();
	}

	@Override
	public int getN() {
		return deriv.getNumOfInputsN();
	}

	@Override
	public void process(double[] input, double []output) {
		func.process(input,funcOutput);
		deriv.process(input, J);

		int N = deriv.getNumOfInputsN();
		int M = deriv.getNumOfOutputsM();
		for( int i = 0; i < N; i++ ) {
			output[i] = 0;
		}

		for( int i = 0; i < M; i++ ) {
			double f = funcOutput[i];
			for( int j = 0; j < N; j++ ) {
				output[j] += 2*f*J.get(i,j);
			}
		}
	}
}
