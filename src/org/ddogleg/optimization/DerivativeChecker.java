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

import org.ddogleg.optimization.derivative.NumericalGradientForward;
import org.ddogleg.optimization.derivative.NumericalJacobianForward_DDRM;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.ops.MatrixFeatures_D;

/**
 * Used to validate an algebraic Jacobian numerically.
 *
 * @author Peter Abeles
 */
public class DerivativeChecker {

	public static <S extends DMatrix>
	void jacobianPrint(FunctionNtoM func , FunctionNtoMxN<S> jacobian ,
					   double param[] , double tol )
	{
		jacobianPrint(func,jacobian,param,tol,Math.sqrt(UtilEjml.EPS));
	}

	public static <S extends DMatrix>
	void jacobianPrint( FunctionNtoM func , FunctionNtoMxN<S> jacobian ,
						double param[] , double tol , double differenceScale )
	{
		NumericalJacobianForward_DDRM numerical = new NumericalJacobianForward_DDRM(func,differenceScale);

		S found = jacobian.declareMatrixMxN();
		DMatrixRMaj expected = new DMatrixRMaj(func.getNumOfOutputsM(),func.getNumOfInputsN());

		jacobian.process(param,found);
		numerical.process(param,expected);

		checkJacobianShape((S) found, expected);

		System.out.println("FOUND:");
		found.print();
		System.out.println("-----------------------------");
		System.out.println("Numerical");
		expected.print();

		System.out.println("-----------------------------");
		System.out.println("Large Differences");
		for( int y = 0; y < expected.numRows; y++ ) {
			for( int x = 0; x < expected.numCols; x++ ) {
				double diff = Math.abs(found.unsafe_get(y,x)-expected.unsafe_get(y,x));
				if( diff > tol ) {
//					double e = expected.get(y,x);
//					double f = found.get(y,x);
					System.out.print("1");
				} else
					System.out.print("0");
			}
			System.out.println();
		}
	}

	public static <S extends DMatrix>
	boolean jacobian( FunctionNtoM func , FunctionNtoMxN<S> jacobian ,
					  double param[] , double tol )
	{
		return jacobian(func,jacobian,param,tol,Math.sqrt(UtilEjml.EPS));
	}

	public static <S extends DMatrix>
	boolean jacobian( FunctionNtoM func , FunctionNtoMxN<S> jacobian ,
					  double param[] , double tol ,  double differenceScale )
	{
		NumericalJacobianForward_DDRM numerical = new NumericalJacobianForward_DDRM(func,differenceScale);

		if( numerical.getNumOfOutputsM() != jacobian.getNumOfOutputsM() )
			throw new RuntimeException("M is not equal "+numerical.getNumOfOutputsM()+"  "+jacobian.getNumOfOutputsM());

		if( numerical.getNumOfInputsN() != jacobian.getNumOfInputsN() )
			throw new RuntimeException("N is not equal: "+numerical.getNumOfInputsN()+"  "+jacobian.getNumOfInputsN());

		S found = jacobian.declareMatrixMxN();
		DMatrixRMaj expected = new DMatrixRMaj(func.getNumOfOutputsM(),func.getNumOfInputsN());

		jacobian.process(param,found);
		numerical.process(param,expected);

		checkJacobianShape(found, expected);

		return MatrixFeatures_D.isIdentical(expected,found,tol);
	}

	private static <S extends DMatrix> void checkJacobianShape(S found, DMatrixRMaj expected) {
		if( expected.getNumRows() != found.getNumRows() || expected.getNumCols() != found.getNumCols() ) {
			String message = "Expected "+expected.getNumRows()+"x"+expected.getNumCols() +
					" Found "+found.getNumRows()+"x"+found.getNumCols();
			throw new RuntimeException("Unexpected jacobian shape. "+message);
		}
	}

	/**
	 * Prints out the difference using a relative error threshold
	 * @param tol fractional difference
	 */
	public static <S extends DMatrix>
	void jacobianPrintR( FunctionNtoM func , FunctionNtoMxN<S> jacobian ,
						 double param[] , double tol )
	{
		jacobianPrintR(func, jacobian, param, tol, Math.sqrt(UtilEjml.EPS));
	}

	public static <S extends DMatrix>
	void jacobianPrintR( FunctionNtoM func , FunctionNtoMxN<S> jacobian ,
						 double param[] , double tol , double differenceScale )
	{
		NumericalJacobianForward_DDRM numerical = new NumericalJacobianForward_DDRM(func,differenceScale);

		S found = jacobian.declareMatrixMxN();
		DMatrixRMaj expected = new DMatrixRMaj(func.getNumOfOutputsM(),func.getNumOfInputsN());

		jacobian.process(param,found);
		numerical.process(param,expected);

		checkJacobianShape( found, expected);

		System.out.println("FOUND:");
		found.print();
		System.out.println("-----------------------------");
		System.out.println("Numerical");
		expected.print();

		System.out.println("-----------------------------");
		System.out.println("Large Differences");
		for( int y = 0; y < expected.numRows; y++ ) {
			for( int x = 0; x < expected.numCols; x++ ) {
				double f = found.get(y,x);
				double e = expected.get(y,x);

				double max = Math.max(Math.abs(f),Math.abs(e));
				if( max == 0 ) {
					max = 1;
				}

				double diff = Math.abs(f-e)/max;
				if( diff > tol ) {
					System.out.print("1");
				} else
					System.out.print("0");
			}
			System.out.println();
		}
	}

