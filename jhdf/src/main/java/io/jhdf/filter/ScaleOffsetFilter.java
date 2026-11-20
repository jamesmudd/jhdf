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
		} else if (minBits != 0) {
			decompress(decoded, elementCount, encodedData, BUFFER_OFFSET, elementBytes, minBits, nativeOrder());
		}

		if (dtypeClass == CLS_INTEGER) {
			long fillValue = 0L;
			if (fillAvailable == FILL_DEFINED) {
				int fillValuesNeeded = (elementBytes + Integer.BYTES - 1) / Integer.BYTES;
				if (filterData.length < PARM_FILVAL + fillValuesNeeded) {
					throw new HdfFilterException("Scale-offset fill value data is incomplete. Expected at least "
						+ (PARM_FILVAL + fillValuesNeeded) + " values but got " + filterData.length);
				}
				fillValue = readFillValue(filterData, elementBytes);
			}
			postDecompressInteger(decoded, elementCount, elementBytes, dtypeSign, fillAvailable, minBits, minVal,
				fillValue);
		} else if (dtypeClass == CLS_FLOAT) {
			throw new UnsupportedHdfException("Scale-offset filter float support is not implemented");
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

	private static void postDecompressInteger(byte[] data, int elementCount, int elementBytes, int dtypeSign,
		int fillAvailable, int minBits, long minVal, long fillValue) {
		long maxValue = fillAvailable == FILL_DEFINED ? (minBits == 64 ? -1L : (1L << minBits) - 1L) : 0L;
		long offset = dtypeSign == SIGN_2 ? toSignedMin(minVal, elementBytes) : minVal;

		switch (elementBytes) {
			case 1 -> postDecompressByte(data, elementCount, fillAvailable, maxValue, offset, fillValue);
			case 2 -> postDecompressShort(data, elementCount, fillAvailable, maxValue, offset, fillValue);
			case 4 -> postDecompressInt(data, elementCount, fillAvailable, maxValue, offset, fillValue);
			case 8 -> postDecompressLong(data, elementCount, fillAvailable, maxValue, offset, fillValue);
			default -> throw new HdfFilterException("Unsupported integer size: " + elementBytes);
		}
	}

	private static void postDecompressByte(byte[] data, int elementCount, int fillAvailable, long maxValue, long offset,
		long fillValue) {
		for (int i = 0; i < elementCount; i++) {
			long value = data[i] & 0xFF;
			data[i] = (byte) applyOffset(value, fillAvailable, maxValue, offset, fillValue);
		}
	}

	private static void postDecompressShort(byte[] data, int elementCount, int fillAvailable, long maxValue, long offset,
		long fillValue) {
		// The decompressed data is in native byte order
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
		for (int i = 0; i < elementCount; i++) {
			long value = Short.toUnsignedLong(buffer.getShort(i * 2));
			buffer.putShort(i * 2, (short) applyOffset(value, fillAvailable, maxValue, offset, fillValue));
		}
	}

	private static void postDecompressInt(byte[] data, int elementCount, int fillAvailable, long maxValue, long offset,
		long fillValue) {
		// The decompressed data is in native byte order
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
		for (int i = 0; i < elementCount; i++) {
			long value = Integer.toUnsignedLong(buffer.getInt(i * 4));
			buffer.putInt(i * 4, (int) applyOffset(value, fillAvailable, maxValue, offset, fillValue));
		}
	}

	private static void postDecompressLong(byte[] data, int elementCount, int fillAvailable, long maxValue, long offset,
		long fillValue) {
		// The decompressed data is in native byte order
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
		for (int i = 0; i < elementCount; i++) {
			long value = buffer.getLong(i * 8);
			long updated = applyOffset(value, fillAvailable, maxValue, offset, fillValue);
			buffer.putLong(i * 8, updated);
		}
	}

	/**
	 * Reconstructs the original value from the decompressed code applying the
	 * stored minimum and, if defined, substituting the fill value for the
	 * maximum code value.
	 */
	private static long applyOffset(long value, int fillAvailable, long maxValue, long offset, long fillValue) {
		if (fillAvailable == FILL_DEFINED && value == maxValue) {
			return fillValue;
		}
		return value + offset;
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

	private static void decompress(byte[] output, int elementCount, byte[] buffer, int bufferOffset,
		int elementBytes, int minBits, int memOrder) {
		int dtypeLen = elementBytes * 8;
		BitCursor cursor = new BitCursor(bufferOffset);

		Arrays.fill(output, (byte) 0);

		if (memOrder == ORDER_LE) {
			int begin = elementBytes - 1 - (dtypeLen - minBits) / 8;
			for (int i = 0; i < elementCount; i++) {
				int elementOffset = i * elementBytes;
				for (int k = begin; k >= 0; k--) {
					decompressOneByte(output, elementOffset, k, begin, buffer, cursor, minBits, dtypeLen);
				}
			}
		} else {
			int begin = (dtypeLen - minBits) / 8;
			for (int i = 0; i < elementCount; i++) {
				int elementOffset = i * elementBytes;
				for (int k = begin; k <= elementBytes - 1; k++) {
					decompressOneByte(output, elementOffset, k, begin, buffer, cursor, minBits, dtypeLen);
				}
			}
		}
	}

	private static void decompressOneByte(byte[] output, int elementOffset, int k, int begin, byte[] buffer,
		BitCursor cursor, int minBits, int dtypeLen) {
		if (cursor.index >= buffer.length) {
			throw new HdfFilterException("Scale-offset buffer too short");
		}
		int val = buffer[cursor.index] & 0xFF;
		int bitsToCopy = (k == begin) ? 8 - (dtypeLen - minBits) % 8 : 8;

		if (cursor.bitsToFill > bitsToCopy) {
			output[elementOffset + k] = (byte) ((val >> (cursor.bitsToFill - bitsToCopy)) & ((1 << bitsToCopy) - 1));
			cursor.bitsToFill -= bitsToCopy;
			return;
		}

		output[elementOffset + k] = (byte) ((val & ((1 << cursor.bitsToFill) - 1)) << (bitsToCopy - cursor.bitsToFill));
		bitsToCopy -= cursor.bitsToFill;
		cursor.index++;
		cursor.bitsToFill = 8;
		if (bitsToCopy == 0) {
			return;
		}
		if (cursor.index >= buffer.length) {
			throw new HdfFilterException("Scale-offset buffer too short");
		}
		val = buffer[cursor.index] & 0xFF;
		output[elementOffset + k] |= (byte) ((val >> (cursor.bitsToFill - bitsToCopy)) & ((1 << bitsToCopy) - 1));
		cursor.bitsToFill -= bitsToCopy;
	}

	private static int nativeOrder() {
		return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? ORDER_LE : ORDER_BE;
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

	private static final class BitCursor {
		private int index;
		private int bitsToFill;

		private BitCursor(int index) {
			this.index = index;
			this.bitsToFill = 8;
		}
	}
}
