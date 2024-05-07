package pt.ulisboa.tecnico.tuplespaces.client.exception;


public class ServerEntryNotFound extends Exception {
    public ServerEntryNotFound(String serverQualifier) {
        super("Server with qualifier " + serverQualifier + " isn't responding.");
    }
}