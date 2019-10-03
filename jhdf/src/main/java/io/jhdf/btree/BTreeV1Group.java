/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree;

import io.jhdf.HdfFileChannel;
import io.jhdf.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * V1 B-trees where the node type is 0 i.e. points to group nodes
 *
 * @author James Mudd
 */
public abstract class BTreeV1Group extends BTreeV1 {

	private BTreeV1Group(HdfFileChannel hdfFc, long address) {
		super(hdfFc, address);
	}

	/* package */ static class BTreeV1GroupLeafNode extends BTreeV1Group {

		private final List<Long> childAddresses;

		/* package */ BTreeV1GroupLeafNode(HdfFileChannel hdfFc, long address) {
			super(hdfFc, address);

			final int keyBytes = (2 * entriesUsed + 1) * hdfFc.getSizeOfLengths();
			final int childPointerBytes = (2 * entriesUsed) * hdfFc.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			final long keysAddress = address + 8 + 2 * hdfFc.getSizeOfOffsets();
			final ByteBuffer keysAndPointersBuffer = hdfFc.readBufferFromAddress(keysAddress, keysAndPointersBytes);

			childAddresses = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + hdfFc.getSizeOfLengths());
				childAddresses.add(Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, hdfFc.getSizeOfOffsets()));
			}
		}

		@Override
		public List<Long> getChildAddresses() {
			return childAddresses;
		}

	}

	/* package */ static class BTreeV1GroupNonLeafNode extends BTreeV1Group {

		private final List<BTreeV1> childNodes;

		/* package */ BTreeV1GroupNonLeafNode(HdfFileChannel hdfFc, long address) {
			super(hdfFc, address);

			final int keyBytes = (2 * entriesUsed + 1) * hdfFc.getSizeOfLengths();
			final int childPointerBytes = (2 * entriesUsed) * hdfFc.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			final long keysAddress = address + 8 + 2 * hdfFc.getSizeOfOffsets();
			final ByteBuffer keysAndPointersBuffer = hdfFc.readBufferFromAddress(keysAddress, keysAndPointersBytes);

			childNodes = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + hdfFc.getSizeOfOffsets());
				long childAddress = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, hdfFc.getSizeOfOffsets());
				childNodes.add(createGroupBTree(hdfFc, childAddress));
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
