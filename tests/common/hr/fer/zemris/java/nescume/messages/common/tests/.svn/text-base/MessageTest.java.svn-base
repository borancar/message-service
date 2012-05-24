package hr.fer.zemris.java.nescume.messages.common.tests;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import junit.framework.JUnit4TestAdapter;

import hr.fer.zemris.java.nescume.messages.Address;
import hr.fer.zemris.java.nescume.messages.Message;
import hr.fer.zemris.java.nescume.messages.QueryMessage;
import hr.fer.zemris.java.nescume.messages.Message.MessageType;

import org.junit.Assert;
import org.junit.Test;

/**
 * Služi za testiranje klase Message koja predstavlja opću poruku.
 */
public class MessageTest {
	
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
	 * Serijalizira poruku u memoriju, zatim je deserijalizira i gleda da li su poruke iste.
	 * Testira metode toByteArray i fromByteArray.
	 */
	@Test
	public void deserializeSerialized() {
		Random random = new Random();
		
		for (int i = 0; i < NUMBER_OF_TESTS; i++) {
			Address destAddress = new Address(random.nextInt(CLIENT_ID_LIMIT), random.nextInt(NODE_ID_LIMIT));
			Address srcAddress = new Address(random.nextInt(CLIENT_ID_LIMIT), random.nextInt(NODE_ID_LIMIT));
			
			byte[] data = null;
			
			try {
				data = random.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException notPossible) {

			}
			
			Message sent = new Message(destAddress, MessageType.LETTER, data);
			sent.setUrgent(random.nextInt(NUMBER_OF_TESTS) > NUMBER_OF_TESTS / 2 ? true : false);
			sent.setSource(srcAddress);
			
			Message received = Message.fromByteArray(sent.toByteArray());
			Assert.assertEquals("Primljena poruka nije jednaka poslanoj!", sent, received);
		}
	}
	
	/**
	 * Metoda testira serijalizaciju i deserijalizaciju QueryMessage
	 */
	@Test
	public void queryMessageTest() {
		Message sent = new QueryMessage(MessageType.LETTER);
		sent.setSource(new Address(100, 1));
		
		Message received = Message.fromByteArray(sent.toByteArray());
		
		Assert.assertEquals("Primljena poruka nije jednaka poslanoj!", sent, received);
	}
	
	/**
	 * Metoda potrebna za pozivanje testa iz ANTa.
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(MessageTest.class);
    }
}
