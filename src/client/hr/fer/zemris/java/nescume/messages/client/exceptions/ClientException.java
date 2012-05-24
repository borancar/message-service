package hr.fer.zemris.java.nescume.messages.client.exceptions;

import hr.fer.zemris.java.nescume.messages.Address;

/**
 * Opći oblik iznimke koje se može pojaviti kod pozivanja nekih metoda klijenta.
 */
public class ClientException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Adresa klijenta kod kojeg se dogodila iznimka.
	 */
	protected Address client;

	/**
	 * Konstruktor općeg oblika iznimke.
	 * @param client adresa klijenta kod kojeg se izmika dogodila
	 */
	public ClientException(Address client) {
		super();
		this.client = client;
	}
	
	/**
	 * Konstruktor kojemu se predaje razlog izazivanja iznimke.
	 * @param client adresa klijenta kod kojeg se izmika dogodila
	 * @param reason razlog
	 */
	public ClientException(Address client, String reason) {
		super(reason);
		this.client = client;
	}
	
	/**
	 * Konstruktor kojemu se predaje razlog i uzrok izazivanja iznimke.
	 * Služi za ulančavanje iznimaka.
	 * @param client adresa klijenta kod kojeg se izmika dogodila
	 * @param reason razlog
	 * @param cause uzrok
	 */
	public ClientException(Address client, String reason, Throwable cause) {
		super(reason, cause);
		this.client = client;
	}
	
	@Override
	public String getMessage() {
		return "Klijent " + client + ": " + super.getMessage();
	}
	
	/**
	 * Vraća adresu klijenta koji je izazvao ovu iznimku.
	 * @return adresa klijenta
	 */
	public Address getClient() {
		return this.client;
	}
}
