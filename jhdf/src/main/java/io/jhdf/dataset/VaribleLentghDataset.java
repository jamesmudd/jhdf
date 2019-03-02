package io.jhdf.dataset;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.jhdf.GlobalHeap;
import io.jhdf.HdfFile;
import io.jhdf.ObjectHeader;
import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.object.datatype.VariableLength;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataTypeMessage;

public class VaribleLentghDataset implements Dataset {

	private final DatasetBase wrappedDataset;
	private final ObjectHeader oh;
	private final FileChannel fc;
	private final Superblock sb;
	private VariableLength type;

	public VaribleLentghDataset(DatasetBase dataset, FileChannel fc, Superblock sb, ObjectHeader oh) {
		this.wrappedDataset = dataset;
		this.oh = oh;
		this.fc = fc;
		this.sb = sb;
		this.type = (VariableLength) oh.getMessageOfType(DataTypeMessage.class).getDataType();
	}

	private List<GlobalHeapId> getGlobalHeapIds() {
		// For variable length datasets the actual data is in the global heap so need to
		// resolve that then build the buffer.

		ByteBuffer bb = wrappedDataset.getDataBuffer().order(LITTLE_ENDIAN);
		int lentgh = type.getSize();

		List<GlobalHeapId> ids = new ArrayList<>(Math.toIntExact(getSize()));

		int skipBytes = lentgh - sb.getSizeOfOffsets() - 4; // id=4

		while (bb.remaining() >= lentgh) {
			// Move past the skipped bytes. TODO figure out what this is for
			bb.position(bb.position() + skipBytes);
			long heapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			int index = Utils.readBytesAsUnsignedInt(bb, 4);
			GlobalHeapId globalHeapId = new GlobalHeapId(heapAddress, index);
			ids.add(globalHeapId);
		}

		return ids;
	}

	@Override
	public Object getData() {
		final Map<Long, GlobalHeap> heaps = new HashMap<>();

		Charset charset = type.getEncoding();
		List<String> objects = new ArrayList<>();
		for (GlobalHeapId globalHeapId : getGlobalHeapIds()) {
			GlobalHeap heap = heaps.computeIfAbsent(globalHeapId.getHeapAddress(), this::createGlobalHeap);

			ByteBuffer bb = heap.getObjectData(globalHeapId.getIndex());
			String element = charset.decode(bb).toString();
			objects.add(element);
		}

		// Make the output array
		Object data = Array.newInstance(getJavaType(), getDimensions());
		fillData(data, getDimensions(), objects.iterator());

		return data;
	}

	private static void fillData(Object data, int[] dims, Iterator<String> objects) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), objects);
			}
		} else {
			for (int i = 0; i < dims[0]; i++) {
				Array.set(data, i, objects.next());
			}
		}
	}

	private static int[] stripLeadingIndex(int[] dims) {
		return Arrays.copyOfRange(dims, 1, dims.length);
	}

	private GlobalHeap createGlobalHeap(long address) {
		return new GlobalHeap(fc, sb, address);
	}

	// Delegate all methods to the underlying dataset

	@Override
	public Group getParent() {
		return wrappedDataset.getParent();
	}

	@Override
	public String getName() {
		return wrappedDataset.getName();
	}

	@Override
	public long getSize() {
		return wrappedDataset.getSize();
	}

	@Override
	public String getPath() {
		return wrappedDataset.getPath();
	}

	@Override
	public long getDiskSize() {
		return wrappedDataset.getDiskSize();
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return wrappedDataset.getAttributes();
	}

	@Override
	public int[] getDimensions() {
		return wrappedDataset.getDimensions();
	}

	@Override
	public NodeType getType() {
		return wrappedDataset.getType();
	}

	@Override
	public int[] getMaxSize() {
		return wrappedDataset.getMaxSize();
	}

	@Override
	public boolean isGroup() {
		return wrappedDataset.isGroup();
	}

	@Override
	public DataLayout getDataLayout() {
		return wrappedDataset.getDataLayout();
	}

	@Override
	public File getFile() {
		return wrappedDataset.getFile();
	}

	@Override
	public Class<?> getJavaType() {
		return wrappedDataset.getJavaType();
	}

	@Override
	public HdfFile getHdfFile() {
		return wrappedDataset.getHdfFile();
	}

	@Override
	public long getAddress() {
		return wrappedDataset.getAddress();
	}

	@Override
	public boolean isLink() {
		return wrappedDataset.isLink();
	}

}
