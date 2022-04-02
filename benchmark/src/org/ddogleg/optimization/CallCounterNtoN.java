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

package org.ddogleg.optimization;

import org.ddogleg.optimization.functions.FunctionNtoN;

/**
 * Wraps around a function and counts the number of times it processes an input.
 *
 * @author Peter Abeles
 */
public class CallCounterNtoN implements FunctionNtoN {

	public int count;
	public FunctionNtoN func;

	public CallCounterNtoN(FunctionNtoN func) {
		this.func = func;
	}

	@Override
	public int getN() {
		return func.getN();
	}

	@Override
	public void process(double[] input, double[] output) {
		count++;
		func.process(input,output);
	}
}
