/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.quasinewton;

import org.ddogleg.optimization.*;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.functions.GradientLineFunction;
import org.ddogleg.optimization.wrap.CachedNumericalGradientLineFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQuasiNewtonBFGS extends CommonChecksUnconstrainedOptimization {

	/**
	 * Basic test that is easily solved.
	 */
	@Test
	public void basicTest() {
		QuasiNewtonBFGS alg = createAlg(new TrivialFunctionNtoS());
		
		alg.initialize(new double[]{1,1,1});

		int i = 0;
		for( ; i < 200 && !alg.iterate(); i++ ){}

		assertTrue(alg.isConverged());

		double[] found = alg.getParameters();

		assertEquals(0, found[0], 1e-4);
		assertEquals(0, found[1], 1e-4);
		assertEquals(1, found[2], 1e-4);  // no change expected in last parameter
	}

	public QuasiNewtonBFGS createAlg( FunctionNtoS function ) {
		LineSearch lineSearch = new LineSearchMore94().setConvergence(1e-3,0.9,1e-4);
		GradientLineFunction f = new CachedNumericalGradientLineFunction(function);

		QuasiNewtonBFGS alg = new QuasiNewtonBFGS(lineSearch);
		alg.setFunction(f,0);
		alg.setConvergence(1e-7,1e-7);
		return alg;
	}


	@Override
	protected UnconstrainedMinimization createSearch() {
		return FactoryOptimization.quasiNewtonBfgs(null);
	}

}
