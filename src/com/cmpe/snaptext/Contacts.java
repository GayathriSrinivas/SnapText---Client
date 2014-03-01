package com.cmpe.snaptext;

public class Contacts {
	
	private String name ; 
	private String phoneNumber;
	
	public void putName(String name) {
		this.name = name;
	}
	
	public void putPhoneNumber(String phoneNumber){
		this.phoneNumber = phoneNumber;
	}
	
	public String getName() {
		return this.name;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}
	
}
