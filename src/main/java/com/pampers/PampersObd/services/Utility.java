/**
 * 
 */
package com.pampers.PampersObd.services;

	import java.nio.charset.StandardCharsets;
	import java.security.spec.KeySpec;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pampers.PampersObd.model.ServiceParam;


/**
 * @author daljeetsingh
 *
 */
public class Utility {
	private static final Logger logger = Logger.getLogger(Utility.class);
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	private Utility() {		
	}
	
	/**
   	 * Check Value is Blank or Not and Assign a Default Value
   	 * @ServiceParam 
   	 * @data
   	 * @return
   	 */
	
	public static String verifyData(ServiceParam serviceparam,String data){
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		try{
			if(data!=null && !data.equalsIgnoreCase("")){
				data=data.trim();
			}else{
				data="NA";
			}			
		}catch(Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
		return data;	
	}
	
	/**
   	 * Returns Current Date and Time 
   	 * @ServiceParam 
   	 * @return
   	 */
	
	public static LocalDateTime getCurrentDatetime(ServiceParam serviceparam){
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		LocalDateTime currentDateTime=null;
		try{
			currentDateTime=java.time.LocalDateTime.now();			
		}catch(Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
		return currentDateTime;
	}
	
	/**
   	 * Returns First Date of Current Month
   	 * @ServiceParam  
   	 * @return
   	 */
	
	public static LocalDate getFirstDateOfMonth(ServiceParam serviceparam) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		LocalDate todaydate=null;
		try {
			todaydate = LocalDate.now();
			todaydate=todaydate.withDayOfMonth(1);			
		}catch(Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
		return todaydate;
	}
	
	/**
   	 * Encrypt AES 256 the String 
   	 * @ServiceParam 
   	 * @strToEncrypt
   	 * @return
   	 */
	
	public static String encrypt(ServiceParam serviceparam,String strToEncrypt){
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);		
	    try{
	        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	        IvParameterSpec ivspec = new IvParameterSpec(iv);
	         
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	        KeySpec spec = new PBEKeySpec(serviceparam.getSecretKey().toCharArray(), serviceparam.getSalt().getBytes(), 65536, 256);
	        SecretKey tmp = factory.generateSecret(spec);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
	         
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);	       
	        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
	    }catch(Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
	    return null;
	}
	
	/**
   	 * Decrypt the String 
   	 * @ServiceParam 
   	 * @strToDecrypt
   	 * @return
   	 */
	
	public static String decrypt(ServiceParam serviceparam,String strToDecrypt) {
		//PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
	    try{
	        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	        IvParameterSpec ivspec = new IvParameterSpec(iv);
	         
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	        KeySpec spec = new PBEKeySpec(serviceparam.getSecretKey().toCharArray(), serviceparam.getSalt().getBytes(), 65536, 256);
	        SecretKey tmp = factory.generateSecret(spec);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
	        
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
	        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
	    }catch(Exception exception) {
			//logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
	    return null;
	}
	
	
}