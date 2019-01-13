package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.exceptions.HdfException;

/**
 * <p>
 * B-tree ‘K’ Values Message
 * </p>
 * 
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#BtreeKValuesMessage">Format
 * Spec</a>
 * </p>
 * 
 * @author James Mudd
 */
public class BTreeKValuesMessage extends Message {

	private final short indexedStorageInternalNodeK;
	private final short groupInternalNodeK;
	private final short groupLeafNodeK;

	/* package */ BTreeKValuesMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		final byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unreconised version " + version);
		}

		indexedStorageInternalNodeK = bb.getShort();
		groupInternalNodeK = bb.getShort();
		groupLeafNodeK = bb.getShort();
	}

	public short getIndexedStorageInternalNodeK() {
		return indexedStorageInternalNodeK;
	}

	public short getGroupInternalNodeK() {
		return groupInternalNodeK;
	}

	public short getGroupLeafNodeK() {
		return groupLeafNodeK;
	}

}
