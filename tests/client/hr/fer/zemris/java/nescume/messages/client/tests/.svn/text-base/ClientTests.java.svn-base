package hr.fer.zemris.java.nescume.messages.client.tests;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.JUnit4TestAdapter;
import hr.fer.zemris.java.nescume.messages.Address;
import hr.fer.zemris.java.nescume.messages.Message;
import hr.fer.zemris.java.nescume.messages.Message.MessageType;
import hr.fer.zemris.java.nescume.messages.client.ClientMsgService;
import hr.fer.zemris.java.nescume.messages.client.IClientMsgService;
import hr.fer.zemris.java.nescume.messages.client.exceptions.ClientCannotStart;
import hr.fer.zemris.java.nescume.messages.client.exceptions.ClientCrashed;
import hr.fer.zemris.java.nescume.messages.client.exceptions.ClientException;
import hr.fer.zemris.java.nescume.messages.client.exceptions.MessageNotSent;
import hr.fer.zemris.java.nescume.messages.client.exceptions.UnableToRegister;
import hr.fer.zemris.java.nescume.messages.server.IMessageServer;
import hr.fer.zemris.java.nescume.messages.server.SocketMessageServer;
import hr.fer.zemris.java.nescume.messages.server.exceptions.ServerException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Klasa unit testova klijenta.
 */
public class ClientTests {

	/**
	 * Mutex koji služi za sinkronizaciju.
	 */
	private static final Object mutex = new Object();

	/**
	 * Zastavica koja kaže da se server pokrenuo (ako true). Služi za
	 * započinjanje testiranja.
	 */
	private static volatile boolean serverStarted = false;

	/**
	 * Server koji služi za testiranje klijenta.
	 */
	private static IMessageServer server;

	/**
	 * Klijenti koji se testiraju.
	 */
	private List<IClientMsgService> clients = new LinkedList<IClientMsgService>();

	/**
	 * Inicijalizira i pokreće jedan server za sve testove.
	 */
	@BeforeClass
	public static void startServer() {
		new Thread(new Runnable() {

			public void run() {
				Properties properties = new Properties();

				serverStarted = false;

				try {
					properties.load(new FileReader(
							"configuration/server.properties"));
				} catch (IOException e) {
					e.printStackTrace();
				}

				server = new SocketMessageServer(properties);

				try {
					synchronized (mutex) {
						serverStarted = true;

						mutex.notifyAll();
					}

					server.start();
				} catch (ServerException e) {
					e.printStackTrace();
				}
			}

		}).start();

		try {
			synchronized (mutex) {
				while (!serverStarted) {
					mutex.wait();
				}
			}
		} catch (InterruptedException ignorable) {
		}
	}

	/**
	 * Inicijalizira i pokreće klijente za svaki test zasebno.
	 */
	@Before
	public void startClients() throws IOException, ClientException {
		System.out.println("----------");
		Properties properties = new Properties();

		properties.load(new FileReader("configuration/client.properties"));

		clients.add(new ClientMsgService(properties));
		clients.add(new ClientMsgService(properties));

		for (IClientMsgService client : clients) {
			client.start();
		}
	}

	/**
	 * Metoda šalje hitnu poruku klijenta klijentu i zatim uspoređuje da li su poruke
	 * jednake. Slanje poruke ide preko servera.
	 * 
	 * @throws ClientCrashed
	 * @throws UnableToRegister
	 */
	@Test(timeout = 5000)
	public void sendUrgentTest() throws ClientException, IOException {
		Address sender = clients.get(0).register();
		Address receiver = clients.get(1).register();

		Message greeting = new Message(receiver, MessageType.LETTER, "Pozdrav!"
				.getBytes());

		greeting.setUrgent(true);
		
		clients.get(0).send(greeting);
		greeting.setSource(sender);

		Message received = clients.get(1).receive();

		Assert.assertEquals("Primljena poruka nije jednaka poslanoj!",
				greeting, received);
	}

	/**
	 * Testiranje stvaranja klijenta.<br />
	 * Test kontrole parametara 1. Zadana je neispravna adresa servera.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClientCannotStart
	 */
	@Test(expected = IllegalArgumentException.class)
	public void parameterTest1() throws FileNotFoundException, IOException,
			ClientCannotStart {
		Properties properties = new Properties();

		properties.load(new FileReader(
				"tests/test-accessories/client.properties-test1"));

		@SuppressWarnings("unused")
		IClientMsgService client = new ClientMsgService(properties);
	}

