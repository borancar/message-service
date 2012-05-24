package hr.fer.zemris.java.nescume.messages.server;

import hr.fer.zemris.java.nescume.messages.server.exceptions.ServerCannotStart;
import hr.fer.zemris.java.nescume.messages.server.exceptions.ServerCrashed;

/**
 * Sučelje servera koji omogućuje komunikaciju klijenata i razmjenu poruka među njima.
 */
public interface IMessageServer {
	
    /**
     * Metoda pokreće centralni server za razmjenu poruka.
     * @throws ServerCannotStart u slučaju da se server ne može pokrenuti
     * @throws ServerCrashed u slučaju da se server sruši (završi neočekivano svoj rad)
     */
    public void start() throws ServerCannotStart, ServerCrashed;

    /**
     * Metoda zaustavlja server.
     */
    public void stop();
}
