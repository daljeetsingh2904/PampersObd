package com.pampers.PampersObd;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pampers.PampersObd.dao.DbHandler;
import com.pampers.PampersObd.model.ServiceParam;
import com.pampers.PampersObd.services.Constants;

@SpringBootApplication
@EnableJpaRepositories
public class PampersObdApplication implements CommandLineRunner {

	@Autowired
	DbHandler dbhandler;

	ServiceParam serviceparam;

	private static final Logger logger = Logger.getLogger(PampersObdApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PampersObdApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if ((args == null || args.length == 0)) {
			System.out.println("Failed to Start the Application as ServiceName cannot be Null");
			 logger.warn("Failed to Start the Application as ServiceName cannot be Null");
		} else {
			serviceparam = new ServiceParam();
			serviceparam.setServiceName(args[0]);
			System.out.println("Service started for service --->>> " + serviceparam.getServiceName());
			logger.info("Service started for service --->>> "+serviceparam.getServiceName());
//			serviceparam = configureServiceParam(serviceparam);
			invokeServiceMethod(serviceparam);
		}

	}

//	private ServiceParam configureServiceParam(ServiceParam serviceparam) {
//		String logPath = serviceparam.getServiceName() + "_log4j_obd_filepath";
//		String dbDriverName = serviceparam.getServiceName() + "_dbDriverName";
//		String dbHostName = serviceparam.getServiceName() + "_dbHostName";
//		String dbUserName = serviceparam.getServiceName() + "_dbUserName";
//		String dbPassword = serviceparam.getServiceName() + "_dbPassword";
//		
//		return serviceparam;
//	}

	private void invokeServiceMethod(ServiceParam serviceparam) throws Exception {
		Class<?> serviceClass = Class.forName("com.pampers.PampersObd.services."
				+ StringUtils.capitalize(serviceparam.getServiceName()) + "Application");
		Object serviceInstance = serviceClass.getDeclaredConstructor().newInstance();

		// Invoke "callInitiator" method using reflection
		Method method = serviceClass.getDeclaredMethod("callInitiator", ServiceParam.class, DbHandler.class);
		method.invoke(serviceInstance, serviceparam, dbhandler);

	}

}
