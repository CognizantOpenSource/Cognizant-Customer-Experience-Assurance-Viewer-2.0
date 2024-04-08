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

package com.axe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.defect.axeparser.Parsing;
import com.deque.axe.AXE.Builder;

public class runA11y {
	
	private static final Logger log = LoggerFactory.getLogger(RunAxe.class);
	
	static RunAxe axe = new RunAxe();
	static JSONArray jsonresults = null;
	static String fileresult = null;
	static ArrayList<String> filelist = new ArrayList<String>();
	static ArrayList<String> pagename = new ArrayList<String>();
	static Parsing parse = new Parsing();

	static final URL scriptUrl = runA11y.class.getResource("axe.min.js");

	public static ArrayList<String> exec(String fileName, String pageName, WebDriver driver)
			throws JSONException, FileNotFoundException, IOException, ParseException {

		log.info("Inside Run A11Y ..");
		String filePath = System.getProperty("user.dir");
		String frameworkPath = new File(Paths.get(filePath).normalize().toString()).getAbsolutePath();
		log.info("Framework path @ " + frameworkPath);
		jsonresults = axe.run_axe(driver, scriptUrl);
		log.info("Results : "+jsonresults);
		fileresult = axe.dynamic_filecreation(fileName + "\\" + pageName, jsonresults);
		filelist.add(fileresult);
		pagename.add(pageName);
		return (filelist);
	}

}
