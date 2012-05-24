package hr.fer.zemris.java.nescume.messages.client;

import hr.fer.zemris.java.nescume.messages.Address;
import hr.fer.zemris.java.nescume.messages.Message;
import hr.fer.zemris.java.nescume.messages.client.exceptions.ClientCannotStart;
import hr.fer.zemris.java.nescume.messages.client.exceptions.ClientCrashed;
import hr.fer.zemris.java.nescume.messages.client.exceptions.UnableToRegister;
import hr.fer.zemris.java.nescume.messages.client.exceptions.MessageNotSent;

/**
 * Sučelje klijenta koji služi za spajanje na server i razmjenu poruka. Nakon inicijalizacije
 * klijenta potrebno ga je pokrenuti metodom start(), zatim se metodom register() klijent
 * registrira serveru i tek onda može slati poruke drugim klijentima i serveru.
 */
public interface IClientMsgService {

	/**
	 * Metoda pokreće klijenta i spaja se na server.
	 * @throws ClientCannotStart ako se servis ne može pokrenuti
	 */
    public void start() throws ClientCannotStart;

    /**
     * Metoda zaustavlja klijenta.
     */
    public void stop();
    
    /**
     * Metoda šalje poruku na server.
     * @param message opći oblik poruke
     * @throws MessageNotSent ako poruka nije poslana ili ju server nije primio
     * @throws ClientCrashed u slučaju da klijent završi neočekivano
     */
    public void send(Message message) throws MessageNotSent, ClientCrashed;

    /**
     * Metoda prima poruku sa servera. Ako poruka ne postoji, metoda čeka.
     * @return poruku namijenjenu ovom klijentu
     * @throws ClientCrashed u slučaju da klijent završi neočekivano
     */
    public Message receive() throws ClientCrashed;
    
    /**
     * Registrira klijenta i vraća njegovu adresu.
     * @throws UnableToRegister ako klijent nije prihvaćen
     * @throws ClientCrashed u slučaju da klijent završi neočekivano
     * @return adresa klijenta
     */
    public Address register() throws UnableToRegister, ClientCrashed;

//	Izmisliti pametne iznimke ili brisati ovo:
//    /**
//     * Pretplaćuje korisnika na jednu sljedeću poruku zadanog tipa.
//     * 
//     * @param type
//     *            Tip poruke na koju se korisnik pretplaćuje
//     */
//    public void query(MessageType type);
//
//    /**
//     * Pretplaćuje korisnika na jednu sljedeću poruku zadanog tipa i zadanog
//     * sadržaja. Korisnik će dobiti samo onu poruku koja se podudara s oba
//     * parametra.
//     * 
//     * @param type
//     *            Tip poruke na koju se korisnik pretplaćuje
//     * @param data
//     *            Sadržaj poruke na koju se korisnik pretplaćuje
//     */
//    public void query(MessageType type, byte[] data);
//
//    /**
//     * Pretplaćuje korisnika na više poruka zadanog tipa.<br />
//     * Broj poruka koje će klijent primiti određen je parametrom.
//     * 
//     * @param type
//     *            Tip poruke na koju se korisnik pretplaćuje
//     * @param count
//     *            Broj poruka na koje se klijent pretplaćuje.
//     */
//    public void query(MessageType type, int count);
//
//    /**
//     * Pretplaćuje korisnika na više poruka zadanog tipa i zadanog sadržaja.
//     * Korisnik će dobiti samo one poruke koje se podudaraju s oba parametra.<br />
//     * Broj poruka koje će klijent primiti određen je parametrom.
//     * 
//     * @param type
//     *            Tip poruke na koju se korisnik pretplaćuje
//     * @param data
//     *            Sadržaj poruke na koju se korisnik pretplaćuje
//     * @param count
//     *            Broj poruka na koje se klijent pretplaćuje.
//     */
//    public void query(MessageType type, byte[] data, int count);
}
