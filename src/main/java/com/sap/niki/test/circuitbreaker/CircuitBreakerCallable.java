package com.sap.niki.test.circuitbreaker;

public interface CircuitBreakerCallable<T, Y> {
	public T call(Y input);
}
