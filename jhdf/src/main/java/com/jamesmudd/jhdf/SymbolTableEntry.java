package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class SymbolTableEntry {
	
	private final long linkNameOffset;
	
	private final long objectHeaderAddress;
	
	private final int cacheType;
	
	private long bTreeAddress = -1;
	
	private long nameHeapAddress = -1;
	
	private long  linkValueoffset = -1;
	
	public SymbolTableEntry(RandomAccessFile file, long offset, int sizeOfOffsets) throws IOException {
		file.seek(offset);
		
		final byte[] offsetBytes = new byte[sizeOfOffsets];
		
		// Link Name Offset
		file.read(offsetBytes);
		linkNameOffset = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
		System.out.println("linkNameOffset = " + linkNameOffset);
		
		// Link Name Offset
		file.read(offsetBytes);
		objectHeaderAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
		System.out.println("objectHeaderAddress = " + objectHeaderAddress);
		
		final byte[] fourBytes = new byte[4];
		
		// Link Name Offset
		file.read(fourBytes);
		cacheType = ByteBuffer.wrap(fourBytes).order(LITTLE_ENDIAN).getInt();
		System.out.println("cacheType = " + cacheType);
		
		// Reserved 4 bytes
		file.skipBytes(4);
		
		// Scratch pad
		switch (cacheType) {
		case 0:
			// Nothing in scratch pad space
			break;
		case 1:
			// B Tree
			// Address of B Tree
			file.read(offsetBytes);
			bTreeAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("addressOfBTree = " + bTreeAddress);
			
			// Address of Name Heap
			file.read(offsetBytes);
			nameHeapAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("nameHeapAddress = " + nameHeapAddress);			
			break;
		case 2:
			// Link
			file.read(fourBytes);
			linkValueoffset = ByteBuffer.wrap(fourBytes).order(LITTLE_ENDIAN).getInt();
			System.out.println("linkValueoffset = " + linkValueoffset);			
			break;
		default:
			throw new IllegalStateException("SymbolTableEntry: Unreconized cache type = " + cacheType);
		}
		
	}

}
