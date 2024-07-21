package io.jhdf.examples;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WriteAttributesTest {
	@Test
	void testWriteAttributes() {
		assertDoesNotThrow(() ->
			WriteAttributes.main(new String[]{}));

		assertTrue(Files.exists(Paths.get("attributes.hdf5")));
	}
}
