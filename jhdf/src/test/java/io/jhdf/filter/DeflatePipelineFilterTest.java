/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DeflatePipelineFilterTest {

	@Test
	void decodeZeroLengthThrows() {
		DeflatePipelineFilter deflatePipelineFilter = new DeflatePipelineFilter();
		assertThrows(HdfFilterException.class, () ->  deflatePipelineFilter.decode(new byte[0], new int[0]));
	}

	@Test
	void decodeMalformedThrows()  {
		DeflatePipelineFilter deflatePipelineFilter = new DeflatePipelineFilter();
		assertThrows(HdfFilterException.class, () ->  deflatePipelineFilter.decode(new byte[]{1,2,3}, new int[0]));
	}
}
