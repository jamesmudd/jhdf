/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.links;

import io.jhdf.exceptions.HdfException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * Guards link resolution against cycles and excessively long chains.
 */
final class LinkResolutionGuard {

	private static final int MAX_LINK_RESOLUTION_DEPTH = 16;
	private static final ThreadLocal<Deque<String>> RESOLUTION_STACK =
		ThreadLocal.withInitial(ArrayDeque::new);

	private LinkResolutionGuard() {
		throw new AssertionError("No instances of LinkResolutionGuard");
	}

	static <T> T resolve(String key, Supplier<T> resolver) {
		final Deque<String> resolutionStack = RESOLUTION_STACK.get();

		if (resolutionStack.contains(key)) {
			throw new HdfException("Cyclic link resolution detected for: " + key);
		}
		if (resolutionStack.size() >= MAX_LINK_RESOLUTION_DEPTH) {
			throw new HdfException("Link resolution exceeds the maximum depth of " + MAX_LINK_RESOLUTION_DEPTH);
		}

		resolutionStack.push(key);
		try {
			return resolver.get();
		} finally {
			resolutionStack.pop();
			if (resolutionStack.isEmpty()) {
				RESOLUTION_STACK.remove();
			}
		}
	}
}
