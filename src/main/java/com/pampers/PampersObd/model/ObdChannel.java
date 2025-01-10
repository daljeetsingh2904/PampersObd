package com.pampers.PampersObd.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "_tbl_obdchannel_master")
public class ObdChannel {

	@Id
	@Column(name = "obdchannel_id") // Maps the field to the database column
	private int obdChannelId;

	@Column(name = "obdchannel_name") // Maps the field to the database column
	private String obdChannelName;

	@Column(name = "obdchannel_status") // Maps the field to the database column
	private String obdChannelStatus;

	@Column(name = "obdchannel_context") // Maps the field to the database column
	private String obdChannelContext;

	@Column(name = "status") // Maps the field to the database column
	private int status;

	public String getObdChannelStatus() {
		return obdChannelStatus;
	}

	public void setObdChannelStatus(String obdChannelStatus) {
		this.obdChannelStatus = obdChannelStatus;
	}

	public String getObdChannelContext() {
		return obdChannelContext;
	}

	public void setObdChannelContext(String obdChannelContext) {
		this.obdChannelContext = obdChannelContext;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getObdChannelName() {
		return obdChannelName;
	}

	public void setObdChannelName(String obdChannelName) {
		this.obdChannelName = obdChannelName;
	}

	public int getObdChannelId() {
		return obdChannelId;
	}

	public void setObdChannelId(int obdChannelId) {
		this.obdChannelId = obdChannelId;
	}

}
