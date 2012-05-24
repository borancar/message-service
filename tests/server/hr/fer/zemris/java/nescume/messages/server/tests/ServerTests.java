package hr.fer.zemris.java.nescume.messages.server.tests;

import hr.fer.zemris.java.nescume.messages.Address;
import hr.fer.zemris.java.nescume.messages.Message;
import hr.fer.zemris.java.nescume.messages.QueryMessage;
import hr.fer.zemris.java.nescume.messages.Message.MessageType;
import hr.fer.zemris.java.nescume.messages.client.ClientMsgService;
import hr.fer.zemris.java.nescume.messages.client.IClientMsgService;
import hr.fer.zemris.java.nescume.messages.client.exceptions.ClientCrashed;
import hr.fer.zemris.java.nescume.messages.client.exceptions.ClientException;
import hr.fer.zemris.java.nescume.messages.client.exceptions.MessageNotSent;
import hr.fer.zemris.java.nescume.messages.server.IMessageServer;
import hr.fer.zemris.java.nescume.messages.server.SocketMessageServer;
import hr.fer.zemris.java.nescume.messages.server.exceptions.ServerCannotStart;
import hr.fer.zemris.java.nescume.messages.server.exceptions.ServerCrashed;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

/**
 * Klasa unit testova servera.
 */
public class ServerTests {

	/**
	 * Server.
	 */
	private static IMessageServer server;
	
	/**
	 * Lista klijenata.
	 */
	private static List<IClientMsgService> clients = new ArrayList<IClientMsgService>();
	
	/**
	 * Mapiranje klijenata na njihove adrese, tako da se zna tko što prima i kada.
	 */
	private static Map<IClientMsgService, Address> addresses = new HashMap<IClientMsgService, Address>();

	/**
	 * Zastavica koja javlja da li je server pokrenut (true) ili ne (false).
	 */
	private static boolean serverStarted = false;
	
	/**
	 * Broj klijenata kojima se testira server.
	 */
	private static final int NUMBER_OF_CLIENTS = 100;
	
	/**
	 * Broj nasumičnih poruka.
	 */
	private static final int NUMBER_OF_MESSAGES = 100;
	
	/**
	 * Metoda testira predavanje neispravnih parametara konstruktoru servera.
	 * @throws IOException u slučaju greške pri čitanju iz konfiguracijske datoteke
	 */
	@Test(expected = IllegalArgumentException.class)
	public void illegalParametersTest() throws IOException {
		Properties properties = new Properties();
				properties.load(new FileReader("configuration/server.properties"));
		
		properties.setProperty("packet.size", "-1");
		
		new SocketMessageServer(properties);
	}
	
	/**
	 * Metoda pokreće servera i klijente koji služe za testiranje.
	 * @throws IOException u slučaju greške pri čitanju
	 */
	@BeforeClass
	public static void startServerAndClients() throws IOException, ClientException {
		Properties serverProperties = new Properties();
		Properties clientProperties = new Properties();
		
		serverProperties.load(new FileReader("configuration/server.properties"));
		clientProperties.load(new FileReader("configuration/client.properties"));
		
		server = new SocketMessageServer(serverProperties);
		
		final Object mutex = new Object();
		
		new Thread(new Runnable() {
		
			public void run() {
				try {
					synchronized(mutex) {
						serverStarted = true;
						mutex.notifyAll();
					}
					
					server.start();
				} catch (ServerCannotStart e) {
					e.printStackTrace();
				} catch (ServerCrashed e) {
					e.printStackTrace();
				}
			}
		
		}).start();
		
		synchronized(mutex) {
			try {
				while(!serverStarted) {
					mutex.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// 1 extra klijent, ako zatreba
		for (int i = 0; i <= NUMBER_OF_CLIENTS; i++) {
			IClientMsgService client = new ClientMsgService(clientProperties);
			
			client.start();
			
			addresses.put(client, client.register());
			
			clients.add(client);
		}
	}

	/**
	 * Metoda slučajnim odabirom šalje poruke od jednog klijenta do drugog. Samo jedan klijent
	 * šalje u nekom trenutku. Metoda testira ispravno dostavljanje.
	 */
	@Test
	public void randomSendTest() throws ClientException {
		Random random = new Random();
		
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			IClientMsgService sender = clients.get(random.nextInt(10));
			IClientMsgService receiver = clients.get(random.nextInt(10));
			
			Message sent = new Message(addresses.get(receiver), MessageType.LETTER, "Pozdrav!".getBytes());

			sent.setUrgent(random.nextInt(100) > 50 ? true : false);
			
			sender.send(sent);
		
			Message received = receiver.receive();
			
			Assert.assertEquals("Primljena poruka nije jednaka poslanoj!", sent, received);
		}
	}
	
	/**
	 * Predstavlja neovisnog klijenta koji može slati poruku. Služi za instanciranje
	 * klijenata koji u isto vrijeme šalju poruke na server.
	 */
	public class IndependentClient implements Runnable {
		
		/**
		 * Klijent koji šalje poruku.
		 */
		private IClientMsgService client;
		
		/**
		 * Poruka koja se šalje.
		 */
		private Message message;
		
		/**
		 * Konstruktor koji prima klijenta koji šalje i poruku koju treba slati.
		 * @param client klijent
		 * @param message poruka
		 */
		public IndependentClient(IClientMsgService client, Message message) {
			this.client = client;
			this.message = message;
		}
		
		public void run() {		
			try {
				this.client.send(message);
			} catch (MessageNotSent e) {
				e.printStackTrace();
			} catch (ClientCrashed e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Metoda služi za stvoriti nezavisne klijente koji svi šalju jednom zajedničkom.
	 */
	private void sendOutManyClientsToOne() {
		for (int i = 1; i < NUMBER_OF_CLIENTS; i++) {
			new Thread(new IndependentClient(
					clients.get(i),
					new Message(addresses.get(clients.get(0)),
					MessageType.LETTER,
					addresses.get(clients.get(i)).getGID()
				))).start();
		}		
	}
	
	/**
	 * Metoda stvara nezavisne klijente koji u isto vrijeme svi šalju jednom klijentu.
	 * Svaki klijent stavi svoju adresu u podatke. Tako se može provjeriti da li je poruka
	 * stigla od pravog klijenta.
	 * @throws ClientException u slučaju greške u klijentu
	 */
	@Test
	public void manyToOneTest() throws ClientException {		
		sendOutManyClientsToOne();
		
		for (int i = 1; i < NUMBER_OF_CLIENTS; i++) {
			Message received = clients.get(0).receive();
			
			Assert.assertEquals("Tip poruke nije isti!", MessageType.LETTER, received.getType());
			//		TODO: NE RADI U ANT-u, treba ručnu ispitivati...
			//			Assert.assertArrayEquals("Podaci poruke nisu isti!", received.getSource().getGID(), received.getData());
		}
	}
	
	@Test
	public void manyToOneQueryTest() throws ClientException {		
		QueryMessage query = new QueryMessage(MessageType.LETTER);
		query.setUrgent(true);
		
		clients.get(100).send(query);

		sendOutManyClientsToOne();
			
		clients.get(100).receive();
	}
	
	/**
	 * Zaustavlja servera i klijente i čisti za njima.
	 */
	@AfterClass
	public static void stopServerAndClients() throws InterruptedException {
		for(IClientMsgService client : clients) {
			client.stop();
		}
		
		clients.clear();
		
		addresses.clear();
		
		server.stop();
		
		Thread.sleep(2000);

		serverStarted = false;
	}
	
	/**
	 * Metoda potrebna za pozivanje testova iz ANT-a.
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ServerTests.class);
	}
}
