package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Superblock;

/**
 * <p>
 * Data Space Message. Used to describe the dimensionality of datasets.
 * </p>
 * 
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#DataspaceMessage">Format
 * Spec</a>
 * </p>
 * 
 * @author James Mudd
 */
public class DataSpaceMessage extends Message {

	private final DataSpace dataSpace;

	/* package */ DataSpaceMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		super(flags);

		dataSpace = DataSpace.readDataSpace(bb, sb);
	}

	public DataSpace getDataSpace() {
		return dataSpace;
	}
}
