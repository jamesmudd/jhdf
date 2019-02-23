package io.jhdf.btree;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

/**
 * V1 B-trees where the node type is 0 i.e. points to group nodes
 * 
 * @author James Mudd
 */
public abstract class BTreeV1Group extends BTreeV1 {

	private BTreeV1Group(FileChannel fc, Superblock sb, long address) {
		super(fc, sb, address);
	}

	/* package */ static class BTreeV1GroupLeafNode extends BTreeV1Group {

		private final List<Long> childAddresses;

		/* package */ BTreeV1GroupLeafNode(FileChannel fc, Superblock sb, long address) {
			super(fc, sb, address);

			int keyBytes = (2 * entriesUsed + 1) * sb.getSizeOfLengths();
			int childPointerBytes = (2 * entriesUsed) * sb.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			ByteBuffer keysAndPointersBuffer = ByteBuffer.allocate(keysAndPointersBytes);
			try {
				fc.read(keysAndPointersBuffer, address + 8 + 2 * sb.getSizeOfOffsets());
			} catch (IOException e) {
				throw new HdfException("Error reading BTreeV1NonLeafNode at address: " + address);
			}
			keysAndPointersBuffer.order(LITTLE_ENDIAN);
			keysAndPointersBuffer.rewind();

			childAddresses = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + sb.getSizeOfLengths());
				childAddresses.add(Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfOffsets()));
			}
		}

		@Override
		public List<Long> getChildAddresses() {
			return childAddresses;
		}

	}

	/* package */ static class BTreeV1GroupNonLeafNode extends BTreeV1Group {

		private final List<BTreeV1> childNodes;

		/* package */ BTreeV1GroupNonLeafNode(FileChannel fc, Superblock sb, long address) {
			super(fc, sb, address);

			int keyBytes = (2 * entriesUsed + 1) * sb.getSizeOfLengths();
			int childPointerBytes = (2 * entriesUsed) * sb.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			ByteBuffer keysAndPointersBuffer = ByteBuffer.allocate(keysAndPointersBytes);
			try {
				fc.read(keysAndPointersBuffer, address + 8 + 2 * sb.getSizeOfOffsets());
			} catch (IOException e) {
				throw new HdfException("Error reading BTreeV1NonLeafNode at address: " + address);
			}
			keysAndPointersBuffer.order(LITTLE_ENDIAN);
			keysAndPointersBuffer.rewind();

			childNodes = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + sb.getSizeOfOffsets());
				long childAddress = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfOffsets());
				childNodes.add(createGroupBTree(fc, sb, childAddress));
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
