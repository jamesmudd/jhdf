package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import com.jamesmudd.jhdf.exceptions.HdfException;

public class BTreeNode {
	
	private static final byte[] BTREE_NODE_SIGNATURE = "TREE".getBytes();
	
	private final short nodeType;
	private final short nodeLevel;
	private final short entriesUsed;
	private final long leftSiblingAddress;
	private final long rightSiblingAddress;
		
	public BTreeNode(RandomAccessFile file, long address, int sizeOfOffsets, int sizeOfLengths, int leafK, int internalK) {
		try {
			FileChannel fc = file.getChannel();
			
			// B Tree Node Header
			int headerSize = 8 + 2*sizeOfOffsets;
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
			entriesUsed = header.getShort();
			
			System.out.println("Entires = " + getEntriesUsed());
			
			final byte[] offsetBytes = new byte[sizeOfOffsets];
			
			// Link Name Offset
			header.get(offsetBytes);
			leftSiblingAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("left address = " + getLeftSiblingAddress());
			
			header.get(offsetBytes);
			rightSiblingAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("right address = " + getRightSiblingAddress());
			
			// FIXME not sure what is happening here
			switch (nodeType) {
			case 0: // Group nodes
				
				break;
			case 1: // Raw data

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

}
