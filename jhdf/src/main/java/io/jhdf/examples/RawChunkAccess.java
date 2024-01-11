/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.dataset.ChunkedDataset;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.file.Paths;

/**
 * Example application for raw chunk access from HDF5
 *
 * @author James Mudd
 */
public class RawChunkAccess {
	public static void main(String[] args) {
		try (HdfFile hdfFile = new HdfFile(Paths.get(args[0]))) {
			Dataset dataset = hdfFile.getDatasetByPath(args[1]);
			if (dataset instanceof ChunkedDataset) {
				ChunkedDataset chunkedDataset = (ChunkedDataset) dataset;
				int[] chunkOffset = new int[chunkedDataset.getChunkDimensions().length];
				System.out.println("Chunk offset: " + ArrayUtils.toString(chunkOffset)); //NOSONAR - sout in example
				// For the example just get the zero chunk but you can get any
				ByteBuffer rawChunkBuffer = chunkedDataset.getRawChunkBuffer(chunkOffset);
				// If you need the buffer just use it directly here, if you want the byte[]
				byte[] byteArray = new byte[rawChunkBuffer.capacity()];
				rawChunkBuffer.get(byteArray);
				// Now you have the byte[] to use as you like
				System.out.println("Raw bytes: " + ArrayUtils.toString(byteArray)); //NOSONAR - sout in example
			} else {
				throw new IllegalArgumentException("Dataset is not chunked");
			}
		}
	}
}
