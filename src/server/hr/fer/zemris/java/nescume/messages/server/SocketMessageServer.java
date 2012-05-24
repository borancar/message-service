package hr.fer.zemris.java.nescume.messages.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import hr.fer.zemris.java.nescume.messages.Address;
import hr.fer.zemris.java.nescume.messages.Message;
import hr.fer.zemris.java.nescume.messages.QueryMessage;
import hr.fer.zemris.java.nescume.messages.TimeoutBuffer;
import hr.fer.zemris.java.nescume.messages.Message.MessageType;
import hr.fer.zemris.java.nescume.messages.server.exceptions.ServerCannotStart;
import hr.fer.zemris.java.nescume.messages.server.exceptions.ServerCrashed;

/**
 * Implemetacija servera koji komunicira s klijentima preko socketa.
 */
public class SocketMessageServer implements IMessageServer {

	/**
	 * Mapiranje adresa u opsluživače klijenata, služi kod razmjene poruka među klijentima.
	 */
	private Map<Address, ClientHandler> clients = Collections.synchronizedMap(new HashMap<Address, ClientHandler>());
	
	/**
	 * Mapiranje tipova poruka u opsluživače klijenata, služi za registriranje querya.
	 * Namjena ovog je ubrzavanje, bez ove kolekcije, svaki klijent bi se trebao pitati da
	 * li prihvaća poruke određenog tipa.
	 */
	private Map<MessageType, Set<ClientHandler>> queries = Collections.synchronizedMap(new HashMap<MessageType, Set<ClientHandler>>());
	
	/**
	 * Klasa iz koje se stvaraju objekti koji opslužuju klijente. Svi opsluživači se trebaju
	 * vrtiti u svojim dretvama.
	 */
	private class ClientHandler extends Thread {
		
		/**
		 * Primatelj. Dio opsluživača namijenjen primanju poruka od klijenta i obrađivanju
		 * istih. Svaki primatelj zakačen je na svoj ulazni kanal i svaki radi u svojoj
		 * dretvi.
		 */
		private class Receiver implements Runnable {
			
			/**
			 * Ulazni kanal primatelja.
			 */
			private InputStream input;
			
			/**
			 * Objekt koji je stvorio ovog primatelja.
			 */
			private ClientHandler clientHandler;
			
			/**
			 * Konstruktor koji stvara novog primatelja i spaja ga na ulazni kanal.
			 * @param input ulazni kanal
			 * @param clientHandler objekt koji je stvorio ovog primatelja
			 */
			public Receiver(InputStream input, ClientHandler clientHandler) {
				this.input = new BufferedInputStream(input);
				this.clientHandler = clientHandler;
			}
			
			/**
			 * Metoda obavlja posao primatelja. Hvata sve poruke koje klijent šalje na
			 * server i obrađuje ih.
			 */
			public void run() {
				try {
					while(active) {
						if(input.available() > 0) {
							Message received = Message.fromStream(input);
	
							if(queries.containsKey(received.getType())) {
								for(ClientHandler toNotify : queries.get(received.getType())) {
									if(toNotify.matchesQueryType(received)) {
										toNotify.sendMessage(received);
										
										System.out.println("Poruka poslana " + toNotify + " jer se pretplatio na nju.");
									}
								}
							}
							
							switch(received.getType()) {
							case REGISTER:
								synchronized(dispatchers) {
									clients.put(received.getSource(), (ClientHandler) clientHandler);
									
									//@DebugStart
									System.out.println("Klijent " + received.getSource() + " se registrirao!");
									//@DebugEnd
	
									dispatchers.notifyAll();
								}
								break;
								
							case QUERY:
								QueryMessage queryMessage = (QueryMessage) received;
								
								if(!queries.containsKey(queryMessage.getQueryType())) {
									queries.put(queryMessage.getQueryType(), new HashSet<ClientHandler>());
								}
								
								queries.get(queryMessage.getQueryType()).add(clientHandler);
								
								synchronized(queryPatterns) {
									queryPatterns.put(queryMessage.getQueryType(), queryMessage.getPattern());
								}
								
								// DebugStart
								System.out.println("Klijent se registirirao na " + queryMessage.getQueryType() + " " + queryMessage.getPattern());
								// DebugEnd
								
								break;
								
							default:
								//@DebugStart
								System.out.println(received);
								System.out.println("Stvaram novog dispečera poruke!");
								//@DebugEnd
																
								new Thread(new Dispatcher(received)).start();
															
								break;
							}
						}
						
						Thread.yield();
					}					
				} catch (IOException e) {
					return;
				} finally {
					try {
						input.close();
					} catch (IOException ignorable) {
						
					}
				}
			}
		}
		
