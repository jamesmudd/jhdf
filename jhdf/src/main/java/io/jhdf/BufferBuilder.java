package io.jhdf;

import io.jhdf.exceptions.HdfException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferBuilder {

	private final ByteArrayOutputStream byteArrayOutputStream;
	private final DataOutputStream dataOutputStream;

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

	public ByteBuffer build() {
		try {
			ByteBuffer byteBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
			dataOutputStream.close();
			byteArrayOutputStream.close();
			return byteBuffer;
		} catch (IOException e) {
			throw new BufferBuilderException(e);
		}
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
