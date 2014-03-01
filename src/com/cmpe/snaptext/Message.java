package com.cmpe.snaptext;

public class Message {
	
	public String phoneNumber;
	public boolean received;
	public String message;
	
	public Message(String phoneNumber, boolean received, String message) {
		this.phoneNumber = phoneNumber;
		this.received = received;
		this.message = message;
	} 

}
