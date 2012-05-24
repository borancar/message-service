package hr.fer.zemris.java.nescume.messages.client.exceptions;

import hr.fer.zemris.java.nescume.messages.Address;

/**
 * Iznimka koja govori da se klijent ne može pokrenuti.
 */
public class ClientCannotStart extends ClientException {

	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor općeg oblika iznimke.
	 * @param client adresa klijenta koji je izazvao iznimku
	 */
	public ClientCannotStart(Address client) {
		super(client);
	}
	
	/**
	 * Konstruktor kojemu se predaje razlog zašto se klijent ne može pokrenuti.
	 * @param client adresa klijenta koji je izazvao iznimku
	 * @param reason razlog
	 */
	public ClientCannotStart(Address client, String reason) {
		super(client, reason);
	}
	
	/**
	 * Konstruktor kojemu se predaje razlog i uzrok zašto se klijent ne može pokrenuti.
	 * Služi za ulančavanje iznimaka.
	 * @param client adresa klijenta koji je izazvao iznimku
	 * @param reason razlog
	 * @param cause uzrok
	 */
	public ClientCannotStart(Address client, String reason, Throwable cause) {
		super(client, reason, cause);
	}
}
