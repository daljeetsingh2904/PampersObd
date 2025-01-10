/**
 * 
 */
package com.pampers.PampersObd.services;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;

import com.pampers.PampersObd.model.ServiceParam;


/**
 * @author daljeetsingh
 *
 */
public class OutboundSender{
	
	ManagerConnectionFactory managerConnectionFactory=null;
	ManagerConnection managerConnection = null;	
	private static OutboundSender outboundInstance = null;	
	private static final Logger logger = Logger.getLogger(OutboundSender.class);
		
	public static OutboundSender getInstance(ServiceParam serviceparam) {
    	if (outboundInstance == null) {
    		outboundInstance = new OutboundSender(serviceparam);
        }
        return outboundInstance;
    }
	
	private OutboundSender(ServiceParam serviceparam) {		
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		try {
			logger.info("AMI Connection Initiated with-->" + serviceparam.getAmiHostname()+"-->"+ serviceparam.getAmiUsername()+"-->"+ serviceparam.getAmiPassword());
			managerConnectionFactory = new ManagerConnectionFactory(serviceparam.getAmiHostname(), serviceparam.getAmiUsername(), serviceparam.getAmiPassword());
			this.managerConnection = managerConnectionFactory.createManagerConnection();			
		}catch(Exception exception) {
			logger.error("Erros in Establishing AMI Connection-->"+exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
	}
	
	public String sendCall(ServiceParam serviceparam,String channel,String mobileNumber,String outboundVariables) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		logger.info("Call Initiation Request Received for -->"+outboundVariables);
		OriginateAction originateAction;
        ManagerResponse originateResponse=null;        
        String callResponse="";
        int connectStatusFlag;
		try {			
			if(this.managerConnection.getState()!=ManagerConnectionState.CONNECTED){
				connectStatusFlag=openCloseAmiConnection(1);
				if(connectStatusFlag==-1) {
					return "Error-disconnected";
				}
			}
			logger.info(channel+mobileNumber);
			originateAction = new OriginateAction();
	        originateAction.setChannel(channel+mobileNumber);
	        originateAction.setVariable("callVariables",outboundVariables);	        
	        originateAction.setContext(serviceparam.getContextName());
	        originateAction.setExten("s");
	        originateAction.setPriority(1);
	        originateAction.setAsync(true);
	        originateAction.setTimeout(serviceparam.getObdRingTimeout());
	        originateAction.setCallerId(serviceparam.getCallerId());	        
	        originateResponse = managerConnection.sendAction(originateAction, 30000);
	        callResponse=originateResponse.getResponse()+"-"+originateResponse.getMessage();
	        logger.info("Call Completed for -->"+outboundVariables);
		}catch(Exception exception) {			
			callResponse="Error-exception";
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
		return callResponse;
	}	
	
	public int openCloseAmiConnection(int statusFlag) {
		int connectionStatus=0;
		try {
			logger.debug(statusFlag==1?"Login":"Logout" + " AMI Connection Received");
			if(statusFlag==0) {
				this.managerConnection.logoff();
			}else if(statusFlag==1) {
				this.managerConnection.login();
			}else if(statusFlag==2) {
				this.managerConnection.logoff();
				this.managerConnection.login();
			}
			connectionStatus=1;
		}catch(Exception exception) {			
			connectionStatus=-1;
			logger.error("Error in Login/Logout "+statusFlag+" AMI Connection-->"+exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
		return connectionStatus;
	}
}