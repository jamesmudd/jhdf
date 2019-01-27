package io.jhdf;

import static io.jhdf.Constants.UNDEFINED_ADDRESS;
import static io.jhdf.Utils.readBytesAsUnsignedInt;
import static io.jhdf.Utils.readBytesAsUnsignedLong;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.exceptions.HdfException;

public class FractalHeap {
	private static final Logger logger = LoggerFactory.getLogger(FractalHeap.class);

	private static final byte[] FRACTAL_HEAP_SIGNATURE = "FRHP".getBytes();
	private static final byte[] INDIRECT_BLOCK_SIGNATURE = "FHIB".getBytes();
	private static final byte[] DIRECT_BLOCK_SIGNATURE = "FHDB".getBytes();

	private static final BigInteger TWO = BigInteger.valueOf(2L);

	private final long address;

	private final byte version;

	private final int maxHeapSize;

	private final int maxDirectBlockSize;

	private final long maxSizeOfManagedObjects;

	private final FileChannel fc;

	private final long addressOfRootBlock;

	private int idLength;

	private int ioFiltersLength;

	private int currentRowsInRootIndirectBlock;

	private int startingRowsInRootIndirectBlock;

	private int startingBlockSize;

	private int tableWidth;

	private long numberOfTinyObjectsInHeap;

	private long sizeOfTinyObjectsInHeap;

	private long numberOfHugeObjectsInHeap;

	private long sizeOfHugeObjectsInHeap;

	private long numberOfManagedObjectsInHeap;

	private long offsetOfDirectBlockAllocationIteratorInManagedSpace;

	private long amountOfAllocatedManagedSpaceInHeap;

	private long amountOfManagedSpaceInHeap;

	private long addressOfManagedBlocksFreeSpaceManager;

	private long freeSpaceInManagedBlocks;

	private long bTreeAddressOfHugeObjects;

	private long nextHugeObjectId;

	private BitSet flags;

	private Superblock sb;

	private int blockIndex = 0;

	private Map<Long, DirectBlock> directBlocks = new TreeMap<>(); // Sorted map

	public FractalHeap(FileChannel fc, Superblock sb, long address) {
		this.fc = fc;
		this.sb = sb;
		this.address = address;

		try {
			final int headerSize = 4 + 1 + 2 + 2 + 1 + 4 + 12 * sb.getSizeOfLengths() + 3 * sb.getSizeOfOffsets() + 2
					+ 2 + 2 + 2;

			ByteBuffer bb = ByteBuffer.allocate(headerSize);

			fc.read(bb, address);
			bb.rewind();
			bb.order(LITTLE_ENDIAN);

			byte[] formatSignitureByte = new byte[4];
			bb.get(formatSignitureByte, 0, formatSignitureByte.length);

			// Verify signature
			if (!Arrays.equals(FRACTAL_HEAP_SIGNATURE, formatSignitureByte)) {
				throw new HdfException(
						"Fractal heap signature '" + FRACTAL_HEAP_SIGNATURE + "' not matched, at address " + address);
			}

			// Version Number
			version = bb.get();
			if (version != 0) {
				throw new HdfException("Unsupported fractal heap version detected. Version: " + version);
			}

			idLength = readBytesAsUnsignedInt(bb, 2);
			ioFiltersLength = readBytesAsUnsignedInt(bb, 2);

			flags = BitSet.valueOf(new byte[] { bb.get() });

			maxSizeOfManagedObjects = readBytesAsUnsignedLong(bb, 4);

			nextHugeObjectId = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());

			bTreeAddressOfHugeObjects = readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

			freeSpaceInManagedBlocks = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());

			addressOfManagedBlocksFreeSpaceManager = readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

