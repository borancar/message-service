package hr.fer.zemris.java.nescume.messages.client.exceptions;

import hr.fer.zemris.java.nescume.messages.Address;

/**
 * Iznimka u slučaju da klijent nije prihvaćen od strane servera.
 */
public class UnableToRegister extends ClientException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Konstruktor općeg oblika iznimke.
	 * @param client adresa klijenta koji je izazvao iznimku
	 */
	public UnableToRegister(Address client) {
		super(client);
	}
	
	/**
	 * Konstruktor koji prima razlog zašto se klijent ne može registrirati.
	 * @param client adresa klijenta koji je izazvao iznimku
	 * @param reason razlog
	 */
	public UnableToRegister(Address client, String reason) {
		super(client, reason);
	}
	
	/**
	 * Konstruktor koji prima razlog i uzrok zašto se klijent ne može registrirati.
	 * Služi za ulančavanje iznimaka.
	 * @param client adresa klijenta koji je izazvao iznimku
	 * @param reason razlog
	 * @param cause uzrok
	 */
	public UnableToRegister(Address client, String reason, Throwable cause) {
		super(client, reason, cause);
	}
}
