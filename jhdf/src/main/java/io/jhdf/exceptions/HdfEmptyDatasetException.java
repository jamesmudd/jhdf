/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

/**
 * Exception thrown when a attempt is made to access an empty dataset
 *
 * @author James Mudd
 */
public class HdfEmptyDatasetException extends HdfException {

    public HdfEmptyDatasetException(String message) {
        super(message);
    }

}
