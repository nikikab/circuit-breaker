package com.sap.niki.test.circuitbreaker;

/**
 * 
 * @author i030736
 *
 * @param <T> type of the result 
 * @param <Y> type of the input parameter
 */
public interface CircuitBreakerCallable<T, Y> {
	public T call(Y input);
}
