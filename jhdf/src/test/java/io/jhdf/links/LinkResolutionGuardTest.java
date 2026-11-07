/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.links;

import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkResolutionGuardTest {

	@Test
	void testLinkResolutionCycleIsDetected() {
		HdfException exception = assertThrows(HdfException.class,
			() -> LinkResolutionGuard.resolve("same", () -> LinkResolutionGuard.resolve("same", () -> null)));

		assertThat(exception.getMessage(), containsString("Cyclic link resolution"));
	}

	@Test
	void testLinkResolutionDepthExceeded() {
		HdfException exception = assertThrows(HdfException.class, () -> resolveNestedLinks(20));

		assertThat(exception.getMessage(), containsString("maximum depth"));
	}

	private static Object resolveNestedLinks(int remaining) {
		return LinkResolutionGuard.resolve("depth:" + remaining,
			() -> remaining == 0 ? null : resolveNestedLinks(remaining - 1));
	}
}
