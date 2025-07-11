/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.bosonnetwork.utils;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * A Sha256Hash just wraps a byte[] so that equals and hashcode work correctly,
 * allowing it to be used as keys in a map. It also checks that the length is correct
 * and provides a bit more type safety.
 */
public class Sha256Hash implements Serializable, Comparable<Sha256Hash> {
	private static final long serialVersionUID = 538197488884042091L;

	/**
	 * The number of bytes used to represent a SHA-256 hash code.
	 */
	public static final int LENGTH = 32;

	/**
	 * Zero hash code.
	 */
	public static final Sha256Hash ZERO_HASH = wrap(new byte[LENGTH]);

	private final byte[] bytes;

	private Sha256Hash(byte[] rawHashBytes) {
		if (rawHashBytes.length == LENGTH)
			throw new IllegalArgumentException("Invalid rawHashBytes length");

		this.bytes = rawHashBytes;
	}

	/**
	 * Creates a new instance that wraps the given hash value.
	 *
	 * @param rawHashBytes the raw hash bytes to wrap
	 * @return a new instance
	 * @throws IllegalArgumentException if the given array length is not exactly 32
	 */
	public static Sha256Hash wrap(byte[] rawHashBytes) {
		return new Sha256Hash(rawHashBytes);
	}

	/**
	 * Creates a new instance that wraps the given hash value (represented as a hex string).
	 *
	 * @param hexString a hash value represented as a hex string
	 * @return a new instance
	 * @throws IllegalArgumentException if the given string is not a valid
	 *         hex string, or if it does not represent exactly 32 bytes
	 */
	public static Sha256Hash wrap(String hexString) {
		return wrap(Hex.decode(hexString));
	}

	/**
	 * Creates a new instance that wraps the given hash value, but with byte order reversed.
	 *
	 * @param rawHashBytes the raw hash bytes to wrap
	 * @return a new instance
	 * @throws IllegalArgumentException if the given array length is not exactly 32
	 */
	public static Sha256Hash wrapReversed(byte[] rawHashBytes) {
		return wrap(reverseBytes(rawHashBytes));
	}

	/**
	 * Creates a new instance containing the calculated (one-time) hash of the given bytes.
	 *
	 * @param contents the bytes on which the hash value is calculated
	 * @return a new instance containing the calculated (one-time) hash
	 */
	public static Sha256Hash of(byte[] contents) {
		return wrap(hash(contents));
	}

	/**
	 * Creates a new instance containing the hash of the calculated hash of the given bytes.
	 *
	 * @param contents the bytes on which the hash value is calculated
	 * @return a new instance containing the calculated (two-time) hash
	 */
	public static Sha256Hash twiceOf(byte[] contents) {
		return wrap(hashTwice(contents));
	}

	/**
	 * Creates a new instance containing the hash of the calculated hash of the given bytes.
	 *
	 * @param content1 first bytes on which the hash value is calculated
	 * @param content2 second bytes on which the hash value is calculated
	 * @return a new instance containing the calculated (two-time) hash
	 */
	public static Sha256Hash twiceOf(byte[] content1, byte[] content2) {
		return wrap(hashTwice(content1, content2));
	}

