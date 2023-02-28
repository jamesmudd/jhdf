package io.jhdf.api;

public interface WritableGroup extends Group {

	void putDataset(String name, Object data);

	WritableGroup putGroup(String name);
}
