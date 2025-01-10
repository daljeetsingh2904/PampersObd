/**
 * 
 */
package com.pampers.PampersObd.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * @author vinay.sethi
 *
 */
public class Constants {
	
	private Constants() {		
	}	
	
	public static final String WHERE=" where ";
	public static final String CHANNELFREESTATUS="Idle";
	public static final String CHANNELBUSYSTATUS="Busy";
	public static final String MISSCALLTABLENAME="_tbl_misscall_details";
	public static final String MISSCALLREDEMPTIONTABLENAME="_tbl_misscall_details_redemption";
	public static final String OBDCHANNELTABLENAME="_tbl_obdchannel_master";
	public static final String OPTINREQUESTABLENAME="_tbl_optinrequest_details";
	public static final String SMSENDTABLENAME="_tbl_smsend_details";
	public static final String LOGCONDITION=" with Condition ";
	public static final String MOBILENUMBER="mobile_number";
	public static final String LANGUAGEID="language_id";
	public static final String STATUSCONDITION="status=?";
	public static final String THREECONDITIONVARIABLE="?,?,?";
	public static final String SMSCOLUMN="mobilenumber_id,sms_category,misscall_id";
	public static final int STATUS_ACTIVE=1;
	public static final int STATUS_INITIAL=1;
	public static final int STATUS_FINAL=2;
	public static final int STATUS_REGRET=4;
	public static final int STATUS_REDEMPTION_REGRET=24;
	public static final int STATUS_ERROR=6;
	public static final int STATUS_ALREADYONCALL=7;
	public static final int STATUS_PHARMACY_INITIAL=11;
	public static final int STATUS_PHARMACY_FINAL=12;
	public static final int STATUS_PHARMACY_REGRET=14;	
	public static final int STATUS_PHARMACY_ERROR=16;
	
//	 @Value("${pampers.log4j.obd.filepath}")
//	public static String LOG_PATH;

}