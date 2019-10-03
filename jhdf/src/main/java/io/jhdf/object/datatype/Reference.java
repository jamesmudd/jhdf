/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;


/**
 * Class to represent a Reference data type.
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#DatatypeMessage">Datatypes</a>
 * </p>
 *
 * @author Gisa Meier
 */
public class Reference extends DataType {

	public enum ReferenceType{

		/** Object Reference: A reference to another object in the HDF5 file.*/
		OBJECT,

		/** Dataset Region Reference: A reference to a region within a dataset in the HDF5 file. */
		REGION
	}

	private final ReferenceType type;

	public Reference(ByteBuffer bb) {
		super(bb);

		int rawType = Utils.bitsToInt(classBits, 0, 4);
		switch (rawType) {
			case 0:
				type = ReferenceType.OBJECT;
				break;
			case 1:
				type = ReferenceType.REGION;
				throw new UnsupportedHdfException("Dataset Region references not supported for Reference data type");
			default:
				throw new HdfException("Unrecognized Reference type. Index is: " + rawType);
		}

	}

	public ReferenceType getType() {
		return type;
	}

	@Override
	public Class<?> getJavaType() {
		//object reference type handles addresses, which are always longs
		if (getSize() > 8) {
			throw new HdfTypeException("Unsupported address size in reference data type " + getSize() + "bytes");
		}
		return long.class;
	}


}
