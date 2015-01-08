package com.ip.notifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@SuppressWarnings("serial")
public class ConfigurationProperties extends Properties {

	private static final String CONFIG_FILE = "ip-notifier.ini";

	private URL whatIsMyIPUrl;
	private String userAgent;
	private long checkIpDelay;
	private String username;
	private String password;
	private String mailTo;
	private boolean sendAnyWay;
	private boolean shutdownOnException;
	private int failRetries;
	private StringCryptor cryptor;

	/**
	 * 
	 * @throws ConfigurationException
	 * @throws NumberFormatException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 */
	public ConfigurationProperties() throws Exception {
		fetchConfig();
		cryptor = new StringCryptor();
		whatIsMyIPUrl = new URL(getMyProperty("what.is.my.ip"));
		userAgent = getMyProperty("user.agent");
		checkIpDelay = Long.parseLong(getMyProperty("check.ip.delay"));
		username = getMyProperty("mail.smtp.user");
		password = getMyEncryptProperty("mail.smtp.password");
		mailTo = getMyProperty("mail.to");
		sendAnyWay = Boolean.parseBoolean(getMyProperty("send.any.way"));
		shutdownOnException = Boolean
				.parseBoolean(getMyProperty("shutdown.on.exception"));
		failRetries = Integer.parseInt(getMyProperty("fail.retries"));
	}

	private void fetchConfig() throws ConfigurationException {
		InputStream input = null;
		try {
			input = new FileInputStream(CONFIG_FILE);
			load(input);
		} catch (IOException ex1) {
			throw new ConfigurationException("Cannot open and load "
					+ CONFIG_FILE, ex1);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				throw new ConfigurationException("Cannot close " + CONFIG_FILE,
						ex);
			}
		}
	}

	private String getMyProperty(String key) throws ConfigurationException {
		String value = super.getProperty(key);
		if (value == null) {
			throw new ConfigurationException("Missing property " + key
					+ " in config file " + CONFIG_FILE);
		}
		return value;
	}

	private String getMyEncryptProperty(String key) throws Exception {
		return cryptor.decrypt(getMyProperty(key));
	}

	public URL whatIsMyIPUrl() {
		return whatIsMyIPUrl;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public long getCheckIpDelay() {
		return checkIpDelay;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the mailTo
	 */
	public String getMailTo() {
		return mailTo;
	}

	/**
	 * @return the sendAnyWay
	 */
	public boolean getSendAnyWay() {
		return sendAnyWay;
	}

	/**
	 * @return the shutdownOnException
	 */
	public boolean isShutdownOnException() {
		return shutdownOnException;
	}

	public int getRetries() {
		return failRetries;
	}
	
	public static void main(String[] args) throws Exception {
		new ConfigurationProperties();
	}
}
