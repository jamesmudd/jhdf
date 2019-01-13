package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Utils;

/**
 * <p>
 * Fill Value (Old) Message
 * </p>
 * 
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#OldFillValueMessage">Format
 * Spec</a>
 * </p>
 * 
 * @author James Mudd
 */
public class FillValueOldMessage extends Message {

	private final ByteBuffer fillValue;

	/* package */ FillValueOldMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		final int size = Utils.readBytesAsUnsignedInt(bb, 4);
		fillValue = Utils.createSubBuffer(bb, size);
	}

	/**
	 * The fill value. The bytes of the fill value are interpreted using the same
	 * datatype as for the dataset.
	 * 
	 * @return a buffer containing the fill value
	 */
	public ByteBuffer getFillValue() {
		return fillValue.asReadOnlyBuffer();
	}

}
