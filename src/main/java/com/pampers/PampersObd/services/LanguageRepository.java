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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pampers.PampersObd.dao.DbHandler;
import com.pampers.PampersObd.model.ServiceParam;

/**
 * 
 * @author daljeetsingh
 *
 */

@Service
public class LanguageRepository {
	private static final Logger logger = Logger.getLogger(LanguageRepository.class);	
//	private static LanguageRepository mInstance = null;
    private static Map<Integer,String> mRepository=new HashMap<>();
    
    @Autowired
    DbHandler dbHandler;

    private LanguageRepository() {
    }

//    public synchronized LanguageRepository getInstance(ServiceParam serviceparam) {                
//        if (mInstance == null) {
//            mInstance = new LanguageRepository();
//            makeRepository(serviceparam);
//        }
//        return mInstance;
//    }

    public void makeRepository(ServiceParam serviceparam) {
        try{	
			mRepository = dbHandler.fetchLanguages(serviceparam);		
            logger.info("Language Repository is Successfully Made");     
        }catch(Exception exception) {
			logger.error("Erros in Establishing AMI Connection-->"+exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
    }
    
    public void reLoadRepository(ServiceParam serviceparam) {
    	mRepository = new HashMap<>();
        makeRepository(serviceparam);
    }
    
    public String getValue(int key){
        return mRepository.getOrDefault(key, "0");
    }
}