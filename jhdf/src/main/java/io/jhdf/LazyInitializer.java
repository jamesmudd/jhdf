/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.exceptions.ConcurrentException;

/**
 * A simple thread-safe holder which initializes its value lazily, on first access, then
 * caches it for all subsequent calls to {@link #get()}. Initialization is only ever
 * performed once, even when {@link #get()} is called concurrently from multiple threads.
 * <p>
 * This is a lightweight, drop in replacement for the (now removed)
 * {@code org.apache.commons.lang3.concurrent.LazyInitializer}, implemented using standard
 * Java double checked locking, so no external dependency is required.
 *
 * @param <T> the type of the object held by this lazy initializer
 * @author James Mudd
 */
public abstract class LazyInitializer<T> {

	private volatile boolean initialized;
	private volatile T value;

	/**
	 * Creates the value to be held by this lazy initializer. This is only called once, no
	 * matter how many times {@link #get()} is called, or from how many threads.
	 *
	 * @return the value to hold
	 * @throws ConcurrentException if an error occurs during initialization
	 */
	protected abstract T initialize() throws ConcurrentException;

	/**
	 * Gets the value, initializing it on the first call.
	 *
	 * @return the fully initialized value
	 * @throws ConcurrentException if an error occurs during initialization
	 */
	public T get() throws ConcurrentException {
		// First check without locking, this is safe because both fields are volatile
		if (!initialized) {
			synchronized (this) {
				// Check again now we hold the lock, another thread may have initialized already
				if (!initialized) {
					value = initialize();
					initialized = true;
				}
			}
		}
		return value;
	}
}