	/**
	 * Testiranje stvaranja klijenta.<br />
	 * Test kontrole parametara 2. Zadan je neispravan port servera.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClientCannotStart
	 */
	@Test(expected = IllegalArgumentException.class)
	public void parameterTest2() throws FileNotFoundException, IOException,
			ClientCannotStart {
		Properties properties = new Properties();

		properties.load(new FileReader(
				"tests/test-accessories/client.properties-test2"));

		@SuppressWarnings("unused")
		IClientMsgService client = new ClientMsgService(properties);
	}

	/**
	 * Testiranje stvaranja klijenta.<br />
	 * Test kontrole parametara 3. Zadan je negativan buffer size.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClientCannotStart
	 */
	@Test(expected = IllegalArgumentException.class)
	public void parameterTest3() throws FileNotFoundException, IOException,
			ClientCannotStart {
		Properties properties = new Properties();

		properties.load(new FileReader(
				"tests/test-accessories/client.properties-test3"));

		@SuppressWarnings("unused")
		IClientMsgService client = new ClientMsgService(properties);
	}

	/**
	 * Testiranje stvaranja klijenta.<br />
	 * Test kontrole parametara 4. Nije zadan server IP.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClientCannotStart
	 */
	@Test(expected = IllegalArgumentException.class)
	public void parameterTest4() throws FileNotFoundException, IOException,
			ClientCannotStart {
		Properties properties = new Properties();

		properties.load(new FileReader(
				"tests/test-accessories/client.properties-test4"));

		@SuppressWarnings("unused")
		IClientMsgService client = new ClientMsgService(properties);
	}

	/**
	 * Testiranje stvaranja klijenta.<br />
	 * Test kontrole parametara 5. Nije zadan buffer size.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClientCannotStart
	 */
	@Test(expected = IllegalArgumentException.class)
	public void parameterTest5() throws FileNotFoundException, IOException,
			ClientCannotStart {
		Properties properties = new Properties();

		properties.load(new FileReader(
				"tests/test-accessories/client.properties-test5"));

		@SuppressWarnings("unused")
		IClientMsgService client = new ClientMsgService(properties);
	}

	/**
	 * Metoda popuni buffer poruka jednog klijenta, te provjerava je li drugi
	 * klijent primio poruku. Slanje poruke ide preko servera.
	 * 
	 * @throws ClientCrashed
	 * @throws UnableToRegister
	 */
	@Test(timeout = 10000)
	public void sendBufferTest() throws ClientException, IOException {
		Address sender = clients.get(0).register();
		Address receiver = clients.get(1).register();

		Properties properties = new Properties();
		properties.load(new FileReader("configuration/client.properties"));
		int bufferSize = Integer
				.parseInt(properties.getProperty("buffer.size"));
		Message greeting = new Message(receiver, MessageType.LETTER, "Pozdrav"
				.getBytes());
		greeting.setSource(sender);

		for (int i = 1; i <= bufferSize; i++) {
			clients.get(0).send(greeting);
		}

		for (int i = 1; i <= bufferSize; i++) {
			Message received = clients.get(1).receive();
			Assert.assertEquals("Primljena poruka nije jednaka poslanoj!",
					greeting, received);
		}
	}

