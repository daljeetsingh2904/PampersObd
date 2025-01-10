package com.pampers.PampersObd.dao;

import java.nio.channels.Channel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.text.TabExpander;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pampers.PampersObd.model.ObdChannel;
import com.pampers.PampersObd.model.OutboundCallParameters;
import com.pampers.PampersObd.model.ServiceParam;
import com.pampers.PampersObd.repository.ObdChannelRepository;
import com.pampers.PampersObd.repository.ServiceConfigRepository;
import com.pampers.PampersObd.services.Constants;
import com.pampers.PampersObd.services.EncryptionRepository;
import com.pampers.PampersObd.services.LanguageRepository;
import com.pampers.PampersObd.services.LogPathConstant;
import com.pampers.PampersObd.services.Utility;

@Component
public class DbHandler {

	@Autowired
	private ServiceConfigRepository serviceRepo;

	@Autowired
	private ObdChannelRepository obdRepo;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	LanguageRepository languageRepository;

	ServiceParam serviceParam;

	OutboundCallParameters callingData;

	private static final Logger logger = Logger.getLogger(DbHandler.class);

	public ServiceParam fetchConfiguration(ServiceParam serviceparam) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		ServiceParam serviceConfig = serviceRepo.findByServiceNameAndStatus(serviceparam.getServiceName(), 1);

