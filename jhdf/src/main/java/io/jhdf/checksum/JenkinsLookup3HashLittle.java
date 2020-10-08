/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.checksum;

import static java.lang.Byte.toUnsignedInt;
import static java.lang.Integer.rotateLeft;

/**
 * Hash used for HDF5 consistency checking Java code inspired by the Bob Jenkins C
 * code.
 * <p>
 * lookup3.c, by Bob Jenkins, May 2006, Public Domain.
 *
 * You can use this free for any purpose.  It's in the public domain.
 * It has no warranty.
 * </p>
 *
 * @author James Mudd
 * @see <a href="http://burtleburtle.net/bob/c/lookup3.c">lookup3.c</a>
 */
public final class JenkinsLookup3HashLittle {

	private static final int INITIALISATION_CONSTANT = 0xdeadbeef;

	private JenkinsLookup3HashLittle() {
		throw new AssertionError("No instances of JenkinsLookup3HashLittle");
	}

	/**
	 * Equivalent to {@link #hash(byte[], int)} with initialValue = 0
	 *
	 * @param key bytes to hash
	 * @return hash value
	 */
	public static int hash(final byte[] key) {
		return hash(key, 0);
	}

	/**
	 * <p>
	 * The best hash table sizes are powers of 2.  There is no need to do mod
	 * a prime (mod is sooo slow!).  If you need less than 32 bits, use a bitmask.
	 * For example, if you need only 10 bits, do
	 * <code>h = (h {@literal &} hashmask(10));</code>
	 * In which case, the hash table should have hashsize(10) elements.
	 * </p>
	 *
	 * <p>If you are hashing n strings byte[][] k, do it like this:
	 * for (int i = 0, h = 0; i {@literal <} n; ++i) h = hash(k[i], h);
	 * </p>
	 *
	 * <p>By Bob Jenkins, 2006.  bob_jenkins@burtleburtle.net.  You may use this
	 * code any way you wish, private, educational, or commercial.  It's free.
	 * </p>
	 *
	 * <p>
	 * Use for hash table lookup, or anything where one collision in 2^^32 is
	 * acceptable.  Do NOT use for cryptographic purposes.
	 * </p>
	 *
	 * @param key          bytes to hash
	 * @param initialValue can be any integer value
	 * @return hash value.
	 */
	@SuppressWarnings({"squid:S128"}) // fallthrough
	public static int hash(final byte[] key, final int initialValue) {

		// Initialise a, b and c
		int a = INITIALISATION_CONSTANT + key.length + initialValue;
		int b = a;
		int c = b;

		int offset = 0;
		int i;



		for (i = key.length; i > 12; offset += 12, i -= 12) {
			a += toUnsignedInt(key[offset]);
			a += toUnsignedInt(key[offset + 1]) << 8;
			a += toUnsignedInt(key[offset + 2]) << 16;
			a += toUnsignedInt(key[offset + 3]) << 24;
			b += toUnsignedInt(key[offset + 4]);
			b += toUnsignedInt(key[offset + 5]) << 8;
			b += toUnsignedInt(key[offset + 6]) << 16;
			b += toUnsignedInt(key[offset + 7]) << 24;
			c += toUnsignedInt(key[offset + 8]);
			c += toUnsignedInt(key[offset + 9]) << 8;
			c += toUnsignedInt(key[offset + 10]) << 16;
			c += toUnsignedInt(key[offset + 11]) << 24;

			/*
			 * mix -- mix 3 32-bit values reversibly.
			 * This is reversible, so any information in (a,b,c) before mix() is
			 * still in (a,b,c) after mix().
			 *
			 * If four pairs of (a,b,c) inputs are run through mix(), or through
			 * mix() in reverse, there are at least 32 bits of the output that
			 * are sometimes the same for one pair and different for another pair.
			 *
			 * This was tested for:
			 * - pairs that differed by one bit, by two bits, in any combination
			 *   of top bits of (a,b,c), or in any combination of bottom bits of
			 *   (a,b,c).
			 * - "differ" is defined as +, -, ^, or ~^.  For + and -, I transformed
			 *   the output delta to a Gray code (a^(a>>1)) so a string of 1's (as
			 *    is commonly produced by subtraction) look like a single 1-bit
			 *    difference.
			 * - the base values were pseudorandom, all zero but one bit set, or
			 *   all zero plus a counter that starts at zero.
			 *
			 * Some k values for my "a-=c; a^=rot(c,k); c+=b;" arrangement that
			 * satisfy this are
			 *     4  6  8 16 19  4
			 *     9 15  3 18 27 15
			 *    14  9  3  7 17  3
			 * Well, "9 15 3 18 27 15" didn't quite get 32 bits diffing for
			 * "differ" defined as + with a one-bit base and a two-bit delta.  I
			 * used http://burtleburtle.net/bob/hash/avalanche.html to choose
			 * the operations, constants, and arrangements of the variables.
			 *
			 * This does not achieve avalanche.  There are input bits of (a,b,c)
			 * that fail to affect some output bits of (a,b,c), especially of a.
			 * The most thoroughly mixed value is c, but it doesn't really even
			 * achieve avalanche in c.
			 *
			 * This allows some parallelism.  Read-after-writes are good at doubling
			 * the number of bits affected, so the goal of mixing pulls in the
			 * opposite direction as the goal of parallelism.  I did what I could.
			 * Rotates seem to cost as much as shifts on every machine I could lay
			 * my hands on, and rotates are much kinder to the top and bottom bits,
			 * so I used rotates.
			 */
			a -= c;
			a ^= rotateLeft(c, 4);
			c += b;

			b -= a;
			b ^= rotateLeft(a, 6);
			a += c;

			c -= b;
			c ^= rotateLeft(b, 8);
			b += a;

			a -= c;
			a ^= rotateLeft(c, 16);
			c += b;

			b -= a;
			b ^= rotateLeft(a, 19);
			a += c;

			c -= b;
			c ^= rotateLeft(b, 4);
			b += a;
		}

		// last block: affect all 32 bits of (c)
		// Intentional fall-through
		switch (i) {
			case 12:
				c += toUnsignedInt(key[offset + 11]) << 24;
			case 11:
				c += toUnsignedInt(key[offset + 10]) << 16;
			case 10:
				c += toUnsignedInt(key[offset + 9]) << 8;
			case 9:
				c += toUnsignedInt(key[offset + 8]);
			case 8:
				b += toUnsignedInt(key[offset + 7]) << 24;
			case 7:
				b += toUnsignedInt(key[offset + 6]) << 16;
			case 6:
				b += toUnsignedInt(key[offset + 5]) << 8;
			case 5:
				b += toUnsignedInt(key[offset + 4]);
			case 4:
				a += toUnsignedInt(key[offset + 3]) << 24;
			case 3:
				a += toUnsignedInt(key[offset + 2]) << 16;
			case 2:
				a += toUnsignedInt(key[offset + 1]) << 8;
			case 1:
				a += toUnsignedInt(key[offset]);
				break;
			case 0:
				return c;
			default:
				throw new AssertionError("invalid i value i=" + i);
		}

		return finalMix(a, b, c);
	}