		/**
		 * Dispečer poruka klijenta klijentu. Dispečer šalje klijentu poruke namijenjene
		 * za njega.
		 */
		private class Dispatcher implements Runnable {

			/**
			 * Poruka koju treba poslati.
			 */
			private Message toSend;
			
			/**
			 * Preostali broj pokušaja slanja poruke klijentu ako on nije dostupan.
			 */
			private int retries;
			
			/**
			 * Konstruktor novog dispečera, prima poruku koju treba dispečirati.
			 * @param toSend poruka
			 */
			public Dispatcher(Message toSend) {
				this.toSend = toSend;
				
				this.retries = dispatcherRetries;
			}
			
			/**
			 * Metoda obavlja posao dispečera, ako klijent kojemu treba isporučiti poruku
			 * još nije registriran, dispečer čeka.
			 */
			public void run() {
				while(!clients.containsKey(toSend.getDestination())) {
					//@DebugStart
					System.out.println("Drugi još nije registriran, dretva ide na čekanje!");
					//@DebugEnd
					
					try {
						synchronized(dispatchers) {
							dispatchers.wait(dispatcherTimeout);
							
							if(!active) break;
							
							retries--;
							if(retries == 0) {
								return;
							}
						}
						
						if(!active) break;
					} catch (InterruptedException ignorable) {
						
					}
				}
				
				if(!active) return;
				
				try {
					clients.get(toSend.getDestination()).sendMessage(toSend);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Flusher koji šalje podatke klijentu.
		 */
		private TimeoutBuffer sender;
		
		/**
		 * Receiver koji prima poruke od klijenta.
		 */
		private Receiver receiver;
		
		/**
		 * Objekt preko kojeg se vrši sinhronizacija dispečera.
		 */
		private Object dispatchers = new Object();
		
		/**
		 * Klijent kojeg ovaj obrađivač obrađuje.
		 */
		private Socket client;
		
		/**
		 * True ako je opsluživač aktivan, false inače. Služi za gašenje opsluživača
		 * preko metode close()
		 */
		private volatile boolean active = false;
		
		/**
		 * Mapa tipova poruka na patterne koje moraju zadovoljiti poruke toga tipa da bi
		 * bile prosljeđene klijentu.
		 */
		private Map<MessageType, Byte[]> queryPatterns = new HashMap<MessageType, Byte[]>();
		
		/**
		 * Konstruktor obrađivača, prima klijenta kojeg će obrađivati.
		 * @param client klijent
		 */
		public ClientHandler(Socket client) {
			super();
			this.client = client;
			this.active = true;
			
			//@DebugStart
			System.out.println("Klijent " + client.getRemoteSocketAddress() + " se spojio.");
			//@DebugEnd
			
			try {
				sender = new TimeoutBuffer(bufferTimeout, bufferSize, packetSize, client.getOutputStream());
				receiver = new Receiver(client.getInputStream(), this);
				
				new Thread(sender).start();
				new Thread(receiver).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Provjerava da li klijenta treba obavijestiti o primanju ove poruke.
		 * @param received primljena poruka
		 * @return true ako da, false inače
		 */
		public synchronized boolean matchesQueryType(Message received) {
			
			synchronized(queryPatterns) {
				if(queryPatterns.containsKey(received.getType())) {
					return patternMatch(received.getData(), queryPatterns.get(received.getType()));
				} else {
					return false;
				}
			}
		}
		
		/**
		 * Pattern-match za message.data, provjera matches.
		 * @param data message.data
		 * @param pattern patterna
		 * @return true ako da, false inače
		 */
		private boolean patternMatch(byte[] data, Byte[] pattern) {
			if(pattern[0] == '0') {
				return true;
			}
			
			List<Byte> toCheck = new ArrayList<Byte>();
			
			for(byte bajt : data) {
				toCheck.add(bajt);
			}
			
			List<Byte> toUse = Arrays.asList(pattern);
			
			return Collections.indexOfSubList(toCheck, toUse) != -1;
		}

		/**
		 * Šalje poruku klijentu kojeg ovaj obrađivač obrađuje. Poruka će zapravo biti
		 * poslana klijentu najkasnije nakon bufferTimeout vremena. Nakon toga, potrebno je
		 * još neko vrijeme (latencija mreže) da poruke stigne do klijenta.
		 * @param message poruka
		 */
		public void sendMessage(Message message) throws IOException {
			sender.addMessage(message);
		}
		
		/**
		 * Gasi ovog opsluživača klijenta, a s njim i konekciju.
		 */
		public void close() {
			active = false;
						
			synchronized(sender) {
				sender.notifyAll();
			}
			
			try {
				this.client.close();
			} catch (Exception ignorable) {

			}
		}
	}
	
	/**
	 * Adresa na kojoj sluša server.
	 */
	private InetAddress bindAddress;
	
	/**
	 * Port na kojem sluša server.
	 */
	private int port;
	
	/**
	 * Vrijeme nakon kojeg ističe pokušaj hvatanja klijenta.
	 */
	private int socketTimeout;
	
	/**
	 * Vrijeme nakon kojeg se bezuvjetno šalje paket klijentu.
	 */
	private int bufferTimeout;
	
	/**
	 * Veličina buffera (nakon koliko poruka treba prazniti buffer).
	 */
	private int bufferSize;
	
	/**
	 * Ciljana veličina paketa (u bajtovima) koje server šalje klijentima.
	 */
	private int packetSize;
	
	/**
	 * Vrijeme nakon kojeg dispečer poruka pokušava ponovno slati poruku klijentu koji još
	 * nije registriran.
	 */
	private int dispatcherTimeout;
	
	/**
	 * Broj pokušaja slanja poruke klijentu koji još nije registriran.
	 */
	private int dispatcherRetries;
	
	/**
	 * True ako server ovog objekta radi, false inače. Služi za gašenje servera preko metode stop().
	 */
	private volatile boolean running = false;
	
	/**
	 * Konstruktor koji stvara novi server i služi za inicijalizaciju parametara preko
	 * propertiesa.
	 * @param properties parametri servera
	 */
	public SocketMessageServer(Properties properties) {
		try {
			bindAddress = InetAddress.getByName(properties.getProperty("listen.address", "0.0.0.0"));
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("listen.address mora biti valjana IP adresa ili postojeća domena!", e);
		}
		
		port = Integer.parseInt(properties.getProperty("listen.port", "0"));
		
		if(port < 0 || port > 65535) {
			throw new IllegalArgumentException("listen.port mora biti od 0 do 65535, uključivo!");
		}
		
		       packetSize = parsePositive(properties, "packet.size"       ,  "512");
		    bufferTimeout = parsePositive(properties, "buffer.timeout"    , "1000");
		       bufferSize = parsePositive(properties, "buffer.size"       ,    "3");
		    socketTimeout = parsePositive(properties, "socket.timeout"    , "2000");
		dispatcherTimeout = parsePositive(properties, "dispatcher.timeout", "1000");
		dispatcherRetries = parsePositive(properties, "dispatcher.retries",    "3");
	}
	
	/**
	 * Parsira zadani property predanih propertyja koji je prirodan broj, moguće je
	 * prosljediti i defaultnu vrijednost koja se koristi u slučaju da timeout zadanog 
	 * imena ne postoji.
	 * @param properties propertyji
	 * @param propertyName ime traženog propertyja
	 * @param defaultValue defaultna vrijednost
	 * @return traženi property
	 */
	private int parsePositive(Properties properties, String propertyName, String defaultValue) {
		try {
			int value = Integer.parseInt(properties.getProperty(propertyName, defaultValue));
			
			if(value <= 0) {
				throw new IllegalArgumentException(propertyName + " mora biti veći od 0!");
			}
			
			return value;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(propertyName + " mora biti cijeli broj!");
		}
	}
	
	public void start() throws ServerCannotStart, ServerCrashed {			
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(port, 0, bindAddress);
		
			//@DebugStart
			System.out.println("Server pokrenut na: " + serverSocket.getLocalSocketAddress());
			//@End
			
			running = true;

			serverSocket.setSoTimeout(socketTimeout);
		} catch (SocketException e) {
			throw new ServerCannotStart("Greška u TCP protokolu!", e);
		} catch (SecurityException e) {
			throw new ServerCannotStart("Nije dozvoljeno otvoriti socket!", e);
		} catch (IOException e) {
			throw new ServerCannotStart("Nije moguće otvoriti socket!", e);
		}
		
		try {
			while(running) {
				try {
					Socket clientSocket = serverSocket.accept();
					
					new ClientHandler(clientSocket).start();
				} catch(SocketTimeoutException ignorable) {
					
				}
			}
		} catch (IOException e) {
			throw new ServerCrashed("Server ne može primati konekcije, postojeći klijenti se i dalje opslužuju!", e);
		} finally {
			try {
				running = false;
				serverSocket.close();
			} catch(IOException ignorable) {
				
			}
		}
	}
	
	public void stop() {
		running = false;
		
		for(Address client : clients.keySet()) {
			clients.get(client).close();
		}
	}
}
