package com.pampers.PampersObd.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "_tbl_serviceconfiguration_details")
public class ServiceParam {

	@Override
	public String toString() {
		return "ServiceParam [configure_id=" + configure_id + ", ServiceName=" + ServiceName + ", contextName="
				+ contextName + ", secretKey=" + secretKey + ", salt=" + salt + ", amiUsername=" + amiUsername
				+ ", amiPassword=" + amiPassword + ", amiHostname=" + amiHostname + ", callerId=" + callerId
				+ ", obdCallParameters=" + obdCallParameters + ", obdRingTimeout=" + obdRingTimeout
				+ ", obdThreadSleepTime=" + obdThreadSleepTime + ", callStatus=" + callStatus + ", status=" + status
				+ ", regretStatus=" + regretStatus 
				+ ", flowType="+flowType+"]";
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int configure_id;

	@Column(name = "ServiceName")
	private String ServiceName;
	@Column(name = "ContextName")
	private String contextName;
	@Column(name = "EncryptionSecretKey")
	private String secretKey;
	@Column(name = "EncryptionSalt")
	private String salt;
//	private String dbDriverName;
//	private String dbHostname;
//	private String dbUsername;
//	private String dbPassword;
	@Column(name = "AmiUserName")
	private String amiUsername;
	@Column(name = "AmiPassword")
	private String amiPassword;
	@Column(name = "AmiHostname")
	private String amiHostname;
	@Column(name = "CallerId")
	private String callerId;
	private String obdCallParameters;
	private long obdRingTimeout;
	private int obdThreadSleepTime;
	private int callStatus;
	private int status;
	private String flowType;

	// Getters and Setters
	public int getConfigure_id() {
		return configure_id;
	}

	public void setConfigure_id(int configure_id) {
		this.configure_id = configure_id;
	}

	public String getServiceName() {
		return ServiceName;
	}

	public void setServiceName(String serviceName) {
		this.ServiceName = serviceName;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

//	public String getDbDriverName() {
//		return dbDriverName;
//	}
//
//	public void setDbDriverName(String dbDriverName) {
//		this.dbDriverName = dbDriverName;
//	}
//
//	public String getDbHostname() {
//		return dbHostname;
//	}
//
//	public void setDbHostname(String dbHostname) {
//		this.dbHostname = dbHostname;
//	}
//
//	public String getDbUsername() {
//		return dbUsername;
//	}
//
//	public void setDbUsername(String dbUsername) {
//		this.dbUsername = dbUsername;
//	}
//
//	public String getDbPassword() {
//		return dbPassword;
//	}
//
//	public void setDbPassword(String dbPassword) {
//		this.dbPassword = dbPassword;
//	}

	public String getAmiUsername() {
		return amiUsername;
	}

	public void setAmiUsername(String amiUsername) {
		this.amiUsername = amiUsername;
	}

	public String getAmiPassword() {
		return amiPassword;
	}

	public void setAmiPassword(String amiPassword) {
		this.amiPassword = amiPassword;
	}

	public String getAmiHostname() {
		return amiHostname;
	}

	public void setAmiHostname(String amiHostname) {
		this.amiHostname = amiHostname;
	}

	public String getCallerId() {
		return callerId;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	public String getObdCallParameters() {
		return obdCallParameters;
	}

	public void setObdCallParameters(String obdCallParameters) {
		this.obdCallParameters = obdCallParameters;
	}

	public long getObdRingTimeout() {
		return obdRingTimeout;
	}

	public void setObdRingTimeout(long obdRingTimeout) {
		this.obdRingTimeout = obdRingTimeout;
	}

	public int getObdThreadSleepTime() {
		return obdThreadSleepTime;
	}

	public void setObdThreadSleepTime(int obdThreadSleepTime) {
		this.obdThreadSleepTime = obdThreadSleepTime;
	}

	public int getCallStatus() {
		return callStatus;
	}

	public void setCallStatus(int callStatus) {
		this.callStatus = callStatus;
	}

	@Column(name="regretStatus")
	private int regretStatus;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getRegretStatus() {
		return regretStatus;
	}

	public void setRegretStatus(int regretStatus) {
		this.regretStatus = regretStatus;
	}

	public String getFlowType() {
		return flowType;
	}

	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}
}