			amountOfManagedSpaceInHeap = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			amountOfAllocatedManagedSpaceInHeap = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			offsetOfDirectBlockAllocationIteratorInManagedSpace = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			numberOfManagedObjectsInHeap = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());

			sizeOfHugeObjectsInHeap = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			numberOfHugeObjectsInHeap = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			sizeOfTinyObjectsInHeap = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			numberOfTinyObjectsInHeap = readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());

			tableWidth = readBytesAsUnsignedInt(bb, 2);

			startingBlockSize = readBytesAsUnsignedInt(bb, sb.getSizeOfLengths());
			maxDirectBlockSize = readBytesAsUnsignedInt(bb, sb.getSizeOfLengths());

			maxHeapSize = readBytesAsUnsignedInt(bb, 2);
			startingRowsInRootIndirectBlock = readBytesAsUnsignedInt(bb, 2);

			addressOfRootBlock = readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

			currentRowsInRootIndirectBlock = readBytesAsUnsignedInt(bb, 2);

			if (ioFiltersLength > 0) {
				// TODO read IO filters
			}

			// Read the root block
			if (addressOfRootBlock != UNDEFINED_ADDRESS) {
				if (currentRowsInRootIndirectBlock == 0) {
					// Read direct block
				} else {
					// Read indirect block
					IndirectBlock indirectBlock = new IndirectBlock(addressOfRootBlock);
					for (long directBlockAddres : indirectBlock.childBlockAddresses) {
						int blockSize = getSizeOfDirectBlock(blockIndex++);
						if (blockSize != -1) {
							DirectBlock db = new DirectBlock(directBlockAddres);
							directBlocks.put(db.blockOffset, db);
						} else {
							new IndirectBlock(address);
						}
					}
				}
			}

			logger.debug("Read fractal heap at address {}, loaded {} direct blocks", address, directBlocks.size());

		} catch (Exception e) {
			throw new HdfException("Error reading fractal heap at address " + address, e);
		}

	}

	private boolean isIoFilters() {
		return ioFiltersLength > 0;
	}

	public ByteBuffer getId(ByteBuffer buffer) {
		if (buffer.capacity() != idLength) {
			throw new HdfException("ID lentgh is incorrect accessing fractal heap at address " + address
					+ ". IDs should be " + idLength + "bytes but was " + buffer.capacity() + "bytes.");
		}

		BitSet flags = BitSet.valueOf(new byte[] { buffer.get() });

		final int version = Utils.bitsToInt(flags, 6, 2);
		if (version != 0) {
			throw new HdfException("Unsupported B tree v2 version detected. Version: " + version);
		}

		final int type = Utils.bitsToInt(flags, 4, 2);
		if (type != 0) {
			System.out.println("Hi");
		}

		switch (type) {
		case 0: // Managed Objects
			int offset = Utils.readBytesAsUnsignedInt(buffer, 4); // bytesNeededToHoldNumber(maxHeapSize));
			int lentgh = Utils.readBytesAsUnsignedInt(buffer, 2);
//					bytesNeededToHoldNumber(Math.min(maxHeapSize, maxSizeOfMangledObjects)));

			logger.debug("Getting ID at offset={} lentgh={}", offset, lentgh);
			// Figure out which direct block holds the offset
			Iterator<Entry<Long, DirectBlock>> i = directBlocks.entrySet().iterator();
			long blockOffset = 0;
			while (i.hasNext()) {
				long nextBlockOffset = i.next().getKey();
				if (offset > nextBlockOffset) {
					blockOffset = nextBlockOffset;
				} else {
					break;
				}
			}
			ByteBuffer bb = directBlocks.get(blockOffset).getData();
			bb.order(LITTLE_ENDIAN);
			bb.position((int) (offset - blockOffset));
			return Utils.createSubBuffer(bb, lentgh);

		default:
			break;
		}

		// Might no be set depending on type
		final int lentgh = Utils.bitsToInt(flags, 0, 3);

		if (buffer.capacity() < 19) {
			// Tiny data in id itself
			buffer.position(1);
			return Utils.createSubBuffer(buffer, buffer.remaining());
		}
		return null;
	}

	private int bytesNeededToHoldNumber(long number) {
		return BigInteger.valueOf(number).bitLength() / 8;
//		return (Long.numberOfTrailingZeros(Long.highestOneBit(number)) + 8) / 8;
	}

	private class IndirectBlock {

		private final List<Long> childBlockAddresses;
		private final long blockOffset;

		public IndirectBlock(long address) throws IOException {
			final int headerSize = 4 + 1 + sb.getSizeOfOffsets() + getBlockOffsetSize()
					+ currentRowsInRootIndirectBlock * tableWidth * getRowSize() + 4;

			ByteBuffer bb = ByteBuffer.allocate(headerSize);

			fc.read(bb, address);
			bb.rewind();
			bb.order(LITTLE_ENDIAN);

			byte[] formatSignitureByte = new byte[4];
			bb.get(formatSignitureByte, 0, formatSignitureByte.length);

			// Verify signature
			if (!Arrays.equals(INDIRECT_BLOCK_SIGNATURE, formatSignitureByte)) {
				throw new HdfException("Fractal heap indirect block signature '" + INDIRECT_BLOCK_SIGNATURE
						+ "' not matched, at address " + address);
			}

			// Version Number
			byte version = bb.get();
			if (version != 0) {
				throw new HdfException("Unsupported indirect block version detected. Version: " + version);
			}

			long heapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			if (heapAddress != FractalHeap.this.address) {
				throw new HdfException("Indirect block read from invalid fractal heap");
			}

			blockOffset = Utils.readBytesAsUnsignedLong(bb, getBlockOffsetSize());

			childBlockAddresses = new ArrayList<>(currentRowsInRootIndirectBlock * tableWidth);
			for (int i = 0; i < currentRowsInRootIndirectBlock * tableWidth; i++) {
				// TODO only works for unfiltered
				long childAddress = Utils.readBytesAsUnsignedLong(bb, getRowSize());
				if (childAddress == UNDEFINED_ADDRESS) {
					break;
				} else {
					childBlockAddresses.add(childAddress);
				}
				System.out.println(childBlockAddresses.size());
			}

			// TODO Checksum
		}

		private int getRowSize() {
			int size = sb.getSizeOfOffsets();
			if (isIoFilters()) {
				size += sb.getSizeOfLengths();
				size += 4; // filter mask
			}
			return size;
		}

		private int getBlockOffsetSize() {
			return (int) Math.ceil(maxHeapSize / 8.0);
		}

	}

	private int getSizeOfDirectBlock(int blockIndex) {
		int row = blockIndex / tableWidth; // int division
		if (row < 2) {
			return startingBlockSize;
		} else {
			int size = startingBlockSize * TWO.pow(row - 1).intValueExact();
			if (size < maxDirectBlockSize) {
				return size;
			} else {
				return -1; // Indicates the block is an indirect block
			}
		}
	}

	private class DirectBlock {

		private static final int CHECKSUM_PRESENT_BIT = 1;
		private final long address;
		private final ByteBuffer data;
		private final long blockOffset;

		public DirectBlock(long address) throws IOException {
			this.address = address;

			final int headerSize = 4 + 1 + sb.getSizeOfOffsets() + getBlockOffsetSize() + 4;

			ByteBuffer bb = ByteBuffer.allocate(headerSize);
			fc.read(bb, address);
			bb.rewind();
			bb.order(LITTLE_ENDIAN);

			byte[] formatSignitureByte = new byte[4];
			bb.get(formatSignitureByte, 0, formatSignitureByte.length);

			// Verify signature
			if (!Arrays.equals(DIRECT_BLOCK_SIGNATURE, formatSignitureByte)) {
				throw new HdfException("Fractal heap direct block signature '" + DIRECT_BLOCK_SIGNATURE
						+ "' not matched, at address " + address);
			}

			// Version Number
			byte directBlockersion = bb.get();
			if (directBlockersion != 0) {
				throw new HdfException("Unsupported direct block version detected. Version: " + directBlockersion);
			}

			long heapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			if (heapAddress != FractalHeap.this.address) {
				throw new HdfException("Indirect block read from invalid fractal heap");
			}

			blockOffset = Utils.readBytesAsUnsignedLong(bb, getBlockOffsetSize());

			if (checksumPresent()) {
				// TODO Checksum for now skip over
				bb.position(bb.position() + 4);
			}
			data = fc.map(READ_ONLY, address, getSizeOfDirectBlock(blockIndex));
		}

		private boolean checksumPresent() {
			return flags.get(CHECKSUM_PRESENT_BIT);
		}

		private int getRowSize() {
			int size = sb.getSizeOfOffsets();
			if (isIoFilters()) {
				size += sb.getSizeOfLengths();
				size += 4; // filter mask
			}
			return size;
		}

		private int getBlockOffsetSize() {
			return (int) Math.ceil(maxHeapSize / 8.0);
		}

		public ByteBuffer getData() {
			return data;
		}

		public long getBlockOffset() {
			return blockOffset;
		}

		@Override
		public String toString() {
			return "DirectBlock [address=" + address + ", blockOffset=" + blockOffset + ", data=" + data + "]";
		}

	}

	@Override
	public String toString() {
		return "FractalHeap [address=" + address + ", idLength=" + idLength + ", numberOfTinyObjectsInHeap="
				+ numberOfTinyObjectsInHeap + ", numberOfHugeObjectsInHeap=" + numberOfHugeObjectsInHeap
				+ ", numberOfManagedObjectsInHeap=" + numberOfManagedObjectsInHeap + "]";
	}

}
