/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.exceptions.HdfArithmeticException;

/**
 * Checked arithmetic helpers that report failures as {@link HdfArithmeticException}.
 */
public final class ArithmeticUtils {

	private ArithmeticUtils() {
		throw new AssertionError("No instances of ArithmeticUtils");
	}

	/**
	 * Adds two ints, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the first value
	 * @param b the second value
	 * @return the sum
	 * @throws HdfArithmeticException if the result overflows an int
	 */
	public static int addExact(int a, int b) {
		try {
			return Math.addExact(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Adds two longs, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the first value
	 * @param b the second value
	 * @return the sum
	 * @throws HdfArithmeticException if the result overflows a long
	 */
	public static long addExact(long a, long b) {
		try {
			return Math.addExact(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Subtracts two ints, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the first value
	 * @param b the second value
	 * @return the difference
	 * @throws HdfArithmeticException if the result overflows an int
	 */
	public static int subtractExact(int a, int b) {
		try {
			return Math.subtractExact(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Subtracts two longs, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the first value
	 * @param b the second value
	 * @return the difference
	 * @throws HdfArithmeticException if the result overflows a long
	 */
	public static long subtractExact(long a, long b) {
		try {
			return Math.subtractExact(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Multiplies two ints, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the first value
	 * @param b the second value
	 * @return the product
	 * @throws HdfArithmeticException if the result overflows an int
	 */
	public static int multiplyExact(int a, int b) {
		try {
			return Math.multiplyExact(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Multiplies a long and an int, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the first value
	 * @param b the second value
	 * @return the product
	 * @throws HdfArithmeticException if the result overflows a long
	 */
	public static long multiplyExact(long a, int b) {
		try {
			return Math.multiplyExact(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Multiplies two longs, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the first value
	 * @param b the second value
	 * @return the product
	 * @throws HdfArithmeticException if the result overflows a long
	 */
	public static long multiplyExact(long a, long b) {
		try {
			return Math.multiplyExact(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Increments an int, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the value to increment
	 * @return the incremented value
	 * @throws HdfArithmeticException if the result overflows an int
	 */
	public static int incrementExact(int a) {
		try {
			return Math.incrementExact(a);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Increments a long, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the value to increment
	 * @return the incremented value
	 * @throws HdfArithmeticException if the result overflows a long
	 */
	public static long incrementExact(long a) {
		try {
			return Math.incrementExact(a);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Decrements an int, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the value to decrement
	 * @return the decremented value
	 * @throws HdfArithmeticException if the result overflows an int
	 */
	public static int decrementExact(int a) {
		try {
			return Math.decrementExact(a);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Decrements a long, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the value to decrement
	 * @return the decremented value
	 * @throws HdfArithmeticException if the result overflows a long
	 */
	public static long decrementExact(long a) {
		try {
			return Math.decrementExact(a);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Negates an int, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the value to negate
	 * @return the negated value
	 * @throws HdfArithmeticException if the result overflows an int
	 */
	public static int negateExact(int a) {
		try {
			return Math.negateExact(a);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Negates a long, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param a the value to negate
	 * @return the negated value
	 * @throws HdfArithmeticException if the result overflows a long
	 */
	public static long negateExact(long a) {
		try {
			return Math.negateExact(a);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Converts a long to an int, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param value the value to convert
	 * @return the value as an int
	 * @throws HdfArithmeticException if the value does not fit in an int
	 */
	public static int toIntExact(long value) {
		try {
			return Math.toIntExact(value);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Returns the absolute value of an int, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param value the value to inspect
	 * @return the absolute value
	 * @throws HdfArithmeticException if the value cannot be represented as a positive int
	 */
	public static int absExact(int value) {
		try {
			return Math.absExact(value);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Returns the absolute value of a long, reporting overflow as an {@link HdfArithmeticException}.
	 *
	 * @param value the value to inspect
	 * @return the absolute value
	 * @throws HdfArithmeticException if the value cannot be represented as a positive long
	 */
	public static long absExact(long value) {
		try {
			return Math.absExact(value);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Divides two ints with floor rounding, reporting arithmetic failures as an {@link HdfArithmeticException}.
	 *
	 * @param a the dividend
	 * @param b the divisor
	 * @return the floor division result
	 * @throws HdfArithmeticException if the divisor is zero
	 */
	public static int floorDiv(int a, int b) {
		try {
			return Math.floorDiv(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Divides two longs with floor rounding, reporting arithmetic failures as an {@link HdfArithmeticException}.
	 *
	 * @param a the dividend
	 * @param b the divisor
	 * @return the floor division result
	 * @throws HdfArithmeticException if the divisor is zero
	 */
	public static long floorDiv(long a, long b) {
		try {
			return Math.floorDiv(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Divides a long by an int with floor rounding, reporting arithmetic failures as an {@link HdfArithmeticException}.
	 *
	 * @param a the dividend
	 * @param b the divisor
	 * @return the floor division result
	 * @throws HdfArithmeticException if the divisor is zero
	 */
	public static long floorDiv(long a, int b) {
		try {
			return Math.floorDiv(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Returns the floor modulus of two ints, reporting arithmetic failures as an {@link HdfArithmeticException}.
	 *
	 * @param a the dividend
	 * @param b the divisor
	 * @return the floor modulus result
	 * @throws HdfArithmeticException if the divisor is zero
	 */
	public static int floorMod(int a, int b) {
		try {
			return Math.floorMod(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Returns the floor modulus of two longs, reporting arithmetic failures as an {@link HdfArithmeticException}.
	 *
	 * @param a the dividend
	 * @param b the divisor
	 * @return the floor modulus result
	 * @throws HdfArithmeticException if the divisor is zero
	 */
	public static long floorMod(long a, long b) {
		try {
			return Math.floorMod(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}

	/**
	 * Returns the floor modulus of a long and an int, reporting arithmetic failures as an {@link HdfArithmeticException}.
	 *
	 * @param a the dividend
	 * @param b the divisor
	 * @return the floor modulus result
	 * @throws HdfArithmeticException if the divisor is zero
	 */
	public static int floorMod(long a, int b) {
		try {
			return Math.floorMod(a, b);
		} catch (ArithmeticException e) {
			throw new HdfArithmeticException("Arithmetic overflow", e);
		}
	}
}
