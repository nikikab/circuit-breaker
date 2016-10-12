package com.sap.niki.test.circuitbreaker.junit;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import org.easymock.IAnswer;
import org.junit.Test;

import com.sap.niki.test.circuitbreaker.Caller;
import com.sap.niki.test.circuitbreaker.Configurator.BreakType;
import com.sap.niki.test.circuitbreaker.exc.CircuitTimeoutIsOpen;
import com.sap.niki.test.external.ExternalDummyService;

import static org.easymock.EasyMock.*;

public class TestCallerMetrix {


	@Test
	public void testLambdaExternalDelayMetrix() throws InterruptedException {
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
		c.setUseMetrix(true);
		

		for (int i = 0; i < 5; i++) {
				wrapCall(me, c, i);
				Thread.sleep(1000);
		}
		

	}

	private String wrapCall(String me, Caller<String, String> c, int i) throws InterruptedException {
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
				/*circuit is closed, so call external */
				assertTrue(e instanceof  TimeoutException);
			}else if (isClosedBeforeExecuteionState && ! c.isCircuitClose(BreakType.TIMEOUT)){
				/*this is the actual external call which is meant to opens up the circuit in case of timeout*/
				assertTrue(e instanceof  TimeoutException);
			}else {
				/* circuit is open, hence no external calls*/
				assertTrue( e instanceof CircuitTimeoutIsOpen );
			}
		}
		System.out.println( "\n==================================================================================");
		return call;
	}

	private String mockedExternalCall(String input, long delayMilis) throws InterruptedException {
		System.out.println("External method executed ....");
		Thread.sleep(delayMilis);
		return new ExternalDummyService().helloWorld(input);	
	}
	

}
