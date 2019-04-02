/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 * 
 * http://jhdf.io
 * 
 * Copyright 2019 James Mudd
 * 
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;

/**
 * <p>
 * The Data Storage - Filter Pipeline Message
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

	private final byte version;
	private final List<Filter> filters;

	public FilterPipelineMessage(ByteBuffer bb, BitSet messageFlags) {
		super(messageFlags);

		version = bb.get();

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
			// Filter ID
			final int filterId = Utils.readBytesAsUnsignedInt(bb, 2);

			// Name length
			final int nameLength;
			if (version == 2 && filterId < 256) {
				nameLength = 0;
			} else {
				nameLength = Utils.readBytesAsUnsignedInt(bb, 2);
			}

			// 2 bytes of flags
			BitSet flags = BitSet.valueOf(new byte[] { bb.get(), bb.get() });
			final boolean optional = flags.get(0);

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

			filters.add(new Filter(filterId, name, flags, data));
		}

	}

	public List<Filter> getFilters() {
		return filters;
	}

	public class Filter {

		private final int id;
		private final String name;
		private final BitSet flags;
		private final int[] data;

		public Filter(int id, String name, BitSet flags, int[] data) {
			this.id = id;
			this.name = name;
			this.flags = flags;
			this.data = data;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public BitSet getFlags() {
			return flags;
		}

		public int[] getData() {
			return data;
		}

		@Override
		public String toString() {
			return "Filter [id=" + id + ", name=" + name + ", flags=" + flags + ", data=" + Arrays.toString(data) + "]";
		}
	}
}
