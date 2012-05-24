package hr.fer.zemris.java.nescume.messages.client.exceptions;

import hr.fer.zemris.java.nescume.messages.Address;

/**
 * Iznimka u slučaju da poruka nije stigla do servera. Poruka je mogla biti prekinuta
 * bilo gdje u komunikacijskom kanalu.
 */
public class MessageNotSent extends ClientException {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor kojemu se predaje razlog i uzrok zašto nije moguće poslati poruku.
	 * @param client adresa klijenta koji je izazvao iznimku
	 * @param reason razlog
	 * @param cause uzrok
	 */
	public MessageNotSent(Address client, String reason, Throwable cause) {
		super(client, reason, cause);
	}

	/**
	 * Konstruktor kojemu se predaje razlog zašto nije moguće poslati poruku.
	 * @param client adresa klijenta koji je izazvao iznimku
	 * @param reason razlog
	 */
	public MessageNotSent(Address client, String reason) {
		super(client, reason);
	}

	/**
	 * Konstruktor općeg oblika iznimke.
	 * @param client adresa klijenta koji je izazvao iznimku
	 */
	public MessageNotSent(Address client) {
		super(client);
	}
}
