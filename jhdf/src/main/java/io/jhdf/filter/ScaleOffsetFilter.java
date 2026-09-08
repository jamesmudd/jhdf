/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScaleOffsetFilter implements Filter {

	private static final int PARM_SCALETYPE = 0;
	private static final int PARM_SCALEFACTOR = 1;
	private static final int PARM_NELMTS = 2;
	private static final int PARM_CLASS = 3;
	private static final int PARM_SIZE = 4;
	private static final int PARM_SIGN = 5;
	private static final int PARM_ORDER = 6;
	private static final int PARM_FILAVAIL = 7;
	private static final int PARM_FILVAL = 8;
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
		int elementCount = filterData[PARM_NELMTS];
		int elementBytes = filterData[PARM_SIZE];
		int dtypeClass = filterData[PARM_CLASS];
		int dtypeSign = filterData[PARM_SIGN];
		int dtypeOrder = filterData[PARM_ORDER];
		int fillAvailable = filterData[PARM_FILAVAIL];

		int minBits = readIntLE(encodedData, 0);
		int minValSize = Byte.toUnsignedInt(encodedData[4]);
		long minVal = readUnsignedLongLE(encodedData, 5, minValSize);

		int bufOffset = BUFFER_OFFSET;
		int outputSize = elementCount * elementBytes;

		byte[] decoded = new byte[outputSize];
		if (minBits == elementBytes * 8) {
			System.arraycopy(encodedData, bufOffset, decoded, 0, outputSize);
			if (needsByteSwap(dtypeOrder)) {
				swapEndianInPlace(decoded, elementBytes);
			}
			return decoded;
		} else if (minBits != 0) {
			decompress(decoded, elementCount, encodedData, bufOffset, elementBytes, minBits, nativeOrder());
		}

		if (dtypeClass == CLS_INTEGER) {
			long fillValue = fillAvailable == FILL_DEFINED ? readFillValue(filterData, elementBytes) : 0L;
			postDecompressInteger(decoded, elementCount, elementBytes, dtypeSign, fillAvailable, minBits, minVal, fillValue);
		} else if (dtypeClass == CLS_FLOAT) {
			throw new IllegalArgumentException("Scale-offset filter float support is not implemented");
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

		ByteBuffer buffer = ByteBuffer.wrap(fillBytes).order(ByteOrder.nativeOrder());
		return switch (elementBytes) {
			case 1 -> buffer.get() & 0xFFL;
			case 2 -> buffer.getShort() & 0xFFFFL;
			case 4 -> buffer.getInt() & 0xFFFFFFFFL;
			case 8 -> buffer.getLong();
			default -> throw new IllegalArgumentException("Unsupported fill value size: " + elementBytes);
		};
	}

	private static void postDecompressInteger(byte[] data, int elementCount, int elementBytes, int dtypeSign,
		int fillAvailable, int minBits, long minVal, long fillValue) {
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
		long maxValue = minBits == 64 ? -1L : (1L << minBits) - 1L;
		long signedMin = dtypeSign == SIGN_2 ? toSignedMin(minVal, elementBytes) : minVal;

		for (int i = 0; i < elementCount; i++) {
			long value = switch (elementBytes) {
				case 1 -> buffer.get(i) & 0xFFL;
				case 2 -> buffer.getShort(i * elementBytes) & 0xFFFFL;
				case 4 -> buffer.getInt(i * elementBytes) & 0xFFFFFFFFL;
				case 8 -> buffer.getLong(i * elementBytes);
				default -> throw new IllegalArgumentException("Unsupported integer size: " + elementBytes);
			};

			long updated;
			if (fillAvailable == FILL_DEFINED && value == maxValue) {
				updated = fillValue;
			} else if (dtypeSign == SIGN_NONE) {
				updated = value + minVal;
			} else {
				updated = value + signedMin;
			}

			switch (elementBytes) {
				case 1 -> buffer.put(i, (byte) updated);
				case 2 -> buffer.putShort(i * elementBytes, (short) updated);
				case 4 -> buffer.putInt(i * elementBytes, (int) updated);
				case 8 -> buffer.putLong(i * elementBytes, updated);
				default -> throw new IllegalArgumentException("Unsupported integer size: " + elementBytes);
			}
		}
	}

	private static long toSignedMin(long minVal, int elementBytes) {
		return switch (elementBytes) {
			case 1 -> (byte) minVal;
			case 2 -> (short) minVal;
			case 4 -> (int) minVal;
			case 8 -> minVal;
			default -> throw new IllegalArgumentException("Unsupported integer size: " + elementBytes);
		};
	}

	private static void decompress(byte[] output, int elementCount, byte[] buffer, int bufferOffset,
		int elementBytes, int minBits, int memOrder) {
		int dtypeLen = elementBytes * 8;
		BitCursor cursor = new BitCursor(bufferOffset);

		Arrays.fill(output, (byte) 0);

		for (int i = 0; i < elementCount; i++) {
			int elementOffset = i * elementBytes;
			if (memOrder == ORDER_LE) {
				int begin = elementBytes - 1 - (dtypeLen - minBits) / 8;
				for (int k = begin; k >= 0; k--) {
					decompressOneByte(output, elementOffset, k, begin, buffer, cursor, minBits, dtypeLen);
				}
			} else {
				int begin = (dtypeLen - minBits) / 8;
				for (int k = begin; k <= elementBytes - 1; k++) {
					decompressOneByte(output, elementOffset, k, begin, buffer, cursor, minBits, dtypeLen);
				}
			}
		}
	}

	private static void decompressOneByte(byte[] output, int elementOffset, int k, int begin, byte[] buffer,
		BitCursor cursor, int minBits, int dtypeLen) {
		if (cursor.index >= buffer.length) {
			throw new IllegalArgumentException("Scale-offset buffer too short");
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
			throw new IllegalArgumentException("Scale-offset buffer too short");
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
		for (int i = 0; i < data.length; i += elementBytes) {
			for (int j = 0; j < elementBytes / 2; j++) {
				byte tmp = data[i + j];
				data[i + j] = data[i + elementBytes - 1 - j];
				data[i + elementBytes - 1 - j] = tmp;
			}
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
