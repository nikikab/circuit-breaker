package com.sap.niki.test.circuitbreaker;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.sap.niki.test.circuitbreaker.Configurator.BreakType;
import com.sap.niki.test.circuitbreaker.exc.CircuitTimeoutIsOpen;

public class Caller <T, Y> {
	
	static final MetricRegistry metrics = new MetricRegistry();
	private ConsoleReporter reporter = null;
	
	private Map<Configurator.BreakType, Integer> circuitActualTypesCountDown = new HashMap<Configurator.BreakType, Integer>();
	private Map<Configurator.BreakType, Long> circuitOpenStartTyme = new HashMap<Configurator.BreakType, Long>();
	private Map<Configurator.BreakType, Long> circuitHalfOpenStartTyme = new HashMap<Configurator.BreakType, Long>();
	
	private boolean useMetrix = false;
	

	private String name;
	private Configurator config;
	CircuitBreakerCallable<T, Y> caller;
	
	
	public Caller(String name, CircuitBreakerCallable<T, Y> caller){
		setName(name);
		setConfig(Configurator.getDefaultConfig());
		this.caller= caller;
		initStates();
	}


	private void initStates() {
		Iterator<BreakType> types = getConfig().getTypes();
		while (types.hasNext()){
			BreakType next = types.next();
			if (next != null){
				circuitActualTypesCountDown.put(next, config.getCountDown4Type(next));
				/*meaning the circuit is closed */
				circuitOpenStartTyme.put(next, 0l);
				circuitHalfOpenStartTyme.put(next, 0l);
			}
		}
	}
	
	
	/**
	 * This method implements the real circuitBreaker logic by 
	 * - Executing the callable generic method if and only if the circuit is closed. 
	 * - Open it up based on the configurator 
	 * 
	 * @param y
	 * @return
	 */
	public T call (Y y)throws TimeoutException{
		
		startMetrixReport("call circuitbreaker");
		
		T result = null;
		ExecutorService executor = Executors.newFixedThreadPool(1);

		circuitUpdate(BreakType.TIMEOUT);
		if (isCircuitOpen( BreakType.TIMEOUT ) ){
			throw new CircuitTimeoutIsOpen();
		}

		
		Future<T> future = 
				executor.submit(new Callable<T>() {
					@Override
					public T call(){
						System.out.println("inside caller: execute external ...");
						startMetrixReport("call external");

						return caller.call(y);
					}

				});


		try {
			 result = future.get(config.getTimeOut4Type(BreakType.TIMEOUT), TimeUnit.SECONDS);
			 circuitClose(BreakType.TIMEOUT);
		} catch (TimeoutException e) {
			openingCircuit( BreakType.TIMEOUT );
			throw e;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}		
		
		return result;
	
	}



	public String getState(BreakType type) {
		StringBuilder state = new StringBuilder();
		
		state
			.append( String.format("\n\tIs circuit close: %s", String.valueOf( isCircuitClose(type) ) ) )
			.append( 
					String.format(
							"\n\tIs circuit open: %s, \n\tcountdown: %s, \n\tleft timeout in milisec: %s", 
							String.valueOf( isCircuitOpen(type) ), 
							String.valueOf( getActualCountDown(BreakType.TIMEOUT) ),
							String.valueOf( config.getGlobalCircuitOpenTimeout() -  (getNowGMTMilis() - circuitOpenStartTyme.get(type)) )
							) 
					)
			.append( 
					String.format(
							"\n\tIs circuit half open: %s\n", 
							String.valueOf( isCircuitHalfOpen(type) )
							) 
					)
			;
			
		
		
		return state.toString();
	}


	private Integer getActualCountDown(BreakType type) {
		return circuitActualTypesCountDown.get( type );
	}


	public boolean isCircuitOpen(BreakType type) {
		return (circuitOpenStartTyme.get(type) != 0);
	}


	public boolean isCircuitClose(BreakType type) {
		return (circuitOpenStartTyme.get(type) == 0);
	}


	private void circuitClose(BreakType timeout) {
		circuitOpenStartTyme.put(BreakType.TIMEOUT, 0l);
		circuitHalfOpenStartTyme.put(BreakType.TIMEOUT, 0l);
		circuitActualTypesCountDown.put(BreakType.TIMEOUT, config.getCountDown4Type(BreakType.TIMEOUT));
	}


	private void openingCircuit(BreakType type) {
		Integer countdown = getActualCountDown(type);		
		if (countdown == 1){
			circuitOpenStartTyme.put(type, getNowGMTMilis());
			circuitHalfOpenStartTyme.put(type, 0l);
		}
		circuitActualTypesCountDown.put(
				type, 
				(countdown > 0) ? --countdown: 0);
		
	}


	private long getNowGMTMilis() {
		return new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTimeInMillis();
	}

	public boolean isCircuitHalfOpen(BreakType type) {
		return (circuitHalfOpenStartTyme.get(type) != 0);
	}

	private void circuitUpdate(BreakType type ) {
		Long now = getNowGMTMilis();
//		System.out.println("-->now = " + now);
//		System.out.println("-->circuitOpenStartTyme.get(type) = " + circuitOpenStartTyme.get(type));
//		System.out.println("--> now - = " + (now - (Long)config.getGlobalCircuitOpenTimeout()) );
//		System.out.println("-->config.getGlobalCircuitOpenTimeout()" + config.getGlobalCircuitOpenTimeout());
		
		if ( 	getActualCountDown(type) == 0 
				&& 
				now - (Long)config.getGlobalCircuitOpenTimeout() >= (Long)circuitOpenStartTyme.get(type)
				){
			circuitClose(type);
			
		}
	}


	public Configurator getConfig() {
		return config;
	}

	public void setConfig(Configurator config) {
		this.config = config;
//		this.config.
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public boolean isUseMetrix() {
		return useMetrix;
	}
	
	
	public void setUseMetrix(boolean useMetrix) {
		this.useMetrix = useMetrix;
	}

	/*
	 * ============================
	 * 			Metrics
	 * ============================ 
	 */
	private void startMetrixReport(String s) {
		if (!useMetrix){
			return;
		}
		
		startReport();
		Meter requests = metrics.meter(s);
		requests.mark();
	}
	private void startReport() {
		reporter = ConsoleReporter.forRegistry(metrics)
	          .convertRatesTo(TimeUnit.SECONDS)
	          .convertDurationsTo(TimeUnit.MILLISECONDS)
	          .build();
	    reporter.start(1, TimeUnit.SECONDS);
	}
	
	public void stopReporter(){
		reporter.stop();
	}


}
