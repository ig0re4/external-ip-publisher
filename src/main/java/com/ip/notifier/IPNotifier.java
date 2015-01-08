package com.ip.notifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.EventLog;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.ServiceException;

import sun.net.util.IPAddressUtil;

@SuppressWarnings("restriction")
public class IPNotifier extends AbstractService implements TimerServiceListener {
	
	private static final String SERVER_NEW_IP_ADDRESS = "Server new IP Address";
	private static final String EXTERNAL_IP_FILE = "external-ip.txt";
	private int pingCount = 0;
	private ConfigurationProperties configurationProperties;
	private String oldIpAddress;
	private Object isRunning = new Object();
	private MimeMessage emailMessage;
	private int failures = 0;
	
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private Calendar cal = Calendar.getInstance();
	
	
	/**
	 * Constructor
	 */
	public IPNotifier() {
		super();
		try {
			configurationProperties = new ConfigurationProperties();
			oldIpAddress = readFromStorage();
			Log.info("Received ip address " + oldIpAddress + " from " + EXTERNAL_IP_FILE);
			Session session = Session.getDefaultInstance(configurationProperties,
					new MyAuthenticator());
			emailMessage = new MimeMessage(session);
			emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(
					configurationProperties.getMailTo()));
			new TimerService().registerForRepetetive(this, 0, configurationProperties.getCheckIpDelay());
		} catch (Exception e) {
			Log.error("Exception occures - " + e.toString());
			shutdown();
		}
	}
	
	@Override
	public int serviceMain(String[] args) throws ServiceException {
		try {
			while (!shutdown){
				synchronized (isRunning) {                    
					isRunning.wait();
				}
			}
		} catch (InterruptedException e) {
			Log.error("Exception occures - " + e.toString());
			shutdown();
		}
		return 0;
	}

	@Override
	public void timerNotify() {
		try {
			checkExternalIpAddress();
			if((++ pingCount) % 6 == 0) {
				EventLog.report("IPNotification Service", 4, "Ping:" + oldIpAddress);
			}
			failures  = 0;
		} catch (Exception e) {
			Log.error("Exception occures - " + e.toString());
			if(configurationProperties.isShutdownOnException()){
				shutdown();
			}else{
				//check if the service not shuted down already.
				if(!shutdown){
					Log.info("Since ShutdownOnException=false, keep on trying.");
					//check if we reach fail retries amount
					if(++failures > configurationProperties.getRetries()){
						Log.info("Reached retry amount " + configurationProperties.getRetries() + 
								", will shutdown shortly.");
						shutdown();
					}else{
						Log.info("Failures = " + failures);
					}
				}
			}
		}
	}
	
	@Override
	public int serviceRequest(int control) throws ServiceException {
		Log.info("Received " + ServiceControlEnum.getDescription(control));
        switch (control) {
        case SERVICE_CONTROL_STOP:
        case SERVICE_CONTROL_SHUTDOWN:
            shutdown();
            break;
        default:
            break;
        }
        return 0;
    }
	
	/**
	 * notifies shutdown process
	 */
	private void shutdown(){
		Log.info("Shuting Down the IPNotifier");
		shutdown = true;
		synchronized (isRunning) {
			isRunning.notify();
        }
	}
	
	/**
	 * Check the external ip address, if the address is different 
	 * then the old one, then sends the mail
	 * @param configurationLoader
	 * @param ipAddress
	 * @throws IOException
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private void checkExternalIpAddress()
			throws IOException, AddressException, MessagingException {
		String newIpAddress = whatIsMyIp(configurationProperties.whatIsMyIPUrl(), 
				configurationProperties.getUserAgent());
		Log.info("Received ip address " + newIpAddress + " from the " + 
				configurationProperties.whatIsMyIPUrl());
		Log.info("Current time is:" + dateFormat.format(cal.getTime()));
		if(newIpAddress == null){
			Log.info("The ip address is null probably url is down - will shutdown the service.");
			shutdown();
		}else if (!newIpAddress.equals(oldIpAddress)){
			Log.info("The New Ip Address " + newIpAddress + 
					" different then old one - " + oldIpAddress + 
					".Shall save the ip address to the " + EXTERNAL_IP_FILE);
			updateStorage(newIpAddress);
			oldIpAddress = newIpAddress;
			sendEmail(SERVER_NEW_IP_ADDRESS, newIpAddress);
		}else {
			Log.info("The Ip Address is same as the old one - " + newIpAddress);
			if(configurationProperties.getSendAnyWay()){
				Log.info("Sending it anyway");
				sendEmail(SERVER_NEW_IP_ADDRESS, newIpAddress);
			}
		}
	}
	
	/**
	 * 
	 * @param whatIsMyIpUrl
	 * @param userAgent
	 * @return
	 * @throws IOException
	 */
	private String whatIsMyIp(URL whatIsMyIpUrl, String userAgent)
			throws IOException {
		String ip = null;
		HttpURLConnection connection = (HttpURLConnection)whatIsMyIpUrl
				.openConnection();
		connection.setRequestProperty("User-Agent", userAgent);
		connection.setRequestMethod("GET");
		connection.connect();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while (in != null) {
			String line = in.readLine();
			if ((line != null) && (!line.isEmpty())	&& 
					((IPAddressUtil.isIPv4LiteralAddress(line)) || 
					(IPAddressUtil.isIPv6LiteralAddress(line)))) {
				ip = line;
				break;
			}
		}
		return ip;
	}
	
	/**
	 * 
	 * @param newIpAddress
	 * @throws IOException
	 */
	private void updateStorage(String newIpAddress) throws IOException{
		File file = new File(EXTERNAL_IP_FILE);
		if(file.exists()){
			file.delete();
		}
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(EXTERNAL_IP_FILE), "utf-8"));
			writer.write(newIpAddress);
		} catch (IOException iox) {
			Log.error("Exception happen then writing to the " + EXTERNAL_IP_FILE);
			throw iox;
		} finally{
			try {
				if(writer != null){
					writer.close();
				}
			} catch (IOException iox) {
				Log.error("Exception happen then closing " + EXTERNAL_IP_FILE);
				throw iox;
			}	
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private String readFromStorage() throws IOException{
		String ipAddress = null;
		File file = new File(EXTERNAL_IP_FILE);
		if(file.exists()){
			BufferedReader reader = null;
			try{
				reader = new BufferedReader(new FileReader(EXTERNAL_IP_FILE));
				ipAddress = reader.readLine();
			}catch (IOException iox) {
				Log.error("Exception happen then reading from the " + EXTERNAL_IP_FILE);
				throw iox;
			}finally{
				try {
					if(reader != null){
						reader.close();
					}
				} catch (IOException iox) {
					Log.error("Exception happen then closing " + EXTERNAL_IP_FILE);
					throw iox;
				}
			}
		}
	    return ipAddress;
	}
	
	/**
	 * 
	 * @param configurationLoader
	 * @param aSubject
	 * @param aBody
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmail(String aSubject, String aBody) 
			throws AddressException, MessagingException {
		Log.info("Sending mail " + aBody + " to " + configurationProperties.getMailTo());
		emailMessage.setSubject(aSubject);
		emailMessage.setText(aBody);
		Transport.send(emailMessage);
	}
	
	public static void main(String[] args) {
		
	}
	
	private class MyAuthenticator extends Authenticator {
		private PasswordAuthentication authentication;
		public MyAuthenticator() {
			authentication = new PasswordAuthentication(
					configurationProperties.getUsername(),
					configurationProperties.getPassword());
		}
		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}
}