package com.jamesmudd.jhdf.exceptions;

/**
 * Superclass of all exceptions thrown by jHDF.
 * 
 * @author James Mudd
 */
public class HdfException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HdfException(String message) {
		super(message);
	}

	public HdfException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
