package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;

public class FilterpipelineMessage extends Message {

	private final byte version;
	private final List<Filter> filters;

	public FilterpipelineMessage(ByteBuffer bb, BitSet messageFlags) {
		super(messageFlags);

		version = bb.get();

		if (version != 1 && version != 2) {
			throw new UnsupportedHdfException("Only filer pipeline version 1 is supported");
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
			final int nameLentgh;
			if (version == 2 && filterId < 256) {
				nameLentgh = 0;
			} else {
				nameLentgh = Utils.readBytesAsUnsignedInt(bb, 2);
			}

			// 2 bytes of flags
			BitSet flags = BitSet.valueOf(new byte[] { bb.get(), bb.get() });
			final boolean optional = flags.get(0);

			final int numberOfDataValues = Utils.readBytesAsUnsignedInt(bb, 2);

			if (nameLentgh >= 2) {
				final String name = Utils.readUntilNull(Utils.createSubBuffer(bb, nameLentgh));
			}

			final int[] data = new int[numberOfDataValues];
			for (int j = 0; j < numberOfDataValues; j++) {
				data[i] = bb.getInt();
			}
			// If there are a odd number of values then there are 4 bytes of padding
			if (version == 1 && numberOfDataValues % 2 != 0) {
				// Skip 4 padding bytes
				bb.position(bb.position() + 4);
			}

		}

	}
}
