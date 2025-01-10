/**
 * 
 */
package com.pampers.PampersObd.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

/**
 * @author vinay.sethi
 *
 */
public class EncryptionRepository {
	private static final Logger logger = Logger.getLogger(EncryptionRepository.class);	
	private static EncryptionRepository mInstance = null;
    private static Map<String,String> mRepository=new HashMap<>();

    private EncryptionRepository() {
    }

    public static synchronized EncryptionRepository getInstance() {                
        if (mInstance == null) {
            mInstance = new EncryptionRepository();
            makeRepository();
        }
        return mInstance;
    }

    private static void makeRepository() {
    	String [] secretArr={"p@mp$rS@05062020","h@h)(032020","!st0re)(032020","WS)(032020","sm$GeN@06072020","l@thc@13072020","rurb@n@30102020"};
    	String [] saltArr={"png@ppl!210620@)","png@ppl!c@tions","png@ppl!c@tions","png@ppl!c@tions","sMs@ppl!)%)620@)","png@l@thc!#)7","png@rurb@n!#)7"};
        try{	
        	for(int i=0;i<secretArr.length;i++) {
        		mRepository.put((i+1)+"SECRET", secretArr[i]);
        		mRepository.put((i+1)+"SALT", saltArr[i]);
        	}        	
            logger.info("Encryption Repository is Successfully Made");     
        }catch(Exception exception) {
			logger.error("Erros in Establishing AMI Connection-->"+exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
    }
    
    public static void reLoadRepository() {
    	mRepository = new HashMap<>();
        makeRepository();
    }
    
    public String getValue(String key){
        return mRepository.getOrDefault(key, "0");
    }
}