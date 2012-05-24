package hr.fer.zemris.java.nescume.messages;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Buffer za poruke koji se brine da poruke u bufferu sigurno budu odaslane nakon nekog
 * vremena ili nakon popunjenja buffer-a. Iznimke su urgent poruke koje odmah uzrokuju
 * praženjenje buffera.
 */
public class TimeoutBuffer implements Runnable {
	
	/**
	 * True ako je timer aktiviran i flusher odbrojava, false inače.
	 */
	private boolean timer;
	
	/**
	 * True ako je flusher aktiva, false inače. Služi za gašenje flushera.
	 */
	private boolean active;
	
	/**
	 * Buffer timeout nakon kojeg treba prazniti buffer.
	 */
	private int bufferTimeout;

	/**
	 * Veličina buffera. Kada buffer dosegne ovu veličinu, flusha se na izlaz.
	 */
	private int bufferSize;
	
	/**
	 * Buffer sa porukama. Poruke se šalju kad se buffer napuni.
	 */
	private List<Message> sendBuffer;
	
	/**
	 * Izlazni kanal na kojeg se šalje.
	 */
	private OutputStream output;
	
	/**
	 * Stvara novi timeout buffer koja nakon popunjenja buffer.size ili isteka
	 * buffer.timeout šalje sve poruke koje prije toga drži u sebi na izlazni kanal.
	 * @param bufferTimeout buffer.timeout
	 * @param bufferSize buffer.size
	 * @param output izlazni kanal
	 */
	public TimeoutBuffer(int bufferTimeout, int bufferSize, OutputStream output) {
		this.timer = false;
		this.bufferTimeout = bufferTimeout;
		this.bufferSize = bufferSize;
		this.output = new BufferedOutputStream(output);
		this.sendBuffer = new ArrayList<Message>(bufferSize);
		this.active = true;
	}
	
	/**
	 * Stvara novi buffer prema specifikaciji klase, dodatno omogućuje postavljanje ciljane
	 * veličine paketa koji se šalju.
	 * @param bufferTimeout buffer.timeout
	 * @param bufferSize buffer.size
	 * @param packetSize packet.size
	 * @param output izlazni kanal
	 */
	public TimeoutBuffer(int bufferTimeout, int bufferSize, int packetSize, OutputStream output) {
		this.timer = false;
		this.bufferTimeout = bufferTimeout;
		this.bufferSize = bufferSize;
		this.output = new BufferedOutputStream(output, packetSize);
		this.sendBuffer = new ArrayList<Message>(bufferSize);
		this.active = true;		
	}
	
	/**
	 * Dodaje poruku u buffer. Ponašanje buffera je opisano u implementaciji buffera.
	 * @param message poruka
	 * @throws IOException u slučaju greške pri slanju
	 */
	public synchronized void addMessage(Message message) throws IOException {
		this.sendBuffer.add(message);
		
		if(message.isUrgent() || this.sendBuffer.size() == this.bufferSize) {
			this.flush();
		} else {
			this.timer = true;
			this.notifyAll();
		}
	}
	
	public void run() {
		while(this.active) {
			try {
				synchronized(this) {
					while(!this.timer) {
						this.wait();
					
						if(!this.active) break;
					}
				}
				
				if(!this.active) break;
				
				Thread.sleep(this.bufferTimeout);

				this.flush();
			} catch(InterruptedException ignorable) {
				
			} catch(IOException e) {
				this.active = false;
			}
		}
	}
	
	/**
	 * Zaustavlja i gasi timeout buffer nad kojim je pozvano.
	 */
	public synchronized void stop() {
		this.active = false;
		
		this.notifyAll();
	}

	/**
	 * Slanje bufferiranih poruka.
	 * 
	 * @throws MessageNotSent
	 *             Ukoliko poruka nije poslana.
	 */
	private synchronized void flush() throws IOException {
		this.timer = false;
		
		for (Message msg : this.sendBuffer) {
			msg.toStream(this.output);
		}
		
		this.output.flush();
	
		this.sendBuffer.clear();
	}
}
