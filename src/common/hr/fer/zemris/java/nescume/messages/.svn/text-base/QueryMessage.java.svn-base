package hr.fer.zemris.java.nescume.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Poruka koja služi klijentima za obavijestiti server da žele primati obavijesti o
 * određenim porukama koje server primi. Različiti kriteriji se zadaju preko konstruktora.
 */
public class QueryMessage extends Message {
	
	/**
	 * Tip poruke na koji se klijent želi pretplatiti da prima obavijesti.
	 */
	private MessageType queryType;
	
	/**
	 * Uzorak kojemu mora odgovarati poruka da bi pošiljatelj ove poruke bio obavješten o
	 * njoj.
	 */
	private byte[] pattern;
	
	/**
	 * Konstruktor koji služi za stvoriti poruku za primati obavijesti o porukama određenog
	 * tipa.
	 * @param typeOfMessage tip poruke na koji pretplatiti pošiljatelja
	 */
	public QueryMessage(MessageType typeOfMessage) {
		this.type = MessageType.QUERY;
		
		this.queryType = typeOfMessage;
		
		// Serverova adresa neka bude dogovorno 0:0
		this.dest = new Address(0, 0);
		
		// 0 neka bude wildcard, primamo sve poruke ovog tipa
		this.pattern = new byte[]{0};
	}
	
	/**
	 * Konstruktor koji služi za stvoriti poruku za primati obavijesti o porukama određenog
	 * tipa kojima sadržaj odgovara predanom uzorku.
	 * @param typeOfMessage tip
	 * @param data uzorak
	 */
	public QueryMessage(MessageType typeOfMessage, byte[] pattern) {
		this.type = MessageType.QUERY;
		this.queryType = typeOfMessage;
		this.dest = new Address(0,0);
		this.pattern = pattern;
	}
	
	/**
	 * Defaultni konstruktor, ne koristiti ga za stvaranje poruka prilikom slanja.
	 */
	public QueryMessage() {
		this.type = MessageType.QUERY;
	}
	
	/**
	 * Vraća na koji tip poruke ova poruka pretplaćuje klijenta koji ju je poslao.
	 * @return tip poruke na koji se klijent želi pretplatiti 
	 */
	public MessageType getQueryType() {
		return queryType;
	}
	
	/**
	 * Vraća pattern kakav traži klijent koji je poslao ovu poruku, pogodan za spremanje u kolekciju.
	 * @return pattern
	 */
	public Byte[] getPattern() {
		Byte[] toReturn = new Byte[pattern.length];
		
		for (int i = 0; i < pattern.length; i++) {
			toReturn[i] = pattern[i];
		}
		
		return toReturn;
	}
	
	@Override
	protected void dataFromStream(DataInputStream inputStream, int dataLength) throws IOException {
		this.pattern = new byte[dataLength - 1];
		
		this.queryType = MessageType.fromByte(inputStream.readByte());
		inputStream.readFully(this.pattern);
	}
	
	@Override
	protected void dataToStream(DataOutputStream outputStream) throws IOException {
		outputStream.writeShort(1 + pattern.length);
		outputStream.writeByte(queryType.toByte());
		outputStream.write(pattern);
	}
	
	@Override
	public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2 * Address.addressLength + 1 + 2 + 1 + pattern.length);
        
        try {
			toStream(baos);
		} catch (IOException ignorable) {

		}
		
        return baos.toByteArray();	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof QueryMessage)) {
			return false;
		}
		
		QueryMessage other = (QueryMessage) obj;
		
		return (this.queryType.equals(other.queryType) && Arrays.equals(this.pattern, other.pattern)) ||
				super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return queryType.hashCode() ^ Arrays.hashCode(pattern);
	}
}
