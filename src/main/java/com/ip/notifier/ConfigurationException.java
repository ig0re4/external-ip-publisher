package com.ip.notifier;

@SuppressWarnings("serial")
public class ConfigurationException extends Exception {
	
	private StringBuffer message = new StringBuffer("");
	
	public ConfigurationException() {
		super();
	}

	public ConfigurationException(String string, Exception e) {
		message.append(string).append("\n").append(e.toString());
	}

	public ConfigurationException(String string) {
		message.append(string);
	}
	
	@Override
	public String toString() {
		return message.toString();
	}
}
