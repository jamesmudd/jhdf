/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import java.nio.ByteOrder;

public interface OrderedDataType {

	ByteOrder getByteOrder();

}
