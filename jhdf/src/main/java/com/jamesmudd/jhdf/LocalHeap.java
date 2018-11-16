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

public class LocalHeap {
	private static final Logger logger = LoggerFactory.getLogger(LocalHeap.class);
	
	private static final byte[] HEAP_SIGNATURE = "HEAP".getBytes();

	private final short version;
	private final long dataSegmentSize;
	private final long offsetToHeadOfFreeList;
	private final long addressOfDataSegment;
	
	public LocalHeap(RandomAccessFile file, long address, int sizeOfOffsets, int sizeOfLengths) {
		try {
			FileChannel fc = file.getChannel();
			
			// B Tree Node Header
			int headerSize = 8 + sizeOfLengths + sizeOfLengths + sizeOfOffsets;
			ByteBuffer header = ByteBuffer.allocate(headerSize);

			fc.read(header, address);
			header.rewind();
			
			byte[] formatSignitureByte = new byte[4];
			header.get(formatSignitureByte, 0, formatSignitureByte.length);

			// Verify signature
			if (!Arrays.equals(HEAP_SIGNATURE, formatSignitureByte)) {
				throw new HdfException("Heap signature not matched");
			}
			
			// Version
			version = header.get();
			
			// Move past reserved space
			header.position(8);
			
			final byte[] lengthsBytes = new byte[sizeOfLengths];

			// Data Segment Size
			header.get(lengthsBytes);
			dataSegmentSize = ByteBuffer.wrap(lengthsBytes).order(LITTLE_ENDIAN).getLong();
			logger.trace("dataSegmentSize = {}", dataSegmentSize);

			// Offset to Head of Free-list
			header.get(lengthsBytes);
			offsetToHeadOfFreeList = ByteBuffer.wrap(lengthsBytes).order(LITTLE_ENDIAN).getLong();
			logger.trace("offsetToHeadOfFreeList = {}", offsetToHeadOfFreeList);
			
			final byte[] offsetBytes = new byte[sizeOfOffsets];

			// Address of Data Segment
			header.get(offsetBytes);
			addressOfDataSegment = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			logger.trace("addressOfDataSegment = {}", addressOfDataSegment);
		}
		catch (IOException e) {
			throw new HdfException("Error reading heap", e);
		}
	}


	public short getVersion() {
		return version;
	}


	public long getDataSegmentSize() {
		return dataSegmentSize;
	}


	public long getOffsetToHeadOfFreeList() {
		return offsetToHeadOfFreeList;
	}


	public long getAddressOfDataSegment() {
		return addressOfDataSegment;
	}			
}
