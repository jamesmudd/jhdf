package io.jhdf.exceptions;

import java.io.IOException;

public class HdfWritingExcpetion extends HdfException {

	public HdfWritingExcpetion(String message) {
		super(message);
	}

	public HdfWritingExcpetion(String message, Throwable e) {
		super(message, e);
	}
}
