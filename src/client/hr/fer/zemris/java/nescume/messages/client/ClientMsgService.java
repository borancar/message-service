package hr.fer.zemris.java.nescume.messages.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;

import hr.fer.zemris.java.nescume.messages.Address;
import hr.fer.zemris.java.nescume.messages.Message;
import hr.fer.zemris.java.nescume.messages.QueryMessage;
import hr.fer.zemris.java.nescume.messages.RegisterMessage;
import hr.fer.zemris.java.nescume.messages.TimeoutBuffer;
import hr.fer.zemris.java.nescume.messages.Message.MessageType;
import hr.fer.zemris.java.nescume.messages.client.exceptions.ClientCannotStart;
import hr.fer.zemris.java.nescume.messages.client.exceptions.MessageNotSent;
import hr.fer.zemris.java.nescume.messages.client.exceptions.UnableToRegister;

/**
 * Klijent message service-a. Služi za komuniciranje sa serverom ili drugim
 * klijentima sustava.
 */
public class ClientMsgService implements IClientMsgService {

	/** Static varijabla za određivanje nodeID-a klijenta */
	private static int nextNodeID = 1;

	/** Objekt koji sadrži IP servera, port servera i ID node-a klijenta. */
	private ClientParameters parametri;

	/** Socket za vezu prema serveru */
	private Socket socket;

	/** Unikatna adresa klijenta. */
	private Address adresa;

	/** Input stream klijenta za primanje poruka */
	private InputStream input;

	/** Izlazni buffer koji ima implementiran timeout */
	private TimeoutBuffer buffer;

	/** Flag za provjeru je li se klijent registrirao na serveru */
	private boolean isRegistered;

	/**
	 * Konstruktor klijenta. Unutar konstruktora ostvaruje se veza prema
	 * serveru.
	 * 
	 * @param config
	 *            Objekt sa parametrima klijenta.
	 * @throws IOException
	 *             Ukoliko dođe do greške prilikom komunikacije s drugim
	 *             računalom.
	 * @throws UnknownHostException
	 *             Ukoliko se spajamo na host koji ne postoji ili nije dostupan.
	 * @throws IllegalArgumentException
	 *             Ukoliko su parametri klijenta neispravni.
	 * @throws SocketException
	 *             Ako se ne uspostavi konekcija unutar SOCKET_TIMEOUT vremena.
	 */
	public ClientMsgService(Properties config) throws UnknownHostException,
			IOException, IllegalArgumentException {
		this.parametri = new ClientParameters(config);
		this.socket = null;

		int nodeId = nextNodeID++;
		int clientID = this.parametri.getClientID();
		this.adresa = new Address(clientID, nodeId);
		this.isRegistered = false;
	}

	public void start() throws ClientCannotStart {
		try {
			this.socket = new Socket(this.parametri.getServerIP(),
					this.parametri.getServerPort());
			this.socket.setSoTimeout(this.parametri.getSocketTimeout());
			this.input = new BufferedInputStream(this.socket.getInputStream());
			this.buffer = new TimeoutBuffer(parametri.getBufferTimeout(),
					parametri.getBufferSize(), this.socket.getOutputStream());

			new Thread(this.buffer).start();

			register();
		} catch (UnknownHostException e) {
			throw new ClientCannotStart(this.adresa, "Ne mogu pronaći server: "
					+ this.parametri.getServerIP());
		} catch (SocketException e) {
			throw new ClientCannotStart(this.adresa,
					"Ne mogu se spojiti na server.");
		} catch (IOException e) {
			throw new ClientCannotStart(this.adresa,
					"Ne mogu se spojiti na server.");
		} catch (UnableToRegister e) {
			throw new ClientCannotStart(this.adresa,
					"Ne mogu se registrirati na serveru.");
		}
	}