		if (serviceConfig == null) {
			logger.warn("No configuration find for service " + serviceparam.getServiceName());
		}
		logger.info("Serviceparam bean value is --->>> "+serviceConfig);
		System.out.println("Serviceparam bean value is --->>> "+serviceConfig);
		serviceParam = new ServiceParam();
		serviceParam.setServiceName(serviceConfig.getServiceName());
		serviceParam.setAmiUsername(serviceConfig.getAmiUsername());
		serviceParam.setAmiPassword(serviceConfig.getAmiPassword());
		serviceParam.setAmiHostname(serviceConfig.getAmiHostname());
		serviceParam.setCallStatus(serviceConfig.getCallStatus());
		serviceParam.setObdRingTimeout(serviceConfig.getObdRingTimeout());
		serviceParam.setContextName(serviceConfig.getContextName());
		serviceParam.setCallerId(serviceConfig.getCallerId());
		serviceParam.setObdThreadSleepTime(serviceConfig.getObdThreadSleepTime());
		serviceParam.setObdCallParameters(serviceConfig.getObdCallParameters());
		serviceParam.setFlowType(serviceConfig.getFlowType());
		serviceParam.setSalt(EncryptionRepository.getInstance().getValue(serviceConfig.getSalt() + "SALT"));
		serviceParam.setSecretKey(EncryptionRepository.getInstance().getValue(serviceConfig.getSecretKey() + "SECRET"));
		return serviceParam;

	}

	@Transactional(readOnly = true)
	public List<String> fetchCallingChannels(ServiceParam serviceparam, DbHandler dbhandler) {
		List<String> channelArr = new ArrayList<>();
		StringBuilder dataBuilder = new StringBuilder();
		try {
			List<ObdChannel> channels = obdRepo.findByObdChannelContextAndStatus("Idle", serviceparam.getContextName());

			for (ObdChannel channel : channels) {
				dataBuilder.append(channel.getObdChannelId());
				dataBuilder.append("@");
				dataBuilder.append(channel.getObdChannelName());
				channelArr.add(dataBuilder.toString());
				dataBuilder.delete(0, dataBuilder.length());
			}
			logger.debug("Available Channels are -->" + channelArr);
			logger.info("Idle Channel size is ---->>>"+channelArr.size());
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {

		}
		return channelArr;

	}

	public List<OutboundCallParameters> fetchCallingNumbers(ServiceParam serviceparam, List<String> obdChannelData) {
		String columnName;
		String tableName;
		String conditionValues;
		String query;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		List<OutboundCallParameters> recordsArr;
		String mobileNumber;
		List<String> uniqueIdData = new ArrayList<>();
		int callCounter = 0;
		int validateOptionFlag;
		String columnValues = null;
		StringBuilder dataBuilder = new StringBuilder();
		List<OutboundCallParameters> obdCallingData = new ArrayList<>();
		int recordCounter = 0;
		Connection conn = null;
		try {
			columnName = "md.misscall_id,md.mobile_number,md.project_id,md.retry_count,pmm.site_id,sm.language_id";
			tableName = Constants.MISSCALLTABLENAME
					+ " md inner join _tbl_projectsite_mapping_master pmm on md.project_id=pmm.project_id inner join _tbl_site_master sm on pmm.site_id=sm.site_id";
			conditionValues = "md.status=? and if(md.updateDate is NULL,md.createDate<now(),md.updateDate<now()) limit ?";
			query = "select " + columnName + " from " + tableName + " where " + conditionValues;
			logger.info("Query is ---->> " + query);
			conn = jdbcTemplate.getDataSource().getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Integer.toString(0));
			pstmt.setInt(2, obdChannelData.size());
//			pstmt.setString(2, Integer.toString(2));
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				callingData = new OutboundCallParameters();
				callingData.setMisscallId(resultSet.getInt("misscall_id"));
				callingData.setMobileNumber(resultSet.getString(Constants.MOBILENUMBER));
				callingData.setRetryCount(resultSet.getInt("retry_count"));
				if (callingData.getRetryCount() > 0) {
					mobileNumber = fetchMobileNumber(serviceparam, callingData.getMobileNumber());
					if (mobileNumber == null) {
						mobileNumber = callingData.getMobileNumber();
						callingData
								.setMobileNumberEncrypted(Utility.encrypt(serviceparam, callingData.getMobileNumber()));
					} else if (mobileNumber.length() == 24 && mobileNumber.endsWith("==")) {
						callingData.setMobileNumberEncrypted(mobileNumber);
						mobileNumber = Utility.decrypt(serviceparam, mobileNumber);
					}
					callingData.setMobileNumber(mobileNumber);
				} else {
					callingData.setMobileNumberEncrypted(Utility.encrypt(serviceparam, callingData.getMobileNumber()));
				}
				uniqueIdData = fetchMobileNumberUniqueId(serviceparam, callingData);
				if (uniqueIdData.get(0).equals("0")) {
					logger.error("Some Error in Generation/Insertion of Mobile Number UniqueId for MissCallId-->"
							+ callingData.getMisscallId());
				} else {
					callingData.setMobileNumberId(Integer.parseInt(uniqueIdData.get(0).split("@")[0]));
					callCounter = getMissCallCount(serviceparam, callingData);
					if (callCounter > 0) {
						logger.info("The number is already in status (1,2) ----->>> "+callingData.getMobileNumberId());
						updateCallStatus(serviceparam, callingData.getMobileNumberId(), callingData.getMisscallId(),
								Constants.STATUS_ALREADYONCALL, "Already On Call");
					} else {
						if (serviceparam.getCallStatus() == 0) {
							serviceparam.setRegretStatus(Constants.STATUS_REGRET);
							validateOptionFlag = validateOptin(serviceparam, callingData);
						} else if (serviceparam.getCallStatus() == 1) {
							validateOptionFlag = 0;
						} else {
							serviceparam.setRegretStatus(Constants.STATUS_PHARMACY_REGRET);
							validateOptionFlag = validatePharmacyRegistration(serviceparam, callingData);
						}
						logger.info("Validate Option Flag is --->> "+validateOptionFlag);
						switch (validateOptionFlag) {
						case 0:
							languageRepository.makeRepository(serviceparam);
							dataBuilder.append(callingData.getMisscallId() + ",");
							callingData.setProjectId(resultSet.getInt("project_id"));
							callingData.setSiteId(resultSet.getInt("site_id"));
							callingData.setLanguageName(
									languageRepository.getValue(resultSet.getInt(Constants.LANGUAGEID)));
							callingData
									.setObdChannelId(Integer.parseInt(obdChannelData.get(recordCounter).split("@")[0]));
							callingData.setObdChannelName(obdChannelData.get(recordCounter++).split("@")[1]);
							obdCallingData.add(callingData);
							deleteTrialDetails(serviceparam, callingData);
							
							
							break;
						case 1:
							columnName = Constants.SMSCOLUMN;
							tableName = Constants.SMSENDTABLENAME;
							columnValues = Constants.THREECONDITIONVARIABLE;
							query = "Insert into " + tableName + "(" + columnName + ")values(" + columnValues+")";
//							jdbcTemplate.getDataSource().getConnection().prepareStatement(query);
							int updateCount=jdbcTemplate.update(query, Integer.toString(callingData.getMobileNumberId()),
									Integer.toString(serviceparam.getRegretStatus()),
									Integer.toString(callingData.getMisscallId()));
							logger.info("Query is --->>> "+query+ " and rows affected are -->>"+updateCount);
							break;
						default:
							logger.error("Error in Validating Optin Details for MissCallId-->"
									+ callingData.getMisscallId());
							break;
						}
					}
				}

			}

		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				conn.close();
				resultSet.close();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return obdCallingData;
	}

	public String fetchMobileNumber(ServiceParam serviceparam, String mobileNumberId) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String columnName;
		String tableName;
		String conditionValues;
		ResultSet resultSet = null;
		String mobileNumber = null;
		boolean found;
		String query = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			columnName = Constants.MOBILENUMBER;
			tableName = "_tbl_mobilenumber_details";
			conditionValues = "mobilenumber_id=? and " + Constants.STATUSCONDITION;
			query = "select " + columnName + " from " + tableName + " where " + conditionValues;
			conn = jdbcTemplate.getDataSource().getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, mobileNumberId);
			pstmt.setString(2, Integer.toString(Constants.STATUS_ACTIVE));
			resultSet = pstmt.executeQuery();
			found = resultSet.next();
			if (found) {
				mobileNumber = resultSet.getString(Constants.MOBILENUMBER);
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (conn != null) {
					conn.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception finallyexception) {
				logger.error(finallyexception + Arrays.asList(finallyexception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return mobileNumber;

	}

	/**
	 * Fxtn for returning primary id i.e is generated
	 */

	private String insertMobileNumberAndReturnId(OutboundCallParameters callingData) {
		String query = "INSERT INTO _tbl_mobilenumber_details (mobile_number) VALUES (?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		// Execute the update using a PreparedStatement to get the generated key
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(query, new String[] { "mobile_number_unique_id" }); // Change
																													// to
																													// your
																													// auto-generated
																													// ID
																													// column
			ps.setString(1, callingData.getMobileNumberEncrypted());
			return ps;
		}, keyHolder);

		// Return the generated key
		if (keyHolder.getKey() != null) {
			return keyHolder.getKey().toString();
		} else {
			return "0"; // Return '0' if no key was generated
		}
	}

	public List<String> fetchMobileNumberUniqueId(ServiceParam serviceparam, OutboundCallParameters callingData) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		Map<Integer, String> parameterValues = new HashMap<>();
		ResultSet resultSet = null;
		List<String> uniqueIdData = new ArrayList<>();
//		StringBuilder dataBuilder = new StringBuilder();
		String mobileNumberId=null;
		try {
			mobileNumberId = fetchMobileNumberId(serviceparam, callingData);
			if (mobileNumberId==null) {
				String mobileNumberUniqueId = insertMobileNumberAndReturnId(callingData);

				if (!mobileNumberUniqueId.equals("0")) {
					mobileNumberId = mobileNumberUniqueId;
				} else {
					mobileNumberId=null;
				}
			}
			uniqueIdData.add(mobileNumberId);
			logger.debug("Mobile Number UniqueId for MissCallId-->" + callingData.getMisscallId() + "-->"
					+ mobileNumberId);
		} catch (Exception exception) {
			uniqueIdData.add(mobileNumberId);
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			parameterValues.clear();
			try {
				if (resultSet != null) {
					resultSet.close();
				}

			} catch (Exception finallyexception) {
				logger.error(finallyexception + Arrays.asList(finallyexception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return uniqueIdData;
	}

	public String fetchMobileNumberId(ServiceParam serviceparam, OutboundCallParameters callingData) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String columnName;
		String tableName;
		String conditionValues;
		Map<Integer, String> parameterValues = new HashMap<>();
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		String query = null;
		Connection conn = null;
		String mobileNumberId = null;
		try {
			columnName = "mobilenumber_id";
			tableName = "_tbl_mobilenumber_details";
			conditionValues = "mobile_number=? and status=1";
			query = "select " + columnName + " from " + tableName + " where " + conditionValues;
			conn = jdbcTemplate.getDataSource().getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, callingData.getMobileNumberEncrypted());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				mobileNumberId = resultSet.getString("mobilenumber_id");
			}
			System.out.println("Result set is --------->>>> " + resultSet);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			parameterValues.clear();
			try {
				if (conn != null) {
					conn.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception finallyexception) {
				logger.error(finallyexception + Arrays.asList(finallyexception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return mobileNumberId;
	}

	public int getMissCallCount(ServiceParam serviceparam, OutboundCallParameters callingData) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String columnName;
		String tableName;
		String conditionValues;
		Map<Integer, String> parameterValues = new HashMap<>();
		ResultSet resultSet = null;
		int optinStatus = 0;
		String query = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			columnName = "count(*) as optin_status";
			tableName = Constants.MISSCALLTABLENAME;
			conditionValues = "(mobile_number=? or mobile_number=?) and status in (1,2)";
			query = "select " + columnName + " from " + tableName + " where " + conditionValues;
			conn = jdbcTemplate.getDataSource().getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Integer.toString(callingData.getMobileNumberId()));
			pstmt.setString(2, callingData.getMobileNumber());
			resultSet = pstmt.executeQuery();
			boolean found = resultSet.next();
			if (found) {
				optinStatus = resultSet.getInt("optin_status");
			}
			pstmt.close();
			resultSet.close();
			conn.close();
			if (optinStatus == 0) {
				columnName = "count(*) as optin_status";
				tableName = Constants.MISSCALLTABLENAME;
				conditionValues = "(mobile_number=? or mobile_number=?) and status in (0)";
				query = "select " + columnName + " from " + tableName + " where " + conditionValues;
				conn = jdbcTemplate.getDataSource().getConnection();
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, Integer.toString(callingData.getMobileNumberId()));
				pstmt.setString(2, callingData.getMobileNumber());
				resultSet = pstmt.executeQuery();
				found = resultSet.next();
				if (found) {
					optinStatus = resultSet.getInt("optin_status");
				}
				if (optinStatus <= 1) {
					optinStatus = 0;
				}
			}
		} catch (Exception exception) {
			optinStatus = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			parameterValues.clear();
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (conn != null) {
					conn.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception finallyexception) {
				logger.error(finallyexception + Arrays.asList(finallyexception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return optinStatus;
	}

	public int updateCallStatus(ServiceParam serviceparam, int mobileNumberId, int misscallId, int status,
			String callResponse) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String columnName;
		String tableName;
		String conditionValues;
		int updateStatus = 0;
		String query = null;
		Map<Integer, String> parameterValues = new HashMap<>();
		try {
			columnName = "mobile_number=?,ami_response=?," + Constants.STATUSCONDITION + ",updateDate=now()";
			tableName = Constants.MISSCALLTABLENAME;
			conditionValues = "misscall_id=?";
			query = "update " + tableName + " set " + columnName + " where " + conditionValues;
			updateStatus = jdbcTemplate.update(query, Integer.toString(mobileNumberId), callResponse,
					Integer.toString(status), Integer.toString(misscallId));
		} catch (Exception exception) {
			updateStatus = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			parameterValues.clear();
		}
		return updateStatus;
	}

	public int validateOptin(ServiceParam serviceparam, OutboundCallParameters callingData) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String columnName;
		String tableName;
		String conditionValues;
		Map<Integer, String> parameterValues = new HashMap<>();
		ResultSet resultSet = null;
		int optinStatus = 0;
		String query = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			columnName = "count(*) as optin_status";
			tableName = "_tbl_validtrial_details";
			conditionValues = "mobilenumber_id=? and TrialValidityDate>now() and status=1";
			query = "select " + columnName + " from " + tableName + " where " + conditionValues;
			conn = jdbcTemplate.getDataSource().getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Integer.toString(callingData.getMobileNumberId()));
			resultSet = pstmt.executeQuery();
			boolean found = resultSet.next();
			if (found) {
				optinStatus = resultSet.getInt("optin_status");
			}
			if (optinStatus > 0) {
				optinStatus = 1;
			}
		} catch (Exception exception) {
			optinStatus = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (conn != null) {
					conn.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception finallyexception) {
				logger.error(finallyexception + Arrays.asList(finallyexception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return optinStatus;
	}

	public int validatePharmacyRegistration(ServiceParam serviceparam, OutboundCallParameters callingData) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String columnName;
		String tableName;
		String conditionValues;
		Map<Integer, String> parameterValues = new HashMap<>();
		ResultSet resultSet = null;
		int registerStatus = 0;
		String query = null;
		PreparedStatement pstmt;
		try {
			columnName = "count(*) as registration_status";
			tableName = "_tbl_pharmacy_registration_details";
			conditionValues = "mobilenumber_id=?";
			query = "select " + columnName + " from " + tableName + " where " + conditionValues;
			pstmt = jdbcTemplate.getDataSource().getConnection().prepareStatement(query);
			pstmt.setString(1, Integer.toString(callingData.getMobileNumberId()));
			resultSet = pstmt.executeQuery();
			boolean found = resultSet.next();
			if (found) {
				registerStatus = resultSet.getInt("registration_status");
			}
		} catch (Exception exception) {
			registerStatus = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			parameterValues.clear();
		}
		return registerStatus;
	}

	public int deleteTrialDetails(ServiceParam serviceparam, OutboundCallParameters callingData) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String tableName;
		String conditionValues;
		Map<Integer, String> parameterValues = new HashMap<>();
		int optinStatus = 0;
		String query = null;
		try {
			tableName = "_tbl_validtrial_details";
			conditionValues = "mobilenumber_id=?";
			query = "delete from " + tableName + " where " + conditionValues;
			optinStatus = jdbcTemplate.update(query, Integer.toString(callingData.getMobileNumberId()));
			logger.info("Deleted record with id : " + callingData.getMobileNumberId());
		} catch (Exception exception) {
			optinStatus = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			parameterValues.clear();
		}
		return optinStatus;
	}

	public int updateChannelStatus(ServiceParam serviceparam, int misscallId, int odbChannelId,
			String obdChannelStatus) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String columnName;
		String tableName;
		String conditionValues;
		int updateStatus = 0;
		Map<Integer, String> parameterValues = new HashMap<>();
		String query = null;
		try {
			columnName = "obdchannel_status=?,misscall_id=?,updateDate=now()";
			tableName = Constants.OBDCHANNELTABLENAME;
			conditionValues = "obdchannel_id=? and status=1";
			query = "update " + tableName + " set " + columnName + " where " + conditionValues;
			updateStatus = jdbcTemplate.update(query, obdChannelStatus, Integer.toString(misscallId),
					Integer.toString(odbChannelId));

		} catch (Exception exception) {
			updateStatus = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			parameterValues.clear();
		}
		return updateStatus;
	}

	public Map<Integer, String> fetchLanguages(ServiceParam serviceparam) {
		PropertyConfigurator.configure(LogPathConstant.LOG_PATH);
		String columnName;
		String tableName;
		String conditionValues;
		Map<Integer, String> parameterValues = new HashMap<>();
		Map<Integer, String> languageDetails = new HashMap<>();
		ResultSet resultSet = null;
		String query = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			columnName = "language_id,language_name";
			tableName = "_tbl_language_master";
			conditionValues = Constants.STATUSCONDITION;
			query = "select " + columnName + " from " + tableName + " where " + conditionValues;
			conn = jdbcTemplate.getDataSource().getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Integer.toString(Constants.STATUS_ACTIVE));
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				languageDetails.put(resultSet.getInt("language_id"), resultSet.getString("language_name"));
			}
		} catch (Exception exception) {
			languageDetails.clear();
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			parameterValues.clear();
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (conn != null) {
					conn.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception finallyexception) {
				logger.error(finallyexception + Arrays.asList(finallyexception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return languageDetails;
	}
}