	/*
	 * Final mixing of 3 32-bit values (a,b,c) into c
	 *
	 * Pairs of (a,b,c) values differing in only a few bits will usually
	 * produce values of c that look totally different.  This was tested for
	 * - pairs that differed by one bit, by two bits, in any combination
	 *   of top bits of (a,b,c), or in any combination of bottom bits of
	 *   (a,b,c).
	 *
	 * - "differ" is defined as +, -, ^, or ~^.  For + and -, I transformed
	 *   the output delta to a Gray code (a^(a>>1)) so a string of 1's (as
	 *   is commonly produced by subtraction) look like a single 1-bit
	 *   difference.
	 *
	 * - the base values were pseudorandom, all zero but one bit set, or
	 *   all zero plus a counter that starts at zero.
	 *
	 * These constants passed:
	 *   14 11 25 16 4 14 24
	 *   12 14 25 16 4 14 24
	 * and these came close:
	 *    4  8 15 26 3 22 24
	 *   10  8 15 26 3 22 24
	 *   11  8 15 26 3 22 24
	 */
	private static int finalMix(int a, int b, int c) {
		c ^= b;
		c -= rotateLeft(b, 14);
		a ^= c;
		a -= rotateLeft(c, 11);
		b ^= a;
		b -= rotateLeft(a, 25);
		c ^= b;
		c -= rotateLeft(b, 16);
		a ^= c;
		a -= rotateLeft(c, 4);
		b ^= a;
		b -= rotateLeft(a, 14);
		c ^= b;
		c -= rotateLeft(b, 24);

		return c;
	}

}
