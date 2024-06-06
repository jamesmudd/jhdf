/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Utils;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.BitSet;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * <p>
 * Old Object Modification Time Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#OldModificationTimeMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class OldObjectModificationTimeMessage extends Message {

	public static final int MESSAGE_TYPE = 14;

	final LocalDateTime modificationTime;

	public OldObjectModificationTimeMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		final ByteBuffer yearBuffer = Utils.createSubBuffer(bb, 4);
		final int year = parseInt(US_ASCII.decode(yearBuffer).toString());

		final ByteBuffer monthBuffer = Utils.createSubBuffer(bb, 2);
		final int month = parseInt(US_ASCII.decode(monthBuffer).toString());

		final ByteBuffer dayBuffer = Utils.createSubBuffer(bb, 2);
		final int day = parseInt(US_ASCII.decode(dayBuffer).toString());

		final ByteBuffer hourBuffer = Utils.createSubBuffer(bb, 2);
		final int hour = parseInt(US_ASCII.decode(hourBuffer).toString());

		final ByteBuffer minuteBuffer = Utils.createSubBuffer(bb, 2);
		final int minute = parseInt(US_ASCII.decode(minuteBuffer).toString());

		final ByteBuffer secondBuffer = Utils.createSubBuffer(bb, 2);
		final int second = parseInt(US_ASCII.decode(secondBuffer).toString());

		// Skip reserved bytes
		bb.position(bb.position() + 2);

		this.modificationTime = LocalDateTime.of(year, month, day, hour, minute, second);
	}

	public LocalDateTime getModifiedTime() {
		return modificationTime;
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}


}
