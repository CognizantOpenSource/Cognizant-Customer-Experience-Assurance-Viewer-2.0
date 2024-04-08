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

package com.charles.POC;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import org.brotli.dec.BrotliInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetworkRecommend {
	
	private static final Logger logger = LoggerFactory.getLogger(NetworkRecommend.class);
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
	
	public int totalrequests;
	public Map<String, List<String>> condition1 = new HashMap<>();
	public List<String> condition2 = new ArrayList<>();
	public List<String> condition3 = new ArrayList<>();
	public Map<String, List<String>> errorurls = new HashMap<>();
	public Map<String, List<String>> cssurls = new HashMap<>();
	public Map<String, List<String>> jsurls = new HashMap<>();
	public List<String> timetakingurls = new ArrayList<>();

	public static void main(String[] args) throws InterruptedException, IOException {
		String PathForHarFile = properties.getProperty("PathForHarFile");
		NetRecommend(PathForHarFile, "Home");
	}

	public static JSONObject NetRecommend(String harfile, String pagename) {
		
		JSONParser parser = new JSONParser();
		JSONObject recommendation = new JSONObject();
		JSONArray array = new JSONArray();

		try {
			Object obj = parser.parse(new FileReader(harfile));
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject log = (JSONObject) jsonObject.get("log");
			JSONArray entries = (JSONArray) log.get("entries");
			NetworkRecommend networkCommand = new NetworkRecommend();
			networkCommand.totalrequests = entries.size();
			
			// Rule1
			JSONObject rule1 = new JSONObject();
			logger.info("Rule1 Validate no. of requests in a page ...");
			rule1.put("ruleHeader", "Validate no. of requests in a page");
			if (networkCommand.totalrequests >= 10) {
				rule1.put("Message", "Total no. of requests in the page:" + networkCommand.totalrequests
						+ ".Consider reducing total no. of resources getting downloaded");
				rule1.put("Recommendation",
						"If possible combine multiple js/css files from same domain to single js/css.CSS spriting for images also reduces the no. of network calls");
			} else {
				rule1.put("Message", "Total no. of requests in the page:" + networkCommand.totalrequests
						+ ".No. of requests per page is within the industry standard");
				rule1.put("Recommendation", "none"); 
			}
			logger.info("Total No. Of Requests in the page:" + networkCommand.totalrequests);
			array.add(rule1);
			logger.info("Rule1 is added to the array : "+array);
			
			// Rule2
			JSONObject rule2 = new JSONObject();
			networkCommand.condition1 = networkCommand.checkcachecontrol(entries);
			logger.info("Rule2 Cache Control ...");
			rule2.put("ruleHeader", "Leverage Browsing Cache");
			boolean chk = false;
			String message = "";
			if (!((List) networkCommand.condition1.get("Expiry")).isEmpty()) {
				logger.info(
						"Expires Header is not mentioned for the below resources\n" + networkCommand.condition1.get("Expiry"));
				message = "Url's without any expiry header:\n\n" + ((List) networkCommand.condition1.get("Expiry")).toString()
						.substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				chk = true;
			}
			if (!((List) networkCommand.condition1.get("CacheControl")).isEmpty()) {
				logger.info("Cache Control Header is not mentioned for the below resources"
						+ networkCommand.condition1.get("CacheControl"));
				message = message + "Url's without cache control header:" + "\n\n"
						+ ((List) networkCommand.condition1.get("CacheControl")).toString().substring(1).replaceFirst("]", "")
								.replaceAll(",", "\n");
				chk = true;
			}
			if (!((List) networkCommand.condition1.get("CacheStatus")).isEmpty()) {
				logger.info("Below resources are having 304 as status code\n" + networkCommand.condition1.get("CacheStatus"));
				message = message + "\n\nUrl's 304 status:\n\n" + ((List) networkCommand.condition1.get("CacheStatus")).toString()
						.substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				chk = true;
			}
			if (!chk) {
				rule2.put("Message", "none");
				rule2.put("Recommendation", "none");
			} else {
				logger.info("Check : " + message);
				rule2.put("Message", message);
				rule2.put("Recommendation",
						"For having a good caching startegy it is recoomended to have cache control and expires header for all the resources, Also as a best practice it is recommended that no resources to get 304 status");
			}
			array.add(rule2);
			logger.info("Rule2 is added to the array : "+array);
			
			// Rule3
			JSONObject rule3 = new JSONObject();
			networkCommand.condition2 = networkCommand.findCompression(entries);
			rule3.put("ruleHeader", "Apply Compression Technique");
			logger.info("Rule2 Compression Check ...");
			if (!networkCommand.condition2.isEmpty()) {
				logger.info("Compression is not applied to below resources:\n" + networkCommand.condition2);
				rule3.put("Message", "No compression methodologies has been applied for the below URL's:\n\n"
						+ networkCommand.condition2.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
				rule3.put("Recommendation",
						"It is recommended to apply gzip/deflate/br compression techniques to the resources by which we can minimize the amount of data getting transferred");
			} else {
				rule3.put("Message", "none");
				rule3.put("Recommendation", "none");
			}
			array.add(rule3);
			logger.info("Rule3 is added to the array : "+array);
			
			// Rule4
			JSONObject rule4 = new JSONObject();
			networkCommand.condition3 = networkCommand.findDuplicates(entries);
			rule4.put("ruleHeader", "Avoid Duplicate calls");
			message = "";
			logger.info("Rule4 Duplicate calls in the page ...");
			if (!networkCommand.condition3.isEmpty()) {
				logger.info("Below duplicate calls are observed in the page:\n\n" + networkCommand.condition3);
				rule4.put("Message", "Below duplicate calls were observed:\n"
						+ networkCommand.condition3.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
				rule4.put("Recommendation", "Duplicate call needs to be avoided. Remove unnecessary network calls");
			} else {
				rule4.put("Message", "none");
				rule4.put("Recommendation", "none");
			}
			array.add(rule4);
			logger.info("Rule4 is added to the array : "+array);
			
			// Rule5
			JSONObject rule5 = new JSONObject();
			networkCommand.errorurls = networkCommand.errorenousurls(entries);
			rule5.put("ruleHeader", "Errorneous Requests");
			new ArrayList();
			new ArrayList();
			List<String> list400 = (List) networkCommand.errorurls.get("404");
			List<String> list302 = (List) networkCommand.errorurls.get("302");
			logger.info("Rule4 Errorneous requests ...");
			if (!list400.isEmpty()) {
				logger.info("Below errorenous requests(400/404) were observed:\n\n" + list400);
				rule5.put("Message", "Below resources have status code 400/404:\n"
						+ list400.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
				rule5.put("Recommendation", "Resolve 400/404 resources else remove the unwanted calls");
			} else {
				rule5.put("Message", "none");
				rule5.put("Recommendation", "none");
				logger.info("No errorenous requests were observed");
			}
			array.add(rule5);
			logger.info("Rule5 is added to the array : "+array);
			
			
			// Rule6
			JSONObject rule6 = new JSONObject();
			logger.info("Rule6 Avoid redirects ...");
			rule6.put("ruleHeader", "Avoid Redirects");
			if (!list302.isEmpty()) {
				logger.info("Below requests with 302 status code were observed:\n\n" + list302);
				rule6.put("Message", "Status code 302 was observed for the url's:\n\n"
						+ list302.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
				rule6.put("Recommendation",
						"Provide direct url to the resource which will reduce the unwanted roundtrip of network call");
			} else {
				logger.info("No redirects were observed in the page");
				rule6.put("Message", "none");
				rule6.put("Recommendation", "none");
			}
			array.add(rule6);
			logger.info("Rule6 is added to the array : "+array);
			
			
			// Rule7
			JSONObject rule7 = new JSONObject();
			networkCommand.timetakingurls = networkCommand.timeconsuming(entries);
			rule7.put("ruleHeader", "Server time consuming");
			if (!networkCommand.timetakingurls.isEmpty()) {
				rule7.put("Message", "Response time for the below individual request is over 500ms:\n\n"
						+ networkCommand.timetakingurls.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
				rule7.put("Recommendation",
						"The requests seems to be time consuming from server/network side. This needs to be profiled");
			} else {
				rule7.put("Message", "none");
				rule7.put("Recommendation", "none");
			}
			array.add(rule7);
			logger.info("Rule7 is added to the array : "+array);
			
			
			// Rule8
			JSONObject rule8 = new JSONObject();
			networkCommand.cssurls = networkCommand.getDomainurls(entries, ".css");
			rule8.put("ruleHeader", "Combine CSS and JS");
			message = "";
			chk = false;
			Iterator var23 = networkCommand.cssurls.keySet().iterator();
			while (var23.hasNext()) {
				String key = (String) var23.next();
				if (((List) networkCommand.cssurls.get(key)).size() > 1) {
					chk = true;
					message = message + "Below urls from the domain-" + key + "are the candidates for merging css:"
							+ "\n\n" + ((List) networkCommand.cssurls.get(key)).toString().substring(1).replaceFirst("]", "")
									.replaceAll(",", "\n");
				}
			}
			networkCommand.jsurls = networkCommand.getDomainurls(entries, ".js");
			boolean chk1 = false;
			Iterator var24 = networkCommand.jsurls.keySet().iterator();
			String Htmlcontent;
			while (var24.hasNext()) {
				Htmlcontent = (String) var24.next();
				if (((List) networkCommand.jsurls.get(Htmlcontent)).size() > 1) {
					chk1 = true;
					message = message + "\n\nBelow urls from the domain-" + Htmlcontent
							+ "are the candidates for merging js:" + "\n\n" + ((List) networkCommand.jsurls.get(Htmlcontent))
									.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n");
				}
			}
			if (!chk && !chk1) {
				rule8.put("Message", "none");
				rule8.put("Recommendation", "none");
			} else {
				rule8.put("Message", message);
				rule8.put("Recommendation",
						"Combine the candidate files into a single file or lesser multiple files which would reduce the no. of network calls in the page");
			}
			array.add(rule8);
			logger.info("Rule8 is added to the array : "+array);
			
			Htmlcontent = networkCommand.findHtmlContent(entries);
			logger.info("Html Content : "+Htmlcontent);
			if (Htmlcontent != "") {
				boolean imprtcnt = false;
				Pattern pattern = Pattern.compile("@import", 2);
				Matcher matcher = pattern.matcher(Htmlcontent);
				int count;
				for (count = 0; matcher.find(); ++count) {
					;
				}
				// Rule9
				JSONObject rule9 = new JSONObject();
				rule9.put("ruleHeader", "Check for IMPORT tag");
				logger.info("Count : "+count);
				if (count != 0) {
					rule9.put("Message", "@IMPORT statement has been used for stylesheets in HTML document around "
							+ count + " places");
					rule9.put("Recommendation",
							"Instead use a LINK tag which allows the browser to download stylesheets in parallel.");
				} else {
					rule9.put("Message", "none");
					rule9.put("Recommendation", "none");
				}
				array.add(rule9);
				logger.info("Rule9 is added to the array : "+array);
				
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
				int headSODCount = 0;
				int emptyiFrameCnt = html.select("IFRAME").size();
				
				// Rule10
				JSONObject rule10 = new JSONObject();
				rule10.put("ruleHeader", "Use of IFRAMES");
				if (emptyiFrameCnt != 0) {
					rule10.put("Message", "IFRAMES has been used in " + emptyiFrameCnt + " places");
					rule10.put("Recommendation",
							"If the contents are nor important than the main page, set these IFRAME(S) SRC dynamically after high priority resources are downloaded");
				} else {
					rule10.put("Message", "none");
					rule10.put("Recommendation", "none");
				}
				array.add(rule10);
				logger.info("Rule10 is added to the array : "+array);
				
				int emptyLinkCount = 0;
				int noscaleCount = 0;
				int totImgCount = html.select("img").size();
				List<String> noScaleImgs = new ArrayList<>();
				List<String> scaleImgs = new ArrayList<>();
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

				// Rule11
				JSONObject rule11 = new JSONObject();
				rule11.put("ruleHeader", "Empty SRC or HREF Tags");
				if (emptyLinkCount != 0) {
					rule11.put("Message", emptyLinkCount
							+ "instance(s) of empty SRC or HREF used in IMG,SCRIPT or LINK tag was found in the HTML document.");
					rule11.put("Recommendation",
							"Remove the tags from the HTML document to avoid unnecessary HTTP call to server.");
				} else {
					rule11.put("Message", "none");
					rule11.put("Recommendation", "none");
				}
				array.add(rule11);
				logger.info("Rule11 is added to the array : "+array);
				
				// Rule12
				JSONObject rule12 = new JSONObject();
				rule12.put("ruleHeader", "Dimension of Images needs to be mentioned");
				if (noscaleCount != 0) {
					rule12.put("Message", noscaleCount
							+ " instance(s) of IMG has no WIDTH or HEIGHT or STYLE defined. Below are the Images where diemnsion has not been mentioned:"
							+ noScaleImgs.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
					rule12.put("Recommendation",
							"Be sure to specify dimensions on the image element or block-level parent to avoid browser reflow or repaint.");
				} else {
					rule12.put("Message", "none");
					rule12.put("Recommendation", "none");
				}
				array.add(rule12);
				logger.info("Rule12 is added to the array : "+array);
				
				// Rule13
				JSONObject rule13 = new JSONObject();
				rule13.put("ruleHeader", "Avoid Image scaling");
				if (totImgCount - noscaleCount > 0) {
					rule13.put("Message", totImgCount - noscaleCount
							+ " instance(s) of IMG has scaling defined. Below are the Images where scaling has been defined:"
							+ scaleImgs.toString().substring(1).replaceFirst("]", "").replaceAll(",", "\n"));
					rule13.put("Recommendation", "Make sure right size image used and avoid scaling in HTML");
				} else {
					rule13.put("Message", "none");
					rule13.put("Recommendation", "none");
				}
				array.add(rule13);
				logger.info("Rule13 is added to the array : "+array);
				
				// Rule14
				JSONObject rule14 = new JSONObject();
				rule14.put("ruleHeader", "Avoid charset in meta tag");
				if (head.select("meta").attr("content").contains("charset")) {
					rule14.put("Message", "Charset has been mentioned in the meta tag of HTML document");
					rule14.put("Recommendation",
							"Specifying a character set in a meta tag disables the lookahead downloader in IE8.To improve resource download parallelization move the character set to the HTTP ContentType response header.");
				} else {
					rule14.put("Message", "none");
					rule14.put("Recommendation", "none");
				}
				array.add(rule14);
				logger.info("Rule14 is added to the array : "+array);
				
				// Rule15
				JSONObject rule15 = new JSONObject();
				int intJSCount = html.select("script").size() - (html.select("script[src]").size() + count);
				rule15.put("ruleHeader", "Make JS as External");
				if (intJSCount > 0) {
					rule15.put("Message",
							intJSCount + " instance(s) of internal Javascript has been identified in the HTML page");
					rule15.put("Recommendation", "Make internal javascript to external if javascript is not simple.");
				} else {
					rule15.put("Message", "none");
					rule15.put("Recommendation", "none");
				}
				array.add(rule15);
				logger.info("Rule15 is added to the array : "+array);
				
				
				// Rule16
				JSONObject rule16 = new JSONObject();
				int cssBodyCount = body.select("style").size();
				rule16.put("ruleHeader", "PUT CSS at Top");
				if (cssBodyCount > 0) {
					rule16.put("Message", cssBodyCount + " instance(s) of CSS stylesheet has been found in BODY");
					rule16.put("Recommendation",
							"Specifying external stylesheet and inline style blocks in the body of an HTML document can negatively affect the browser's rendering performance. Move the CSS stylsheet to top of the HTML");
				} else {
					rule16.put("Message", "none");
					rule16.put("Recommendation", "none");
				}
				array.add(rule16);
				logger.info("Rule16 is added to the array : "+array);
				
				// Rule17
				JSONObject rule17 = new JSONObject();
				rule17.put("ruleHeader", "PUT JavaScript at Bottom");
				int jscntHead = head.select("script").size()
						- (head.select("script[async]").size() + head.select("script[defer]").size() + headSODCount);
				List<String> jsList = new ArrayList();
				if (jscntHead > 0) {
					for (i = 0; i < head.select("script:not(script[async],script[defer])").size(); ++i) {
						if (head.select("script:not(script[async],script[defer])").get(i).attr("src") != "") {
							jsList.add(head.select("script:not(script[async],script[defer])").get(i).attr("src"));
						}
					}
					if (jsList.size() > 0) {
						rule17.put("Message", jscntHead
								+ " instance(s) of Javascript has been called in HEAD without ASYNC or DEFER attribute can block parallel download of resources. Below are the identified resources:"
								+ jsList.toString().substring(1).replaceFirst("]", ""));
					} else {
						rule17.put("Message", jscntHead
								+ " instance(s) of Inline Javascript has been called in HEAD without ASYNC or DEFER attribute which can block parallel download of resources.");
					}
					rule17.put("Recommendation",
							"Move the Javascript to the bottom of the HTML or use ASYNC or DEFER attribute");
				} else {
					rule17.put("Message", "none");
					rule17.put("Recommendation", "none");
				}
				array.add(rule17);
				logger.info("Rule17 is added to the array : "+array);
			}
			recommendation.put("recommendation", array);
			recommendation.put("pagename", pagename);
			logger.info("Recommendations : "+recommendation);
		} catch (Exception var53) {
			var53.printStackTrace();
		}

		return recommendation;
	}

	public static String decompressBrotli(byte[] compressed) throws IOException {
		BufferedReader br = null;
		BrotliInputStream gis = null;
		ByteArrayInputStream bis = null;
		StringBuilder sb = new StringBuilder();
		try {
			bis = new ByteArrayInputStream(compressed);
			gis = new BrotliInputStream(bis);
			br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));

			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (Exception ex) {

		} finally {
			br.close();
			gis.close();
			bis.close();
		}
		return sb.toString();
	}

	public static String decompressGzip(byte[] compressed) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
		GZIPInputStream gis = new GZIPInputStream(bis);
		BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		br.close();
		gis.close();
		bis.close();
		return sb.toString();
	}

	public static String decompressDeflate(byte[] compressed) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
		DeflaterInputStream gis = new DeflaterInputStream(bis);
		BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		br.close();
		gis.close();
		bis.close();
		return sb.toString();
	}

	public List<String> timeconsuming(JSONArray b1) {
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

	public Map<String, List<String>> getDomainurls(JSONArray b1, String str) {
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

	public String findHtmlContent(JSONArray entries) {
		String Htmlcontent = "";

		try {
			for (int k = 0; k < entries.size(); ++k) {
				JSONObject temp = (JSONObject) entries.get(k);
				JSONObject response = (JSONObject) temp.get("response");
				if (response.get("status").toString().contains("20")) {
					JSONObject content = (JSONObject) response.get("content");
					JSONArray headers = (JSONArray) response.get("headers");
					String contentencoding = "";
					int checkparse = 0;

					int l;
					for (l = 0; l < headers.size(); ++l) {
						temp = (JSONObject) headers.get(l);
						if (temp.get("name").toString().contains("Content-Type")) {
							logger.info("Content type " + temp.get("value").toString());
							if (!temp.get("value").toString().contains("image")
									&& !temp.get("value").toString().contains("javascript")
									&& !temp.get("value").toString().contains("octet-stream")
									&& !temp.get("value").toString().contains("application")
									&& !temp.get("value").toString().contains("css")) {
								checkparse = 1;
							}

							logger.info("Check Parse : " + checkparse);
							break;
						}
					}

					if (checkparse == 1) {
						contentencoding = "plain";
					}

					for (l = 0; l < headers.size(); ++l) {
						temp = (JSONObject) headers.get(l);
						if (temp.get("name").toString().contains("Content-Encoding")) {
							logger.info("Content Encoding " + temp.get("value").toString());
							if (temp.get("value").toString().contains("gzip")) {
								contentencoding = "gzip";
							} else if (temp.get("value").toString().contains("br")) {
								contentencoding = "br";
							} else if (temp.get("value").toString().contains("deflate")) {
								contentencoding = "deflate";
							}
						}
					}

					logger.info("ContentEncoding " + contentencoding);
					logger.info("mimetype " + content.get("mimeType").toString());
					if (content.get("mimeType").toString().contains("html")) {
						byte[] barr;
						if (contentencoding == "br") {
							if (content.get("text").toString().contains("</html>")) {
								Htmlcontent = content.get("text").toString();
							} else {
								logger.info("before value is " + new String(content.get("text").toString()));
								barr = Base64.getDecoder().decode(content.get("text").toString().getBytes("UTF-8"));
								logger.info("Decoded using br value is " + decompressBrotli(barr));
								Htmlcontent = decompressBrotli(barr);
							}
							break;
						}

						if (contentencoding == "gzip") {
							if (content.get("text").toString().contains("</html>")) {
								Htmlcontent = content.get("text").toString();
							} else {
								logger.info("before value is " + new String(content.get("text").toString()));
								barr = Base64.getDecoder().decode(content.get("text").toString().getBytes("UTF-8"));
								logger.info("Decodedusing gzip value is " + decompressGzip(barr));
								Htmlcontent = decompressGzip(barr);
							}
							break;
						}

						if (contentencoding == "deflate") {
							if (content.get("text").toString().contains("</html>")) {
								Htmlcontent = content.get("text").toString();
							} else {
								logger.info("before value is " + new String(content.get("text").toString()));
								barr = Base64.getDecoder().decode(content.get("text").toString().getBytes("UTF-8"));
								logger.info("Decodedusing gzip value is " + decompressDeflate(barr));
								Htmlcontent = decompressDeflate(barr);
							}
							break;
						}

						if (contentencoding == "plain") {
							logger.info("using plain " + content.get("text").toString());
							if (content.get("text").toString().contains("</html>")) {
								Htmlcontent = content.get("text").toString();
								break;
							}
						}
					}
				}
			}
		} catch (Exception var11) {
			var11.printStackTrace();
		}

		return Htmlcontent;
	}

	public Map<String, List<String>> checkcachecontrol(JSONArray b1) {
		int size = b1.size();
		List<String> cacheContorlUrl = new ArrayList();
		List<String> expiryUrl = new ArrayList();
		List<String> wrongcachestatus = new ArrayList();
		Map<String, List<String>> map = new HashMap();

		for (int i = 0; i < size; ++i) {
			JSONObject chk = (JSONObject) b1.get(i);
			JSONObject request = (JSONObject) chk.get("request");
			String url = request.get("url").toString();
			JSONObject response = (JSONObject) chk.get("response");
			if (response.get("status").toString().contains("304")) {
				wrongcachestatus.add(url);
			}

			JSONArray headers = (JSONArray) response.get("headers");
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

	public List<String> findCompression(JSONArray b1) {
		int size = b1.size();
		List<String> compressionUrl = new ArrayList();

		for (int i = 0; i < size; ++i) {
			JSONObject chk = (JSONObject) b1.get(i);
			JSONObject request = (JSONObject) chk.get("request");
			String url = request.get("url").toString();
			if (!url.contains(".png") && !url.contains(".gif") && !url.contains(".jpeg") && !url.contains(".jpg")) {
				JSONObject response = (JSONObject) chk.get("response");
				JSONArray headers = (JSONArray) response.get("headers");
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

	public List<String> findDuplicates(JSONArray b1) {
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

	public Map<String, List<String>> errorenousurls(JSONArray b1) {
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
}
