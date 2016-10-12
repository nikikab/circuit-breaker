package com.sap.niki.test.circuitbreaker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 
 * @author i030736
 * Each circuit breaker has a configurator which keeps the    	
 */
public class Configurator {
	
	public enum BreakType{
		TIMEOUT
	}

	public static Integer DEFAULT_BREAK_COUNT_DOWN = 3;
	public static Integer DEFAULT_TIMEOUT_SEC = 1;
	public static Long DEFAULT_CIRCUIT_OPEN_TIMEOUT_MILIS = 10*1000l;
	private Map<BreakType, Pair<Integer, Integer>> breakTypeCountDown = new HashMap<BreakType, Pair<Integer, Integer>>();
	private Long GlobalCircuitOpenTimeout;
	
	public Configurator(){
	}
	
	public static Configurator getDefaultConfig(){
		Configurator c = new Configurator();
		c.addBreakType(BreakType.TIMEOUT);
		c.setGlobalCircuitOpenTimeout(DEFAULT_CIRCUIT_OPEN_TIMEOUT_MILIS);
		return c;
	}

	
	public void addBreakType(BreakType type){
		addBreakType(type, DEFAULT_BREAK_COUNT_DOWN, DEFAULT_TIMEOUT_SEC);
	}
	
	public void addBreakType(BreakType type, Integer countdown, Integer timeoutSecs){
		breakTypeCountDown.put(type, new ImmutablePair<Integer, Integer>(countdown, timeoutSecs));
	}

	public boolean hasBreakType(BreakType type){
		return breakTypeCountDown.containsKey(type);
	}
	
	/**
	 * 
	 * @param type
	 * @return null in case this type is not suported
	 */
	public Integer getCountDown4Type(BreakType type) {
		Pair<Integer, Integer> p = breakTypeCountDown.get(type);
		return (p!=null) ? p.getLeft() : null;
		
	}
	
	public Integer getTimeOut4Type(BreakType type) {
		Pair<Integer, Integer> p = breakTypeCountDown.get(type);
		return p.getRight();
		
	}

	public Iterator<BreakType> getTypes(){
		return breakTypeCountDown.keySet().iterator();
	}

	public Long getGlobalCircuitOpenTimeout() {
		return GlobalCircuitOpenTimeout;
	}

	public void setGlobalCircuitOpenTimeout(Long globalCircuitOpenTimeout) {
		GlobalCircuitOpenTimeout = globalCircuitOpenTimeout;
	}
	
}

