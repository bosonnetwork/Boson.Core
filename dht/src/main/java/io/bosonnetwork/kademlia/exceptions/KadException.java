/*
 * Copyright (c) 2022 - 2023 trinity-tech.io
 * Copyright (c) 2023 -      bosonnetwork.io
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

package io.bosonnetwork.kademlia.exceptions;

import io.bosonnetwork.BosonException;

/**
 * Signals that a Kademlia exception of some sort has occurred.
 * This class is the general class of exceptions produced by the Kademlia implementation.
 */
public class KadException extends BosonException {
	private static final long serialVersionUID = -2702047825432607326L;
	/**
	 * The error code that related with this exception.
	 */
	private final int code;

	/**
	 * Constructs a new Kad exception with {@code null} as its detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 *
	 * @param code the error code.
	 */
	public KadException(int code) {
		super();
		this.code = code;
	}

	/**
	 * Constructs a new Kad exception with the specified detail message.  The
	 * cause is not initialized, and may subsequently be initialized by
	 * a call to {@link #initCause}.
	 *
	 * @param   code      the error code.
	 * @param   message   the detail message. The detail message is saved for
	 *		  later retrieval by the {@link #getMessage()} method.
	 */
	public KadException(int code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * Constructs a new Kad exception with the specified detail message and
	 * cause.  <p>Note that the detail message associated with
	 * {@code cause} is <i>not</i> automatically incorporated in
	 * this exception's detail message.
	 *
	 * @param   code the error code.
	 * @param  message the detail message (which is saved for later retrieval
	 *		 by the {@link #getMessage()} method).
	 * @param  cause the cause (which is saved for later retrieval by the
	 *		 {@link #getCause()} method).  (A {@code null} value is
	 *		 permitted, and indicates that the cause is nonexistent or
	 *		 unknown.)
	 */
	public KadException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	/**
	 * Constructs a new Kad exception with the specified cause and a detail
	 * message of {@code (cause==null ? null : cause.toString())} (which
	 * typically contains the class and detail message of {@code cause}).
	 * This constructor is useful for exceptions that are little more than
	 * wrappers for other throwables (for example, {@link
	 * java.security.PrivilegedActionException}).
	 *
	 * @param  code the error code.
	 * @param  cause the cause (which is saved for later retrieval by the
	 *		 {@link #getCause()} method).  (A {@code null} value is
	 *		 permitted, and indicates that the cause is nonexistent or
	 *		 unknown.)
	 */
	public KadException(int code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	/**
	 * Gets the error code.
	 *
	 * @return the error code.
	 */
	public int getCode() {
		return code;
	}
}
