package io.jhdf.api;

public interface WritableGroup extends Group {

	WritiableDataset putDataset(String name, Object data);

	WritableGroup putGroup(String name);
}
