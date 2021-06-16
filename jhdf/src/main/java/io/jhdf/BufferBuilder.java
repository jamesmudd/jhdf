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

	public final long UNSIGNED_BYTE_MAX = Byte.MAX_VALUE * 2;
	public final long UNSIGNED_SHORT_MAX = Short.MAX_VALUE * 2;

	private final ByteArrayOutputStream byteArrayOutputStream;
	private final DataOutputStream dataOutputStream; // Note always big endian
	private final ByteOrder byteOrder = LITTLE_ENDIAN;

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
			if(byteOrder == LITTLE_ENDIAN) {
				i = Integer.reverseBytes(i);
			}
			dataOutputStream.writeShort(i);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public BufferBuilder writeInt(int i) {
		try {
			if(byteOrder == LITTLE_ENDIAN) {
				i = Integer.reverseBytes(i);
			}
			dataOutputStream.writeInt(i);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public BufferBuilder writeLong(long l) {
		try {
			if(byteOrder == LITTLE_ENDIAN) {
				l = Long.reverseBytes(l);
			}
			dataOutputStream.writeLong(l);
			return this;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public ByteBuffer build() {
		try {
			ByteBuffer byteBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
			byteBuffer.order(byteOrder);
			dataOutputStream.close();
			byteArrayOutputStream.close();
			return byteBuffer;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
	}

	public BufferBuilder writeBitSet(BitSet bitSet, int length) {
		if(bitSet.toByteArray().length > length) {
			throw new IllegalArgumentException("BitSet is longer than length provided");
		}
		try {
			final byte[] bytes = Arrays.copyOf(bitSet.toByteArray(), length); // Ensure empty Bitset are not shortened
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

}