	/**
	 * Timer test 1.<br />
	 * Šalje se jedna (ne hitna) poruka (klijent-klijent), i čeka istek vremena.
	 * Zatim se provjerava je li poruka stigla drugom klijentu.<br />
	 * NAPOMENA: BufferTimeout mora biti manji od 5000!
	 * 
	 * @throws ClientCrashed
	 * @throws UnableToRegister
	 * @throws MessageNotSent
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@Test(timeout = 10000)
	public void timerTest1() throws UnableToRegister, ClientCrashed,
			MessageNotSent, FileNotFoundException, IOException {
		
		class Broj {
			private int broj;
			
			public Broj() {
				this.broj = 0;
			}
			
			public void inc(int x) {
				this.broj += x;
			}
			
			public int getBroj() {
				return this.broj;
			}
		}
		final Broj broj = new Broj();
		Timer timeMeter = new Timer(true);
		timeMeter.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				broj.inc(500);
				System.out.println("--> Vrijeme: " + broj.getBroj());
			}
		
		}, 0, 500);
		
		System.out.println(">> timer test 1");

		Properties properties = new Properties();
		properties.load(new FileReader("configuration/client.properties"));
		int bufferTimeout = Integer.parseInt(properties
				.getProperty("buffer.timeout"));

		Address sender = clients.get(0).register();
		Address receiver = clients.get(1).register();

		final Message greeting = new Message(receiver, MessageType.LETTER,
				"Pozdrav!".getBytes());
		greeting.setSource(sender);
		clients.get(0).send(greeting);
		
		class BooleanFlag {
			private boolean flag;

			public BooleanFlag(boolean flag) {
				this.flag = flag;
			}

			public boolean getFlag() {
				return this.flag;
			}

			public void setFlag(boolean flag) {
				this.flag = flag;
			}
		}
		
		final BooleanFlag istekVremena = new BooleanFlag(true);
		Timer timer = new Timer(false);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				istekVremena.setFlag(false);
			}

		}, bufferTimeout + 20);
		while (istekVremena.getFlag()) {
			
		}

		Message received = clients.get(1).receive();
		Assert.assertEquals("Primljena poruka nije jednaka poslanoj!",
				greeting, received);
	}
	
	/**
	 * Timer test 2.<br />
	 * Šalje se jedna (ne hitna) poruka (klijent-klijent), i čeka istek polu vremena buffer timeout-a.
	 * Dodaje se nova poruka u buffer i čeka istek vremena buffer timeout-a.
	 * Zatim se provjerava jesu li poruke stigle drugom klijentu.<br />
	 * NAPOMENA: BufferTimeout mora biti manji od 5000!
	 */
	@Test(timeout = 10000)
	public void timerTest2() throws UnableToRegister, ClientCrashed,
			MessageNotSent, FileNotFoundException, IOException {
		
		class Broj {
			private int broj;
			
			public Broj() {
				this.broj = 0;
			}
			
			public void inc(int x) {
				this.broj += x;
			}
			
			public int getBroj() {
				return this.broj;
			}
		}
		final Broj broj = new Broj();
		Timer timeMeter = new Timer(true);
		timeMeter.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				broj.inc(500);
				System.out.println("--> Vrijeme: " + broj.getBroj());
			}
		
		}, 0, 500);
		
		System.out.println(">> timer test 2");

		Properties properties = new Properties();
		properties.load(new FileReader("configuration/client.properties"));
		int bufferTimeout = Integer.parseInt(properties
				.getProperty("buffer.timeout"));

		Address sender = clients.get(0).register();
		Address receiver = clients.get(1).register();

		final Message greeting = new Message(receiver, MessageType.LETTER,
				"Pozdrav!".getBytes());
		final Message greeting2 = new Message(receiver, MessageType.LETTER,
				"Pozdrav2!".getBytes());
		greeting.setSource(sender);
		greeting2.setSource(sender);
		
		clients.get(0).send(greeting);
		
		class BooleanFlag {
			private boolean flag;

			public BooleanFlag(boolean flag) {
				this.flag = flag;
			}

			public boolean getFlag() {
				return this.flag;
			}

			public void setFlag(boolean flag) {
				this.flag = flag;
			}
		}
		
		final BooleanFlag istekVremena = new BooleanFlag(true);
		Timer timer = new Timer(false);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				istekVremena.setFlag(false);
			}

		}, bufferTimeout/2);
		while (istekVremena.getFlag()) {
			
		}
		
		clients.get(0).send(greeting2);
		
		istekVremena.setFlag(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				istekVremena.setFlag(false);
			}

		}, bufferTimeout + 20);
		while (istekVremena.getFlag()) {
			
		}
		
		Message received = clients.get(1).receive();
		Message received2 = clients.get(1).receive();
		Assert.assertEquals("Primljena poruka nije jednaka poslanoj!",
				greeting, received);
		Assert.assertEquals("Primljena poruka nije jednaka poslanoj!",
				greeting2, received2);
	}
	
	/**
	 * Gasi klijente.
	 */
	@After
	public void stopClients() {
		for (IClientMsgService client : clients) {
			client.stop();
		}

		clients.clear();
	}

	/**
	 * Gasi server.
	 */
	@AfterClass
	public static void stopServer() throws InterruptedException {
		server.stop();
		Thread.sleep(2000);
	}

	/**
	 * Metoda potrebna za pozivanje testa iz ANTa.
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ClientTests.class);
	}
}
