package io.jhdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.powermock.reflect.Whitebox.invokeConstructor;

class ConstantsTest {

	@Test
	void noInstances() {
		assertThrows(AssertionError.class, () -> invokeConstructor(Constants.class));
	}


}