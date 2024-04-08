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

package com.defect.axeparser;

import java.io.File;

import java.io.FileNotFoundException;

import java.io.FileReader;

import java.io.FileWriter;

import java.io.IOException;

import java.util.ArrayList;

import java.util.HashSet;

import java.util.List;

import java.util.Set;

import org.json.simple.JSONArray;

import org.json.simple.JSONObject;

import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.charles.POC.NetworkRecommend;
import com.cxintegration.elastic.*;

public class Parsing {

	private static final Logger logger = LoggerFactory.getLogger(Parsing.class);
	
	private static final String IndexName1 = null;
	ArrayList<JSONArray> json = new ArrayList<>();
	JSONArray json1 = new JSONArray();
	String help = null;
	String RunId = "1";
	String description = null;
	String id = null;
	String helpUrl = null;
	String html = null;
	String impact = null;
	JSONArray nodes2 = null;
	String message = null;
	String TransportClient = null;
	String project_name = null;
	String run_id = null;
	JSONArray obj5;
	JSONArray obj6;
	List<String> obj7;
	JSONArray newarray;
	JSONObject object1;
	Set<String> hs;
	JSONObject counterobject1 = new JSONObject();
	JSONArray counterarray1 = new JSONArray();
	JSONObject mainObject = new JSONObject();
	JSONObject runID = new JSONObject();
	JSONArray counterarray = new JSONArray();
	static int RunID = 0;
	static String testcase;
	static Elastic ES;

	public Parsing(int RunID, String testcase, Elastic ES) {
		this.RunID = RunID;
		this.testcase = testcase;
		this.ES = ES;
	}

	public Parsing() {
		RunID = 0;
	}

