/**
 * 
 */
package hr.fer.zemris.java.nescume.messages.server.exceptions;

/**
 * Iznimka koja se baca u slučaju da se server ne može pokrenuti.
 */
public class ServerCannotStart extends ServerException {

	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor iznimke.
	 */
	public ServerCannotStart() {

	}

	/**
	 * Konstruktor iznimke koji prima razlog zašto se server ne može pokrenuti.
	 * @param reason razlog
	 */
	public ServerCannotStart(String reason) {
		super(reason);
	}

	/**
	 * Konstruktor iznimke koji prima razlog i uzrok zašto se server ne može pokrenuti.
	 * @param reason razlog
	 * @param cause uzrok
	 */
	public ServerCannotStart(String reason, Throwable cause) {
		super(reason, cause);
	}
}