	/**
	 * Checks the jacobian using a relative error threshold.
	 * @param tol fractional difference
	 */
	public static <S extends DMatrix>
	boolean jacobianR( FunctionNtoM func , FunctionNtoMxN<S> jacobian ,
								 double param[] , double tol )
	{
		return jacobianR(func,jacobian,param,tol,Math.sqrt(UtilEjml.EPS));
	}

	/**
	 * Checks the jacobian using a relative error threshold.
	 * @param tol fractional difference
	 */
	public static <S extends DMatrix>
	boolean jacobianR( FunctionNtoM func , FunctionNtoMxN<S> jacobian ,
								 double param[] , double tol ,  double differenceScale )
	{
		NumericalJacobianForward_DDRM numerical = new NumericalJacobianForward_DDRM(func,differenceScale);

		if( numerical.getNumOfOutputsM() != jacobian.getNumOfOutputsM() )
			throw new RuntimeException("M is not equal "+numerical.getNumOfOutputsM()+"  "+jacobian.getNumOfOutputsM());

		if( numerical.getNumOfInputsN() != jacobian.getNumOfInputsN() )
			throw new RuntimeException("N is not equal: "+numerical.getNumOfInputsN()+"  "+jacobian.getNumOfInputsN());

		S found = jacobian.declareMatrixMxN();
		DMatrixRMaj expected = new DMatrixRMaj(func.getNumOfOutputsM(),func.getNumOfInputsN());

		jacobian.process(param, found);
		numerical.process(param, expected);

		checkJacobianShape((S) found, expected);

		for( int y = 0; y < expected.numRows; y++ ) {
			for( int x = 0; x < expected.numCols; x++ ) {
				double f = found.get(y,x);
				double e = expected.get(y,x);

				double max = Math.max(Math.abs(f),Math.abs(e));
				if( max == 0 ) {
					max = 1;
				}

				double diff = Math.abs(f-e)/max;
				if( diff > tol ) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Compares the passed in gradient function to a numerical calculation.  Comparison is done using
	 * an absolute value.
	 * @return true for within tolerance and false otherwise
	 */
	public static boolean gradient( FunctionNtoS func , FunctionNtoN gradient ,
									double param[] , double tol )
	{
		return gradient(func, gradient, param, tol, Math.sqrt(UtilEjml.EPS));
	}

	public static boolean gradient( FunctionNtoS func , FunctionNtoN gradient ,
									double param[] , double tol ,  double differenceScale )
	{
		NumericalGradientForward numerical = new NumericalGradientForward(func,differenceScale);

		if( numerical.getN() != gradient.getN() )
			throw new RuntimeException("N is not equal: "+numerical.getN()
					+"  "+gradient.getN());

		int N = numerical.getN();
		double[] found = new double[N];
		double[] expected = new double[N];

		gradient.process(param, found);
		numerical.process(param,expected);

		for (int i = 0; i < N; i++) {
			if(Math.abs(found[i]-expected[i]) > tol)
				return false;
		}
		return true;
	}

	/**
	 * Compares the passed in gradient function to a numerical calculation.  Comparison is done using
	 * an absolute value.
	 * @return true for within tolerance and false otherwise
	 */
	public static boolean gradientR( FunctionNtoS func , FunctionNtoN gradient ,
									double param[] , double tol )
	{
		return gradient(func, gradient, param, tol, Math.sqrt(UtilEjml.EPS));
	}

	public static boolean gradientR( FunctionNtoS func , FunctionNtoN gradient ,
									double param[] , double tol ,  double differenceScale )
	{
		NumericalGradientForward numerical = new NumericalGradientForward(func,differenceScale);

		if( numerical.getN() != gradient.getN() )
			throw new RuntimeException("N is not equal: "+numerical.getN()
					+"  "+gradient.getN());

		int N = numerical.getN();
		double[] found = new double[N];
		double[] expected = new double[N];

		gradient.process(param, found);
		numerical.process(param,expected);

		for (int i = 0; i < N; i++) {
			double f = found[i];
			double e = expected[i];
			double max = Math.max(Math.abs(f),Math.abs(e));
			if( max == 0 ) {
				max = 1;
			}

			double diff = Math.abs(f-e)/max;
			if( diff > tol ) {
				return false;
			}
		}
		return true;
	}
}
