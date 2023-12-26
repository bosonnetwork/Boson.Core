/*
 * Copyright (c) 2022 - 2023 trinity-tech.io
 * Copyright (c) 2023 -	  bosonnetwork.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.bosonnetwork.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

/**
 * Thread local objects factory.
 */
public class ThreadLocals {
	private static ThreadLocal<MessageDigest> localSha256 = ThreadLocal.withInitial(() -> {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("expected SHA-256 digest to be available", e);
		}
	});

	private static ThreadLocal<CBORFactory> localCBORFactory = ThreadLocal.withInitial(() -> {
		CBORFactory factory = new CBORFactory();
		factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
		factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
		return factory;
	});

	private static ThreadLocal<ObjectMapper> localObjectMapper = ThreadLocal.withInitial(() -> {
		return new ObjectMapper();
	});

	/**
	 * Returns the current thread's {@code ThreadLocalRandom} object.
	 * Methods of this object should be called only by the current thread,
	 * not by other threads.
	 *
	 * @return the current thread's {@code ThreadLocalRandom}
	 */
	public static ThreadLocalRandom random() {
		return ThreadLocalRandom.current();
	}

	/**
	 * Returns the current thread's SHA256 {@code MessageDigest} object.
	 * Methods of this object should be called only by the current thread,
	 * not by other threads.
	 *
	 * @return the current thread's SHA256 {@code MessageDigest}
	 */
	public static MessageDigest sha256() {
		return localSha256.get();
	}

	/**
	 * Returns the current thread's Jackson {@code CBORFactory} object.
	 * Methods of this object should be called only by the current thread,
	 * not by other threads.
	 *
	 * @return the current thread's Jackson {@code CBORFactory}
	 */
	public static CBORFactory CBORFactory() {
		return localCBORFactory.get();
	}

	/**
	 * Returns the current thread's Jackson {@code ObjectMapper} object.
	 * Methods of this object should be called only by the current thread,
	 * not by other threads.
	 *
	 * @return the current thread's Jackson {@code ObjectMapper}
	 */
	public static ObjectMapper ObjectMapper() {
		return localObjectMapper.get();
	}
}
