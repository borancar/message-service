package hr.fer.zemris.java.nescume.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasa predstavlja opću poruku koja služi za komunikaciju klijenata i
 * komunikaciju klijenta i servera. Ova klasa se može, i trebala bi se
 * derivirati u klase koje predstavljaju specifične poruke i obrade nad njima.
 */
public class Message {
	
	/**
	 * Mapiranje byte-vrsta poruke, služi za deserijalizaciju poruka.
	 */
    private static final Map<Byte, MessageType> messages = new HashMap<Byte, MessageType>();
	
    /**
     * Enumeracija tipova poruka.
     */
    public enum MessageType {
        
    	/**
    	 * Poruka koja služi za registraciju klijenta na server.
    	 */
    	REGISTER(1),
    	
    	/**
    	 * Poruka koja služi za registriranje slušača na serveru.
    	 */
    	QUERY(2),
    	
    	/**
    	 * Poruka klijenta klijentu.
    	 */
    	LETTER(3);
        
    	/**
    	 * Vrijednosti poruka.
    	 */
        private final int value;
        
        /**
         * Konstruktor tipa poruke.
         * @param value brojčana vrijednost pridjeljena poruci
         */
        private MessageType(int value) {
        	this.value = value;
        	messages.put(((Integer) value).byteValue(), this);
        }
        
        /**
         * Vraća brojčanu vrijednost tipa poruke.
         * @return vrijednost
         */
        public byte toByte() {
        	return (byte) this.value;
        }
        
        /**
         * Vraća tip poruke pridjeljen brojčanoj vrijednosti.
         * @param type brojčana vrijednost
         * @return tip poruke
         */
        public static MessageType fromByte(byte type) {
        	if(!messages.containsKey(type)) {
        		throw new IllegalArgumentException("Nepostojeći tip poruke: " + type);
        	}
        	
        	return messages.get(type);
        }
    }

    /**
     * Izvorišna adresa (tko šalje).
     */
    protected Address src;

    /**
     * Odredišna adresa (tko prima).
     */
    protected Address dest;

    /**
     * Tip poruke.
     */
    protected MessageType type;
    
    /**
     * Hitnost poruke
     */
    // Neka je tu za sada. Bufferiranje se može vršiti i na serveru i nitko ne govori da neće
    protected boolean urgent = false;
    
    /**
     * Sadržaj poruke.
     */
    private byte[] data;

    /**
     * Konstruktor koji služi za stvaranje poruke u trenutku slanja, omogućuje
     * unos odredišta, tipa i sadržaja. Izvorište popunjava metoda
     * koja šalje poruku.
     * 
     * @param destination
     *            odredište
     * @param type
     *            tip poruke
     * @param data
     *            sadržaj poruke
     */
    public Message(Address destination, MessageType type, byte[] data) {
        this.dest = destination;
        this.type = type;
        this.data = data;
    }

    /**
     * Defaultni konstruktor, stvara praznu opću poruku.
     */
    public Message() {
    	
    }

    /**
     * Getter za sadržaj poruke.
     * 
     * @return sadržaj
     */
    public byte[] getData() {
        return data;
    }

    /**
	 * Setter za sadržaj poruke.
	 * @param data sadržaj poruke
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
     * Getter za odredišnu adresu.
     * 
     * @return odredišna adresa
     */
    public Address getDestination() {
        return dest;
    }

    /**
     * Setter za odredišnu adresu.
     * 
     * @param destination odredišna adresa
     */
    public void setDestination(Address destination) {
        this.dest = destination;
    }

    /**
     * Getter za izvorišnu adresu.
     * 
     * @return izvorišna adresa
     */
    public Address getSource() {
        return src;
    }

    /**
     * Setter za izvorišnu adresu.
     * 
     * @param source izvorišna adresa
     */
    public void setSource(Address source) {
        this.src = source;
    }

    /**
     * Getter za tip poruke.
     * 
     * @return tip poruke
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Setter za tip poruke.
     * 
     * @param type tip poruke
     */
    public void setType(MessageType type) {
        this.type = type;
    }

    /**
     * Sastavlja kompletnu poruku u obliku polja bajtova.
     * 
     * @return Poruku u obliku polja bajtova
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2 * Address.addressLength + 1 + 2 + data.length);
 
        try {
			toStream(baos);
		} catch (IOException ignorable) {

		}
		
        return baos.toByteArray();
    }
    
    /**
     * Postavlja hitnost poruke.
     * @param urgent true ako je poruka hitna, false inače
     */
    public void setUrgent(boolean urgent) {
    	this.urgent = urgent;
    }
    
