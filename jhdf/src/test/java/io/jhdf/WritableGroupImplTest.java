package io.jhdf;

import io.jhdf.api.WritableGroup;
import io.jhdf.dataset.NoParent;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.*;

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
