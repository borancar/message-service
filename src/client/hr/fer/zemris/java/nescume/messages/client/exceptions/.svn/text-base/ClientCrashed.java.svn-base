/**
 * 
 */
package hr.fer.zemris.java.nescume.messages.client.exceptions;

import hr.fer.zemris.java.nescume.messages.Address;

/**
 * Iznimka koju klijent baca kada se sruši (završi neočekivano svoj rad).
 */
public class ClientCrashed extends ClientException {

	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor koji prima klijenta koji se srušio.
	 * @param client klijent
	 */
	public ClientCrashed(Address client) {
		super(client);
	}

	/**
	 * Konstruktor koji prima klijenta koji se srušio i razlog zašto se srušio taj klijent.
	 * @param client klijent
	 * @param reason razlog
	 */
	public ClientCrashed(Address client, String reason) {
		super(client, reason);
	}

	/**
	 * Konstruktor koji prima klijenta koji se srušio, te razlog i uzrok rušenja.
	 * @param client klijent
	 * @param reason razlog
	 * @param cause uzrok
	 */
	public ClientCrashed(Address client, String reason, Throwable cause) {
		super(client, reason, cause);
	}
}
