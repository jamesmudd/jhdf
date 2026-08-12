/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.h5dump;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.jhdf.Utils;

import java.util.regex.Pattern;

public class DataXml {
	private static final Pattern PATTERN = Pattern.compile("[\\n\\s\"]{2,}");
	@JacksonXmlProperty(localName = "DataFromFile")
	String dataString;

	public String[] getData() {
		return PATTERN.splitAsStream(dataString)
			.filter(s -> !Utils.isBlank(s))
			.toArray(String[]::new);
	}
}