	public ArrayList<JSONArray> parsingfile(List<String> filelist, ArrayList<String> pagename, String localPath,
			String IsESRequired) throws FileNotFoundException, IOException, ParseException {

		for (int m = 1; m <= filelist.size(); ++m) {
			this.object1 = new JSONObject();
			String filename = (String) filelist.get(m - 1);
			String pagenames = (String) pagename.get(m - 1);
			this.newarray = new JSONArray();
			
			JSONObject countobject = new JSONObject();
			JSONParser parser = new JSONParser();
			JSONArray obj = (JSONArray) parser.parse(new FileReader(filename));
			JSONArray array1 = obj;
			
			int counter = 0;
			int counter1 = 0;
			int counter2 = 0;
			int counter3 = 0;
			int physical = 0;
			int visual = 0;
			int hearing = 0;
			int cognitive = 0;
			int deafblind = 0;

			for (int i = 0; i < array1.size(); ++i) {
				JSONObject obj1 = (JSONObject) array1.get(i);
				this.help = (String) obj1.get("help");
				this.description = (String) obj1.get("description");
				this.id = (String) obj1.get("id");
				this.helpUrl = (String) obj1.get("helpUrl");
				JSONArray nodes = (JSONArray) obj1.get("nodes");
				this.obj5 = new JSONArray();
				this.obj6 = new JSONArray();
				this.obj7 = new ArrayList();
				
				JSONObject object, ESObjectViolationArray;
				String recommendations;

				for (int j = 1; j <= nodes.size(); ++j) {
					object = (JSONObject) nodes.get(j - 1);
					this.html = (String) object.get("html");
					this.obj6.add(this.html);
					this.impact = (String) object.get("impact");
					this.nodes2 = (JSONArray) object.get("target");
					recommendations = (String) this.nodes2.get(0);
					this.obj5.add(recommendations);
					JSONArray nodesnone = (JSONArray) object.get("none");
					for (int l = 1; l <= nodesnone.size(); ++l) {
						JSONObject obj22 = (JSONObject) nodesnone.get(l - 1);
						this.message = (String) obj22.get("message");
						if (this.message != null) {
							this.obj7.add(this.message);
						}
					}

					JSONArray nodes11 = (JSONArray) object.get("any");

					for (int k = 1; k <= nodes11.size(); ++k) {
						JSONObject obj22 = (JSONObject) nodes11.get(k - 1);
						this.message = (String) obj22.get("message");
						if (this.message != null) {
							this.obj7.add(this.message);
						}
					}

				}

				JSONArray tags = (JSONArray) obj1.get("tags");

				if (this.impact.equalsIgnoreCase("critical")) {
					++counter;
				} else if (this.impact.equalsIgnoreCase("serious")) {
					++counter1;
				} else if (this.impact.equalsIgnoreCase("moderate")) {
					++counter2;
				} else if (this.impact.equalsIgnoreCase("minor")) {
					++counter3;
				}

				object = new JSONObject();

				recommendations = "--> Element does not have an alt attribute\n--> aria-label attribute does not exist or is empty\n--> aria-labelledby attribute does not exist, references elements that do not exist or references elements that are empty or not visible\n--> Element has no title attribute or the title attribute is empty\n--> Element's default semantics were not overridden with role=presentation\n--> Element's default semantics were not overridden with role=none";
				// recommendations = "Element does not have an alt attribute;--> aria-label
				// attribute does not exist or is empty;--> aria-labelledby attribute does not
				// exist, references elements that do not exist or references elements that are
				// empty or not visible;--> Element has no title attribute or the title
				// attribute is empty;--> Element's default semantics were not overridden with
				// role=presentation;--> Element's default semantics were not overridden with
				// role=none";
				recommendations = recommendations.replaceAll("\"", "");

				String heading = "--> Element does not have text that is visible to screen readers\n--> Element's default semantics were not overridden with role=presentation\n--> Element's default semantics were not overridden with role=none";
				heading = heading.replaceAll("\"", "");

				String frames = "--> aria-label attribute does not exist or is empty\n--> aria-labelledby attribute does not exist, references elements that do not exist or references elements that are empty or not visible\n--> Element has no title attribute or the title attribute is empty\n--> Element's default semantics were not overridden with role=presentation\n--> Element's default semantics were not overridden with role=none";
				frames = frames.replaceAll("\"", "");

				String forms = "--> aria-label attribute does not exist or is empty\n--> aria-labelledby attribute does not exist, references elements that do not exist or references elements that are empty or not visible\n--> Form element does not have an implicit (wrapped) <label>\n--> Form element does not have an explicit <label>\n--> Element has no title attribute or the title attribute is empty";
				forms = forms.replaceAll("\"", "");

				String links = "--> Element is in tab order and does not have accessible text\n--> Element does not have text that is visible to screen readers\n--> aria-label attribute does not exist or is empty\n--> aria-labelledby attribute does not exist, references elements that do not exist or references elements that are empty or not visible\n--> Element's default semantics were not overridden with role=presentation\n--> Element's default semantics were not overridden with role=none";
				links = links.replaceAll("\"", "");

				String colorcontrast = "--> Element has insufficient color contrast of 2.74 (foreground color: #00bced, background color: #52656a, font size: 19.5pt, font weight: normal)\n--> Expected contrast ratio of 3:1\n--> Element has insufficient color contrast of 4.4 (foreground color: #ffffff, background color: #3c8834, font size: 17.3pt, font weight: normal).\n--> Expected contrast ratio of 4.5:1\n--> Element has insufficient color contrast of 4.4 (foreground color: #ffffff, background color: #3c8834, font size: 13.5pt, font weight: normal). \n--> Expected contrast ratio of 4.5:1\n--> Element has insufficient color contrast of 3.01 (foreground color: #ffffff, background color: #5da62d, font size: 15.0pt, font weight: normal).\n--> Expected contrast ratio of 4.5:1\n--> Element has insufficient color contrast of 3.28 (foreground color: #ffffff, background color: #4298b5, font size: 12.0pt, font weight: bold). \n--> Expected contrast ratio of 4.5:1\n--> Element has insufficient color contrast of 3.28 (foreground color: #ffffff, background color: #4298b5, font size: 12.0pt, font weight: bold). \n--> Expected contrast ratio of 4.5:1\n--> Element has insufficient color contrast of 3.28 (foreground color: #ffffff, background color: #4298b5, font size: 11.3pt, font weight: normal). \n--> Expected contrast ratio of 4.5:1\n--> Element has insufficient color contrast of 2.37 (foreground color: #ebebeb, background color: #999999, font size: 8.3pt, font weight: bold). \n--> Expected contrast ratio of 4.5:1\n--> Element has insufficient color contrast of 4.13 (foreground color: #3385ad, background color: #ffffff, font size: 9.0pt, font weight: normal).\n--> Expected contrast ratio of 4.5:1\n--> Element has insufficient color contrast of 2.65 (foreground color: #999999, background color: #f7f7f7, font size: 8.3pt, font weight: normal). \n--> Expected contrast ratio of 4.5:1";
				colorcontrast = colorcontrast.replaceAll("\"", "");

				String color = "-- >Ensures the contrast between foreground and background colors meets WCAG 2 AA contrast ratio thresholds.\n--> Expected contrast ratio of 4.5:1 for the below mentioned issues";
				color = color.replaceAll("\"", "");

				String idattribute = "--> Document has multiple elements with the same id attribute: hidden-content\n--> Document has multiple elements with the same id attribute: back\n--> Document has multiple elements with the same id attribute: Page-1\n--> Document has multiple elements with the same id attribute: Shape \n--> Document has multiple elements with the same id attribute: videoCode_odd\n--> Document has multiple elements with the same id attribute: segments-we-serve\n--> Document has multiple elements with the same id attribute: bluepage\n--> Document has multiple elements with the same id attribute: OLT\n--> Document has multiple elements with the same id attribute: callout\n--> Document has multiple elements with the same id attribute: segments\n--> Document has multiple elements with the same id attribute: callout-tab\n--> Document has multiple elements with the same id attribute: videoCode";
				idattribute = idattribute.replaceAll("\"", "");

				String groups = "--> List element has direct children that are not allowed inside <dt> or <dd> elements";
				groups = groups.replaceAll("\"", "");

				String lang = "--> The <html> element does not have a lang attribute";
				lang = lang.replaceAll("\"", "");

				String zoom = "--> <meta> tag disables zooming on mobile devices";
				zoom = zoom.replaceAll("\"", "");

				String zero = "--> Element has a tabindex greater than 0";
				zero = zero.replaceAll("\"", "");

				String checkbox = "--> All elements with the name \"quesImage\" do not reference the same element with aria-labelledby,\nFieldset does not have a legend as its first child";
				checkbox = checkbox.replaceAll("\"", "");

				String aria = "--> ARIA attribute is not allowed: aria-selected=true" + "\n(OR)\n"
						+ "--> ARIA attribute is not allowed: aria-expanded=false\"";
				aria = aria.replaceAll("\"", "");

				String certainaria = "--> Required ARIA child role not present: tab";
				certainaria = certainaria.replaceAll("\"", "");

				if (!this.help.contains("<ul>") && !this.help.contains("<script>") && !this.help.contains("<ol>")
						&& !this.help.contains("<li>") && !this.help.contains("<template>")) {
					object.put("Help", this.help);
				} else {
					object.put("Help", "Ensures that lists are structured correctly");
				}

				object.put("description", this.description);
				object.put("id", this.id);
				object.put("helpUrl", this.helpUrl);

				if (this.obj7.isEmpty() && this.obj7 == null) {
					object.put("message", this.obj7.toString().replaceAll(",", "\n\n").replaceAll("\"", "")
							.replaceAll("[\\[\\]]", ""));
				} else if (this.help.equalsIgnoreCase("Images must have alternate text")) {
					object.put("message", recommendations);
				} else if (this.help.equalsIgnoreCase("Headings must not be empty")) {
					object.put("message", heading);
				} else if (this.help.equalsIgnoreCase("Frames must have title attribute")) {
					object.put("message", frames);
				} else if (this.help.equalsIgnoreCase("Form elements must have labels")) {
					object.put("message", forms);
				} else if (this.help.equalsIgnoreCase("Links must have discernible text")) {
					object.put("message", links);
				} else if (this.help.equalsIgnoreCase("Elements must have sufficient color contrast")) {
					object.put("message", color);
				} else if (this.help.equalsIgnoreCase("id attribute value must be unique")) {
					object.put("message", idattribute);
				} else if (this.help
						.equalsIgnoreCase("elements must only directly contain properly-ordered and groups")) {
					object.put("message", groups);
				} else if (this.help.equalsIgnoreCase("element must have a lang attribute")) {
					object.put("message", lang);
				} else if (this.help.equalsIgnoreCase("Zooming and scaling must not be disabled")) {
					object.put("message", zoom);
				} else if (this.help.equalsIgnoreCase("Elements should not have tabindex greater than zero")) {
					object.put("message", zero);
				} else if (this.help.equalsIgnoreCase(
						"Checkbox inputs with the same name attribute value must be part of a group")) {
					object.put("message", checkbox);
				} else if (this.help.equalsIgnoreCase("Elements must only use allowed ARIA attributes")) {
					object.put("message", aria);
				} else if (this.help.equalsIgnoreCase("Certain ARIA roles must contain particular children")) {
					object.put("message", certainaria);
				}
				/*
				 * else { object.put("message", "Recommendation not found in aXe!!"); }
				 */

				object.put("html", this.obj6.toJSONString().replaceAll(",", "\n\n").replaceAll("\"", "")
						.replaceAll("[\\[\\]]", ""));
				object.put("impact", this.impact);

				List<String> disabilitytype = new ArrayList();
				this.hs = new HashSet();
				
				if (id.equals("blink") || id.equals("marquee") || id.equals("css-orientation-lock")) {
					this.hs.add("Visual, Physiacal, Cognitive");
					++visual;
					++cognitive;
					++physical;
				}

				if (id.equals("definition-list") || id.equals("object-alt") || id.equals("list")
						|| id.equals("th-has-data-cells") || id.equals("td-headers-attr") || id.equals("td-has-header")
						|| id.equals("landmark-banner-is-top-level") || id.equals("button-name")
						|| id.equals("table-fake-caption") || id.equals("role-img-alt")
						|| id.equals("identical-links-same-purpose") || id.equals("frame-tested")
						|| id.equals("duplicate-id-active") || id.equals("duplicate-id")
						|| id.equals("duplicate-id-aria") || id.equals("image-alt") || id.equals("input-button-name")
						|| id.equals("page-has-heading-one") || id.equals("bypass") || id.equals("image-redundant-alt")
						|| id.equals("table-duplicate-name")) {

					this.hs.add("Visual, Deafblind");

					++visual;
					++deafblind;
				}

				if (id.equals("dlitem") || id.equals("area-alt") || id.equals("region") || id.equals("aria-valid-attr")
						|| id.equals("aria-valid-attr-value") || id.equals("aria-input-field-name")
						|| id.equals("aria-allowed-role") || id.equals("aria-toggle-field-name")
						|| id.equals("aria-hidden-focus") || id.equals("p-as-heading")
						|| id.equals("aria-required-parent") || id.equals("aria-required-children")
						|| id.equals("landmark-complementary-is-top-level")
						|| id.equals("landmark-contentinfo-is-top-level") || id.equals("document-title")
						|| id.equals("focus-order-semantics") || id.equals("aria-allowed-attr") || id.equals("tabindex")
						|| id.equals("scrollable-region-focusable") || id.equals("landmark-no-duplicate-main")
						|| id.equals("label") || id.equals("label-title-only")
						|| id.equals("form-field-multiple-labels") || id.equals("frame-title-unique")
						|| id.equals("heading-order") || id.equals("empty-heading") || id.equals("input-image-alt")
						|| id.equals("label-content-name-mismatch") || id.equals("landmark-unique")
						|| id.equals("link-name") || id.equals("landmark-main-is-top-level")
						|| id.equals("landmark-one-main") || id.equals("landmark-no-duplicate-banner")
						|| id.equals("landmark-no-duplicate-contentinfo") || id.equals("aria-required-attr")
						|| id.equals("scope-attr-valid") || id.equals("server-side-image-map")
						|| id.equals("svg-img-alt") || id.equals("skip-link") || id.equals("meta-refresh")
						|| id.equals("aria-roledescription") || id.equals("aria-roledescription")) {

					this.hs.add("Visual, Deafblind, Physical");

					++visual;
					++deafblind;
					++physical;
				}

				if (id.equals("html-has-lang") || id.equals("html-lang-valid") || id.equals("html-xml-lang-mismatch")
						|| id.equals("no-autoplay-audio") || id.equals("valid-lang")) {

					this.hs.add("Visual, Deafblind, Cognitive");

					++visual;
					++deafblind;
					++cognitive;
				}
				if (id.equals("listitem")) {

					this.hs.add("Visual, Physical, Hearing");

					++visual;
					++physical;
					++hearing;
				}
				if (id.equals("accesskeys") || id.equals("hidden-content")) {

					this.hs.add("Visual, Physical");

					++visual;
					++physical;

				}
				if (id.equals("aria-hidden-body") || id.equals("link-in-text-block") || id.equals("color-contrast")
						|| id.equals("meta-viewport-large") || id.equals("meta-viewport")) {

					this.hs.add("Visual");

					++visual;

				}
				if (id.equals("autocomplete-valid") || id.equals("avoid-inline-spacing")) {

					this.hs.add("Visual, Physical, Cognitive, Deafblind, Attention Deficit");

					++visual;
					++physical;
					++deafblind;
					++cognitive;

				}

				disabilitytype.clear();
				disabilitytype.addAll(this.hs);
				
				object.put("Guidelines", tags);
				object.put("DisabilityType", disabilitytype);
				object.put("Target", this.obj5);

				// logger.info("Dharik : "+pagenames.substring(id.lastIndexOf("\\") +
				// 1));
				// added by dharik to get the accessibility pagename from the file path
				String[] bits = pagenames.split("\\\\");
				String page = bits[bits.length - 1];
				// logger.info("Pagename: "+page);

				this.object1.put("pagename", page);

				ESObjectViolationArray = object;
				if ((IsESRequired.equalsIgnoreCase("yes") == true)) {
					
					ESObjectViolationArray.put("RunID", RunID);
					ESObjectViolationArray.put("testcase", testcase);
					ESObjectViolationArray.put("pagename", page);
					
					ES.post_Violation_Recommondation(ESObjectViolationArray);
					
					object.remove("RunID");
					object.remove("testcase");
					object.remove("pagename");
					object.remove("Timestamp");
					
					countobject.remove("Project");
					countobject.remove("Build");
				}

				this.newarray.add(object);
				logger.info("OBJECT: "+object);

			}

			// added by dharik to get the accessibility pagename from the file path
			String[] bits = pagenames.split("\\\\");
			String page = bits[bits.length - 1];
			logger.info("Pagename: "+page);
			
			countobject.put("pagename", page);
			countobject.put("critical", counter);
			countobject.put("serious", counter1);
			countobject.put("moderate", counter2);
			countobject.put("minor", counter3);
			countobject.put("visual", visual);
			countobject.put("physical", physical);
			countobject.put("hearing", hearing);
			countobject.put("cognitive", cognitive);
			countobject.put("deafblind", deafblind);

			if ((RunID != 0) && (IsESRequired.equalsIgnoreCase("yes") == true)) {
				// if critical or serious or moderate or minor issues >0
				if (counter > 0 || counter1 > 0 || counter2 > 0 || counter3 > 0) {
					countobject.put("PageStatus", "Need Improvement!!");
				} else {
					countobject.put("PageStatus", "Good!");
				}
				// countobject.put("Guidelines", "Accessibility Audit against WCAG 2.1
				// Guidelines");
				// Similar content of donut.json for elasticsearch
				ES.post_Impact_Severity_To_Elastic(countobject, testcase);
				countobject.remove("PageStatus");
				countobject.remove("testcase");
				countobject.remove("Timestamp");
				countobject.remove("RunID");
				countobject.remove("Project");
				countobject.remove("Build");
			}

			this.counterarray.add(countobject);
			this.newarray.add(this.object1);
			this.json.add(this.newarray);
		}

		logger.info("Violation Result:" + this.json);

		File myfile = new File(localPath + "/violations.json");
		myfile.createNewFile();
		FileWriter writerviolationsfile = null;

		try {
			writerviolationsfile = new FileWriter(myfile);
			writerviolationsfile.write(this.json.toString());
			writerviolationsfile.flush();
		}

		catch (Exception ex) {
			logger.info("Exception : "+ex.getMessage());
		} finally {
			writerviolationsfile.close();
		}

		this.mainObject.put("rundetails", this.counterarray);
		this.json1.add(this.mainObject);

		logger.info("Severity Count Result:" + this.json1);

		File myfile1 = new File(localPath + "/donut.json");
		myfile1.createNewFile();
		FileWriter writerviolationsfile1 = null;

		try {
			writerviolationsfile1 = new FileWriter(myfile1);
			writerviolationsfile1.write(this.mainObject.toString());
			writerviolationsfile1.flush();
		} catch (Exception ex) {
			logger.info("Exception : "+ex.getMessage());
		} finally {
			writerviolationsfile1.close();
		}

		return this.counterarray;

	}

}
