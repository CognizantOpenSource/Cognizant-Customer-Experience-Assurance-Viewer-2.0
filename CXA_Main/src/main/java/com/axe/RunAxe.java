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

import com.deque.axe.AXE.Builder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunAxe {

	private static final Logger log = LoggerFactory.getLogger(RunAxe.class);

	public static JSONArray run_axe(WebDriver driver, URL scriptUrl) throws JSONException {
		JSONArray violations = null;
		JSONObject responseJSON = (new Builder(driver, scriptUrl)).analyze();
		violations = responseJSON.getJSONArray("violations");
		return violations;
	}

	public static String Sub_String_url(String urlString, int count) {
		String urlString1 = null;
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException var5) {
			var5.printStackTrace();
		}
		urlString1 = url.getHost().replaceFirst("^[^\\.]+\\.([^\\.]+)\\..*$", "$1");
		String newfile = ".\\..\\" + urlString1 + count + ".json";
		return newfile;
	}

	public static String dynamic_filecreation(String map, JSONArray results) throws IOException {
		File file = new File(map);
		FileWriter writerhome = null;
		try {
			writerhome = new FileWriter(file);
			writerhome.write(results.toString());
			writerhome.flush();
			if (file.exists()) {
				log.info("File is created under the path @"+file.getAbsolutePath());
			} else {
				log.error("File is not created");
			}
		} catch (IOException var6) {
			var6.printStackTrace();
		} finally {
			writerhome.close();
		}
		return map;
	}

	public List<String> filelist1(List<String> filename) {
		return filename;
	}
}
