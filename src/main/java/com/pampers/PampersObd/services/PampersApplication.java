package com.pampers.PampersObd.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pampers.PampersObd.dao.DbHandler;
import com.pampers.PampersObd.model.OutboundCallParameters;
import com.pampers.PampersObd.model.ServiceParam;

@Service
public class PampersApplication {

	@Autowired
	DbHandler dbhandler;

	@Autowired
	JdbcTemplate jdbcTemplate;

//	OutboundCallParameters callingData;	

	private static final Logger logger = Logger.getLogger(PampersApplication.class);

	public void callInitiator(ServiceParam serviceparam, DbHandler dbhandler) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		List<OutboundCallParameters> recordsArr;
		List<String> obdChannelData = new ArrayList<>();
		String amiCallingResponse = null;
		String outboundParameters;
		while (true) {
			try {
				serviceparam = dbhandler.fetchConfiguration(serviceparam);
				obdChannelData = dbhandler.fetchCallingChannels(serviceparam, dbhandler);
				if (obdChannelData.isEmpty()) {
					logger.debug("All Channels for Outbound is Busy or No Channel is Configured for OBD");
				} else {
					recordsArr = dbhandler.fetchCallingNumbers(serviceparam, obdChannelData);
					if (recordsArr.isEmpty()) {
						logger.debug("No Numbers are Available for Calling for " + serviceparam.getServiceName()
								+ " so Waiting for " + serviceparam.getObdThreadSleepTime() + " milliseconds");
						Thread.sleep(serviceparam.getObdThreadSleepTime());
					} else {
						for (OutboundCallParameters data : recordsArr) {
							outboundParameters = serviceparam.getObdCallParameters()
									.replace("%MISSCALLID", Integer.toString(data.getMisscallId()))
									.replace("%MOBILEID", Integer.toString(data.getMobileNumberId()))
									.replace("%PROJECTID", Integer.toString(data.getProjectId()))
									.replace("%RETRYCOUNT", Integer.toString(data.getRetryCount()))
									.replace("%SITEID", Integer.toString(data.getSiteId()))
									.replace("%LANGUAGENAME", data.getLanguageName())
									.replace("%FLOWTYPE", serviceparam.getFlowType())
									.replace("%OBDCHANNELID", Integer.toString(data.getObdChannelId()))
									.replace("%CALLSENDTIME", Utility.getCurrentDatetime(serviceparam).toString()
											.replace("T", " ").split("\\.")[0]);
							logger.debug("Initiated Call for Service " + serviceparam.getServiceName()
									+ " for MissCallId-->" + data.getMisscallId() + "-->" + outboundParameters);
							amiCallingResponse = OutboundSender.getInstance(serviceparam).sendCall(serviceparam,
									data.getObdChannelName(), data.getMobileNumber(), outboundParameters);
							if (amiCallingResponse.startsWith("Success")) {
								logger.info("Final Status Update Request Initiated for Service "
										+ serviceparam.getServiceName() + " for MissCallId-->" + data.getMisscallId()
										+ "--> and obdChannelID-->" + data.getObdChannelId());
								dbhandler.updateCallStatus(serviceparam, data.getMobileNumberId(), data.getMisscallId(),
										Constants.STATUS_FINAL, amiCallingResponse);
								dbhandler.updateChannelStatus(serviceparam, data.getMisscallId(),
										data.getObdChannelId(), Constants.CHANNELBUSYSTATUS);
							} else {
								logger.info("Error Status Update Request Initiated Service "
										+ serviceparam.getServiceName() + " for MissCallId-->" + data.getMisscallId());
								dbhandler.updateCallStatus(serviceparam, data.getMobileNumberId(), data.getMisscallId(),
										Constants.STATUS_ERROR, amiCallingResponse);
							}
							logger.info("Submitted Call Response for Service  " + serviceparam.getServiceName()
									+ " for MissCallId-->" + data.getMisscallId() + "-->" + outboundParameters + "-->"
									+ amiCallingResponse);

						}
						OutboundSender.getInstance(serviceparam).openCloseAmiConnection(0);
					}

				}
			} catch (Exception exception) {
				logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
		}
	}
}
