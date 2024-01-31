/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree;

import io.jhdf.Utils;
import io.jhdf.storage.HdfBackingStorage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * V1 B-trees where the node type is 0 i.e. points to group nodes
 *
 * @author James Mudd
 */
public abstract class BTreeV1Group extends BTreeV1 {

	private BTreeV1Group(HdfBackingStorage hdfBackingStorage, long address) {
		super(hdfBackingStorage, address);
	}

	/* package */ static class BTreeV1GroupLeafNode extends BTreeV1Group {

		private final List<Long> childAddresses;

		/* package */ BTreeV1GroupLeafNode(HdfBackingStorage hdfBackingStorage, long address) {
			super(hdfBackingStorage, address);

			final int keyBytes = (2 * entriesUsed + 1) * hdfBackingStorage.getSizeOfLengths();
			final int childPointerBytes = (2 * entriesUsed) * hdfBackingStorage.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			final long keysAddress = address + 8L + 2L * hdfBackingStorage.getSizeOfOffsets();
			final ByteBuffer keysAndPointersBuffer = hdfBackingStorage.readBufferFromAddress(keysAddress, keysAndPointersBytes);

			childAddresses = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + hdfBackingStorage.getSizeOfLengths());
				childAddresses.add(Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, hdfBackingStorage.getSizeOfOffsets()));
			}
		}

		@Override
		public List<Long> getChildAddresses() {
			return childAddresses;
		}

	}

	/* package */ static class BTreeV1GroupNonLeafNode extends BTreeV1Group {

		private final List<BTreeV1> childNodes;

		/* package */ BTreeV1GroupNonLeafNode(HdfBackingStorage hdfBackingStorage, long address) {
			super(hdfBackingStorage, address);

			final int keyBytes = (2 * entriesUsed + 1) * hdfBackingStorage.getSizeOfLengths();
			final int childPointerBytes = (2 * entriesUsed) * hdfBackingStorage.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			final long keysAddress = address + 8L + 2L * hdfBackingStorage.getSizeOfOffsets();
			final ByteBuffer keysAndPointersBuffer = hdfBackingStorage.readBufferFromAddress(keysAddress, keysAndPointersBytes);

			childNodes = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + hdfBackingStorage.getSizeOfOffsets());
				long childAddress = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, hdfBackingStorage.getSizeOfOffsets());
				childNodes.add(createGroupBTree(hdfBackingStorage, childAddress));
			}

		}

		@Override
		public List<Long> getChildAddresses() {
			List<Long> childAddresses = new ArrayList<>();
			for (BTreeV1 child : childNodes) {
				childAddresses.addAll(child.getChildAddresses());
			}
			return childAddresses;
		}
	}
}
