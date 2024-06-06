/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.WritableGroup;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

class WritableGroupImplTest {

	@Test
	void createFileTree() {
		WritableGroupImpl group = new WritableGroupImpl(null, "firstGroup");
		assertThat(group.isGroup(), is(true));
		assertThat(group.getParent(), is(nullValue()));
		assertThat(group.getPath(), is("/firstGroup"));

		WritableGroup secondGroup = group.putGroup("secondGroup");
		assertThat(secondGroup.isGroup(), is(true));
		assertThat(secondGroup.getParent(), is(sameInstance(group)));
		assertThat(secondGroup.getPath(), is("/firstGroup/secondGroup"));
	}
}