    /**
     * Ispituje da li je poruka hitna.
     * @return true ako je, false inače
     */
    public boolean isUrgent() {
    	return this.urgent;
    }
    
    /**
     * Poruku ispisuje na izlaz.
     * @param outputStream izlazni stream
     */
    public void toStream(OutputStream outputStream) throws IOException {
    	DataOutputStream dos = new DataOutputStream(outputStream);
        
		dos.write(dest.getGID());
		dos.write(src.getGID());
		
		// Spremi tip + hitnost (hitnost ide na najviši bit)
		dos.writeByte(type.toByte() | (urgent ? (byte) 128 : 0));

		dataToStream(dos);
    }
    
    /**
     * Ispisuje tijelo poruke, sadržaj poruke na izlazni stream. Sve poruke svoje podatke
     * pišu u ovom, a za headere se oslanjaju na metodu toStream(). Ovu metodu razredi
     * derivirani iz ovog razreda <b>moraju</b> pregaziti.
     * 
     * U svakom slučaju, sve implementacije ove metode <b>moraju</b> prvo ispisati jedan
     * short koji označava koliko je veliko tijelo poruke.
     * @param outputStream izlazni stream
     * @throws IOException u slučaju greške pri pisanju
     */
    protected void dataToStream(DataOutputStream outputStream) throws IOException {
    	outputStream.writeShort(data.length);
    	outputStream.write(data);
    }
    
    /**
     * Čita tijelo poruke, sadržaj poruke sa ulaznog streama. Sve poruke svoj podatke čitaju
     * preko ove metode, a za headere se oslanjaju na metodu fromStream(). Ovu metodu razredi
     * derivirani iz ovog razreda <b>moraju</b> pregaziti.
     * @param inputStream ulazni stream
     * @param dataLength veličina tijela poruke
     * @throws IOException u slučaju greške pri čitanju
     */
    protected void dataFromStream(DataInputStream inputStream, int dataLength) throws IOException {
    	this.data = new byte[dataLength];
		inputStream.readFully(this.data, 0, dataLength);
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(!(obj instanceof Message)) {
    		return false;
    	}
    	
    	Message other = (Message) obj;
    	
    	return this.src.equals(other.src) && this.dest.equals(other.dest) &&
    			this.type.equals(other.type) && Arrays.equals(this.data, other.data);
    }
    
    @Override
    public int hashCode() {
    	return src.hashCode() ^ dest.hashCode() ^ type.hashCode() ^ Arrays.hashCode(data);
    }

    @Override
    public String toString() {
    	return "Opća poruka za " + dest + " od " + src + " tipa " + type + " sadržaja " + Arrays.toString(data) + ".";
    }
    
    /**
     * Deserijalizira poruku.
     * 
     * @param messageByteArray
     *            Polje bajtova koje sadrži serijaliziranu poruku
     * 
     * @return Nova poruka
     * @throws IOException
     */
    public static Message fromByteArray(byte[] messageByteArray) {
    	try {
    		return fromStream(new ByteArrayInputStream(messageByteArray));
    	} catch (IOException ignorable) {
    		return null;
    	}
    }
    
    /**
     * Deserijalizira poruku.
     * 
     * @param inputStream ulaz koji sadržava serijaliziranu poruku
     * 
     * @return Nova poruka
     * @throws IOException
     */
    public static Message fromStream(InputStream inputStream) throws IOException {
		DataInputStream dataInput = new DataInputStream(inputStream);

		byte[] addressBuffer = new byte[Address.addressLength];
		
		dataInput.readFully(addressBuffer);
		Address dest = Address.parseGID(addressBuffer);
		
		dataInput.readFully(addressBuffer);
		Address src = Address.parseGID(addressBuffer);
		
		byte type = dataInput.readByte();

		// Na najvećem bitu leži hitnost
		boolean urgent = ((type & 128) != 0 ? true : false);
		
		// Makni hitnost
		type = (byte) (type & 127);
		
		MessageType messageType = MessageType.fromByte(type);

		short bodyLength = dataInput.readShort();
		
		Message message;
		
		switch(messageType) {
		case QUERY:
			message = new QueryMessage();
			break;

		case REGISTER:
			message = new RegisterMessage();
			break;
			
		default:
			message = new Message();
		}
		
		message.dest = dest;
		message.src = src;
		message.type = messageType;
		message.urgent = urgent;
		message.dataFromStream(dataInput, bodyLength);
		
		return message;
    }
}
