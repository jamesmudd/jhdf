package io.jhdf.examples;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WriteHdf5Test {

	@Test
	void testWriteHdf5() {
		assertDoesNotThrow(() ->
			WriteHdf5.main(new String[]{}));

		assertTrue(Files.exists(Paths.get("jhdf.hdf5")));
	}
}
