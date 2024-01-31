/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.object.datatype.CompoundDataType;
import io.jhdf.object.datatype.CompoundDataType.CompoundDataMember;
import io.jhdf.storage.HdfBackingStorage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CompoundDatasetReader {

	private CompoundDatasetReader() {
		throw new AssertionError("No instances of CompoundDatasetReader");
	}

	public static Map<String, Object> readDataset(CompoundDataType type, ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
		final int sizeAsInt = Arrays.stream(dimensions).reduce(1, Math::multiplyExact);

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

			final Object memberData = DatasetReader.readDataset(member.getDataType(), memberBuffer, dimensions, hdfBackingStorage);
			data.put(member.getName(), memberData);
		}

		return data;
	}
}
