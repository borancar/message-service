package hr.fer.zemris.java.nescume.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasa predstavlja poruku koja služi za registraciju klijenta na server.
 */
public class RegisterMessage extends Message {
	
	/**
	 * Konstruktor registracijske poruke kada su statički clientID i nodeID.
	 * @param address adresa klijenta koji šalje ovu poruku
	 */
	public RegisterMessage(Address address) {
		this.dest = new Address(0,0);
		this.src = address;
		this.urgent = true;
		this.type = MessageType.REGISTER;
	}
	
	/**
	 * Defaultni konstruktor, ne koristiti ga prilikom slanja poruka.
	 */
	public RegisterMessage() {
		this.type = MessageType.REGISTER;
	}
	
	@Override
	public String toString() {
		return "Registracijska poruka za klijenta " + this.src + ".";
	}
	
	@Override
	protected void dataFromStream(DataInputStream inputStream, int dataLength) throws IOException {
		return;
	}
	
	@Override
	protected void dataToStream(DataOutputStream outputStream) throws IOException {
		outputStream.writeShort(0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof RegisterMessage)) {
			return false;
		}
		
		RegisterMessage other = (RegisterMessage) obj;
		
		return (this.dest == other.dest && this.src == other.src && this.type == other.type) ||
				super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return this.dest.hashCode() ^ this.src.hashCode() ^ this.type.hashCode();
	}
}
