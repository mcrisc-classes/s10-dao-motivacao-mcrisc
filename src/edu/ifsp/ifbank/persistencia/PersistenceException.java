package edu.ifsp.ifbank.persistencia;

public class PersistenceException extends Exception {

	public PersistenceException(Throwable cause) {
		super(cause);
	}
	
	public PersistenceException(String message) {
		super(message);
	}
}
