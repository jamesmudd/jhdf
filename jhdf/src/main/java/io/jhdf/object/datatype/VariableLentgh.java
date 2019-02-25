package io.jhdf.object.datatype;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

public class VariableLentgh extends DataType {

	private final int type;
	private final int paddingType;
	private final Charset encoding;
	private final DataType parent;

	public VariableLentgh(ByteBuffer bb) {
		super(bb);

		type = Utils.bitsToInt(classBits, 0, 4);
		paddingType = Utils.bitsToInt(classBits, 4, 4);
		int characterEncoding = Utils.bitsToInt(classBits, 8, 4);
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

	@Override
	public Class<?> getJavaType() {
		if (type == 1) {
			return String.class;
		} else {
			return parent.getJavaType();
		}
	}

}