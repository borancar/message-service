package hr.fer.zemris.java.nescume.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implementacija adrese koja je oblika clientID:nodeID, služi za interno
 * adresiranje prijavljenih klijenata na serveru, i međusobno adresiranje
 * klijenata kod slanja poruka.
 */
public class Address {
    /** Veličina adrese u bajtovima */
    public static final int addressLength = 3;

    /**
     * Fizička adresa JVM-a na kojem se nalazi service klijenta.
     */
    private short clientID;

    /**
     * Adresa čvora na JVM-u.
     */
    // Neće biti više od 255 dretvi jer je to onda jaako neefikasno.
    private byte nodeID;

    /**
     * Stvara novi primjerak adrese iz zadane kombinacije clientID:nodeID.
     * 
     * @param clientID
     *            fizička adresa računala
     * @param nodeID
     *            logička adresa čvora
     */
    public Address(int clientID, int nodeID) {
        this.clientID = (short) clientID;
        this.nodeID = (byte) nodeID;
    }

    /**
     * Vraća fizičku adresu JVM-a (adrese JVM-a su jedinstvene).
     * 
     * @return jedinstvena fizička adresa računala
     */
    public int getClientID() {
        return clientID;
    }

    /**
     * Vraća adresu čvora na JVM-u (čvorovi su jedinstveni samo u svom
     * JVM-u).
     * 
     * @return jedinstvena adresa čvora na tom računalu
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * Vraća adresu u GID obliku, slijed podataka clientID pa nodeID kao polje.
     * 
     * @return GID (globalni identifikator)
     */
    public byte[] getGID() {
        /*
         * Koriste se streamovi u slučaju da se odluči promijeniti širina
         * clientID-a ili nodeID-a.
         */
        ByteArrayOutputStream bos = new ByteArrayOutputStream(addressLength);
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            dos.writeShort(clientID);
            dos.writeByte(nodeID);
        } catch (IOException notPossible) {
        } finally {
            try {
                dos.close();
            } catch (Exception ignorable) {
            }
        }

        return bos.toByteArray();
    }

    /**
     * Parsira GID i iz njega stvara novi primjerak adrese.
     * 
     * @param GID
     *            globalni identifikator
     * @return adresa koja odgovara zadanom GID-u
     */
    public static Address parseGID(byte[] GID) {
        /*
         * Koriste se streamovi u slučaju da se odluči promijeniti širina
         * clientID-a ili nodeID-a.
         */
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(GID));

        try {
            return new Address(dis.readShort(), dis.readByte());
        } catch (IOException e) {
            throw new IllegalArgumentException("Predan neispravan GID!");
        } finally {
            try {
                dis.close();
            } catch (Exception ignorable) {
            }
        }
    }

    /**
     * Stvara adresu iz tekstulanog zapisa GID-a clientID:nodeID gdje su
     * clientID i nodeID cijeli brojevi raspona od 0 do 2147483647
     * 
     * @param GID
     *            globalni identifikator
     * @return adresa koja odgovara zadanom GID-u
     */
    public static Address parseGID(String GID) {
        try {
            String[] parts = GID.split(":");

            return new Address(Integer.parseInt(parts[0]), Byte.parseByte(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Predan neispravan GID!");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(!(obj instanceof Address)) {
    		return false;
    	}
    	
    	Address other = (Address) obj;
    	
    	return this.clientID == other.clientID && this.nodeID == other.nodeID;
    }
    
    @Override
    public int hashCode() {
    	return Integer.valueOf(clientID).hashCode() ^ Byte.valueOf(nodeID).hashCode();
    }
    
    @Override
    public String toString() {
    	return clientID + ":" + nodeID;
    }
}