	public void stop() {
		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (IOException zanemarivo) {
			}
		}
	}

	public synchronized Message receive() {
		try {
			this.socket.setSoTimeout(0);

			while (this.input.available() < 2 * Address.addressLength + 1) {
				Thread.yield();
			}

			return Message.fromStream(this.input);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Pretplaćuje korisnika na jednu sljedeću poruku zadanog tipa.
	 * 
	 * @param type
	 *            Tip poruke na koju se korisnik pretplaćuje
	 */
	public void query(MessageType type) throws MessageNotSent {
		Message poruka = new QueryMessage(type);
		send(poruka);
	}

	/**
	 * Pretplaćuje korisnika na jednu sljedeću poruku zadanog tipa i zadanog
	 * sadržaja. Korisnik će dobiti samo onu poruku koja se podudara s oba
	 * parametra.
	 * 
	 * @param type
	 *            Tip poruke na koju se korisnik pretplaćuje
	 * @param data
	 *            Sadržaj poruke na koju se korisnik pretplaćuje
	 */
	public void query(MessageType type, byte[] data) throws MessageNotSent {
		Message poruka = new QueryMessage(type, data);
		send(poruka);
	}

	/**
	 * Pretplaćuje korisnika na više poruka zadanog tipa.<br />
	 * Broj poruka koje će klijent primiti određen je parametrom.
	 * 
	 * @param type
	 *            Tip poruke na koju se korisnik pretplaćuje
	 * @param count
	 *            Broj poruka na koje se klijent pretplaćuje.
	 */
	public void query(MessageType type, int count) throws MessageNotSent {
		Message poruka = new QueryMessage(type);
		for (int i = 1; i <= count; i++) {
			send(poruka);
		}
	}

	public Address register() throws UnableToRegister {
		if (!this.isRegistered) {
			Message poruka = new RegisterMessage(this.adresa);

			try {
				send(poruka);
			} catch (MessageNotSent e) {
				throw new UnableToRegister(this.adresa);
			}

			this.isRegistered = true;
		}

		return this.adresa;
	}

	public void send(Message message) throws MessageNotSent {
		message.setSource(adresa);

		try {
			buffer.addMessage(message);
		} catch (IOException e) {
			throw new MessageNotSent(this.adresa, "Poruka nije poslana!", e);
		}
	}

	/**
	 * Klasa-container parametara klijenta.
	 */
	private class ClientParameters {

		/**
		 * Parametar. Veličina send buffera. Poruke se šalju na server kad se
		 * buffer napuni.
		 */
		private int sendBufferSize;

		/**
		 * Parametar. Buffer timeout nakon kojeg treba prazniti buffer.
		 */
		private int sendBufferTime;

		/**
		 * Vrijeme (u milisekundama) koje socket čeka na konekciju prije nego
		 * što baci SocketException exception.
		 */
		private int socketTimeout;

		/** Port preko kojeg se spajamo na server */
		private int serverPort;

		/** IP servera */
		private String serverIP;

		/** ID node-a na kojem se nalazi klijent */
		private int nodeID;

		/** ID klijenta */
		private int clientID;

		/**
		 * Konstruktor.
		 * 
		 * @param config
		 *            Objekt koji sadrži IP servera, port servera i ID node-a
		 *            klijenta.
		 * @throws IllegalArgumentException
		 *             Ukoliko su parametri neispravni.
		 */
		public ClientParameters(Properties config)
				throws IllegalArgumentException {

			try {
				this.serverPort = getNumericProperty(config, "server.port", 0, 65535);
				this.sendBufferSize = getNumericProperty(config, "buffer.size", 0);
				this.sendBufferTime = getNumericProperty(config, "buffer.timeout", 0);
				this.socketTimeout = getNumericProperty(config, "socket.timeout", 0);
				this.clientID = getNumericProperty(config, "client.ID", 0);

				this.serverIP = config.getProperty("server.address");
				if (!isValidIP(this.serverIP)) {
					throw new IllegalArgumentException("Neispravna IP adresa: "
							+ this.serverIP);
				}

			} catch (NullPointerException e) {
				throw new IllegalArgumentException(
						"Niste zadali sve parametre klijenta!");
			}
		}

		/**
		 * Čitanje i provjera brojčanog parametra iz Properties objekta.
		 * 
		 * @param properties
		 *            Objekt sa parametrima.
		 * @param name
		 *            Naziv parametra.
		 * @param bottom
		 *            Donja granica koju parametar mora poštivati.
		 * @param top
		 *            Gornja granica koju parametar mora poštivati.
		 * @return Vrijednost parametra.
		 * @throws IllegalArgumentException
		 *             Ukoliko parametar ne zadovoljava granice.
		 */
		private int getNumericProperty(Properties properties, String name,
				int bottom, int top) {
			int value;
			try {
				value = Integer.parseInt(properties.getProperty(name));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Neispravan parametar: "
						+ name + ". Potrebna je brojčana vrijednost!");
			}

			if ((value < bottom) || (value > top)) {
				throw new IllegalArgumentException("Neispravan parametar: "
						+ name + ". Zadali ste: " + value);
			} else {
				return value;
			}
		}
		
		/**
		 * Čitanje i provjera brojčanog parametra iz Properties objekta.
		 * 
		 * @param properties
		 *            Objekt sa parametrima.
		 * @param name
		 *            Naziv parametra.
		 * @param bottom
		 *            Donja granica koju parametar mora poštivati.
		 * @return Vrijednost parametra.
		 * @throws IllegalArgumentException
		 *             Ukoliko parametar ne zadovoljava granicu.
		 */
		private int getNumericProperty(Properties properties, String name,
				int bottom) {
			int value;
			try {
				value = Integer.parseInt(properties.getProperty(name));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Neispravan parametar: "
						+ name + ". Potrebna je brojčana vrijednost!");
			}

			if (value < bottom) {
				throw new IllegalArgumentException("Neispravan parametar: "
						+ name + ". Zadali ste: " + value);
			} else {
				return value;
			}
		}

		/**
		 * Getter server porta.
		 * 
		 * @return Port preko kojeg se spajamo na server.
		 */
		public int getServerPort() {
			return this.serverPort;
		}

		/**
		 * Getter IP-a servera.
		 * 
		 * @return IP servera.
		 */
		public String getServerIP() {
			return this.serverIP;
		}

		/**
		 * Getter node ID-a.
		 * 
		 * @return ID node-a na kojem se nalazi klijent.
		 */
		public int getNodeID() {
			return this.nodeID;
		}

		/**
		 * Getter sendBufferSize-a.
		 * 
		 * @return Broj poruka koje može primiti sendBuffer.
		 */
		public int getBufferSize() {
			return this.sendBufferSize;
		}

		/**
		 * Getter sendBufferTime-a.
		 * 
		 * @return Vrijeme (u milisekundama). Buffer timeout nakon kojeg treba
		 *         prazniti buffer.s
		 */
		public int getBufferTimeout() {
			return this.sendBufferTime;
		}

		/**
		 * Getter socketTimeout-a.
		 * 
		 * @return Vrijeme (u milisekundama). Ukoliko se klijent ne spoji na
		 *         server unutar ovog vremenskog intervala, baca se
		 *         socketException.
		 */
		public int getSocketTimeout() {
			return this.socketTimeout;
		}

		/**
		 * Getter clientID-a.
		 * 
		 * @return ID klijenta u string zapisu.
		 */
		public int getClientID() {
			return this.clientID;
		}

		/**
		 * Metoda za provjeru valjanosti IP-a.
		 * 
		 * @param IP
		 *            IP koji želimo provjeriti
		 * @return True ako IP predstavlja valjan IP, inače false.
		 */
		private boolean isValidIP(String IP) {
			String[] djelovi = IP.split("\\.");

			if (djelovi.length != 4) {
				return false;
			}

			try {
				for (int i = 0; i < 4; i++) {
					int dio = Integer.parseInt(djelovi[i]);
					if ((dio < 0) || (dio > 255))
						return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}

			return true;
		}

	}
}
