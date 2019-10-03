/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.object.datatype.CompoundDataType;
import io.jhdf.object.datatype.CompoundDataType.CompoundDataMember;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.toIntExact;

public final class CompoundDatasetReader {

	/** No instances */
	private CompoundDatasetReader() {
	}

	public static Map<String, Object> readDataset(CompoundDataType type, ByteBuffer buffer, long size, int[] dimensions) {
		final int sizeAsInt = toIntExact(size);

		final List<CompoundDataMember> members = type.getMembers();

		final Map<String, Object> data = new LinkedHashMap<>(members.size());

		for (CompoundDataMember member : members) {
			final byte[] memberBytes = new byte[member.getDataType().getSize()];
			final ByteBuffer memberBuffer = ByteBuffer.allocate(member.getDataType().getSize() * sizeAsInt);

			// Loop through the date buffer extracting the bytes for this member
			for (int i = 0; i < sizeAsInt; i++) {
				buffer.position(type.getSize() * i + member.getOffset());
				buffer.get(memberBytes, 0, memberBytes.length);
				memberBuffer.put(memberBytes);
			}

			// Now read this member
			memberBuffer.rewind();
			final Object memberData = DatasetReader.readDataset(member.getDataType(), memberBuffer, dimensions);
			data.put(member.getName(), memberData);
		}

		return data;
	}
}
