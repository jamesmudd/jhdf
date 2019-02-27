package io.jhdf.dataset;

public class GlobalHeapId {

	private final long heapAddress;
	private final int index;

	public GlobalHeapId(long heapAddress, int index) {
		this.heapAddress = heapAddress;
		this.index = index;
	}

	public long getHeapAddress() {
		return heapAddress;
	}

	public int getIndex() {
		return index;
	}

}
