package io.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.exceptions.HdfException;

public class LocalHeap {
	private static final Logger logger = LoggerFactory.getLogger(LocalHeap.class);

	private static final byte[] HEAP_SIGNATURE = "HEAP".getBytes();

	/** The location of this Heap in the file */
	private final long address;
	private final short version;
	private final long dataSegmentSize;
	private final long offsetToHeadOfFreeList;
	private final long addressOfDataSegment;
	private final ByteBuffer dataBuffer;

	public LocalHeap(FileChannel fc, long address, Superblock sb) {
		this.address = address;
		try {
			// Header
			int headerSize = 8 + sb.getSizeOfLengths() + sb.getSizeOfLengths() + sb.getSizeOfOffsets();
			ByteBuffer header = ByteBuffer.allocate(headerSize);

			fc.read(header, address);
			header.rewind();
			header.order(LITTLE_ENDIAN);

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

			// Data Segment Size
			dataSegmentSize = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfLengths());
			logger.trace("dataSegmentSize = {}", dataSegmentSize);

			// Offset to Head of Free-list
			offsetToHeadOfFreeList = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfLengths());
			logger.trace("offsetToHeadOfFreeList = {}", offsetToHeadOfFreeList);

			// Address of Data Segment
			addressOfDataSegment = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
			logger.trace("addressOfDataSegment = {}", addressOfDataSegment);

			dataBuffer = fc.map(MapMode.READ_ONLY, addressOfDataSegment, dataSegmentSize);
		} catch (IOException e) {
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

	@Override
	public String toString() {
		return "LocalHeap [address=" + Utils.toHex(address) + ", version=" + version + ", dataSegmentSize="
				+ dataSegmentSize + ", offsetToHeadOfFreeList=" + offsetToHeadOfFreeList + ", addressOfDataSegment="
				+ Utils.toHex(addressOfDataSegment) + "]";
	}

	public ByteBuffer getDataBuffer() {
		return dataBuffer;
	}

}
