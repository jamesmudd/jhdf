/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * <p>
 * The Data Storage - FilterInfo Pipeline Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#FilterMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class FilterPipelineMessage extends Message {

	private static final int OPTIONAL = 0;

	private final List<FilterInfo> filters;

	public FilterPipelineMessage(ByteBuffer bb, BitSet messageFlags) {
		super(messageFlags);

		final byte version = bb.get();

		if (version != 1 && version != 2) {
			throw new UnsupportedHdfException("Only filer pipeline version 1 or 2 are supported");
		}

		final byte numberOfFilters = bb.get();
		filters = new ArrayList<>(numberOfFilters);

		if (version == 1) {
			// Skip 6 reserved bytes
			bb.position(bb.position() + 6);
		}

		// Read filters
		for (int i = 0; i < numberOfFilters; i++) {
			// FilterInfo ID
			final int filterId = Utils.readBytesAsUnsignedInt(bb, 2);

			// Name length
			final int nameLength;
			if (version == 2 && filterId < 256) {
				nameLength = 0;
			} else {
				nameLength = Utils.readBytesAsUnsignedInt(bb, 2);
			}

			// 2 bytes of optional
			final BitSet flags = BitSet.valueOf(new byte[] { bb.get(), bb.get() });
			final boolean optional = flags.get(OPTIONAL);

			final int numberOfDataValues = Utils.readBytesAsUnsignedInt(bb, 2);

			final String name;
			if (nameLength >= 2) {
				name = Utils.readUntilNull(Utils.createSubBuffer(bb, nameLength));
			} else {
				name = "undefined";
			}

			final int[] data = new int[numberOfDataValues];
			for (int j = 0; j < numberOfDataValues; j++) {
				data[j] = bb.getInt();
			}
			// If there are a odd number of values then there are 4 bytes of padding
			if (version == 1 && numberOfDataValues % 2 != 0) {
				// Skip 4 padding bytes
				bb.position(bb.position() + 4);
			}

			filters.add(new FilterInfo(filterId, name, optional, data));
		}

	}

	public List<FilterInfo> getFilters() {
		return filters;
	}

	public static class FilterInfo {

		private final int id;
		private final String name;
		private final boolean optional;
		private final int[] data;

		public FilterInfo(int id, String name, boolean optional, int[] data) {
			this.id = id;
			this.name = name;
			this.optional = optional;
			this.data = data;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public boolean isOptional() {
			return optional;
		}

		public int[] getData() {
			return data;
		}

		@Override
		public String toString() {
			return "FilterInfo [id=" + id + ", name=" + name + ", optional=" + optional + ", data="
					+ Arrays.toString(data)
					+ "]";
		}
	}
}
