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

package com.nft.selenium;

import com.charles.POC.NetworkRecommend;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;
import org.apache.commons.io.FileUtils;
import org.jfree.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.remote.RemoteWebDriver;

public class PerformanceMetrics {

	private static final Logger logger = LoggerFactory.getLogger(PerformanceMetrics.class);
	static Properties properties = new Properties();
	static {
		File file = new File("cx_config\\datafile.properties");
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(file);
			properties.load(fileReader);
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JavascriptExecutor js;
	public WebDriverWait wait;
	public boolean dtEnabled;
	public String CUSTOMERID;
	public String USERID;
	public String PASSWORD;
	public boolean timersEnabled;
	public FileWriter timerlog;
	public boolean acceptNextAlert = true;
	public SimpleDateFormat dtFmt;
	public Date date;
	public Date dateTime;
	public double lastLoadTime;
	public DateFormat requiredFormat;
	public String DATE;
	public String TESTAGENT;
	public String timerName;
	public static long start = 0L;
	public static long stop = 0L;
	public static long finish = 0L;
	public boolean dynaTrace = false;
	public boolean browserTimers = true;
	public String PROFILE = "Tino";
	public String URL = "";

	public static PerformanceMetrics collect = new PerformanceMetrics();

	public static void main(String[] args)
			throws FileNotFoundException, InterruptedException, IOException, ParseException {

	}

	public String SiteNavigation1(WebDriver driver, String browser, String ENV, String URL, String BUILD,
			String PROJECT, String reportFolderPath) throws IOException {
		String browserVersion = "";
		this.dtEnabled = this.dynaTrace;
		this.dtFmt = new SimpleDateFormat("yyMMdd");
		this.date = new Date();
		Capabilities browser1 = ((ChromeDriver) driver).getCapabilities();
		browserVersion = browser1.getVersion();
		browser = browser + browserVersion;
		this.js = (JavascriptExecutor) driver;
		this.wait = new WebDriverWait(driver, 30L);
		this.AgentData11(ENV, URL);
		logger.info("BROWSER == " + browser);
		logger.info("ENV == " + ENV);
		logger.info("BUILD == " + BUILD);
		logger.info("TEST URL == " + URL);
		this.PerformanceUtils1(this.js, ENV, browser, BUILD, PROJECT, URL, this.dynaTrace, this.browserTimers,
				reportFolderPath);
		if (this.dtEnabled) {
			this.startRecording(this.getProfile(), PROJECT, driver, browser, ENV, BUILD);
		}

		return browser;
	}

	// Get browser capabilities
	public String SiteNavigationwithCache(WebDriver driver, String browser, String ENV, String URL, String BUILD,
			String PROJECT, String reportFolderPath) throws IOException {
		String browserVersion = "";

		this.dtEnabled = this.dynaTrace;
		this.dtFmt = new SimpleDateFormat("yyMMdd");
		this.date = new Date();

		Capabilities browser1 = null;
		browser1 = ((RemoteWebDriver) driver).getCapabilities();
		browser = browser1.getBrowserName() + ":" + browser1.getVersion();
		browserVersion = browser1.getVersion();
		browser = browser + browserVersion;

		this.js = (JavascriptExecutor) driver;
		this.wait = new WebDriverWait(driver, 30L);
		this.AgentData11(ENV, URL);
		this.PerformanceUtils1withCache(this.js, ENV, browser, BUILD, PROJECT, URL, this.dynaTrace, this.browserTimers,
				reportFolderPath);

		if (this.dtEnabled) {
			this.startRecording(this.getProfile(), PROJECT, driver, browser, ENV, BUILD);
		}

		return browser;
	}

	public void startRecording(String Profile, String Scenario, WebDriver driver, String browser, String ENV,
			String BUILD) throws IOException {
		SimpleDateFormat dtFmt = new SimpleDateFormat("yyMMdd");
		Date date = new Date();
		Profile.replace(" ", "%20");
		String UrlForAdminManagementProfile= properties.getProperty("UrlForAdminManagementProfile");
		driver.get(UrlForAdminManagementProfile + Profile + "/configurations");
		String dtProfile = "";

		for (int i = 1; i < 10; ++i) {
			if (driver.findElement(By.xpath("(//ul[@class='switches']//li)[" + i + "]")).getAttribute("class")
					.equals("active")) {
				dtProfile = driver.findElement(By.xpath("(//ul[@class='list']/li)[" + i + "]")).getText()
						.replace(" » active", "");
				i = 10;
			}
		}

		String sessionName = dtFmt.format(date).toString() + "_Dotcom_" + ENV + "_" + browser + "_" + Scenario + "_"
				+ this.getBuild(BUILD) + "_" + dtProfile;
		logger.info("Session Name : " + sessionName);
		Profile.replace("%20", " ");
		driver.findElement(By.linkText(Profile)).click();
		this.sleep(1500);
		WebElement testName = driver.findElement(By.xpath(".//*[@id='presentableName']"));
		testName.clear();
		testName.sendKeys(new CharSequence[] { sessionName });
		driver.findElement(By.id("isSessionLocked")).click();
		driver.findElement(By.id("but_startrecording")).click();
		driver.quit();
	}

	public void landing(WebDriver driver, String ENV, String URL) throws IOException {
		this.startTimer("Landing Page");
		if (ENV.toUpperCase().contains("QA12")) {
			driver.get(URL);
		} else if (ENV.equals("Jordan")) {
			driver.get(URL + "webapp/wcs/stores/servlet/sahome?storeId=10101");
		} else {
			driver.get(URL);
		}

		this.stopTimer();

		try {
			if (driver.getTitle().matches("Certificate Error: Navigation Blocked")) {
				driver.get("javascript:document.getElementById('overridelink').click();");
				this.sleep(500);
				Alert alert = driver.switchTo().alert();

				try {
					alert.accept();
				} catch (Exception var6) {
					logger.info("Certificate Alert not present.");
				}
			}
		} catch (Exception var7) {
			logger.info("Exception : " + var7.getMessage());
		}

	}

	public void setAppServer(WebDriver driver, String ENV) throws IOException {
		String CookieValue;
		if (ENV != "QP2" && ENV != "PRF") {
			if (ENV == "QP1") {
				CookieValue = driver.manage().getCookieNamed("OSAJSESSIONID").getValue();
				driver.manage().deleteCookieNamed("OSAJSESSIONID");
				CookieValue = CookieValue.substring(0, CookieValue.length() - 5).concat("01QP1");
				this.js.executeScript("document.cookie='OSAJSESSIONID=" + CookieValue + "';", new Object[0]);
			}
		} else {
			CookieValue = driver.manage().getCookieNamed("OSAJSESSIONID").getValue();
			driver.manage().deleteCookieNamed("OSAJSESSIONID");
			CookieValue = CookieValue.substring(0, CookieValue.length() - 5).concat("21QP1");
			this.js.executeScript("document.cookie='OSAJSESSIONID=" + CookieValue + "';", new Object[0]);
		}

	}

	public void newBrowser(WebDriver driver) {
		driver.close();
		this.killProc();

		// WebDriver
		driver = new ChromeDriver();
		this.js = (JavascriptExecutor) driver;
		this.wait = new WebDriverWait(driver, 30L);
		logger.info("JS----------------" + this.js);
		this.setJSX(this.js);
		logger.info("completed");
	}

	public void loadURL(String url, String pageName, WebDriver driver, String browser, String TESTURL, String BUILD,
			String ENV) throws IOException, JSONException {
		driver.get(url);
	}

	public void clearCache() {
		try {
			Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 8");
			this.sleep(5000);
			logger.info("Clearcache complete");
		} catch (Exception var2) {
			logger.info("Failed to clear cache");
		}
	}

	public void findToProceed(String cssPath, WebDriver driver) {
		try {
			this.sleep(4500);
		} catch (Exception var5) {
			var5.printStackTrace();
			logger.info("CSS element " + cssPath + " not found on page!");
		}
	}

	public void clearCache(WebDriver driver) {
		try {
			Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 255");
			driver.manage().deleteAllCookies();
			this.sleep(8000);
		} catch (Exception var3) {
			logger.info("Failed to clear cache");
		}

	}

	public void sleep(int sleepMS) {
		try {
			Thread.sleep((long) sleepMS);
		} catch (InterruptedException var3) {
			var3.printStackTrace();
		}
	}

	public void killProc() {
		try {
			String PathForKillIEDriver = properties.getProperty("PathForKillIEDriver");
			String PathForKillChromeDriver = properties.getProperty("PathForKillChromeDriver");
			Process killProc = Runtime.getRuntime()
					.exec(PathForKillIEDriver);
			killProc.waitFor();
			killProc = Runtime.getRuntime()
					.exec(PathForKillChromeDriver);
			killProc.waitFor();
		} catch (IOException var2) {
			logger.info("Failed to kill the running driver processes");
		} catch (InterruptedException var3) {
			logger.info("Failed to wait for killProc to terminate");
		} catch (NullPointerException var4) {
			logger.info("Failed to wait for killProc to terminate");
		}

	}

	public void stopRecording(String Profile, WebDriver driver) {
		Profile.replace(" ", "%20");
		String UrlForAdminManagementProfile = properties.getProperty("UrlForAdminManagementProfile");
		driver.get(UrlForAdminManagementProfile + Profile);
		driver.findElement(By.id("but_stoprecording")).click();
		driver.quit();
	}

	public void close(WebDriver driver, String ENV, String URL) throws IOException {
		this.pageTimerEnd();
		driver.close();
		if (this.dtEnabled) {
			this.AgentData11(ENV, URL);
			this.stopRecording(this.getProfile(), driver);
		}
		driver.quit();
	}

	public String PerformanceUtils1(JavascriptExecutor jsx, String ENV, String browser, String BUILD, String PROJECT,
			String TESTURL, boolean dynaTrace, boolean browserTimers, String reportFolderPath) throws IOException {
		this.dtEnabled = dynaTrace;
		this.timersEnabled = browserTimers;
		String TestTURL = TESTURL.replace("http:", "").replace("https:", "").replaceAll("/", "");
		if (!PROJECT.isEmpty()) {
			String var10 = PROJECT + "_";
		}

		this.pageTimerInit(ENV, browser, PROJECT, BUILD, reportFolderPath);
		return TestTURL;
	}

	// test url replace
	public String PerformanceUtils1withCache(JavascriptExecutor jsx, String ENV, String browser, String BUILD,
			String PROJECT, String TESTURL, boolean dynaTrace, boolean browserTimers, String reportFolderPath)
			throws IOException {
		this.dtEnabled = dynaTrace;
		this.timersEnabled = browserTimers;
		String TestTURL = TESTURL.replace("http:", "").replace("https:", "").replaceAll("/", "");
		if (!PROJECT.isEmpty()) {
			String var10 = PROJECT + "_";
		}

		this.pageTimerInitWithCache(ENV, browser, PROJECT, BUILD, reportFolderPath);
		return TestTURL;
	}

	public FileWriter pageTimerInit(String ENV, String browser, String PROJECT, String BUILD, String reportFolderPath)
			throws IOException {
		if (!this.timersEnabled) {
			return this.timerlog;
		} else {
			SimpleDateFormat dtFmt = new SimpleDateFormat("yyMMdd");
			this.dateTime = new Date();
			this.requiredFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS");
//			this.requiredFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
			this.DATE = dtFmt.format(this.dateTime).toString();
			String filename = this.DATE + "_DOTCOM" + ENV + "_" + browser + "_" + PROJECT + BUILD;
			String filen = "sample";
			logger.info("filename" + filename);
			this.TESTAGENT = ((String) System.getenv().get("COMPUTERNAME")).toString();
			logger.info("TESTAGENT" + this.TESTAGENT);
			String LogPath = reportFolderPath;

			try {
				String PathForPerformancePageTimerLogs = properties.getProperty("PathForPerformancePageTimerLogs");
				if (System.getProperty("user.name").equals("dharik")) {
					this.timerlog = new FileWriter(PathForPerformancePageTimerLogs + filename + ".log");
				} else {
					this.timerlog = new FileWriter(LogPath + "\\" + filename + ".log");
					logger.info("timerlog" + this.timerlog.getEncoding());
				}

				this.timerlog.append("\n\n-------------" + filename + "-------------\n\n");
				this.timerlog.append(
						"Date/Time\tBrowser\tEnvironment\tBuild\tPageName\tTimeToFirstByte\tFirstImpression\tonLoadTime\tTotalLoadTime\tRedirectTime\tCacheLookup\tDNSLookup\tTCPConnect\tRequestSubmit\tServerTime\tResponseTransmitTime\tDOMLoadingtoInteractive\tDOMLoadingtoLoaded\tDOMLoadingtoComplete\tonLoadExecuteTime\tunLoadTime\tTestURL\tTestAgent\tConnectTime\tTotalServerTime\tClientonLoadTime\tClientTotalTime\tInitialConnection\tDomComplete\tDownloadTime\n");
				this.timerlog.flush();
			} catch (IOException var11) {
				var11.printStackTrace();
				logger.info("Failed to write performance metrics to file.");
			}

			return this.timerlog;
		}
	}

	public void pageTimerEnd() {
		if (this.timersEnabled) {
			try {
				this.timerlog.close();
			} catch (IOException var2) {
				var2.printStackTrace();
				logger.info("Failed to close Page Timer File.");
			}

		}
	}

	// write log file
	public FileWriter pageTimerInitWithCache(String ENV, String browser, String PROJECT, String BUILD,
			String reportFolderPath) throws IOException {
		if (!this.timersEnabled) {
			return this.timerlog;
		} else {
			SimpleDateFormat dtFmt = new SimpleDateFormat("yyMMdd");
			this.dateTime = new Date();
			this.requiredFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS");
//			this.requiredFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
			this.DATE = dtFmt.format(this.dateTime).toString();
			String filename = this.DATE + "_DOTCOM" + ENV + "_" + browser + "_" + PROJECT + BUILD;
			String filen = "sample";
			logger.info("filename" + filename);
			this.TESTAGENT = ((String) System.getenv().get("COMPUTERNAME")).toString();
			logger.info("TESTAGENT" + this.TESTAGENT);
			String LogPath = reportFolderPath;

			try {
				String PathForPerformancePageTimerLogs = properties.getProperty("PathForPerformancePageTimerLogs");
				if (System.getProperty("user.name").equals("dharik")) {
					this.timerlog = new FileWriter(PathForPerformancePageTimerLogs + filename + ".log");
				} else {
					this.timerlog = new FileWriter(LogPath + "\\" + filename + ".log");
					logger.info("timerlog" + this.timerlog.getEncoding());
				}

				this.timerlog.append("\n\n-------------" + filename + "-------------\n\n");
				this.timerlog.append(
						"Date/Time\tBrowser\tEnvironment\tBuild\tPageName\tTimeToFirstByte\tFirstImpression\tonLoadTime\tTotalLoadTime\tRedirectTime\tCacheLookup\tDNSLookup\tTCPConnect\tRequestSubmit\tServerTime\tResponseTransmitTime\tDOMLoadingtoInteractive\tDOMLoadingtoLoaded\tDOMLoadingtoComplete\tonLoadExecuteTime\tunLoadTime\tTestURL\tTestAgent\tConnectTime\tTotalServerTime\tClientonLoadTime\tClientTotalTime\tInitialConnection\tDomComplete\tDownloadTime\n");
				this.timerlog.flush();
			} catch (IOException var11) {
				var11.printStackTrace();
				logger.info("Failed to write performance metrics to file.");
			}

			return this.timerlog;
		}
	}

	public JSONObject pageTimer(String PageName, String browser, String TESTURL, String BUILD, String ENV, long SLA)
			throws IOException, JSONException {
		int temp = 0;
		int script = 0;
		int link = 0;
		int img = 0;
		int video = 0;
		int css = 0;
		int textxml = 0;
		int iframe = 0;
		int other = 0;
		JSONObject browserdata = new JSONObject();
		logger.info(" Page Name ****** " + PageName);
		long navigationStart = -1L;
		long redirectStart = -1L;
		long redirectEnd = -1L;
		long unloadEventStart = -1L;
		long unloadEventEnd = -1L;
		long loadEventEnd = -1L;
		long fetchStart = -1L;
		long connectEnd = -1L;
		long connectStart = -1L;
		long domainLookupEnd = -1L;
		long domainLookupStart = -1L;
		long requestStart = -1L;
		long responseStart = -1L;
		long domInteractive = -1L;
		long domLoading = -1L;
		long msFirstPaint = -1L;
		long responseEnd = -1L;
		long domContentLoadedEventStart = -1L;
		long domComplete = -1L;
		long domContentLoadedEventEnd = -1L;
		long loadEventStart = -1L;
		boolean loading = true;
		String Time = null;
		TimeZone timeZone1 = TimeZone.getTimeZone("America/New_York");
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeZone(timeZone1);
		String Hour = String.valueOf(calendar.get(11));
		String Min = String.valueOf(calendar.get(12));
		Date dateTime = new Date();
		if (Min.length() == 1) {
			(new StringBuilder(String.valueOf(Hour))).append(":0").append(Min).toString();
		} else {
			(new StringBuilder(String.valueOf(Hour))).append(":").append(Min).toString();
		}

		while (loading) {
			try {
				Thread.sleep(3500L);
				logger.info("BROWSER==" + browser);
				logger.info("ENV==" + ENV);
				logger.info("BUILD==" + BUILD);
				logger.info("TESTURL==" + TESTURL);
				logger.info("TESTAGENT==" + this.TESTAGENT);
				logger.info("loading...loadEventEnd: " + loadEventEnd);
				loadEventEnd = Long.valueOf(this.js
						.executeScript("return window.performance.timing.loadEventEnd;", new Object[0]).toString());
				Thread.sleep(1500L);
				if (loadEventEnd != this.lastLoadTime && loadEventEnd > 0L) {
					loading = false;
					this.lastLoadTime = loadEventEnd;
				} else {
					logger.info("loading... lastLoadTime: " + this.lastLoadTime + "\n           loadEventEnd: "
							+ loadEventEnd);
				}
			} catch (Exception var114) {
				logger.info("Exception : " + var114.getMessage());
			}
		}

		try {

			navigationStart = Long.valueOf(this.js
					.executeScript("return window.performance.timing.navigationStart;", new Object[0]).toString());
		} catch (Exception var113) {
			navigationStart = Long.valueOf(
					this.js.executeScript("return window.performance.timing.fetchStart;", new Object[0]).toString());
		}

		try {
			redirectStart = Long.valueOf(
					this.js.executeScript("return window.performance.timing.redirectStart;", new Object[0]).toString());
		} catch (Exception var112) {
			redirectStart = -1L;
		}

		try {
			redirectEnd = Long.valueOf(
					this.js.executeScript("return window.performance.timing.redirectEnd;", new Object[0]).toString());
		} catch (Exception var111) {
			redirectEnd = -1L;
		}

		try {
			unloadEventStart = Long.valueOf(this.js
					.executeScript("return window.performance.timing.unloadEventStart;", new Object[0]).toString());
		} catch (Exception var110) {
			unloadEventStart = -1L;
		}

		try {
			unloadEventEnd = Long.valueOf(this.js
					.executeScript("return window.performance.timing.unloadEventEnd;", new Object[0]).toString());
		} catch (Exception var109) {
			unloadEventEnd = -1L;
		}

		try {
			fetchStart = Long.valueOf(
					this.js.executeScript("return window.performance.timing.fetchStart;", new Object[0]).toString());
			if (navigationStart <= 0L) {
				navigationStart = fetchStart;
			}
		} catch (Exception var108) {
			fetchStart = -1L;
		}

		try {
			connectEnd = Long.valueOf(
					this.js.executeScript("return window.performance.timing.connectEnd;", new Object[0]).toString());
		} catch (Exception var107) {
			connectEnd = -1L;
		}

		try {
			connectStart = Long.valueOf(
					this.js.executeScript("return window.performance.timing.connectStart;", new Object[0]).toString());
		} catch (Exception var106) {
			connectStart = -1L;
		}

		try {
			domainLookupEnd = Long.valueOf(this.js
					.executeScript("return window.performance.timing.domainLookupEnd;", new Object[0]).toString());
		} catch (Exception var105) {
			domainLookupEnd = -1L;
		}

		try {
			domainLookupStart = Long.valueOf(this.js
					.executeScript("return window.performance.timing.domainLookupStart;", new Object[0]).toString());
		} catch (Exception var104) {
			domainLookupStart = -1L;
		}

		try {
			requestStart = Long.valueOf(
					this.js.executeScript("return window.performance.timing.requestStart;", new Object[0]).toString());
		} catch (Exception var103) {
			requestStart = -1L;
		}

		try {
			responseStart = Long.valueOf(
					this.js.executeScript("return window.performance.timing.responseStart;", new Object[0]).toString());
		} catch (Exception var102) {
			responseStart = -1L;
		}

		try {
			domInteractive = Long.valueOf(this.js
					.executeScript("return window.performance.timing.domInteractive;", new Object[0]).toString());
		} catch (Exception var101) {
			domInteractive = -1L;
		}

		try {
			domLoading = Long.valueOf(
					this.js.executeScript("return window.performance.timing.domLoading;", new Object[0]).toString());
		} catch (Exception var100) {
			domLoading = -1L;
		}

		try {
			msFirstPaint = Long.valueOf(
					this.js.executeScript("return window.performance.timing.msFirstPaint;", new Object[0]).toString());
		} catch (Exception var99) {
			msFirstPaint = -1L;
		}

		try {
			responseEnd = Long.valueOf(
					this.js.executeScript("return window.performance.timing.responseEnd;", new Object[0]).toString());
		} catch (Exception var98) {
			responseEnd = -1L;
		}

		try {
			domContentLoadedEventStart = Long.valueOf(
					this.js.executeScript("return window.performance.timing.domContentLoadedEventStart;", new Object[0])
							.toString());
		} catch (Exception var97) {
			domContentLoadedEventStart = -1L;
		}

		try {
			domComplete = Long.valueOf(
					this.js.executeScript("return window.performance.timing.domComplete;", new Object[0]).toString());
		} catch (Exception var96) {
			domComplete = -1L;
		}

		try {
			domContentLoadedEventEnd = Long.valueOf(
					this.js.executeScript("return window.performance.timing.domContentLoadedEventEnd;", new Object[0])
							.toString());
		} catch (Exception var95) {
			domContentLoadedEventEnd = -1L;
		}

		try {
			loadEventStart = Long.valueOf(this.js
					.executeScript("return window.performance.timing.loadEventStart;", new Object[0]).toString());
			logger.info("LoadEventStart : " + loadEventStart);
		} catch (Exception var94) {
			loadEventStart = -1L;
		}

		String resourceAPI;
		try {
			resourceAPI = (String) this.js
					.executeScript("return JSON.stringify(performance.getEntriesByType('resource'))", new Object[0]);
			/*
			 * logger.info("   resourceAPI: " + this.js
			 * .executeScript("return JSON.stringify(performance.getEntriesByType('resource'))"
			 * , new Object[0]));
			 */
		} catch (Exception var93) {
			resourceAPI = (String) this.js
					.executeScript("return JSON.stringify(performance.getEntriesByType('resource'))", new Object[0]);
			var93.printStackTrace();
		}

		JSONArray jsonarray = new JSONArray(resourceAPI);
		// logger.info("Length=========>" + jsonarray.length());

		// logger.info("JSON Array:");

		/*
		 * for(int i=0;i<jsonarray.length();i++) {
		 * logger.info("JSON Array Resource API: "+jsonarray.get(i).toString()); }
		 */

		String tcpConnect;
		for (int i = 0; i < jsonarray.length(); ++i) {
			org.json.JSONObject obj = jsonarray.getJSONObject(i);
			Integer size = 0; // try catch added to handle exception for IE
			try {
				size = (Integer) obj.get("transferSize");
				temp += size;
			} catch (Exception ex) {
				logger.info("Transfer Size " + temp);
			}
			tcpConnect = (String) obj.get("initiatorType");
			if (tcpConnect.equalsIgnoreCase("script")) {
				++script;
			} else if (tcpConnect.equalsIgnoreCase("link")) {
				++link;
			} else if (tcpConnect.equalsIgnoreCase("img")) {
				++img;
			} else if (tcpConnect.equalsIgnoreCase("video")) {
				++video;
			} else if (tcpConnect.equalsIgnoreCase("css")) {
				++css;
			} else if (tcpConnect.equalsIgnoreCase("xmlhttprequest")) {
				++textxml;
			} else if (tcpConnect.equalsIgnoreCase("iframe")) {
				++iframe;
			} else {
				++other;
			}
		}

		logger.info("Script : " + script);
		logger.info("Link : " + link);
		logger.info("Img : " + img);
		logger.info("Video : " + video);
		logger.info("Css : " + css);
		logger.info("XmlHttpRequest : " + textxml);
		logger.info("Iframe : " + iframe);
		logger.info("Other : " + other);

		String redirect = "";
		String AppCache = "";
		String dnsLookup = "";
		tcpConnect = "";
		String requestTime = "";
		String serverTime = "";
		String responseTransmit = "";
		String domLoad2Inter = "";
		String domLoad2DomLoaded = "";
		String domLoad2Complete = "";
		String onloadExecute = "";
		String ttFirstByte = "";
		String ttFirstImpression = "";
		String ttOnLoad = "";
		String Total = "";
		String unLoadTime = "";
		String connectTime = "";
		String totalServerTime = "";
		String clientOnLoadTime = "";
		String clientTotalTime = "";
		String initialConnection = "";
		String DomComplete = "";
		String DownloadTime = "";

		if (requestStart >= 0L || navigationStart >= 0L) {
			initialConnection = String.valueOf(requestStart - navigationStart);
			logger.info("initialConnection: " + initialConnection);
		}

		if (requestStart >= 0L || responseStart >= 0L) {
			ttFirstByte = String.valueOf(responseStart - requestStart);
		}

		if (responseEnd >= 0L || responseStart >= 0L) {
			DownloadTime = String.valueOf(responseEnd - responseStart);
			logger.info("DownloadTime: " + DownloadTime);
		}

		if (domInteractive >= 0L || domLoading >= 0L) {
			domLoad2Inter = String.valueOf(domInteractive - domLoading);
		}

		if (domContentLoadedEventEnd >= 0L || domLoading >= 0L) {
			domLoad2DomLoaded = String.valueOf(domContentLoadedEventEnd - domLoading);
		}

		if (domComplete >= 0L || domLoading >= 0L) {
			domLoad2Complete = String.valueOf(domComplete - domContentLoadedEventEnd);
		}

		if (loadEventEnd >= 0L || navigationStart >= 0L) {
			Total = String.valueOf(loadEventEnd - navigationStart);
		}

		if (this.timersEnabled && !PageName.isEmpty()) {
			try {
				this.timerlog.append(this.requiredFormat.format(dateTime) + "\t" + browser + "\t" + ENV + "\t" + BUILD
						+ "\t" + PageName + "\t" + ttFirstByte + "\t" + ttFirstImpression + "\t" + ttOnLoad + "\t"
						+ Total + "\t" + redirect + "\t" + AppCache + "\t" + dnsLookup + "\t" + tcpConnect + "\t"
						+ requestTime + "\t" + serverTime + "\t" + responseTransmit + "\t" + domLoad2Inter + "\t"
						+ domLoad2DomLoaded + "\t" + domLoad2Complete + "\t" + onloadExecute + "\t" + unLoadTime + "\t"
						+ TESTURL + "\t" + this.TESTAGENT + "\t" + connectTime + "\t" + totalServerTime + "\t"
						+ clientOnLoadTime + "\t" + clientTotalTime + "\t" + initialConnection + "\t" + DomComplete
						+ "\t" + DownloadTime + "\n");
				this.timerlog.flush();
			} catch (IOException var92) {
				logger.info("***Failed to wrtite Performance Timings to file.***");
				var92.printStackTrace();
			}
		}

		logger.info(
				"\t" + PageName + " Loaded in: " + Total + "ms\t\t(" + loadEventEnd + " - " + navigationStart + ")");
		if (navigationStart == 0L || loadEventEnd == 0L || loadEventEnd - navigationStart < 0L
				|| loadEventEnd - navigationStart > 80000L) {
			logger.info("   responseEnd: " + responseEnd);
			logger.info("   msFirstPaint: " + msFirstPaint);
			logger.info("   domLoading: " + domLoading);
			logger.info("   domComplete: " + domComplete);
			logger.info("   domInteractive: " + domInteractive);
			logger.info("   responseStart: " + responseStart);
			logger.info("   requestStart: " + requestStart);
			logger.info("   domainLookupStart: " + domainLookupStart);
			logger.info("   domainLookupEnd: " + domainLookupEnd);
			logger.info("   connectStart: " + connectStart);
			logger.info("   domContentLoadedEventEnd: " + domContentLoadedEventEnd);
			logger.info("   connectEnd: " + connectEnd);
			logger.info("   fetchStart: " + fetchStart);
			logger.info("   unloadEventEnd: " + unloadEventEnd);
			logger.info("   unloadEventStart: " + unloadEventStart);
			logger.info("   redirectEnd: " + redirectEnd);
			logger.info("   redirectStart: " + redirectStart);
			logger.info("   loadEventStart: " + loadEventStart);
			logger.info("   navigationStart: " + navigationStart);
			logger.info("   loadEventEnd: " + loadEventEnd);
			logger.info("   domContentLoadedEventStart: " + domContentLoadedEventStart);

			try {
				Thread.sleep(30000L);
			} catch (InterruptedException var91) {
				logger.info("Exception : " + var91.getMessage());
			}
		}

		browserdata.put("DOMLoadingtoComplete", domLoad2Complete);
		browserdata.put("DOMLoadingtoLoaded", domLoad2DomLoaded);
		browserdata.put("ClientonLoadTime", clientOnLoadTime);
		browserdata.put("onLoadTime", ttOnLoad);
		browserdata.put("CacheLookup", AppCache);
		browserdata.put("onLoadExecuteTime", onloadExecute);
		browserdata.put("PageName", PageName);
		browserdata.put("DNSLookup", dnsLookup);
		browserdata.put("RedirectTime", redirect);
		browserdata.put("Build", BUILD);
		browserdata.put("ResponseTransmitTime", responseTransmit);
		browserdata.put("unLoadTime", unLoadTime);
		browserdata.put("InitialConnection", initialConnection);
		browserdata.put("ConnectTime", connectTime);
		browserdata.put("TestAgent", this.TESTAGENT);
		browserdata.put("TimeToFirstByte", ttFirstByte);
		browserdata.put("ClientTotalTime", clientTotalTime);
		browserdata.put("DomComplete", DomComplete);
		browserdata.put("TotalLoadTime", Total);
		browserdata.put("TCPConnect", tcpConnect);
		browserdata.put("Date/Time", this.requiredFormat.format(dateTime));
		browserdata.put("RequestSubmit", requestTime);
		browserdata.put("ServerTime", serverTime);
		browserdata.put("TestURL", TESTURL);
		browserdata.put("DownloadTime", DownloadTime);
		browserdata.put("Environment", ENV);
		browserdata.put("FirstImpression", ttFirstImpression);
		browserdata.put("TotalServerTime", totalServerTime);
		browserdata.put("Browser", browser);
		browserdata.put("DOMLoadingtoInteractive", domLoad2Inter);
		browserdata.put("PageSize", temp);
		browserdata.put("nwrequest", jsonarray.length());
		browserdata.put("script", script);
		browserdata.put("link", link);
		browserdata.put("img", img);
		browserdata.put("video", video);
		browserdata.put("css", css);
		browserdata.put("xmlhttprequest", textxml);
		browserdata.put("iframe", iframe);
		browserdata.put("other", other);
		browserdata.put("SLA", SLA);
		return browserdata;
	}

	public static long startJavaTimer() {
		start = System.currentTimeMillis();
		logger.info("start*********************" + start);
		return start;
	}

	public void CreateOverallSummary(String reportpath) throws FileNotFoundException, IOException, ParseException {
		org.json.simple.JSONArray arrayfinal = new org.json.simple.JSONArray();
		JSONObject summary1 = new JSONObject();
		JSONObject summary = new JSONObject();
		JSONObject performance = new JSONObject();
		int counter = 0;
		String responsejson = reportpath + "\\response.json";
		File responsepath = new File(responsejson);
		JSONParser jsonparser = new JSONParser();
		Object obj = jsonparser.parse(new FileReader(responsepath.getAbsolutePath()));
		org.json.simple.JSONArray response = (org.json.simple.JSONArray) obj;

		for (int i = 0; i < response.size(); ++i) {
			JSONObject log = (JSONObject) response.get(i);
			long responsetime = (Long) log.get("responsetime");// SLA

			logger.info("page response time: " + responsetime);
			if (responsetime > (Long) log.get("SLA")) {
				++counter;
			}
		}

		logger.info("Counter : " + counter);
		performance.put("slaviolatedpages", counter);
		performance.put("totalpages", response.size());
		Set<String> uniquelist = new HashSet<>();
		List<String> duplicatelist = new ArrayList<>();
		String recommendations = reportpath + "\\recommendations.json";
		JSONParser parser2 = new JSONParser();
		Object obj2 = parser2.parse(new FileReader(recommendations));
		org.json.simple.JSONArray recomarray = (org.json.simple.JSONArray) obj2;
		List<String> addall = new ArrayList<>();

		JSONObject ruleHeader;
		for (int j = 0; j < recomarray.size(); ++j) {
			JSONObject innerarray = (JSONObject) recomarray.get(j);
			org.json.simple.JSONArray recommendation = (org.json.simple.JSONArray) innerarray.get("recommendation");

			for (int r = 0; r < recommendation.size(); ++r) {
				ruleHeader = (JSONObject) recommendation.get(r);
				String Recommendation = (String) ruleHeader.get("Recommendation");
				if (!Recommendation.equalsIgnoreCase("none")) {
					String rules = (String) ruleHeader.get("ruleHeader");
					logger.info("rules**************************" + rules);
					addall.add(rules);
				}
			}
		}

		Iterator var34 = addall.iterator();

		while (var34.hasNext()) {
			String n = (String) var34.next();
			if (!uniquelist.add(n)) {
				duplicatelist.add(n);
			}
		}

		ArrayList<String> newList = removeDuplicates(duplicatelist);
		logger.info("New List : " + newList);
		performance.put("rules", addall);
		summary1.put("performance", performance);
		arrayfinal.add(summary1);
		summary.put("summary", arrayfinal);
		logger.info("summary-------->" + summary);
		String summaryindexfile = reportpath + "\\summaryindex.json";
		File perf = new File(summaryindexfile);
		if (perf.exists()) {
			logger.info("summaryindex.json File is created");
		}

		ruleHeader = null;
		FileWriter writerperffile = null;
		try {
			writerperffile = new FileWriter(perf.getAbsolutePath());
			writerperffile.write(summary.toString());
			writerperffile.flush();
			writerperffile.close();
		} catch (Exception var28) {
			var28.printStackTrace();
		} finally {
			writerperffile.close();
		}
	}

	public static ArrayList<String> removeDuplicates(List<String> duplicatelist) {
		ArrayList<String> newList = new ArrayList();
		Iterator var3 = duplicatelist.iterator();

		while (var3.hasNext()) {
			String element = (String) var3.next();
			if (!newList.contains(element)) {
				newList.add(element);
			}
		}

		return newList;
	}

	public static JSONObject stopJavaTimer(WebDriver driver, String pagename, long SLA, String testcasename,
			int iterationNumber) throws InterruptedException {
		JSONObject timer = new JSONObject();
		int count = 0;
		long timeWaitedChecking = 0L;
		long visbilityStart = System.currentTimeMillis();
		boolean visibility = checkVisbility(driver);
		checkInVisbility(driver);
		long visbilityStop = System.currentTimeMillis();
		long totalTime;

		if (visibility) {
			logger.info("Element size is != 0");
			finish = System.currentTimeMillis();
			logger.info(start + "::" + finish);
			totalTime = finish - start - (visbilityStop - visbilityStart);
			logger.info("totalTime:" + totalTime);
			timer.put("responsetime", totalTime);
			timer.put("pagename", pagename);
			timer.put("SLA", SLA);
			timer.put("testcasename", testcasename);
			timer.put("IterationNumber", iterationNumber);
			if (SLA < totalTime) {
				timer.put("status", "FAIL");
			} else {
				timer.put("status", "PASS");
			}

		} else {
			while (true) {
				visbilityStart = System.currentTimeMillis();
				visibility = checkVisbility(driver);
				boolean invisibility = checkInVisbility(driver);
				visbilityStop = System.currentTimeMillis();
				timeWaitedChecking += visbilityStop - visbilityStart;
				if (invisibility) {
					finish = System.currentTimeMillis();
					totalTime = finish - start - timeWaitedChecking;
					logger.info("totalTime:" + totalTime);
					timer.put("responsetime", totalTime);
					timer.put("pagename", pagename);
					timer.put("SLA", SLA);
					timer.put("testcasename", testcasename);
					timer.put("IterationNumber", iterationNumber);
					if (SLA < totalTime) {
						timer.put("status", "FAIL");
					} else {
						timer.put("status", "PASS");
					}
					break;
				}

				++count;

			}
		}

		return timer;

	}

	public static JSONObject stopJavaTimer(WebDriver driver, String pagename, long SLA) throws InterruptedException {
		JSONObject timer = new JSONObject();
		int count = 0;
		long timeWaitedChecking = 0L;
		long visbilityStart = System.currentTimeMillis();
		boolean visibility = checkVisbility(driver);
		checkInVisbility(driver);
		long visbilityStop = System.currentTimeMillis();
		long totalTime;

		if (visibility) {
			logger.info("Element size is != 0");
			finish = System.currentTimeMillis();
			logger.info(start + "::" + finish);
			totalTime = finish - start - (visbilityStop - visbilityStart);
			logger.info("totalTime:" + totalTime);
			timer.put("responsetime", totalTime);
			timer.put("pagename", pagename);
			timer.put("SLA", SLA);

		} else {
			while (true) {

				visbilityStart = System.currentTimeMillis();
				visibility = checkVisbility(driver);
				boolean invisibility = checkInVisbility(driver);
				visbilityStop = System.currentTimeMillis();
				timeWaitedChecking += visbilityStop - visbilityStart;
				if (visibility) {
					finish = System.currentTimeMillis();
					totalTime = finish - start - timeWaitedChecking;
					logger.info("totalTime:" + totalTime);
					timer.put("responsetime", totalTime);
					timer.put("pagename", pagename);
					timer.put("SLA", SLA);
					break;
				}

				if (invisibility) {
					logger.info(pagename + " Waited for 0.5 seconds !!!");
					Thread.sleep(500L);
				}

				if (count == 30) {
					logger.info(pagename + " Waited for 15 seconds !!! Page not loaded completely!!");
					break;
				}

				++count;

			}
		}

		return timer;

	}

	/*
	 * public static void waitUntilPageReadyStateComplete(long timeOutInSeconds) {
	 * ExpectedCondition<Boolean> pageReadyStateComplete = new
	 * ExpectedCondition<Boolean>() { public Boolean apply(WebDriver driver) {
	 * return ((JavascriptExecutor) driver).executeScript(
	 * "return document.readyState == 'complete' && jQuery.active == 0").equals(true
	 * ); } }; (new WebDriverWait(getWebdriver(),
	 * timeOutInSeconds)).until(pageReadyStateComplete); }
	 */

	public static JSONObject stopJavaTimer(WebDriver driver, By element, String pagename, long SLA, String testcasename,
			int iterationNumber) throws InterruptedException {
		JSONObject timer = new JSONObject();
		int count = 0;
		long timeWaitedChecking = 0L;
		long visbilityStart = System.currentTimeMillis();
		boolean visibility = checkVisbility(driver, element);
		// checkInVisbility(driver, element); //commented to triage the total time
		long visbilityStop = System.currentTimeMillis();
		long totalTime;
		if (visibility) {
			logger.info("Element size is != 0");
			finish = System.currentTimeMillis();
			logger.info(start + "::" + finish);
			totalTime = finish - start - (visbilityStop - visbilityStart);
			logger.info("totalTime:" + totalTime);
			timer.put("responsetime", totalTime);
			timer.put("pagename", pagename);
			timer.put("SLA", SLA);
			timer.put("testcasename", testcasename);
			timer.put("IterationNumber", iterationNumber);
			if (SLA < totalTime) {
				timer.put("status", "FAIL");
			} else {
				timer.put("status", "PASS");
			}
		} else {
			while (true) {
				visbilityStart = System.currentTimeMillis();
				visibility = checkVisbility(driver, element);
				boolean invisibility = checkInVisbility(driver, element);
				visbilityStop = System.currentTimeMillis();
				timeWaitedChecking += visbilityStop - visbilityStart;
				if (visibility) {
					finish = System.currentTimeMillis();
					totalTime = finish - start - timeWaitedChecking;
					logger.info("totalTime:" + totalTime);
					timer.put("responsetime", totalTime);
					timer.put("pagename", pagename);
					timer.put("SLA", SLA);
					timer.put("testcasename", testcasename);
					timer.put("IterationNumber", iterationNumber);
					if (SLA < totalTime) {
						timer.put("status", "FAIL");
					} else {
						timer.put("status", "PASS");
					}
					break;
				}

				if (invisibility) {
					logger.info("Waited for 0.5 seconds !!!");
					Thread.sleep(500L);
				}

				if (count == 30) {
					logger.info("Waited for 15 seconds !!! Unable to find the element");
					break;
				}

				++count;
			}
		}

		return timer;
	}

	public static JSONObject stopJavaTimer(WebDriver driver, String element, String pagename, long SLA)
			throws InterruptedException {
		JSONObject timer = new JSONObject();
		int count = 0;
		long timeWaitedChecking = 0L;
		long visbilityStart = System.currentTimeMillis();
		boolean visibility = checkVisbility(driver, element);
		checkInVisbility(driver, element);
		long visbilityStop = System.currentTimeMillis();
		long totalTime;
		if (visibility) {
			logger.info("Element size is != 0");
			finish = System.currentTimeMillis();
			logger.info(start + "::" + finish);
			totalTime = finish - start - (visbilityStop - visbilityStart);
			logger.info("totalTime:" + totalTime);
			timer.put("responsetime", totalTime);
			timer.put("pagename", pagename);
			timer.put("SLA", SLA);
		} else {
			while (true) {
				visbilityStart = System.currentTimeMillis();
				visibility = checkVisbility(driver, element);
				boolean invisibility = checkInVisbility(driver, element);
				visbilityStop = System.currentTimeMillis();
				timeWaitedChecking += visbilityStop - visbilityStart;
				if (visibility) {
					finish = System.currentTimeMillis();
					totalTime = finish - start - timeWaitedChecking;
					logger.info("totalTime:" + totalTime);
					timer.put("responsetime", totalTime);
					timer.put("pagename", pagename);
					timer.put("SLA", SLA);
					break;
				}

				if (invisibility) {
					logger.info("Waited for 0.5 seconds !!!");
					Thread.sleep(500L);
				}

				if (count == 30) {
					logger.info("Waited for 15 seconds !!! Unable to find the element");

					break;
				}

				++count;
			}
		}

		return timer;
	}

	public static boolean checkVisbility(WebDriver driver, String element) {
		boolean visibility = driver.findElements(By.id(element)).size() != 0
				|| driver.findElements(By.xpath(element)).size() != 0
				|| driver.findElements(By.name(element)).size() != 0;
		return visibility;
	}

	public static boolean checkInVisbility(WebDriver driver, String element) {
		boolean invisibility = driver.findElements(By.id(element)).size() == 0
				|| driver.findElements(By.name(element)).size() == 0
				|| driver.findElements(By.xpath(element)).size() == 0;

		return invisibility;
	}

	public static boolean checkVisbility(WebDriver driver, By element) {
		boolean visibility = driver.findElements(element).size() != 0;
		return visibility;
	}

	public static boolean checkInVisbility(WebDriver driver, By element) {
		boolean invisibility = driver.findElements(element).size() == 0;
		return invisibility;
	}

	public static boolean checkVisbility(WebDriver driver) throws InterruptedException {

		return ((JavascriptExecutor) driver)
				.executeScript(
						"return document.readyState == 'complete' && window.jQuery != undefined && jQuery.active === 0")
				.equals(true);

	}

	public static boolean checkInVisbility(WebDriver driver) throws InterruptedException {

		return ((JavascriptExecutor) driver)
				.executeScript(
						"return document.readyState == 'complete' && window.jQuery != undefined && jQuery.active === 0")
				.equals(false);
		// "return document.readyState == 'complete' && jQuery.active ===
		// 0").equals(false);
	}

	public void CreatePerformanceSummary(String reportPath) throws FileNotFoundException, IOException, ParseException {
		org.json.simple.JSONArray arrayfinal = new org.json.simple.JSONArray();
		JSONObject summary1 = new JSONObject();
		JSONObject summary2 = new JSONObject();
		JSONObject summary = new JSONObject();
		JSONObject performance = new JSONObject();
		int counter = 0;
		String responsejson = reportPath + "\\response.json";
		JSONParser jsonparser = new JSONParser();
		Object obj = jsonparser.parse(new FileReader(responsejson));
		logger.info("Object : "+obj);
		org.json.simple.JSONArray response = (org.json.simple.JSONArray) obj;

		for (int i = 0; i < response.size(); ++i) {
			JSONObject log = (JSONObject) response.get(i);
			long responsetime = (Long) log.get("responsetime");
			logger.info("Response time : "+responsetime);
			if (responsetime > (Long) log.get("SLA")) {
				++counter;
			}
		}

		logger.info("Counter : "+counter);
		performance.put("slaviolatedpages", counter);
		performance.put("totalpages", response.size());
		Set<String> uniquelist = new HashSet();
		List<String> duplicatelist = new ArrayList();
		String recommendations = reportPath + "\\recommendations.json";
		JSONParser parser2 = new JSONParser();
		Object obj2 = parser2.parse(new FileReader(recommendations));
		logger.info("Object : "+obj2);
		org.json.simple.JSONArray recomarray = (org.json.simple.JSONArray) obj2;
		new HashSet();
		List<String> addall = new ArrayList();

		for (int j = 0; j < recomarray.size(); ++j) {
			JSONObject innerarray = (JSONObject) recomarray.get(j);
			org.json.simple.JSONArray recommendation = (org.json.simple.JSONArray) innerarray.get("recommendation");

			for (int r = 0; r < recommendation.size(); ++r) {
				JSONObject ruleHeader = (JSONObject) recommendation.get(r);
				String Recommendation = (String) ruleHeader.get("Recommendation");
				if (!Recommendation.equalsIgnoreCase("none")) {
					String rules = (String) ruleHeader.get("ruleHeader");
					addall.add(rules);
				}
			}
		}

		Iterator var33 = addall.iterator();

		while (var33.hasNext()) {
			String n = (String) var33.next();
			if (!uniquelist.add(n)) {
				duplicatelist.add(n);
			}
		}

		ArrayList<String> newList = removeDuplicates(duplicatelist);
		performance.put("rules", newList);
		summary1.put("performance", performance);
		arrayfinal.add(summary1);
		arrayfinal.add(summary2);
		summary.put("summary", arrayfinal);
		logger.info("summary -------> " + summary);
		String summaryindexfile = reportPath + "\\summaryindex.json";
		File perf = new File(summaryindexfile);
		if (perf.exists()) {
			logger.info("summaryindex.json File is created");
		}

		FileWriter writerperffile = null;

		try {
			writerperffile = new FileWriter(perf);
			writerperffile.write(summary.toString());
			writerperffile.flush();

		} catch (Exception var27) {
			var27.printStackTrace();
		} finally {
			writerperffile.close();
		}

	}

	public String CreateResponseJson(org.json.simple.JSONArray timerarray, String reportPath) throws IOException {
		String performancefile = reportPath + "\\response.json";
		File perf = new File(performancefile);
		if (perf.exists()) {
			logger.info("response.json File is created");
		}

		FileWriter writerperffile = null;

		try {
			writerperffile = new FileWriter(perf);
			writerperffile.write(timerarray.toString());
			writerperffile.flush();

		} catch (Exception var7) {
			var7.printStackTrace();
		} finally {
			writerperffile.close();
		}

		return performancefile;
	}

	// Create Har file
	public static JSONObject CreateHar(BrowserMobProxy proxy, String pagename, int counter, String reportPath)
			throws InterruptedException {
		JSONObject profiling = new JSONObject();
		String sFileName1 = null;
		new ArrayList();
		Thread.sleep(20000L);
		Har har1 = proxy.getHar();
		logger.info("captureType" + proxy.getHarCaptureTypes());
		sFileName1 = reportPath + "\\harfile.json";
		String sFileName2 = reportPath + "\\examples\\sample" + counter + ".har";
		File harfile1 = new File(sFileName1);
		File harfile2 = new File(sFileName2);

		try {
			har1.writeTo(harfile1);
			har1.writeTo(harfile2);
		} catch (IOException var33) {
			var33.printStackTrace();
		}

		proxy.endHar();
		JSONParser jsonparser = new JSONParser();

		try {
			Object obj = jsonparser.parse(new FileReader(harfile1.getAbsolutePath()));
			JSONObject jsonobject = (JSONObject) obj;
			JSONObject log = (JSONObject) jsonobject.get("log");
			org.json.simple.JSONArray entries = (org.json.simple.JSONArray) log.get("entries");
			logger.info("Size of entries : "+entries.size());
			int script = 0;
			int image = 0;
			int html = 0;
			int css = 0;
			int font = 0;
			int other = 0;
			long temp = 0L;
			int xhr = 0;
			int video = 0;

			for (int i = 0; i < entries.size(); ++i) {
				JSONObject entriesget = (JSONObject) entries.get(i);
				JSONObject response = (JSONObject) entriesget.get("response");
				JSONObject content = (JSONObject) response.get("content");
				if (content != null) {
					String type = (String) content.get("mimeType");
					long size = (Long) content.get("size");
					temp += size;
					if (!type.contains("javascript") && !type.contains("js")) {
						if (type.contains("image")) {
							++image;
						} else if (!type.contains("html") && !type.contains("text/plain")
								&& !type.contains("text/html")) {
							if (type.contains("css")) {
								++css;
							} else if (!type.contains("font") && !type.contains("woff2")
									&& !type.contains("application/octet-stream")) {
								if (type.contains("json")) {
									++xhr;
								} else if (type.contains("video")) {
									++video;
								} else {
									++other;
								}
							} else {
								++font;
							}
						} else {
							++html;
						}
					} else {
						++script;
					}
				}
			}

			if (script != 0) {
				profiling.put("javascript", script);
			}

			profiling.put("image", image);
			profiling.put("html", html);
			profiling.put("css", css);
			profiling.put("font", font);
			profiling.put("other", other);
			profiling.put("xhr", xhr);
			profiling.put("pagesize", temp);
			logger.info("other" + other);
			logger.info("pagesize" + temp);
			logger.info("nwrequest" + entries.size());
			profiling.put("nwrequest", entries.size());
			profiling.put("pagename", pagename);
			profiling.put("harpath", "examples\\sample" + counter + ".har");

			logger.info("script" + script);
			logger.info("image" + image);
			logger.info("html" + html);
			logger.info("css" + css);
			logger.info("font" + font);
			logger.info("xhr" + xhr);

		} catch (FileNotFoundException var34) {
			var34.printStackTrace();
		} catch (IOException var35) {
			var35.printStackTrace();
		} catch (ParseException var36) {
			var36.printStackTrace();
		}

		return profiling;
	}

	// Recommendations
	public static JSONObject Recommendations(String pagename, String reportPath) {
		String harfile = reportPath + "\\harfile.json";
		JSONParser parser = new JSONParser();
		JSONObject recommendation = new JSONObject();
		org.json.simple.JSONArray array = new org.json.simple.JSONArray();

		try {
			Object obj = parser.parse(new FileReader(harfile));
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject log = (JSONObject) jsonObject.get("log");
			org.json.simple.JSONArray entries = (org.json.simple.JSONArray) log.get("entries");
			NetworkRecommend t1 = new NetworkRecommend();
			t1.totalrequests = entries.size();
			t1.errorurls = t1.errorenousurls(entries);
			JSONObject rule1 = new JSONObject();
			rule1.put("ruleHeader", "Errorneous Requests");
			new ArrayList();
			new ArrayList();
			List<String> list400 = (List) t1.errorurls.get("404");
			List<String> list302 = (List) t1.errorurls.get("302");
			logger.info("Rule 4 Errorneous requests:");
			if (!list400.isEmpty()) {
				logger.info("Below errorenous requests(400/404) were observed:\n\n" + list400);
				String message = "";
				message = list400.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				rule1.put("Message", "Below resources have status code 400/404:\n"
						+ message.toString().replaceAll("http", "\n �http"));
				rule1.put("Recommendation", "Resolve 400/404 resources else remove the unwanted calls");
			} else {
				rule1.put("Message", "No 400/404 HTTP errors found");
				rule1.put("Recommendation", "none");
				logger.info("No errorenous requests were observed");
			}

			array.add(rule1);
			JSONObject rule2 = new JSONObject();
			rule2.put("ruleHeader", "Avoid Redirects");
			if (!list302.isEmpty()) {
				logger.info("Below requests with 302 status code were observed:\n\n" + list302);
				String message = "";
				message = list302.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				rule2.put("Message", "Status code 302 was observed for the url's:\n\n"
						+ message.toString().replaceAll("http", "\n �http"));
				rule2.put("Recommendation",
						"Provide direct url to the resource which will reduce the unwanted roundtrip of network calls.");
			} else {
				logger.info("No redirects were observed in the page");
				rule2.put("Message", "none");
				rule2.put("Recommendation", "none");
			}

			array.add(rule2);
			t1.condition3 = t1.findDuplicates(entries);
			JSONObject rule3 = new JSONObject();
			rule3.put("ruleHeader", "Avoid Duplicate calls");
			logger.info("Rule 3 Duplicate calls in the page:");
			if (!t1.condition3.isEmpty()) {
				logger.info("Below duplicate calls are observed in the page:\n\n" + t1.condition3);
				String message = "";
				message = t1.condition3.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				rule3.put("Message",
						"Below duplicate calls were observed:\n" + message.toString().replaceAll("http", "\n �http"));
				rule3.put("Recommendation",
						"Duplicate call needs to be avoided and also remove unnecessary network calls");
			} else {
				rule3.put("Message", "none");
				rule3.put("Recommendation", "none");
			}

			array.add(rule3);
			JSONObject rule4 = new JSONObject();
			t1.condition1 = t1.checkcachecontrol(entries);
			logger.info("Total number of requests in the page is :" + t1.totalrequests);
			logger.info("Rule 1 Cache Control:");
			rule4.put("ruleHeader", "Leverage Browsing Cache");
			boolean chk = false;
			String message = "";
			if (!((List) t1.condition1.get("Expiry")).isEmpty()) {
				logger.info("Expires Header is not mentioned for the below resources\n" + t1.condition1.get("Expiry"));
				message = "Url's without any expiry header:\n" + ((List) t1.condition1.get("Expiry")).toString()
						.substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				chk = true;
			}

			if (!((List) t1.condition1.get("CacheControl")).isEmpty()) {
				logger.info("Cache Control Header is not mentioned for the below resources"
						+ t1.condition1.get("CacheControl"));
				message = message + "Url's without cache control header:" + "\n"
						+ ((List) t1.condition1.get("CacheControl")).toString().substring(1).replaceFirst("]", "")
								.replaceAll(",", "\n");
				chk = true;
			}

			if (!((List) t1.condition1.get("CacheStatus")).isEmpty()) {
				logger.info("Below resources are having 304 as status code\n" + t1.condition1.get("CacheStatus"));
				message = message + "\n\nUrl's 304 status:\n" + ((List) t1.condition1.get("CacheStatus")).toString()
						.substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				chk = true;
			}

			if (!chk) {
				rule4.put("Message", "none");
				rule4.put("Recommendation", "none");
			} else {
				rule4.put("Message", message.toString().replaceAll("http", "\n �http"));
				rule4.put("Recommendation",
						"For having a good caching startegy, it is recommended to have cache control and expires header for all the resources.Also, as a best practice it is recommended that no resources should get 304 status.");
			}

			array.add(rule4);
			t1.condition2 = t1.findCompression(entries);
			JSONObject rule5 = new JSONObject();
			rule5.put("ruleHeader", "Apply Compression Technique");
			logger.info("Rule 2 Compression Check:");
			if (!t1.condition2.isEmpty()) {
				logger.info("Compression is not applied to below resources:\n" + t1.condition2);
				message = t1.condition2.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				rule5.put("Message", "No compression methodologies has been applied for the below URL's :\n"
						+ message.toString().replaceAll("http", "\n �http"));
				rule5.put("Recommendation",
						"It is recommended to apply gzip/deflate/br compression techniques to the resources by which we can minimize the amount of data getting transferred");
			} else {
				rule5.put("Message", "none");
				rule5.put("Recommendation", "none");
			}

			array.add(rule5);
			t1.cssurls = t1.getDomainurls(entries, ".css");
			JSONObject rule6 = new JSONObject();
			rule6.put("ruleHeader", "Combine CSS and JS");
			message = "";
			chk = false;
			Iterator var22 = t1.cssurls.keySet().iterator();

			while (var22.hasNext()) {
				String key = (String) var22.next();
				if (((List) t1.cssurls.get(key)).size() > 1) {
					chk = true;
					message = message + "\n\nBelow urls from the domain-" + key + " are the candidates for merging css:"
							+ "\n\n" + ((List) t1.cssurls.get(key)).toString().substring(1).replaceFirst("]", "")
									.replaceAll(",", "\n");
				}
			}

			t1.jsurls = t1.getDomainurls(entries, ".js");
			boolean chk1 = false;
			Iterator var23 = t1.jsurls.keySet().iterator();

			String Htmlcontent;
			while (var23.hasNext()) {
				Htmlcontent = (String) var23.next();
				if (((List) t1.jsurls.get(Htmlcontent)).size() > 1) {
					chk1 = true;
					message = message + "\n\nBelow urls from the domain-" + Htmlcontent
							+ " are the candidates for merging js:" + "\n\n" + ((List) t1.jsurls.get(Htmlcontent))
									.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				}
			}

			if (!chk && !chk1) {
				rule6.put("Message", "none");
				rule6.put("Recommendation", "none");
			} else {
				rule6.put("Message",
						"Please find the below URL: \n" + message.toString().replaceAll("http", "\n �http"));
				rule6.put("Recommendation",
						"Combine the candidate files into a single file or lesser multiple files which would reduce the number of network calls in the page.");
			}

			array.add(rule6);
			Htmlcontent = t1.findHtmlContent(entries);
			// logger.info(Htmlcontent);
			if (Htmlcontent != "") {
				boolean imprtcnt = false;
				Pattern pattern = Pattern.compile("@import", 2);
				Matcher matcher = pattern.matcher(Htmlcontent);

				int count;
				for (count = 0; matcher.find(); ++count) {
					;
				}

				int imprtcnt1 = count;
				Document html = null;
				Document body = null;
				Document head = null;
				html = Jsoup.parse(Htmlcontent);
				body = Jsoup.parse(html.getElementsByTag("body").toString());
				head = Jsoup.parse(html.getElementsByTag("head").toString());
				pattern = Pattern.compile(">registersod", 2);

				for (count = 0; matcher.find(); ++count) {
					;
				}

				int totSODCount = count;
				boolean headSODCount = false;
				matcher = pattern.matcher(html.getElementsByTag("head").toString().toLowerCase());
				pattern = Pattern.compile(">registersod", 2);

				for (count = 0; matcher.find(); ++count) {
					;
				}

				int emptyLinkCount = 0;
				int noscaleCount = 0;
				int totImgCount = html.select("img").size();
				List<String> noScaleImgs = new ArrayList();
				List<String> scaleImgs = new ArrayList();

				int i;
				for (i = 0; i < totImgCount; ++i) {
					if (html.select("img").get(i).attr("src") == "") {
						++emptyLinkCount;
					}

					if (html.select("img").get(i).attr("width") == "" && html.select("img").get(i).attr("height") == ""
							&& html.select("img").get(i).attr("style") == ""
							&& html.select("img").get(i).attr("src") != "") {
						++noscaleCount;
						noScaleImgs.add(html.select("img").get(i).attr("src"));
					} else {
						scaleImgs.add(html.select("img").get(i).attr("src"));
					}
				}

				for (i = 0; i < html.select("script[src]").size(); ++i) {
					if (html.select("script[src]").get(i).attr("src") == "") {
						++emptyLinkCount;
					}
				}

				for (i = 0; i < html.select("link[href]").size(); ++i) {
					if (html.select("link[href]").get(i).attr("href") == "") {
						++emptyLinkCount;
					}
				}

				JSONObject rule7 = new JSONObject();
				rule7.put("ruleHeader", "Empty SRC or HREF Tags");
				if (emptyLinkCount != 0) {
					rule7.put("Message", emptyLinkCount
							+ " instance(s) of empty SRC or HREF used in IMG,SCRIPT or LINK tag was found in the HTML document.");
					rule7.put("Recommendation",
							"Remove the tags from the HTML document to avoid unnecessary HTTP call to server.");
				} else {
					rule7.put("Message", "none");
					rule7.put("Recommendation", "none");
				}

				array.add(rule7);
				int intJSCount = html.select("script").size() - (html.select("script[src]").size() + totSODCount);
				JSONObject rule8 = new JSONObject();
				rule8.put("ruleHeader", "Make JS as external");
				if (intJSCount > 0) {
					rule8.put("Message",
							intJSCount + " instance(s) of internal Javascript has been identified in the HTML page");
					rule8.put("Recommendation", "Make internal javascript to external. if javascript is not simple.");
				} else {
					rule8.put("Message", "none");
					rule8.put("Recommendation", "none");
				}

				array.add(rule8);
				JSONObject rule9 = new JSONObject();
				rule9.put("ruleHeader", "PUT javaScript at bottom");
				int jscntHead = head.select("script").size()
						- (head.select("script[async]").size() + head.select("script[defer]").size() + count);
				List<String> jsList = new ArrayList();
				// int i;
				if (jscntHead > 0) {
					for (i = 0; i < head.select("script:not(script[async],script[defer])").size(); ++i) {
						if (head.select("script:not(script[async],script[defer])").get(i).attr("src") != "") {
							jsList.add(head.select("script:not(script[async],script[defer])").get(i).attr("src"));
						}
					}

					if (jsList.size() > 0) {
						if (jscntHead == jsList.size()) {
							rule9.put("Message", jscntHead
									+ " instance(s) of Javascript has been called in HEAD without ASYNC or DEFER attribute can block parallel download of resources."
									+ "\n\n" + " Below are the identified external javascripts:" + "\n\n"
									+ jsList.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
						} else {
							rule9.put("Message", jscntHead
									+ " instance(s) of Javascript has been called in HEAD without ASYNC or DEFER attribute can block parallel download of resources."
									+ "\n" + (jscntHead - jsList.size()) + " instance(s) of inline javascript and "
									+ jsList.size() + " instance(s) of external java script has been found." + "\n\n"
									+ "Below are the identified external javascripts:" + "\n\n"
									+ jsList.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
						}
					} else {
						rule9.put("Message", jscntHead
								+ " instance(s) of Inline Javascript has been called in HEAD without ASYNC or DEFER attribute which can block parallel download of resources.");
					}

					rule9.put("Recommendation",
							"Move the Javascript to the bottom of the HTML or use ASYNC or DEFER attribute");
				} else {
					rule9.put("Message", "none");
					rule9.put("Recommendation", "none");
				}

				array.add(rule9);
				i = body.select("style").size();
				JSONObject rule10 = new JSONObject();
				rule10.put("ruleHeader", "PUT CSS at top");
				if (i > 0) {
					rule10.put("Message", i + " instance(s) of CSS stylesheet has been found in BODY");
					rule10.put("Recommendation",
							"Specifying external stylesheet and inline style blocks in the body of an HTML document can negatively affect the browser's rendering performance. Move the CSS stylsheet to top of the HTML");
				} else {
					rule10.put("Message", "none");
					rule10.put("Recommendation", "none");
				}

				array.add(rule10);
				JSONObject rule11 = new JSONObject();
				rule11.put("ruleHeader", "Dimension of images needs to be mentioned");
				if (noscaleCount != 0) {
					message = noScaleImgs.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
					rule11.put("Message", noscaleCount
							+ " instance(s) of IMG has no WIDTH or HEIGHT or STYLE defined.Below are the images where dimensions has not been mentioned:"
							+ "\n" + message.toString().replaceAll("http", "\n �http"));
					rule11.put("Recommendation",
							"Be sure to specify dimensions on the image element or block-level parent to avoid browser reflow or repaint.");
				} else {
					rule11.put("Message", "none");
					rule11.put("Recommendation", "none");
				}

				array.add(rule11);
				JSONObject rule12 = new JSONObject();
				rule12.put("ruleHeader", "Avoid image scaling");
				if (totImgCount - noscaleCount > 0) {
					message = scaleImgs.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
					rule12.put("Message", totImgCount - noscaleCount
							+ " instance(s) of IMG has scaling defined. Below are the images where scaling has been defined:"
							+ "\n" + message.toString().replaceAll("http", "\n �http"));
					rule12.put("Recommendation", "Make sure right size image used and avoid scaling in HTML");
				} else {
					rule12.put("Message", "none");
					rule12.put("Recommendation", "none");
				}

				array.add(rule12);
				int emptyiFrameCnt = html.select("IFRAME").size();
				JSONObject rule13 = new JSONObject();
				rule13.put("ruleHeader", "Use of IFRAMES");
				if (emptyiFrameCnt != 0) {
					rule13.put("Message", "IFRAMES has been used in " + emptyiFrameCnt + " places");
					rule13.put("Recommendation",
							"If the contents are nor important than the main page, set these IFRAME(S) SRC dynamically after high priority resources are downloaded.");
				} else {
					rule13.put("Message", "none");
					rule13.put("Recommendation", "none");
				}

				array.add(rule13);
				JSONObject rule14 = new JSONObject();
				rule14.put("ruleHeader", "Check for IMPORT tag");
				// logger.info(imprtcnt1);
				if (imprtcnt1 != 0) {
					rule14.put("Message", "@IMPORT statement has been used for stylesheets in HTML document around "
							+ imprtcnt1 + " places");
					rule14.put("Recommendation",
							"Instead use a LINK tag which allows the browser to download stylesheets in parallel.");
				} else {
					rule14.put("Message", "none");
					rule14.put("Recommendation", "none");
				}

				array.add(rule14);
				JSONObject rule15 = new JSONObject();
				rule15.put("ruleHeader", "Avoid charset in meta tag");
				if (head.select("meta").attr("content").contains("charset")) {
					rule15.put("Message", "Charset has been mentioned in the meta tag of HTML document");
					rule15.put("Recommendation",
							"Specifying a character set in a meta tag disables the lookahead downloader in IE8.To improve resource download parallelization move the character set to the HTTP ContentType response header.");
				} else {
					rule15.put("Message", "none");
					rule15.put("Recommendation", "none");
				}

				array.add(rule15);
				t1.timetakingurls = t1.timeconsuming(entries);
				JSONObject rule16 = new JSONObject();
				rule16.put("ruleHeader", "Server time consuming");
				message = t1.timetakingurls.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				if (!t1.timetakingurls.isEmpty()) {
					rule16.put("Message", "Response time for the below individual request is over 500ms:\n"
							+ message.toString().replaceAll("http", "\n �http"));
					rule16.put("Recommendation",
							"The requests seems to be time consuming from server/network side. This needs to be profiled");
				} else {
					rule16.put("Message", "none");
					rule16.put("Recommendation", "none");
				}

				array.add(rule16);
			}

			JSONObject rule17 = new JSONObject();
			rule17.put("ruleHeader", "Validate number of requests in a page");
			if (t1.totalrequests >= 10) {
				rule17.put("Message", "Total number of requests in the page is :" + t1.totalrequests
						+ ".Consider reducing total number of resources getting downloaded.");
				rule17.put("Recommendation",
						"If possible combine multiple js/css files from same domain to single js/css and CSS spriting for images also reduces the number of network calls.");
			} else {
				rule17.put("Message", "Total number of requests in the page is :" + t1.totalrequests
						+ "Number of requests per page is within the industry standard.");
				rule17.put("Recommendation", "none");
			}

			array.add(rule17);
			logger.info("Array : "+array);
			recommendation.put("recommendation", array);
			recommendation.put("pagename", pagename);
			logger.info("recommendation =======> " + pagename);
		} catch (Exception var52) {
			var52.printStackTrace();
		}

		return recommendation;
	}

	public Map<String, List<String>> checkcachecontrol(org.json.simple.JSONArray b1) {
		int size = b1.size();
		List<String> cacheContorlUrl = new ArrayList<>();
		List<String> expiryUrl = new ArrayList<>();
		List<String> wrongcachestatus = new ArrayList<>();
		Map<String, List<String>> map = new HashMap<>();

		for (int i = 0; i < size; ++i) {
			JSONObject chk = (JSONObject) b1.get(i);
			JSONObject request = (JSONObject) chk.get("request");
			String url = request.get("url").toString();
			JSONObject response = (JSONObject) chk.get("response");
			if (response.get("status").toString().contains("304")) {
				wrongcachestatus.add(url);
			}

			org.json.simple.JSONArray headers = (org.json.simple.JSONArray) response.get("headers");
			boolean c1 = false;

			for (int j = 0; j < headers.size(); ++j) {
				if (headers.get(j).toString().contains("Cache-Control")) {
					c1 = true;
					break;
				}
			}

			if (!c1) {
				cacheContorlUrl.add(url);
			}

			boolean c2 = false;

			for (int j = 0; j < headers.size(); ++j) {
				if (headers.get(j).toString().contains("Expires")) {
					c2 = true;
					break;
				}
			}

			if (!c2) {
				expiryUrl.add(url);
			}
		}

		map.put("Expiry", expiryUrl);
		map.put("CacheControl", cacheContorlUrl);
		map.put("CacheStatus", wrongcachestatus);
		return map;
	}

	public List<String> findCompression(org.json.simple.JSONArray b1) {
		int size = b1.size();
		List<String> compressionUrl = new ArrayList();

		for (int i = 0; i < size; ++i) {
			JSONObject chk = (JSONObject) b1.get(i);
			JSONObject request = (JSONObject) chk.get("request");
			String url = request.get("url").toString();
			if (!url.contains(".png") && !url.contains(".gif") && !url.contains(".jpeg") && !url.contains(".jpg")) {
				JSONObject response = (JSONObject) chk.get("response");
				org.json.simple.JSONArray headers = (org.json.simple.JSONArray) response.get("headers");
				boolean c1 = false;

				for (int j = 0; j < headers.size(); ++j) {
					if (headers.get(j).toString().contains("Content-Encoding")) {
						c1 = true;
						break;
					}
				}

				if (!c1) {
					compressionUrl.add(url);
				}
			}
		}

		return compressionUrl;
	}

	public List<String> findDuplicates(org.json.simple.JSONArray b1) {
		int size = b1.size();
		List<String> duplicateUrl = new ArrayList();
		Map<String, String> urlwithsize = new HashMap();

		for (int i = 0; i < size; ++i) {
			JSONObject chk = (JSONObject) b1.get(i);
			JSONObject request = (JSONObject) chk.get("request");
			String url = request.get("url").toString();
			JSONObject response = (JSONObject) chk.get("response");
			if (urlwithsize.containsKey(url)) {
				if (response.get("bodySize").toString().equalsIgnoreCase((String) urlwithsize.get(url))) {
					duplicateUrl.add(url);
				}
			} else {
				urlwithsize.put(url, response.get("bodySize").toString());
			}
		}

		return duplicateUrl;
	}

	public Map<String, List<String>> errorenousurls(org.json.simple.JSONArray b1) {
		int size = b1.size();
		List<String> url302 = new ArrayList();
		List<String> url404 = new ArrayList();
		Map<String, List<String>> map = new HashMap();

		for (int i = 0; i < size; ++i) {
			JSONObject chk = (JSONObject) b1.get(i);
			JSONObject request = (JSONObject) chk.get("request");
			String url = request.get("url").toString();
			JSONObject response = (JSONObject) chk.get("response");
			if (response.get("status").toString().contains("302")) {
				url302.add(url);
			}

			if (response.get("status").toString().contains("400")
					|| response.get("status").toString().contains("404")) {
				url404.add(url);
			}
		}

		map.put("302", url302);
		map.put("404", url404);
		return map;
	}

	public List<String> timeconsuming(org.json.simple.JSONArray b1) {
		int size = b1.size();
		List<String> timeconurl = new ArrayList();

		for (int i = 0; i < size; ++i) {
			JSONObject chk = (JSONObject) b1.get(i);
			JSONObject request = (JSONObject) chk.get("request");
			String url = request.get("url").toString();
			float f = Float.parseFloat(chk.get("time").toString());
			if (f > 500.0F) {
				timeconurl.add(url);
			}
		}

		return timeconurl;
	}

	public Map<String, List<String>> getDomainurls(org.json.simple.JSONArray b1, String str) {
		int size = b1.size();
		Map<String, List<String>> map = new HashMap();

		for (int i = 0; i < size; ++i) {
			JSONObject chk = (JSONObject) b1.get(i);
			JSONObject request = (JSONObject) chk.get("request");
			String url = request.get("url").toString();
			if (url.endsWith(str)) {
				String[] split = url.split("/");
				String domain = split[2];
				if (map.containsKey(domain)) {
					new ArrayList();
					List<String> content = (List) map.get(domain);
					content.add(url);
					map.put(domain, content);
				} else {
					List<String> content = new ArrayList();
					content.add(url);
					map.put(domain, content);
				}
			}
		}

		Iterator var14 = map.keySet().iterator();

		while (var14.hasNext()) {
			String key = (String) var14.next();
			logger.info("Key:" + key + ";Value:" + map.get(key));
		}

		return map;
	}

	public static org.json.simple.JSONArray CreateWaterfallJson(String newReportPath)
			throws InterruptedException, FileNotFoundException, IOException, ParseException {

		String sFileName1 = newReportPath + "\\harfile.json";
		File harfile1 = new File(sFileName1);
		logger.info("File : " + harfile1);
		JSONParser jsonparser = new JSONParser();
		Object obj = jsonparser.parse(new FileReader(harfile1.getAbsolutePath()));
		JSONObject jsonobject = (JSONObject) obj;
		JSONObject log = (JSONObject) jsonobject.get("log");
		org.json.simple.JSONArray entries = (org.json.simple.JSONArray) log.get("entries");
		long size = 0L;
		String mimeType = null;
		String text = null;
		org.json.simple.JSONArray entriesarray = new org.json.simple.JSONArray();

		String pageref;
		for (int i = 0; i < entries.size(); ++i) {
			JSONObject entriesget = (JSONObject) entries.get(i);
			pageref = (String) entriesget.get("pageref");
			String starttime = (String) entriesget.get("startedDateTime");
			JSONObject request = (JSONObject) entriesget.get("request");
			String method = (String) request.get("method");
			String url = (String) request.get("url");
			JSONObject postData = (JSONObject) request.get("postData");
			if (postData != null) {
				mimeType = (String) postData.get("mimeType");
				text = (String) postData.get("text");
			}

			JSONObject response = (JSONObject) entriesget.get("response");
			long status = (Long) response.get("status");
			JSONObject content = (JSONObject) response.get("content");
			if (content != null) {
				String type = (String) content.get("mimeType");
				size = (Long) content.get("size");
			}

			JSONObject timings = (JSONObject) entriesget.get("timings");
			String ssl = (String) request.get("ssl");
			String connect = (String) request.get("connect");
			String dns = (String) request.get("dns");
			String wait = (String) request.get("wait");
			String blocked = (String) request.get("blocked");
			String send = (String) request.get("send");
			String receive = (String) request.get("receive");
			JSONObject entriesall = new JSONObject();
			entriesall.put("pageref", pageref);
			entriesall.put("startedDateTime", starttime);
			entriesall.put("method", method);
			entriesall.put("url", url);
			entriesall.put("mimeType", mimeType);
			entriesall.put("status", status);
			String sizecheck = Long.toString(size);
			if (sizecheck != null) {
				entriesall.put("size", size);
			} else {
				entriesall.put("size", 0);
			}

			if (ssl != null) {
				entriesall.put("ssl", ssl);
			} else {
				entriesall.put("ssl", 0);
			}

			if (connect != null) {
				entriesall.put("connect", connect);
			} else {
				entriesall.put("connect", 0);
			}

			if (dns != null) {
				entriesall.put("dns", dns);
			} else {
				entriesall.put("dns", 0);
			}

			if (wait != null) {
				entriesall.put("wait", wait);
			} else {
				entriesall.put("wait", 0);
			}

			if (blocked != null) {
				entriesall.put("blocked", blocked);
			} else {
				entriesall.put("blocked", 0);
			}

			if (send != null) {
				entriesall.put("send", send);
			} else {
				entriesall.put("send", 0);
			}

			if (receive != null) {
				entriesall.put("receive", receive);
			} else {
				entriesall.put("receive", 0);
			}

			entriesarray.add(entriesall);
		}

		logger.info("entriesarray--->" + entriesarray);
		String waterfallfile = newReportPath + "\\waterfall.json";
		File waterfal = new File(waterfallfile);
		pageref = null;

		try {
			FileWriter writerwaterfal = new FileWriter(waterfal);
			writerwaterfal.write(entriesarray.toString());
			writerwaterfal.flush();
			writerwaterfal.close();
		} catch (Exception var34) {
			var34.printStackTrace();
		}

		if (waterfal.exists()) {
			logger.info("waterfall.json File is created");
		}

		return entriesarray;
	}

	public String responseAPI(WebDriver driver) throws JSONException {
		int temp = 0;
		int script = 0;
		int link = 0;
		int img = 0;
		int video = 0;
		int css = 0;
		int textxml = 0;
		int iframe = 0;
		int other = 0;
		String resourceAPI = null;
		this.js = (JavascriptExecutor) driver;
		this.wait = new WebDriverWait(driver, 30L);
		new org.json.simple.JSONArray();

		try {
			resourceAPI = (String) this.js
					.executeScript("return JSON.stringify(performance.getEntriesByType('resource'))", new Object[0]);
			/*
			 * logger.info("   resourceAPI: " + this.js
			 * .executeScript("return JSON.stringify(performance.getEntriesByType('resource'))"
			 * , new Object[0]));
			 */
		} catch (Exception var19) {
			var19.printStackTrace();
		}

		JSONArray jsonarray = new JSONArray(resourceAPI);
		logger.info("Length =========> " + jsonarray.length());

		int pagesize;
		for (pagesize = 0; pagesize < jsonarray.length(); ++pagesize) {
			org.json.JSONObject obj = jsonarray.getJSONObject(pagesize);
			Integer size = (Integer) obj.get("transferSize");
			temp += size;
			String type = (String) obj.get("initiatorType");
			if (type.equalsIgnoreCase("script")) {
				++script;
			} else if (type.equalsIgnoreCase("link")) {
				++link;
			} else if (type.equalsIgnoreCase("img")) {
				++img;
			} else if (type.equalsIgnoreCase("video")) {
				++video;
			} else if (type.equalsIgnoreCase("css")) {
				++css;
			} else if (type.equalsIgnoreCase("xmlhttprequest")) {
				++textxml;
			} else if (type.equalsIgnoreCase("iframe")) {
				++iframe;
			} else {
				++other;
			}
		}

		logger.info("script" + script);
		logger.info("link" + link);
		logger.info("img" + img);
		logger.info("video" + video);
		logger.info("css" + css);
		logger.info("xmlhttprequest" + textxml);
		logger.info("iframe" + iframe);
		logger.info("other" + other);
		int convert = 1048576;
		pagesize = temp / convert;
		logger.info("Page Size : " + pagesize);
		return resourceAPI;
	}

	public String CreateJson(org.json.simple.JSONArray output, String reportPath) throws IOException {
		String performancefile = reportPath + "\\performance.json";
		File perf = new File(performancefile);
		if (perf.exists()) {
			logger.info("performance.json File is created");
		}

		FileWriter writerperffile = null;

		try {
			writerperffile = new FileWriter(perf);
			writerperffile.write(output.toString());
			writerperffile.flush();

		} catch (Exception var7) {
			var7.printStackTrace();
		} finally {
			writerperffile.close();
		}
		return performancefile;
	}

	public String CreateHarJson(org.json.simple.JSONArray hararray, String reportPath) throws IOException {
		String performancefile = reportPath + "\\log.json";
		File perf = new File(performancefile);
		if (perf.exists()) {
			logger.info("performance.json File is created");
		}

		FileWriter writerperffile = null;

		try {
			writerperffile = new FileWriter(perf);
			writerperffile.write(hararray.toString());
			writerperffile.flush();

		} catch (Exception var7) {
			var7.printStackTrace();
		} finally {
			writerperffile.close();
		}
		return performancefile;
	}

	public String CreateRecommendationsJSON(org.json.simple.JSONArray recommendationsarray, String reportPath)
			throws IOException {
		String performancefile = reportPath + "\\recommendations.json";
		File perf = new File(performancefile);
		if (perf.exists()) {
			logger.info("recommendations.json File is created");
		}

		FileWriter writerperffile = null;

		try {
			writerperffile = new FileWriter(perf);
			writerperffile.write(recommendationsarray.toString());
			writerperffile.flush();

		} catch (Exception var7) {
			var7.printStackTrace();
		} finally {
			writerperffile.close();
		}

		return performancefile;
	}

	public String CreateJsonCache(org.json.simple.JSONArray output) throws IOException {
		String performancefile = ".\\..\\performancecache.json";
		File perf = new File(performancefile);
		if (perf.exists()) {
			logger.info("performance.json File is created");
		}

		FileWriter writerperffile = null;

		try {
			writerperffile = new FileWriter(perf);
			writerperffile.write(output.toString());
			writerperffile.flush();

		} catch (Exception var6) {
			var6.printStackTrace();
		} finally {
			writerperffile.close();
		}

		return performancefile;
	}

	public void ajaxPageTimer(String PageName, int loadTime) throws IOException {

		String ENV = properties.getProperty("ENV");
		String browser = properties.getProperty("browser");
		String BUILD = properties.getProperty("BUILD");
		if (browser.equals("IE8")) {
			logger.info("\t" + PageName + " Loading");
		} else {
			this.dateTime = new Date();
			if (this.timersEnabled) {
				try {
					this.timerlog.append(this.requiredFormat.format(this.dateTime) + "\t" + browser + "\t" + ENV + "\t"
							+ BUILD + "\t" + PageName + "\t\t\t" + loadTime + "\t" + loadTime
							+ "\t\t\t\t\t\t\t\t\t\t\t\t\n");
					this.timerlog.flush();
				} catch (IOException var10) {
					logger.info("***Failed to wrtite Performance Timings to file.***");
					var10.printStackTrace();
				}
			}

			logger.info("\t" + PageName + " Loaded in: " + loadTime + "ms");
		}

	}

	public void setMarker(String marker) {
		if (this.dtEnabled) {
			try {
				this.js.executeAsyncScript("_dt_addMark('" + marker + "'); callback();", new Object[0]);
			} catch (Exception var3) {
				Log.error("Exception : " + var3.getMessage());
			}

		}
	}

	public void startTimer(String timer) {
		if (this.dtEnabled) {
			this.timerName = timer;

			try {
				this.js.executeAsyncScript("_dt_setTimerName('" + timer + "'); callback();", new Object[0]);
			} catch (Exception var3) {
				Log.error("Exception : " + var3.getMessage());
			}

			start = System.currentTimeMillis();
		}
	}

	public void stopTimer() {
		if (this.dtEnabled) {
			finish = System.currentTimeMillis();

			try {
				this.js.executeAsyncScript("_dt_setTimerName(''); callback();", new Object[0]);
			} catch (Exception var2) {
				Log.error("Exception : " + var2.getMessage());
			}

		}
	}

	public void setJSX(JavascriptExecutor newJSX) {
		this.js = newJSX;
	}

	public String getBuild(String BUILD) throws IOException {
		return BUILD;
	}

	public void AgentData11(String Env, String URL) {
		if (Env == "sample") {
			;
		}

	}

	public String getURL() {
		return this.URL;
	}

	public String getProfile() {
		return this.PROFILE;
	}

	public static JSONObject TxtParser(File srcFile) throws IOException {
		JSONObject finalobj = new JSONObject();
		org.json.simple.JSONArray objarray = new org.json.simple.JSONArray();
		FileReader fr = new FileReader(srcFile);
		String header = null;
		boolean count = false;
		String[] names = null;
		String[] values = null;
		int lines = 0;
		try (BufferedReader br = new BufferedReader(fr);) {
			while (true) {
				String str;
				String value;
				do {
					if ((str = br.readLine()) == null) {
						finalobj.put("output", objarray);
						try {
							String PathForPerformanceFile = properties.getProperty("PathForPerformanceFile"); 
							String performancefile = PathForPerformanceFile;
							logger.info("Performance file : " + performancefile);
							File perf = new File(performancefile);
							if (perf.exists()) {
								logger.info("performance.json File is created");
							}

							value = null;
							FileWriter writerperffile = null;
							try {
								writerperffile = new FileWriter(perf);
								writerperffile.write(finalobj.toString());
								writerperffile.flush();

							} catch (Exception var18) {
								var18.printStackTrace();
							} finally {
								writerperffile.close();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						} 

						return finalobj;
					}

					if (str.contains("Date")) {
						header = str;
						++lines;
						str = br.readLine();
					}
				} while (lines <= 0);

				JSONObject output = new JSONObject();
				StringTokenizer strtkn = new StringTokenizer(header, "\t");
				StringTokenizer strtkn1 = new StringTokenizer(str, "\t");
				int count1 = strtkn.countTokens();
				logger.info("count " + count1);
				names = new String[count1];
				values = new String[count1];

				for (int i = 0; i < count1; ++i) {
					String name = strtkn.nextToken();
					value = strtkn1.nextToken();
					logger.info("before values" + name + value);
					names[i] = name;
					values[i] = value;
					output.put(name, value);
				}

				logger.info("output json at each i" + output);
				objarray.add(output);
			}
		}

	}

	public static JSONObject TxtParserWithCache(File srcFile, org.json.simple.JSONArray recommarray,
			org.json.simple.JSONArray mitiarray) throws IOException {
		JSONObject finalobj = new JSONObject();
		org.json.simple.JSONArray objarray = new org.json.simple.JSONArray();
		FileReader fr = new FileReader(srcFile);
		String header = null;
		boolean count = false;
		String[] names = null;
		String[] values = null;
		int lines = 0;
		try (BufferedReader br = new BufferedReader(fr);) {
			while (true) {
				String str;
				do {
					if ((str = br.readLine()) == null) {
						finalobj.put("output", objarray);
						return finalobj;
					}

					if (str.contains("Date")) {
						header = str;
						++lines;
						str = br.readLine();
					}
				} while (lines <= 0);

				JSONObject output = new JSONObject();
				StringTokenizer strtkn = new StringTokenizer(header, "\t");
				StringTokenizer strtkn1 = new StringTokenizer(str, "\t");
				int count1 = strtkn.countTokens();
				logger.info("Count " + count1);
				names = new String[count1];
				values = new String[count1];

				for (int i = 0; i < count1; ++i) {
					String name = strtkn.nextToken();
					String value = strtkn1.nextToken();
					logger.info("before values" + name + value);
					names[i] = name;
					values[i] = value;
					output.put(name, value);
				}

				if (recommarray.size() != 0) {
					output.put("recommendations", recommarray);
				} else if (recommarray.size() == 0) {
					output.put("recommendations", "Reccomendations not available");
				}

				if (mitiarray.size() != 0) {
					output.put("mitigation", mitiarray);
				} else if (mitiarray.size() == 0) {
					output.put("mitigation", "Mitigation not available");
				}

				logger.info("output json at each i" + output);
				objarray.add(output);
			}
		}

	}

	public static void takeSnapShot(String nftcxurl, String reportPath, String browser) throws Exception {
		String jpgpath = reportPath + "\\test.jpg";
		WebDriver driver = null;
		String browserVersion = null;
		Capabilities browser1 = ((RemoteWebDriver) driver).getCapabilities();
		browser = browser1.getBrowserName() + ":" + browser1.getVersion();
		driver.get(nftcxurl);
		driver.manage().window().maximize();
		Thread.sleep(2000L);
		File scrFile = (File) ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(scrFile, new File(jpgpath));
		SystemDateandTime(reportPath, browser, browserVersion);
		driver.quit();
	}

	public void CreateJSONFiles(org.json.simple.JSONArray output, org.json.simple.JSONArray hararray,
			org.json.simple.JSONArray recommendationsarray, org.json.simple.JSONArray timerarray)
			throws IOException, ParseException {

		collect.CreateJson(output, Paths.get(properties.getProperty("ReportPath")).normalize().toString());
		collect.CreateHarJson(hararray, Paths.get(properties.getProperty("ReportPath")).normalize().toString());
		collect.CreateRecommendationsJSON(recommendationsarray,
				Paths.get(properties.getProperty("ReportPath")).normalize().toString());
		collect.CreateResponseJson(timerarray, Paths.get(properties.getProperty("ReportPath")).normalize().toString());
		collect.CreateOverallSummary(Paths.get(properties.getProperty("ReportPath")).normalize().toString());
	}

	public static void SystemDateandTime(String reportPath, String browser, String browserVersion) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = new Date();
		String date1 = dateFormat.format(date);

		JSONObject reporttime = new JSONObject();
		reporttime.put("datetime", date1);
		reporttime.put("browser", browser + browserVersion);

		File report = new File(reportPath + "\\reporttime.json");
		report.createNewFile();
		FileWriter reportfile = null;
		try {
			reportfile = new FileWriter(report);
			reportfile.write(reporttime.toString());
			reportfile.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			reportfile.close();
		}
	}

	public static void reportTime(String reportPath, String browser, WebDriver driver) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = new Date();
		String date1 = dateFormat.format(date);
		String browserVersion = null;

		Capabilities browser1 = ((RemoteWebDriver) driver).getCapabilities();
		browser = browser1.getBrowserName() + ":" + browser1.getVersion();

		JSONObject reporttime = new JSONObject();
		reporttime.put("datetime", date1);
		reporttime.put("browser", browser);

		File report = new File(reportPath + "\\reporttime.json");
		report.createNewFile();
		FileWriter reportfile = null;
		try {
			reportfile = new FileWriter(report);
			reportfile.write(reporttime.toString());
			reportfile.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			reportfile.close();
		}
	}

	public static JSONObject reportTimeforElasticSearch(String reportPath, String browser, WebDriver driver, int RunID,
			String testCaseName, String PROJECT, String BUILD) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		Date date = new Date();
		String date1 = dateFormat.format(date);
		String timestamp = dateFormat1.format(date);

		String browserVersion = null;

		Capabilities browser1 = ((RemoteWebDriver) driver).getCapabilities();
		browser = browser1.getBrowserName() + ":" + browser1.getVersion();

		JSONObject body = new JSONObject();
		body.put("Timestamp", timestamp);
		body.put("RunID", RunID);
		body.put("testcase", testCaseName);

		JSONObject reporttime = new JSONObject();
		reporttime.put("datetime", date1);
		reporttime.put("browser", browser);

		body.put("reporttime", reporttime);
		body.put("Project", PROJECT);
		body.put("Build", BUILD);

		return body;
	}

}
