package io.jhdf.object.datatype;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

class VariableLentgh extends DataType {

	private final int type;
	private final int paddingType;
	private final Charset encoding;
	private final DataType parent;

	public VariableLentgh(ByteBuffer bb) {
		super(bb);

		type = classBytes[0] >>> 4;
		paddingType = classBytes[0] & 0xF;
		int characterEncoding = classBytes[1] >>> 4;
		switch (characterEncoding) {
		case 0:
			encoding = StandardCharsets.US_ASCII;
			break;
		case 1:
			encoding = StandardCharsets.UTF_8;
			break;
		default:
			throw new HdfException("Unreconised character encoding = " + characterEncoding);
		}

		Utils.seekBufferToNextMultipleOfEight(bb);
		parent = DataType.readDataType(bb);
	}

	public int getType() {
		return type;
	}

	public int getPaddingType() {
		return paddingType;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public DataType getParent() {
		return parent;
	}

}