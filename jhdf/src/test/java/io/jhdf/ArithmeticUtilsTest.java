/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.exceptions.HdfArithmeticException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArithmeticUtilsTest {

	@Test
	void testCheckedArithmeticWrapsArithmeticExceptionInHdfArithmeticException() {
		HdfArithmeticException e = assertThrows(HdfArithmeticException.class,
			() -> ArithmeticUtils.addExact(Integer.MAX_VALUE, 1));
		assertThat(e.getCause(), is(instanceOf(ArithmeticException.class)));

		assertThrows(HdfArithmeticException.class, () -> ArithmeticUtils.multiplyExact(Long.MAX_VALUE, 2));
		assertThrows(HdfArithmeticException.class, () -> ArithmeticUtils.toIntExact(Long.MAX_VALUE));
		assertThrows(HdfArithmeticException.class, () -> ArithmeticUtils.floorDiv(1, 0));
	}
}
