/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.storage.HdfBackingStorage;
import org.apache.commons.lang3.concurrent.ConcurrentException;

public class CommittedDatatype extends AbstractNode {

	public CommittedDatatype(HdfBackingStorage hdfFc, long address, String name, Group parent) {
		super(hdfFc, address, name, parent);
	}

	@Override
	public NodeType getType() {
		return NodeType.COMMITTED_DATATYPE;
	}

	public DataType getDataType() {
		try {
			return header.get().getMessageOfType(DataTypeMessage.class).getDataType();
		} catch (ConcurrentException e) {
			throw new HdfException("Failed to get data type from committed datatype [" + getPath() + "]");
		}
	}
}
