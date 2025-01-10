package com.pampers.PampersObd.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 
 * A @Component or @Configuration class can initialize the LOG_PATH field during
 * Spring's context setup phase
 *
 */
@Component
public class LogPathConstant {

	@Value("${pampers.log4j.obd.filepath}")
	private String logPath; // Instance-level field to receive the value from application.properties

	public static String LOG_PATH; // Static field for global access

	// @PostConstruct is called after the bean is fully initialized, and the static
	// field LOG_PATH is set with the injected value.
	@PostConstruct
	public void init() {
		LOG_PATH = logPath; // Assign the injected value to the static field
	}
}
