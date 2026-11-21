/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.ArithmeticUtils;
import io.jhdf.exceptions.HdfFilterException;
import io.jhdf.exceptions.UnsupportedHdfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * The HDF5 scale-offset filter (filter id 6).
 * <p>
 * Decodes scale-offset compressed chunks. Integer datatypes are supported; float
 * datatypes are not yet implemented.
 *
 * @author James Mudd
 */
public class ScaleOffsetFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(ScaleOffsetFilter.class);

	// Filter client data parameter indices, see the H5Zfilter docs
	private static final int PARM_SCALETYPE = 0;
	private static final int PARM_SCALEFACTOR = 1;
	private static final int PARM_NELMTS = 2;
	private static final int PARM_CLASS = 3;
	private static final int PARM_SIZE = 4;
	private static final int PARM_SIGN = 5;
	private static final int PARM_ORDER = 6;
	private static final int PARM_FILAVAIL = 7;
	private static final int PARM_FILVAL = 8;

	// Size of the scale-offset header: 4 bytes minbits + 1 byte minval size + 16 bytes minval
	private static final int BUFFER_OFFSET = 21;

	private static final int CLS_INTEGER = 0;
	private static final int CLS_FLOAT = 1;

	private static final int SIGN_NONE = 0;
	private static final int SIGN_2 = 1;

	private static final int ORDER_LE = 0;
	private static final int ORDER_BE = 1;

	private static final int FILL_UNDEFINED = 0;
	private static final int FILL_DEFINED = 1;

	/**
	 * Id defined in https://support.hdfgroup.org/services/filters.html
	 *
	 * @return Defined value, 6
	 */
	@Override
	public int getId() {
		return 6;
	}

	/**
	 * The name of this filter, "scale-offset"
	 *
	 * @return "scale-offset"
	 */
	@Override
	public String getName() {
		return "scale-offset";
	}

	@Override
	public byte[] decode(byte[] encodedData, int[] filterData) {
		if (encodedData.length < BUFFER_OFFSET) {
			throw new HdfFilterException("Scale-offset buffer too short. Expected at least " + BUFFER_OFFSET
				+ " bytes but got " + encodedData.length);
		}
		if (filterData.length <= PARM_FILAVAIL) {
			throw new HdfFilterException("Scale-offset filter data is incomplete. Expected at least "
				+ (PARM_FILAVAIL + 1) + " values but got " + filterData.length);
		}

		int elementCount = filterData[PARM_NELMTS];
		int elementBytes = filterData[PARM_SIZE];
		int dtypeClass = filterData[PARM_CLASS];
		int dtypeSign = filterData[PARM_SIGN];
		int dtypeOrder = filterData[PARM_ORDER];
		int fillAvailable = filterData[PARM_FILAVAIL];

		int minBits = readIntLE(encodedData, 0);
		int minValSize = Byte.toUnsignedInt(encodedData[4]);
		long minVal = readUnsignedLongLE(encodedData, 5, minValSize);

		final int outputSize = ArithmeticUtils.multiplyExact(elementCount, elementBytes);

		if (logger.isDebugEnabled()) {
			logger.debug("Decoding scale-offset chunk. minBits = {}, elements = {}, element size = {} bytes, "
					+ "encoded = {} bytes, decoded = {} bytes",
				minBits,
				elementCount,
				elementBytes,
				encodedData.length,
				outputSize);
		}

		byte[] decoded = new byte[outputSize];
		if (minBits == elementBytes * 8) {
			// Full precision, data is stored unchanged after the header
			if (encodedData.length - BUFFER_OFFSET < outputSize) {
				throw new HdfFilterException("Scale-offset buffer too short for full precision data");
			}
			System.arraycopy(encodedData, BUFFER_OFFSET, decoded, 0, outputSize);
			if (needsByteSwap(dtypeOrder)) {
				swapEndianInPlace(decoded, elementBytes);
			}
			return decoded;
		}

		if (dtypeClass == CLS_FLOAT) {
			throw new UnsupportedHdfException("Scale-offset filter float support is not implemented");
		}

		// Integer datatype
		long fillValue = 0L;
		if (fillAvailable == FILL_DEFINED) {
			int fillValuesNeeded = (elementBytes + Integer.BYTES - 1) / Integer.BYTES;
			if (filterData.length < PARM_FILVAL + fillValuesNeeded) {
				throw new HdfFilterException("Scale-offset fill value data is incomplete. Expected at least "
					+ (PARM_FILVAL + fillValuesNeeded) + " values but got " + filterData.length);
			}
			fillValue = readFillValue(filterData, elementBytes);
		}

		long maxValue = fillAvailable == FILL_DEFINED ? (minBits == 64 ? -1L : (1L << minBits) - 1L) : 0L;
		long offset = dtypeSign == SIGN_2 ? toSignedMin(minVal, elementBytes) : minVal;

		// Validate the bit stream is long enough before reading it
		if (minBits != 0) {
			long bitsNeeded = (long) elementCount * minBits;
			if (encodedData.length - BUFFER_OFFSET < (bitsNeeded + 7) / 8) {
				throw new HdfFilterException("Scale-offset buffer too short for " + elementCount + " elements of "
					+ minBits + " bits");
			}
			if (fillAvailable == FILL_DEFINED) {
				if (minBits <= 56) {
					decompressFused(decoded, elementCount, encodedData, BUFFER_OFFSET, elementBytes, minBits, maxValue,
						offset, fillValue);
				} else {
					decompressCarry(decoded, elementCount, encodedData, BUFFER_OFFSET, elementBytes, minBits, maxValue,
						offset, fillValue);
				}
			} else {
				if (minBits <= 56) {
					decompressFusedNoFill(decoded, elementCount, encodedData, BUFFER_OFFSET, elementBytes, minBits,
						offset);
				} else {
					decompressCarryNoFill(decoded, elementCount, encodedData, BUFFER_OFFSET, elementBytes, minBits,
						offset);
				}
			}
		} else {
			// minbits == 0: every element has the same (fill) value
			long value = fillAvailable == FILL_DEFINED ? fillValue : offset;
			fillAllElements(decoded, elementCount, elementBytes, value);
		}

		if (needsByteSwap(dtypeOrder)) {
			swapEndianInPlace(decoded, elementBytes);
		}

		return decoded;
	}

	private static int readIntLE(byte[] data, int offset) {
		return (Byte.toUnsignedInt(data[offset]))
			| (Byte.toUnsignedInt(data[offset + 1]) << 8)
			| (Byte.toUnsignedInt(data[offset + 2]) << 16)
			| (Byte.toUnsignedInt(data[offset + 3]) << 24);
	}

	private static long readUnsignedLongLE(byte[] data, int offset, int length) {
		long value = 0L;
		int bytes = Math.min(length, Long.BYTES);
		for (int i = 0; i < bytes; i++) {
			value |= ((long) Byte.toUnsignedInt(data[offset + i])) << (8 * i);
		}
		return value;
	}

	private static long readFillValue(int[] filterData, int elementBytes) {
		byte[] fillBytes = new byte[elementBytes];
		int remaining = elementBytes;
		int dst = 0;
		int index = PARM_FILVAL;

		while (remaining > 0) {
			int value = filterData[index++];
			int copy = Math.min(4, remaining);
			for (int i = 0; i < copy; i++) {
				fillBytes[dst++] = (byte) ((value >> (8 * i)) & 0xFF);
			}
			remaining -= copy;
		}

		ByteBuffer buffer = ByteBuffer.wrap(fillBytes).order(ByteOrder.LITTLE_ENDIAN);
		return switch (elementBytes) {
			case 1 -> buffer.get() & 0xFFL;
			case 2 -> buffer.getShort() & 0xFFFFL;
			case 4 -> buffer.getInt() & 0xFFFFFFFFL;
			case 8 -> buffer.getLong();
			default -> throw new HdfFilterException("Unsupported fill value size: " + elementBytes);
		};
	}

	private static void fillAllElements(byte[] data, int elementCount, int elementBytes, long value) {
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
		switch (elementBytes) {
			case 1 -> Arrays.fill(data, (byte) value);
			case 2 -> {
				for (int i = 0; i < elementCount; i++) {
					buffer.putShort(i * 2, (short) value);
				}
			}
			case 4 -> {
				for (int i = 0; i < elementCount; i++) {
					buffer.putInt(i * 4, (int) value);
				}
			}
			case 8 -> {
				for (int i = 0; i < elementCount; i++) {
					buffer.putLong(i * 8, value);
				}
			}
			default -> throw new HdfFilterException("Unsupported integer size: " + elementBytes);
		}
	}

	private static long toSignedMin(long minVal, int elementBytes) {
		return switch (elementBytes) {
			case 1 -> (byte) minVal;
			case 2 -> (short) minVal;
			case 4 -> (int) minVal;
			case 8 -> minVal;
			default -> throw new HdfFilterException("Unsupported integer size: " + elementBytes);
		};
	}

	/**
	 * Single pass over the bit stream reconstructing the values. The code for each
	 * element is read MSB-first into a 64 bit buffer, the offset/fill applied and
	 * the element written to the output in native byte order. This avoids the
	 * separate decompress and post-decompress passes (and the associated buffer
	 * zeroing) of the reference implementation.
	 */
	private static void decompressFused(byte[] output, int elementCount, byte[] buffer, int bufferOffset,
		int elementBytes, int minBits, long maxValue, long offset, long fillValue) {
		long bitBuffer = 0;
		int bitsInBuffer = 0;
		int index = bufferOffset;
		long codeMask = minBits == 64 ? -1L : (1L << minBits) - 1L;

		ByteBuffer out = ByteBuffer.wrap(output).order(ByteOrder.nativeOrder());

		for (int i = 0; i < elementCount; i++) {
			while (bitsInBuffer < minBits) {
				bitBuffer = (bitBuffer << 8) | (buffer[index++] & 0xFFL);
				bitsInBuffer += 8;
			}
			long code = (bitBuffer >>> (bitsInBuffer - minBits)) & codeMask;
			bitsInBuffer -= minBits;

			long value = (code == maxValue) ? fillValue : code + offset;

			switch (elementBytes) {
				case 1 -> output[i] = (byte) value;
				case 2 -> out.putShort(i * 2, (short) value);
				case 4 -> out.putInt(i * 4, (int) value);
				case 8 -> out.putLong(i * 8, value);
				default -> throw new HdfFilterException("Unsupported integer size: " + elementBytes);
			}
		}
	}

	/**
	 * As {@link #decompressFused} but for chunks without a fill value, avoiding the
	 * per element fill check.
	 */
	private static void decompressFusedNoFill(byte[] output, int elementCount, byte[] buffer, int bufferOffset,
		int elementBytes, int minBits, long offset) {
		long bitBuffer = 0;
		int bitsInBuffer = 0;
		int index = bufferOffset;
		long codeMask = minBits == 64 ? -1L : (1L << minBits) - 1L;

		ByteBuffer out = ByteBuffer.wrap(output).order(ByteOrder.nativeOrder());

		for (int i = 0; i < elementCount; i++) {
			while (bitsInBuffer < minBits) {
				bitBuffer = (bitBuffer << 8) | (buffer[index++] & 0xFFL);
				bitsInBuffer += 8;
			}
			long value = ((bitBuffer >>> (bitsInBuffer - minBits)) & codeMask) + offset;
			bitsInBuffer -= minBits;

			switch (elementBytes) {
				case 1 -> output[i] = (byte) value;
				case 2 -> out.putShort(i * 2, (short) value);
				case 4 -> out.putInt(i * 4, (int) value);
				case 8 -> out.putLong(i * 8, value);
				default -> throw new HdfFilterException("Unsupported integer size: " + elementBytes);
			}
		}
	}

	private static int nativeOrder() {
		return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? ORDER_LE : ORDER_BE;
	}

	/**
	 * Single pass over the bit stream for large {@code minBits} (more than 56 bits)
	 * where the code may not fit in a single 64 bit buffer between elements. Uses a
	 * running carry analogous to the reference implementation instead. Correct for
	 * all element sizes including 8 byte types.
	 */
	private static void decompressCarry(byte[] output, int elementCount, byte[] buffer, int bufferOffset,
		int elementBytes, int minBits, long maxValue, long offset, long fillValue) {
		int index = bufferOffset;
		int bitsToFill = 8;

		ByteBuffer out = ByteBuffer.wrap(output).order(ByteOrder.nativeOrder());

		for (int i = 0; i < elementCount; i++) {
			long code = 0;
			int bitsLeft = minBits;
			while (bitsLeft > 0) {
				int bitsToCopy = Math.min(bitsLeft, bitsToFill);
				int val = buffer[index] & 0xFF;
				int shift = bitsToFill - bitsToCopy;
				code = (code << bitsToCopy) | ((val >> shift) & ((1 << bitsToCopy) - 1));
				bitsLeft -= bitsToCopy;
				bitsToFill -= bitsToCopy;
				if (bitsToFill == 0) {
					index++;
					bitsToFill = 8;
				}
			}
			long value = (code == maxValue) ? fillValue : code + offset;

			switch (elementBytes) {
				case 1 -> output[i] = (byte) value;
				case 2 -> out.putShort(i * 2, (short) value);
				case 4 -> out.putInt(i * 4, (int) value);
				case 8 -> out.putLong(i * 8, value);
				default -> throw new HdfFilterException("Unsupported integer size: " + elementBytes);
			}
		}
	}

	/**
	 * As {@link #decompressCarry} but for chunks without a fill value.
	 */
	private static void decompressCarryNoFill(byte[] output, int elementCount, byte[] buffer, int bufferOffset,
		int elementBytes, int minBits, long offset) {
		int index = bufferOffset;
		int bitsToFill = 8;

		ByteBuffer out = ByteBuffer.wrap(output).order(ByteOrder.nativeOrder());

		for (int i = 0; i < elementCount; i++) {
			long code = 0;
			int bitsLeft = minBits;
			while (bitsLeft > 0) {
				int bitsToCopy = Math.min(bitsLeft, bitsToFill);
				int val = buffer[index] & 0xFF;
				int shift = bitsToFill - bitsToCopy;
				code = (code << bitsToCopy) | ((val >> shift) & ((1 << bitsToCopy) - 1));
				bitsLeft -= bitsToCopy;
				bitsToFill -= bitsToCopy;
				if (bitsToFill == 0) {
					index++;
					bitsToFill = 8;
				}
			}
			long value = code + offset;

			switch (elementBytes) {
				case 1 -> output[i] = (byte) value;
				case 2 -> out.putShort(i * 2, (short) value);
				case 4 -> out.putInt(i * 4, (int) value);
				case 8 -> out.putLong(i * 8, value);
				default -> throw new HdfFilterException("Unsupported integer size: " + elementBytes);
			}
		}
	}

	private static boolean needsByteSwap(int dtypeOrder) {
		if (dtypeOrder == ORDER_LE) {
			return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
		}
		if (dtypeOrder == ORDER_BE) {
			return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
		}
		return false;
	}

	private static void swapEndianInPlace(byte[] data, int elementBytes) {
		if (elementBytes == 1) {
			return;
		}
		final ByteBuffer buffer = ByteBuffer.wrap(data);
		switch (elementBytes) {
			case 2 -> {
				for (int i = 0; i < data.length; i += 2) {
					buffer.putShort(i, Short.reverseBytes(buffer.getShort(i)));
				}
			}
			case 4 -> {
				for (int i = 0; i < data.length; i += 4) {
					buffer.putInt(i, Integer.reverseBytes(buffer.getInt(i)));
				}
			}
			case 8 -> {
				for (int i = 0; i < data.length; i += 8) {
					buffer.putLong(i, Long.reverseBytes(buffer.getLong(i)));
				}
			}
			default -> throw new HdfFilterException("Unsupported integer size: " + elementBytes);
		}
	}
}
