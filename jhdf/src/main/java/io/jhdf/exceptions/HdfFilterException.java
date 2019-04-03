package io.jhdf.exceptions;

/**
 * Exception to be thrown when operations involving filters fail.
 * 
 * @author James Mudd
 */
public class HdfFilterException extends HdfException {

	private static final long serialVersionUID = 1L;

	public HdfFilterException(String message) {
		super(message);
	}

	public HdfFilterException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
