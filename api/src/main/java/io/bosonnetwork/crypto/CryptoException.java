package io.bosonnetwork.crypto;

import io.bosonnetwork.BosonException;

/**
 * An exception that is thrown when an error occurs using the crypto library.
 */
public class CryptoException extends BosonException {
	private static final long serialVersionUID = 5633767544528399814L;

	/**
	 * Constructs a new crypto exception with {@code null} as its detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 */
	public CryptoException() {
		super();
	}

	/**
	 * Constructs a new crypto exception with the specified detail message.  The
	 * cause is not initialized, and may subsequently be initialized by
	 * a call to {@link #initCause}.
	 *
	 * @param   message   the detail message. The detail message is saved for
	 *		  later retrieval by the {@link #getMessage()} method.
	 */
	public CryptoException(String message) {
		super(message);
	}

	/**
	 * Constructs a new crypto exception with the specified detail message and
	 * cause.  <p>Note that the detail message associated with
	 * {@code cause} is <i>not</i> automatically incorporated in
	 * this exception's detail message.
	 *
	 * @param  message the detail message (which is saved for later retrieval
	 *		 by the {@link #getMessage()} method).
	 * @param  cause the cause (which is saved for later retrieval by the
	 *		 {@link #getCause()} method).  (A {@code null} value is
	 *		 permitted, and indicates that the cause is nonexistent or
	 *		 unknown.)
	 * @since  1.4
	 */
	public CryptoException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new crypto exception with the specified cause and a detail
	 * message of {@code (cause==null ? null : cause.toString())} (which
	 * typically contains the class and detail message of {@code cause}).
	 * This constructor is useful for exceptions that are little more than
	 * wrappers for other throwables (for example, {@link
	 * java.security.PrivilegedActionException}).
	 *
	 * @param  cause the cause (which is saved for later retrieval by the
	 *		 {@link #getCause()} method).  (A {@code null} value is
	 *		 permitted, and indicates that the cause is nonexistent or
	 *		 unknown.)
	 * @since  1.4
	 */
	public CryptoException(Throwable cause) {
		super(cause);
	}
}
