/*
 *
 *   Copyright (C) 2024 - Cognizant Technology Solutions
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.cxintegration.elastic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Scanner;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axe.RunAxe;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Random;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

public class Elastic {

	private static final Logger logger = LoggerFactory.getLogger(Elastic.class);
	public static String elasticHost;
	public static String elasticPort;
	public static String javaCAcertsPath;
	public static String authHeader;
	public static String testtype;
	public static String testStatus;
	public static String project;
	public static String build;
	static int RunID = 0;
	static int sumofATSeverity = 0;
	HttpURLConnection connection;
	URL url;

	public Elastic(String project, String build, String elasticHost, String elasticPort, String authHeader,
			String javaCAcertsPath, String testtype) {
		// initialise the project variables
		this.project = project;
		this.build = build;
		this.elasticHost = elasticHost;
		this.elasticPort = elasticPort;
		this.authHeader = authHeader;
		this.javaCAcertsPath = javaCAcertsPath;
		this.testtype = testtype;
	}

	public int getRunId() throws FileNotFoundException {

		try {
			// Query max run id in all 4 AT indexes
			int maxRunID = 0;
			String[] indexArray = new String[] { "reporttime_at", "impact_at", "violations_at", "runstatus_at" };
			for (int i = 1; i <= indexArray.length; i++) {
				String id = getRunIDFromEachATIndex(indexArray[i - 1].toString());
				int temp = 0;
				if (id.equalsIgnoreCase("") == false) {
					temp = Integer.parseInt(id);
				} else {
					temp = 0;
				}

				if (temp > maxRunID) {
					maxRunID = temp;
				}
			}
			RunID = maxRunID + 1;
			logger.info("RunID for this test is : " + RunID);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("Exception occurred while getting latest run id for the report!!\n" + e.getMessage());
		}
		return (RunID);

	}

	public String getRunIDFromEachATIndex(String index) {
		String numString = "";
		String bodyJson = "";
		StringBuilder response = null;
		try {
			url = new URL(elasticHost + ":" + elasticPort + "/" + "_sql?format=txt");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", authHeader);
			String reporttime_at_index = null;
			String summary_at_index = null;
			String violations_at_index = null;
			String impact_at_index = null;
			if (testtype.equalsIgnoreCase("at") == true) {
				bodyJson = "{" + "\"query\": \"SELECT max(RunID) FROM " + index + "\"" + "}";
			}
			connection.setRequestProperty("Content-Length", Integer.toString(bodyJson.getBytes().length));
			connection.setUseCaches(false);
			connection.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(bodyJson);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			
			for (int i = 0; i < response.length(); i++) {
				int ascii = response.charAt(i);
				
				// ascii value of 0 is 48 and of 9 is 57
				if (ascii >= 48 && ascii <= (57)) {
					numString += (char) ascii;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("Exception Occurred while retrieving run id from " + index);
			logger.info("Exception is "+e.getMessage());

		} finally {
			try {
				if (connection.getResponseCode() != 200) {
					if (connection.getResponseCode() == 400) {
						InputStream is = connection.getErrorStream();
						BufferedReader rd = new BufferedReader(new InputStreamReader(is));
						response = new StringBuilder();
						String line;
						while ((line = rd.readLine()) != null) {
							response.append(line);
							response.append('\r');
						}
						rd.close();
						
						if (response.toString().toLowerCase().contains("unknown index") == true) {
							logger.info("Index '" + index + "' is not found in the elasticsearch!!");
							numString = "0";
						}
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.info(e.getMessage());
			}
			connection.disconnect();
		}
		return numString;
	}

	public void postReportTimetoElastic(JSONObject reportTimeJSON) {

		try {
			// https://localhost:9200/reporttime_at/_doc/
			URL url = new URL(elasticHost + ":" + elasticPort + "/" + "reporttime_at/_doc/");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", authHeader);
			connection.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			connection.setRequestProperty("Content-Length",
					Integer.toString(reportTimeJSON.toString().getBytes().length));
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.connect();

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(reportTimeJSON.toString());
			wr.close();
			logger.info("Successfully posted reporttime to elasticsearch!!");

			connection.getURL();
			connection.getResponseCode();
			connection.disconnect();

		} catch (Exception ex) {
			logger.info("Exception occurred while writing data to 'reporttime_at' index!!\n" + ex);
		}

	}

	public void post_Impact_Severity_To_Elastic(JSONObject impactJSON, String testcase) {
		try {

			DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Date date = new Date();
			String timestamp = dateFormat1.format(date);

			String sumOfMinor = impactJSON.get("minor").toString();
			String sumOfModerate = impactJSON.get("moderate").toString();
			String sumOfSerious = impactJSON.get("serious").toString();
			String sumOfCritical = impactJSON.get("critical").toString();

			sumofATSeverity = sumofATSeverity + Integer.parseInt(sumOfMinor) + Integer.parseInt(sumOfModerate)
					+ Integer.parseInt(sumOfSerious) + Integer.parseInt(sumOfCritical);

			impactJSON.put("testcase", testcase);
			impactJSON.put("Timestamp", timestamp);
			impactJSON.put("RunID", RunID);
			impactJSON.put("Project", project);
			impactJSON.put("Build", build);

			// https://localhost:9200/reporttime_at/_doc/
			URL url = new URL(elasticHost + ":" + elasticPort + "/" + "impact_at/_doc/");

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", authHeader);
			connection.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			connection.setRequestProperty("Content-Length", Integer.toString(impactJSON.toString().getBytes().length));
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.connect();

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(impactJSON.toString());
			wr.close();
			logger.info("Successfully posted impact and severity to elasticsearch");

			connection.getURL();
			connection.getResponseCode();
			connection.disconnect();

		} catch (Exception ex) {
			logger.info("Exception occurred while writing data to 'impact_at' index!!\n" + ex);
		}

	}

	public void post_Violation_Recommondation(JSONObject violationJSON) {

		try {

			DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Date date = new Date();
			String timestamp = dateFormat1.format(date);

			violationJSON.put("Timestamp", timestamp);
			violationJSON.put("Project", project);
			violationJSON.put("Build", build);

			// https://localhost:9200/reporttime_at/_doc/
			url = new URL(elasticHost + ":" + elasticPort + "/" + "violations_at/_doc/");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			connection.setRequestProperty("Authorization", authHeader);
			connection.setRequestProperty("Content-Length",
					Integer.toString(violationJSON.toString().getBytes().length));
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.connect();

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(violationJSON.toString().getBytes());
			wr.close();

			connection.getURL();
			connection.getResponseCode();
			connection.disconnect();

		} catch (Exception ex) {
			logger.info("Exception occurred while writing data to 'violations_at' index!!\n" + ex);
			logger.info("URL: " + connection.getURL());
			logger.info("JSON: " + violationJSON.toString());
		}

	}

	public void updateReportURL(String kibanaReportPath) {
		try {
			// https://localhost:9200/reporttime_at/_doc/
			URL url = new URL(elasticHost + ":" + elasticPort + "/" + "reporttime_at/_update_by_query");

			String updateJSONString = "{" + "  \"script\": {" + "    \"source\": \"ctx._source.reporttime.reportPath='"
					+ kibanaReportPath + "'\"," + "    \"lang\": \"painless\"" + "  },\r\n" + "  \"query\": {"
					+ "    \"term\": {" + "      \"RunID\":" + RunID + "    }" + "  }" + "}";

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", authHeader);
			connection.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			connection.setRequestProperty("Content-Length", Integer.toString(updateJSONString.getBytes().length));
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.connect();

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(updateJSONString.toString());
			wr.close();

			connection.getURL();
			connection.getResponseCode();
			connection.disconnect();

		} catch (Exception ex) {
			logger.info("Exception occurred while writing data to 'reporttime_at' index!!\n" + ex);
		}
	}

	public void postATRunStatus(String Timestamp, String reporttime, String reportPath) {
		try {
			// example url: https://localhost:9200/reporttime_at/_doc/
			URL url = new URL(elasticHost + ":" + elasticPort + "/" + "runstatus_at/_doc");

			JSONObject atRunStatusJson = new JSONObject();
			atRunStatusJson.put("Project", project);
			atRunStatusJson.put("Build", build);
			atRunStatusJson.put("RunID", RunID);
			atRunStatusJson.put("Timestamp", Timestamp);
			atRunStatusJson.put("reporttime", reporttime);
			atRunStatusJson.put("totalSeverity", sumofATSeverity);
			atRunStatusJson.put("reportPath", reportPath);
			if (sumofATSeverity > 0) {
				atRunStatusJson.put("verdict", "FAIL");
			} else {
				atRunStatusJson.put("verdict", "PASS");
			}

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", authHeader);
			connection.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			connection.setRequestProperty("Content-Length",
					Integer.toString(atRunStatusJson.toString().getBytes().length));
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.connect();

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(atRunStatusJson.toString());
			wr.close();

			connection.getURL();
			connection.getResponseCode();
			connection.disconnect();

		} catch (Exception ex) {
			logger.info("Exception occurred while writing data to 'runstatus_at' index!!\n" + ex);
		}
	}

	public String encodedTimeforKibana() {
		// Get encoded time for kibana url
		DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		LocalDateTime kTime = LocalDateTime.now();
		String directTime = dateformat.format(kTime).toString();

		String encodedTime = null;
		try {
			encodedTime = URLEncoder.encode(directTime, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.info(e.getMessage());
		}

		return encodedTime;
	}

}
