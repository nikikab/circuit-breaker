package com.sap.niki.test.circuitbreaker.exc;

import java.util.concurrent.TimeoutException;

public class CircuitTimeoutIsOpen extends TimeoutException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8832504516414914069L;

	public CircuitTimeoutIsOpen(){
		super("CirctuitBreaker says: Circuit is open for timeout");
	}
}
