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

package org.ddogleg.sorting;

import org.ddogleg.util.UtilDouble;
import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TestQuickSort_F32 {
	Random rand = new Random(0xfeed4);

	@Test
	void testSortingRandom() {
		float[] ret = BenchMarkSort.createRandom_F32(rand,200);

		float preTotal = UtilDouble.sum(ret);

		QuickSort_F32 sorter = new QuickSort_F32();

		sorter.sort(ret,ret.length);

		float postTotal = UtilDouble.sum(ret);

		// make sure it didn't modify the list, in an unexpected way
		assertEquals(preTotal,postTotal,1e-2);

		float prev = ret[0];
		for( int i = 1; i < ret.length; i++ ) {
			if( ret[i] < prev )
				fail("Not ascending");
			prev = ret[i];
		}
	}

	@Test
	void testSortingRandom_indexes() {
		for( int a = 0; a < 20; a++ ) {
			float[] normal = BenchMarkSort.createRandom_F32(rand,20);
			float[] original = normal.clone();
			float[] withIndexes = normal.clone();
			int[] indexes = new int[ normal.length ];

			QuickSort_F32 sorter = new QuickSort_F32();

			sorter.sort(normal,normal.length);
			sorter.sort(withIndexes,0,normal.length,indexes);

			for( int i = 0; i < normal.length; i++ ) {
				// make sure the original hasn't been modified
				assertEquals(original[i],withIndexes[i],1e-4);
				// see if it produced the same results as the normal one
				assertEquals(normal[i],withIndexes[indexes[i]],1e-4);
			}
		}
	}

	@Test
	void testSortingRandom_List() {
		for( int a = 0; a < 20; a++ ) {
			float[] arrayA = BenchMarkSort.createRandom_F32(rand,20);
			float[] arrayB = arrayA.clone();
			List<Float> list = new ArrayList<>();
			for( float d : arrayA ) {
				list.add(d);
			}

			QuickSort_F32 sorter = new QuickSort_F32();

			sorter.sort(arrayA,arrayA.length);
			sorter.sort(arrayB,arrayB.length,list);

			for( int i = 0; i < arrayA.length; i++ ) {
				assertEquals(arrayA[i],arrayB[i], UtilEjml.TEST_F64);
				assertEquals(arrayA[i],list.get(i), UtilEjml.TEST_F64);
			}
		}
	}
}