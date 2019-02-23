package io.jhdf.btree;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

/**
 * Class to represent V1 B-trees
 * 
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#V1Btrees">Format
 * Spec</a>
 * </p>
 * 
 * @author James Mudd
 */
public abstract class BTreeV1 {
	private static final Logger logger = LoggerFactory.getLogger(BTreeV1.class);

	private static final byte[] BTREE_NODE_V1_SIGNATURE = "TREE".getBytes();
	private static final int HEADER_BYTES = 6;

	/** The location of this B tree in the file */
	private final long address;
	protected final int entriesUsed;
	private final long leftSiblingAddress;
	private final long rightSiblingAddress;

	public static BTreeV1Group createGroupBTree(FileChannel fc, Superblock sb, long address) {
		ByteBuffer header = readHeaderAndValidateSignature(fc, address);

		final byte nodeType = header.get();
		if (nodeType != 0) {
			throw new HdfException("B tree type is not group. Type is: " + nodeType);
		}

		final byte nodeLevel = header.get();

		if (nodeLevel > 0) {
			return new BTreeV1Group.BTreeV1GroupNonLeafNode(fc, sb, address);
		} else {
			return new BTreeV1Group.BTreeV1GroupLeafNode(fc, sb, address);
		}

	}

	public static BTreeV1Data createDataBTree(FileChannel fc, Superblock sb, long address, int dataDimensions) {
		ByteBuffer header = readHeaderAndValidateSignature(fc, address);

		final byte nodeType = header.get();
		if (nodeType != 1) {
			throw new HdfException("B tree type is not data. Type is: " + nodeType);
		}

		final byte nodeLevel = header.get();

		if (nodeLevel > 0) {
			return new BTreeV1Data.BTreeV1DataNonLeafNode(fc, sb, address, dataDimensions);
		} else {
			return new BTreeV1Data.BTreeV1DataLeafNode(fc, sb, address, dataDimensions);
		}
	}

	public static ByteBuffer readHeaderAndValidateSignature(FileChannel fc, long address) {
		ByteBuffer header = ByteBuffer.allocate(HEADER_BYTES);
		try {
			fc.read(header, address);
		} catch (IOException e) {
			throw new HdfException("Error reading BTreeV1 header at address: " + address);
		}
		header.order(LITTLE_ENDIAN);
		header.rewind();

		// Verify signature
		byte[] formatSignitureByte = new byte[4];
		header.get(formatSignitureByte, 0, formatSignitureByte.length);
		if (!Arrays.equals(BTREE_NODE_V1_SIGNATURE, formatSignitureByte)) {
			throw new HdfException("B tree V1 node signature not matched");
		}
		return header;
	}

	/* package */ BTreeV1(FileChannel fc, Superblock sb, long address) {
		this.address = address;

		int headerSize = 8 * sb.getSizeOfOffsets();
		ByteBuffer header = ByteBuffer.allocate(headerSize);
		try {
			fc.read(header, address + 6);
		} catch (IOException e) {
			throw new HdfException("Error reading BTreeV1 header at address: " + address);
		}
		header.order(LITTLE_ENDIAN);
		header.rewind();

		entriesUsed = Utils.readBytesAsUnsignedInt(header, 2);
		logger.trace("Entries = {}", entriesUsed);

		leftSiblingAddress = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
		logger.trace("left address = {}", leftSiblingAddress);

		rightSiblingAddress = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
		logger.trace("right address = {}", rightSiblingAddress);

	}

	public int getEntriesUsed() {
		return entriesUsed;
	}

	public long getLeftSiblingAddress() {
		return leftSiblingAddress;
	}

	public long getRightSiblingAddress() {
		return rightSiblingAddress;
	}

	public long getAddress() {
		return address;
	}

	public abstract List<Long> getChildAddresses();

}