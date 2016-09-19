package com.sap.niki.test.circuitbreaker.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.sap.niki.test.circuitbreaker.Configurator;
import com.sap.niki.test.circuitbreaker.Configurator.BreakType;

public class TestConfigurator {

	@Test
	public void testAddBreakType() {
		Configurator c = Configurator.getDefaultConfig();
		assertTrue( c.hasBreakType(BreakType.TIMEOUT) );
		assertEquals(Configurator.DEFAULT_BREAK_COUNT_DOWN, c.getCountDown4Type(BreakType.TIMEOUT));
		assertNull(c.getCountDown4Type(BreakType.CONNECTION_REFUSED));
	}
	
	@Test
	public void testAddBreakTypeWithCountdown() {
		Configurator c = Configurator.getDefaultConfig();
		c.addBreakType(BreakType.CONNECTION_REFUSED, 5, 1);
		assertTrue( c.hasBreakType(BreakType.CONNECTION_REFUSED) );
		assertEquals(new Integer(5), c.getCountDown4Type(BreakType.CONNECTION_REFUSED));
	}

	
	

}
