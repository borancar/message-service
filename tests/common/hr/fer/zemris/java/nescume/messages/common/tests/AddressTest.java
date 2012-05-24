package hr.fer.zemris.java.nescume.messages.common.tests;

import java.util.Random;

import junit.framework.JUnit4TestAdapter;

import hr.fer.zemris.java.nescume.messages.Address;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testovi klase Address koji služi za spremanje adrese klijenata.
 */
public class AddressTest {
	
	/**
	 * Broj pojedinih testova.
	 */
	private static final int NUMBER_OF_TESTS = 100;
	
	/**
	 * clientID mora biti strogo manji od ovog broja.
	 */
	private static final int CLIENT_ID_LIMIT = 65536;
	
	/**
	 * nodeID mora biti strogo manji od ovog broja.
	 */
	private static final int NODE_ID_LIMIT = 256;

	/**
	 * Test stvara novu adresu, zatim je serijalizira u polje bajtova, zatim to
	 * deserializira u novu adresu i provjerava da li su adrese iste.
	 * Testira metode parseGID i getGID.
	 */
	@Test
	public void deserializeSerialized() {
		Random random = new Random();
		
		for (int i = 0; i < NUMBER_OF_TESTS; i++) {
			Address adresa = new Address(random.nextInt(CLIENT_ID_LIMIT), random.nextInt(NODE_ID_LIMIT));
			Assert.assertEquals("Adrese nisu iste!", adresa, Address.parseGID(adresa.getGID()));
		}		
	}
	
	/**
	 * Test stvara novu adresu, zatim poziva toString pa onda parseGID i uspoređuje da li
	 * su adrese iste. Metoda testira toString() i parseGID() metode.
	 */
	@Test
	public void parseToString() {
		Random random = new Random();
		
		for(int i = 0; i < NUMBER_OF_TESTS; i++) {
			Address adresa = new Address(random.nextInt(CLIENT_ID_LIMIT), random.nextInt(NODE_ID_LIMIT));		
			Assert.assertEquals("Adrese nisu iste!", adresa, Address.parseGID(adresa.toString()));
		}
	}
	
	/**
	 * Metoda potrebna za pozivanje testa iz ANTa.
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(AddressTest.class);
    }
}
