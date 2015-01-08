package com.ip.notifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class StringCryptor {

	private static String SECURITY_KEY = "NO ONE DISCOVER";
	
	private Cipher deCipher;
	private Cipher enCipher;
	private SecretKeySpec key;
	private IvParameterSpec ivSpec;
	private byte[] ivBytes = { 3, 1, 4, 1, 5, 9, 2, 6 };
	private byte[] keyBytes = SECURITY_KEY.getBytes();

	public StringCryptor() throws Exception {
		// wrap key data in Key/IV specs to pass to cipher
		ivSpec = new IvParameterSpec(ivBytes);
		// create the cipher with the algorithm you choose
		// see javadoc for Cipher class for more info, e.g.
		DESKeySpec dkey = new DESKeySpec(keyBytes);
		key = new SecretKeySpec(dkey.getKey(), "DES");
		deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
	}

	public String encrypt(String string) throws Exception {
		byte[] input = convertToByteArray(string);
		enCipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
		return Base64.encode(enCipher.doFinal(input));
	}

	public String decrypt(String string) throws Exception {
		deCipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		return convertFromByteArray(deCipher.doFinal(
				Base64.decode(string.getBytes(StandardCharsets.UTF_8))));
	}

	private String convertFromByteArray(byte[] array) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(array);
		ObjectInputStream in = new ObjectInputStream(bais);
		Object object = in.readObject();
		in.close();
		return object.toString();
	}

	private byte[] convertToByteArray(String string) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(string);
		out.close();
		return baos.toByteArray();
	}

	private static void usage(){
		System.out.println("This is utility for the encryption and decription of security key.");
		System.out.println("Please run following command:");
		System.out.println("	java com.ip.notifier.StringCryptor <command> <text>");
		System.out.println("	-e - encrypt text");
		System.out.println("	-d - decrypt text");
	}
	
	public static void main(String[] args) {
		usage();		
		StringCryptor cryptor;
		try {
			cryptor = new StringCryptor();
			if((args.length == 2) && 
				(args[0] != null) && 
				(args[1] != null)){
				if("-e".equals(args[0])){
					System.out.println( 
							"Input:" + args[1] + "\nOutput:" + cryptor.encrypt(args[1])); 
				}else if("-d".equals(args[0])){
					System.out.println( 
							"Input:" + args[1] + "\nOutput:" + cryptor.decrypt(args[1]));
				}
			}else{
				System.out.println("Incorrect command.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}