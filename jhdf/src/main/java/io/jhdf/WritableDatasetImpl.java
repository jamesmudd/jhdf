package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritiableDataset;
import io.jhdf.dataset.DatasetBase;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.DataLayout;
import io.jhdf.storage.HdfBackingStorage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WritableDatasetImpl implements WritiableDataset {

	private final Object data;
	private final String name;

	private final DataType dataType;

	private final Group parent;

	public WritableDatasetImpl(Object data, String name, Group parent) {
		this.data = data;
		this.name = name;
		this.dataType = DataType.fromObject(data);
		this.parent = parent;
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public long getSizeInBytes() {
		return 0;
	}

	@Override
	public long getStorageInBytes() {
		return 0;
	}

	@Override
	public int[] getDimensions() {
		return Utils.getDimensions(data);
	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isCompound() {
		return false;
	}

	@Override
	public boolean isVariableLength() {
		return false;
	}

	@Override
	public long[] getMaxSize() {
		return new long[0];
	}

	@Override
	public DataLayout getDataLayout() {
		return null;
	}

	@Override
	public Object getData() {
		return null;
	}

	@Override
	public Object getDataFlat() {
		return null;
	}

	@Override
	public Object getData(long[] sliceOffset, int[] sliceDimensions) {
		return null;
	}

	@Override
	public Class<?> getJavaType() {
		return Utils.getArrayType(data);
	}

	@Override
	public DataType getDataType() {
		return dataType;
	}

	@Override
	public Object getFillValue() {
		return null;
	}

	@Override
	public List<PipelineFilterWithData> getFilters() {
		return null; // TODO empty list
	}

	@Override
	public Group getParent() {
		return parent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return parent.getPath(); // TODO add name?;
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public Attribute getAttribute(String name) {
		return null;
	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	@Override
	public boolean isGroup() {
		return false;
	}

	@Override
	public File getFile() {
		return null;
	}

	@Override
	public Path getFileAsPath() {
		return null;
	}

	@Override
	public HdfFile getHdfFile() {
		return null;
	}

	@Override
	public long getAddress() {
		return 0;
	}

	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return false;
	}
}
