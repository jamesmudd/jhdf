package io.jhdf.api;

import java.nio.ByteBuffer;

/**
 * HDF5 dataset. Datasets contain the real data within a HDF5 file.
 * 
 * @author James Mudd
 */
public interface Dataset extends Node {

	ByteBuffer getDataBuffer();

}