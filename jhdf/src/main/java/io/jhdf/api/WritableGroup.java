package io.jhdf.api;

import java.util.Map;

public interface WritableGroup extends Group, WritableNode {

	WritiableDataset putDataset(String name, Object data);

	WritableGroup putGroup(String name);
}
