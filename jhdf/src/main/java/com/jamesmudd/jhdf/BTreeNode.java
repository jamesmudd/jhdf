package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesmudd.jhdf.exceptions.HdfException;

public class BTreeNode {
	private static final Logger logger = LoggerFactory.getLogger(BTreeNode.class);

	private static final byte[] BTREE_NODE_SIGNATURE = "TREE".getBytes();

	private final short nodeType;
	private final short nodeLevel;
	private final short entriesUsed;
	private final long leftSiblingAddress;
	private final long rightSiblingAddress;
	private final long[] keys;
	private final long[] childAddresses;

	public BTreeNode(RandomAccessFile file, long address, int sizeOfOffsets, int sizeOfLengths, int leafK,
			int internalK) {
		try {
			FileChannel fc = file.getChannel();

			// B Tree Node Header
			int headerSize = 8 + 2 * sizeOfOffsets;
			ByteBuffer header = ByteBuffer.allocate(headerSize);

			fc.read(header, address);
			header.rewind();

			byte[] formatSignitureByte = new byte[4];
			header.get(formatSignitureByte, 0, formatSignitureByte.length);

			// Verify signature
			if (!Arrays.equals(BTREE_NODE_SIGNATURE, formatSignitureByte)) {
				throw new HdfException("B tree node signature not matched");
			}

			header.position(4);
			nodeType = header.get();
			nodeLevel = header.get();

			final byte[] twoBytes = new byte[2];
			header.get(twoBytes);
			entriesUsed = ByteBuffer.wrap(twoBytes).order(LITTLE_ENDIAN).getShort();

			logger.trace("Entries = {}", getEntriesUsed());

			final byte[] offsetBytes = new byte[sizeOfOffsets];

			// Link Name Offset
			header.get(offsetBytes);
			leftSiblingAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			logger.trace("left address = {}", getLeftSiblingAddress());

			header.get(offsetBytes);
			rightSiblingAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			logger.trace("right address = {}", getRightSiblingAddress());

			switch (nodeType) {
			case 0: // Group nodes
				int keyBytes = (2 * entriesUsed + 1) * sizeOfLengths;
				int childPointerBytes = (2 * entriesUsed) * sizeOfOffsets;
				int keysAndPointersBytes = keyBytes + childPointerBytes;

				ByteBuffer keysAndPointersBuffer = ByteBuffer.allocate(keysAndPointersBytes);
				fc.read(keysAndPointersBuffer, address + headerSize);
				keysAndPointersBuffer.rewind();

				keys = new long[entriesUsed + 1];
				childAddresses = new long[entriesUsed];

				final byte[] key = new byte[sizeOfLengths];
				final byte[] child = new byte[sizeOfOffsets];

				for (int i = 0; i < entriesUsed; i++) {
					keysAndPointersBuffer.get(key);
					keys[i] = ByteBuffer.wrap(key).order(LITTLE_ENDIAN).getLong();
					keysAndPointersBuffer.get(child);
					childAddresses[i] = ByteBuffer.wrap(child).order(LITTLE_ENDIAN).getLong();
				}
				keysAndPointersBuffer.get(key);
				getKeys()[entriesUsed] = ByteBuffer.wrap(key).order(LITTLE_ENDIAN).getLong();

				break;
			case 1: // Raw data
				// TODO implement
				throw new HdfException("B tree Raw data not implemented");
			default:
				throw new HdfException("Unreconized node type = " + nodeType);
			}

		} catch (IOException e) {
			// TODO improve message
			throw new HdfException("Error reading B Tree node", e);
		}

	}

	public short getNodeType() {
		return nodeType;
	}

	public short getNodeLevel() {
		return nodeLevel;
	}

	public short getEntriesUsed() {
		return entriesUsed;
	}

	public long getLeftSiblingAddress() {
		return leftSiblingAddress;
	}

	public long getRightSiblingAddress() {
		return rightSiblingAddress;
	}

	public long[] getKeys() {
		return keys;
	}

	public long[] getChildAddresses() {
		return childAddresses;
	}

}
