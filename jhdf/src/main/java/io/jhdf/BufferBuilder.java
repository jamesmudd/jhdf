/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.checksum.ChecksumUtils;
import io.jhdf.exceptions.HdfException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class BufferBuilder {

	public static final long UNSIGNED_BYTE_MAX = Byte.MAX_VALUE * 2L;
	public static final long UNSIGNED_SHORT_MAX = Short.MAX_VALUE * 2L;
	private static final ByteOrder BYTE_ORDER = LITTLE_ENDIAN;

	private final ByteArrayOutputStream byteArrayOutputStream;
	private final DataOutputStream dataOutputStream; // Note always big endian

	public BufferBuilder() {
		this.byteArrayOutputStream = new ByteArrayOutputStream();
		this.dataOutputStream = new DataOutputStream(byteArrayOutputStream);
	}

	public BufferBuilder writeByte(int i) {
		try {
			dataOutputStream.writeByte(i);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public BufferBuilder writeBytes(byte[] bytes) {
		try {
			dataOutputStream.write(bytes);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public BufferBuilder writeShort(int i) {
		try {
			short s = (short) i;
			if(BYTE_ORDER == LITTLE_ENDIAN) {
				s = Short.reverseBytes(s);
			}
			dataOutputStream.writeShort(s);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public BufferBuilder writeInt(int i) {
		try {
			if(BYTE_ORDER == LITTLE_ENDIAN) {
				i = Integer.reverseBytes(i);
			}
			dataOutputStream.writeInt(i);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}
	
	public BufferBuilder writeInts(int[] ints) {
		for (int i=0; i < ints.length; i++) {
			writeInt(ints[i]);
		}
		return this;
	}

	public BufferBuilder writeLong(long l) {
		try {
			if(BYTE_ORDER == LITTLE_ENDIAN) {
				l = Long.reverseBytes(l);
			}
			dataOutputStream.writeLong(l);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}
	
	public BufferBuilder writeLongs(long[] longs) {
		for (int i=0; i < longs.length; i++) {
			writeLong(longs[i]);
		}
		return this;
	}

	public ByteBuffer build() {
		try {
			ByteBuffer byteBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
			byteBuffer.order(BYTE_ORDER);
			dataOutputStream.close();
			byteArrayOutputStream.close();
			return byteBuffer;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public BufferBuilder writeBitSet(BitSet bitSet, int lengthBytes) {
		if(bitSet.toByteArray().length > lengthBytes) {
			throw new IllegalArgumentException("BitSet is longer than length provided");
		}
		try {
			final byte[] bytes = Arrays.copyOf(bitSet.toByteArray(), lengthBytes); // Ensure empty Bitset are not shortened
			dataOutputStream.write(bytes);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public BufferBuilder appendChecksum() {
		writeInt(ChecksumUtils.checksum(byteArrayOutputStream.toByteArray()));
		return this;
	}

	public BufferBuilder writeBuffer(ByteBuffer byteBuffer) {
		try {
			dataOutputStream.write(byteBuffer.array());
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
		return this;
	}

	public BufferBuilder writeShortestRepresentation(int i) {
		try {
			if (i <= UNSIGNED_BYTE_MAX) {
				dataOutputStream.writeByte(i);
			} else if(i <= UNSIGNED_SHORT_MAX) {
				dataOutputStream.writeShort(i);
			} else {
				dataOutputStream.write(i);
			}
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
		return this;
	}

	public static final class BufferBuilderException extends HdfException {
		private BufferBuilderException(String message, Throwable throwable) {
			super(message, throwable);
		}

		private BufferBuilderException(Throwable throwable) {
			this("Error in BufferBuilder", throwable);
		}
	}

	@Override
	public String toString() {
		return "BufferBuilder{" +
			 "bytesWriten=" + dataOutputStream.size() +
			"}";
	}
}
