package com.sap.niki.test.external;

public class ExternalDummyService {

	public String helloWorld(String name){
		return String.format("Hello, %s!", name);
	}
}
