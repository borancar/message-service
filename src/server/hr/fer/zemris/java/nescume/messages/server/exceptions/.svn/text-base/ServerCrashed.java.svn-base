/**
 * 
 */
package hr.fer.zemris.java.nescume.messages.server.exceptions;

/**
 * Iznimka koja se baca u slučaju da se server sruši (završi svoj rad neočekivano).
 */
public class ServerCrashed extends ServerException {

	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor iznimke.
	 */
	public ServerCrashed() {

	}

	/**
	 * Konstruktor iznimke koji prima razlog zašto se server srušio.
	 * @param reason razlog
	 */
	public ServerCrashed(String reason) {
		super(reason);
	}

	/**
	 * Konstruktor iznimke koji prima razlog i uzrok zašto se server srušio.
	 * @param reason razlog
	 * @param cause uzrok
	 */
	public ServerCrashed(String reason, Throwable cause) {
		super(reason, cause);
	}
}
