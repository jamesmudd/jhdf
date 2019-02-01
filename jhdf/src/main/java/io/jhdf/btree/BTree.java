package io.jhdf.btree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.Superblock;
import io.jhdf.exceptions.HdfException;

/**
 * B Tree nodes
 * 
 * @author James Mudd
 */
public abstract class BTree {
	static final Logger logger = LoggerFactory.getLogger(BTree.class);

	private static final byte[] BTREE_NODE_V1_SIGNATURE = "TREE".getBytes();
	private static final byte[] BTREE_NODE_V2_SIGNATURE = "BTHD".getBytes();

	/** The location of this B tree in the file */
	private final long address;

	public BTree(long address) {
		this.address = address;
	}

	public static BTree createBTreeNode(FileChannel fc, Superblock sb, long address) {

		ByteBuffer signatureBuffer = ByteBuffer.allocate(4);

		try {
			fc.read(signatureBuffer, address);
		} catch (IOException e) {
			throw new HdfException("Failed to read B Tree signature", e);
		}

		signatureBuffer.rewind();
		byte[] formatSignitureByte = new byte[4];
		signatureBuffer.get(formatSignitureByte, 0, formatSignitureByte.length);

		// Verify signature
		if (Arrays.equals(BTREE_NODE_V1_SIGNATURE, formatSignitureByte)) {
			return new BTreeV1(fc, sb, address);
		} else if (Arrays.equals(BTREE_NODE_V2_SIGNATURE, formatSignitureByte)) {
			return new BTreeV2(fc, sb, address);
		} else {
			throw new HdfException("B tree node signature not matched");
		}
	}

	public abstract long[] getChildAddresses();

	public abstract short getNodeLevel();

	public long getAddress() {
		return address;
	}
}
