package com.sap.niki.test.circuitbreaker.junit;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import org.easymock.IAnswer;
import org.junit.Test;

import com.google.common.base.Function;
import com.sap.niki.test.circuitbreaker.Caller;
import com.sap.niki.test.circuitbreaker.Configurator.BreakType;
import com.sap.niki.test.circuitbreaker.exc.CircuitTimeoutIsOpen;
import com.sap.niki.test.external.ExternalDummyService;

import static org.easymock.EasyMock.*;

public class TestCaller {

	public boolean externalServiceCalled = false;
	@Test
	public void test() throws TimeoutException {
//		Caller<String, Integer> c = new Caller<String, Integer>("CircuitBreaker caller 1", new CircuitBreakerCallable<String, Integer>() {
//			@Override
//			public String call(Integer input) {
//				return input.toString();
//			};
//		});
		Caller<Integer, String> c = new Caller<Integer, String>("CircuitBreaker caller 1", new Function<Integer, String>() {
			@Override
			public String apply(Integer input) {
				return input.toString();
			};
		});

		int y = 30;
		assertEquals(new Integer(y).toString(), c.call(y));
	}

	@Test
	public void testLambda() throws TimeoutException {
		Caller<Integer, String> c = new Caller<Integer, String>("CircuitBreaker caller 2",  (Integer input) -> {return input.toString();} );
		int y = 30;
		assertEquals(new Integer(y).toString(), c.call(y));
	}

	@Test
	public void testLambdaExternal() throws TimeoutException {
		Caller<String, String> c = new Caller<String, String>("CircuitBreaker caller 3",  (String input) -> {return new ExternalDummyService().helloWorld(input);} );
		String me = "Niki1";
		
		System.out.println( c.call(me) );
		assertEquals(new ExternalDummyService().helloWorld(me), c.call(me));
	}
	

	
	@Test
	public void testLambdaExternalDelay() throws InterruptedException {
		String me = "Niki2";
		ExternalDummyService mockerExternal = createMock(ExternalDummyService.class);
		expect( mockerExternal.helloWorld(me) )
			.andAnswer(
					new IAnswer<String>() {
						@Override
						public String answer() throws InterruptedException{
							return mockedExternalCall(me, 5*1000);
						}
					})
			.anyTimes();
		replay(mockerExternal);
		Caller<String, String> c = new Caller<String, String>("CircuitBreaker caller 4",  (String input) -> {return mockerExternal.helloWorld(input);} );
		c.setUseMetrix(false);

		for (int i = 0; i < 20; i++) {
				wrapCall(me, c, i);
				Thread.sleep(1000);
		}

	}


	private String wrapCall(String me, Caller<String, String> c, int i) throws InterruptedException {
		externalServiceCalled = false;
		System.out.println( "\n==================================================================================");
		System.out.println("Execution" + i);
		System.out.println( String.format("Current circuit breaker TIMEOUT state: %s", c.getState(BreakType.TIMEOUT) ) );
		String call = null;
		boolean isClosedBeforeExecuteionState = c.isCircuitClose(BreakType.TIMEOUT);
		try {
			call = c.call(me);
			/*mocked calls shall  always fail with timeout according to the default circuit breaker config */
			System.out.println("Got Result: " + call);
			assertTrue( call == null || new ExternalDummyService().helloWorld(me).equals( call ) );
		} catch (TimeoutException e) {
			System.out.println("------TimeOut---------");
			System.out.println( String.format("Current circuit breaker TIMEOUT state: %s", c.getState(BreakType.TIMEOUT) ) );
			if (c.isCircuitClose(BreakType.TIMEOUT)){
				assertTrue(e instanceof  TimeoutException);
			}else if (isClosedBeforeExecuteionState && ! c.isCircuitClose(BreakType.TIMEOUT)){
				assertTrue(e instanceof  TimeoutException);
			}else {
				assertTrue( e instanceof CircuitTimeoutIsOpen );
			}
		}
		System.out.println( "\n==================================================================================");
		return call;
	}

	private String mockedExternalCall(String input, long delayMilis) throws InterruptedException {
		System.out.println("External method executed ....");
		externalServiceCalled = true;
		Thread.sleep(delayMilis);
		return new ExternalDummyService().helloWorld(input);	
	}
	

}