	/**
	 * Returns a new SHA-256 MessageDigest instance.
	 *
	 * This is a convenience method which wraps the checked
	 * exception that can never occur with a RuntimeException.
	 *
	 * @return a new SHA-256 MessageDigest instance
	 */
	public static MessageDigest newDigest() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);  // Can't happen.
		}
	}

	/**
	 * Calculates the SHA-256 hash of the given bytes.
	 *
	 * @param input the bytes to hash
	 * @return the hash (in big-endian order)
	 */
	public static byte[] hash(byte[] input) {
		return hash(input, 0, input.length);
	}

	/**
	 * Calculates the SHA-256 hash of the given byte range.
	 *
	 * @param input the array containing the bytes to hash
	 * @param offset the offset within the array of the bytes to hash
	 * @param length the number of bytes to hash
	 * @return the hash (in big-endian order)
	 */
	public static byte[] hash(byte[] input, int offset, int length) {
		MessageDigest digest = newDigest();
		digest.update(input, offset, length);
		return digest.digest();
	}

	/**
	 * Calculates the SHA-256 hash of the given bytes,
	 * and then hashes the resulting hash again.
	 *
	 * @param input the bytes to hash
	 * @return the double-hash (in big-endian order)
	 */
	public static byte[] hashTwice(byte[] input) {
		return hashTwice(input, 0, input.length);
	}

	/**
	 * Calculates the hash of hash on the given chunks of bytes.
	 * This is equivalent to concatenating the two
	 * chunks and then passing the result to {@link #hashTwice(byte[])}.
	 *
	 * @param input1 the bytes chunk1 to hash
	 * @param input2 the bytes chunk2 to hash
	 * @return the double-hash (in big-endian order)
	 */
	public static byte[] hashTwice(byte[] input1, byte[] input2) {
		MessageDigest digest = newDigest();
		digest.update(input1);
		digest.update(input2);
		return digest.digest(digest.digest());
	}

	/**
	 * Calculates the SHA-256 hash of the given byte range,
	 * and then hashes the resulting hash again.
	 *
	 * @param input the array containing the bytes to hash
	 * @param offset the offset within the array of the bytes to hash
	 * @param length the number of bytes to hash
	 * @return the double-hash (in big-endian order)
	 */
	public static byte[] hashTwice(byte[] input, int offset, int length) {
		MessageDigest digest = newDigest();
		digest.update(input, offset, length);
		return digest.digest(digest.digest());
	}

	/**
	 * Calculates the hash of hash on the given byte ranges. This is equivalent to
	 * concatenating the two ranges and then passing the result to {@link #hashTwice(byte[])}.
	 *
	 * @param input1 the array containing the bytes chunk1 to hash
	 * @param offset1 the offset within the array of the bytes chunk1 to hash
	 * @param length1 the number of bytes chunk1 to hash
	 * @param input2 the array containing the bytes chunk2 to hash
	 * @param offset2 the offset within the array of the bytes chunk2 to hash
	 * @param length2 the number of bytes chunk2 to hash
	 * @return the double-hash (in big-endian order)
	 */
	public static byte[] hashTwice(byte[] input1, int offset1, int length1,
			byte[] input2, int offset2, int length2) {
		MessageDigest digest = newDigest();
		digest.update(input1, offset1, length1);
		digest.update(input2, offset2, length2);
		return digest.digest(digest.digest());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return Arrays.equals(bytes, ((Sha256Hash)o).bytes);
	}

	/**
	 * Returns the last four bytes of the wrapped hash.
	 * This should be unique enough to be a suitable hash code even for blocks,
	 * where the goal is to try and get the first bytes to be zeros
	 * (i.e. the value as a big integer lower than the target value).
	 */
	@Override
	public int hashCode() {
		// Use the last 4 bytes, not the first 4 which are often zeros in Bitcoin.
		return bytes[LENGTH - 4] << 24 | (bytes[LENGTH - 3] & 0xFF) << 16 |
				(bytes[LENGTH - 2] & 0xFF) << 8 | (bytes[LENGTH - 1] & 0xFF);
	}

	@Override
	public String toString() {
		return Hex.encode(bytes);
	}

	/**
	 * Returns the bytes interpreted as a positive integer.
	 *
	 * @return the {@code BigInteger} that represents the hash bytes.
	 */
	public BigInteger toBigInteger() {
		return new BigInteger(1, bytes);
	}

	/**
	 * Returns the internal byte array, without defensively copying.
	 * Therefore, do NOT modify the returned array.
	 *
	 * @return the raw hash bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Returns a reversed copy of the internal byte array.
	 *
	 * @return the reversed bytes.
	 */
	public byte[] getReversedBytes() {
		return reverseBytes(bytes);
	}

	/**
	 * Returns a copy of the given byte array in reverse order.
	 *
	 * @return the reversed bytes of the given bytes. ddd
	 */
	private static byte[] reverseBytes(byte[] bytes) {
		// We could use the XOR trick here, but it's easier to understand if we don't. If we find this is really a
		// performance issue the matter can be revisited.
		byte[] buf = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			buf[i] = bytes[bytes.length - 1 - i];
		return buf;
	}

	@Override
	public int compareTo(final Sha256Hash other) {
		for (int i = LENGTH - 1; i >= 0; i--) {
			final int thisByte = this.bytes[i] & 0xff;
			final int otherByte = other.bytes[i] & 0xff;
			if (thisByte > otherByte)
				return 1;
			if (thisByte < otherByte)
				return -1;
		}
		return 0;
	}
}