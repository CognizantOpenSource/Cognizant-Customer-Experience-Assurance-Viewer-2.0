# Cognizant Customer Experience Assurance Viewer 2.0

Cognizant CxA Viewer - 2.0 is an open-source project.

## About the Project

The Cognizant CxA Viewer 2.0 everages existing functional automation (Selenium) scripts for client-side (page-level) performance evaluation and identify accessibility issues and also provide recommendations to improve end-user experience.

## Key Features

- Client Side Evaluation -> Single user client side performance evaluation and profiling
- WCAG Standard & Guidelines –automated accessibility scan based on WCAG guidelines
- Can leverage existing Selenium scripts
- Supports testing with cache/no-cache
- Supports testing with proxy and non-proxy based applciations
- Can use json outputs to build html report

## Benefits

- Identification & resolution of single-user performance issues
- Client side profiling report with optimization recommendations
- Kibana or other dashboard can be integreated easily
- Automated accessibility audits and recommendation to fix violations
- Integrates seamlessly with a test automation tools like Selenium
- Integration with CI tools
- WCAG Standard & Guidelines –automated accessibility scan based on WCAG guidelines

## Download

Click [here](https://github.com/CognizantOpenSource/Cognizant-Customer-Experience-Assurance-Viewer-2.0/archive/refs/heads/main.zip "https://github.com/CognizantOpenSource/Cognizant-Customer-Experience-Assurance-Viewer-2.0/archive/refs/heads/main.zip") to download the latest version of Cognizant CxA Viewer 2.0.
After extracting the zip, follow the steps from docs.

## Getting Started

To get started with the Cognizant Customer Experience Assurance Viewer, follow the steps below:

1. Clone the repository:  `git clone https://github.com/CognizantOpenSource/Cognizant-Customer-Experience-Assurance-Viewer-2.0.git`
2. Both front-end and back-end will be in different folders
3. The installation doc will be available on docs/Techinical details and User Guide.pdf

## Prerequisites

The below jar files can be specified in pom.xml file in the maven project:

- axe-selenium-3.0.jar
- browsermob-core-2.1.5.jar
- browsermob-legacy-2.1.5.jar
- browsermob-dist-2.1.5.jar
- browsermob-rest-2.1.5.jar
- json-simple-1.1.1.jar
- dec-0.1.2.jar
- json-20140107.jar
- jsoup-1.11.3.jar
- json-simple-1.1.1.jar
- selenium-java-3.141.59.jar or latest
- selenium-support-3.141.59.jar or latest
- guava-30.1-jre.jar

## Documentation

You can check the Help Documentation from here

## Installations

### Drivers

- chromedriver.exe (should be same as chrome browser version)
- geckodriver.exe

### Import a java project

1. Open Eclipse
2. Browse your workspace path and click 'OK'button
3. Goto File-> Import-> Existing Maven Projects
4. Click 'Next' button
5. Browse for the existing project where CxA Viewer 2.0 project is exist.
6. Click 'OK' button
7. Click 'Finish' button

### Elements of CxA Viewer 2.0 Project:

| Item      | Description                                  |
| --------- | -------------------------------------------- |
| bin       | Location of the class files                  |
| src       | Location of the source code files            |
| drivers   | Location of the driver (.exe) files          |
| cx_config | Location of the `dataFile.properties` file |

### dataFile.properties

This `dataFile.properties` file located in the `cx_config` folder is used to parameter the fields such as environment, project, build, browser, homepage url, report path, test type and iteration count for CxA Viewer 2.0 to trigger the test and generate the test report. We can change these parameters as needed.

Below is the format of dataFile.properties file:

###### Format

ENV=`<EnvironmentName>`

PROJECT=`<ProjectName>`

browser=`<BrowserName>`-

#browser=Chrome- -> to run in chrome

#browser=Firefox- -> to run in firefox

BUILD=build

URL=`<HomePageURL>`

#URL=www.google.com

ReportPath=cx_reports

testType="`<test type>`"

#testType="pt" -> to trigger client-side performance test

#testType="at" -> to trigger accessibility validations

iterationCount=`<Numberofperformanceiterations>`

#iterationCount=2 -> to run 2 iterations (maximum it will support 3 iterations)

### CxA Viewer 2.0 Methods

**1.In the setup() method, find the direct urls provided for performance and accessibility validations (you can write the code for the click event as well)**

_**2.Syntax of PerformanceExecution() Method:**_

```java

Prototype:PerformanceExecution(browser,environment,URL,PageName,XPATH,SLA)

E.g.PerformanceExecution(browser,ENV,"http://google.com","Google Home","//*[@id=\"tab\"]/div/div/div[2]/div/div/div/form/div",4000L);

```

**3.In the performance execution method, just change the following arguements as per the requirements:**

> URL

> PageName

> XPATH

> SLA

**4._Syntax of runA11y.exec() Method:_**

```java

Prototype:runA11y.exec(ReportPath,PageName,driver);

E.g.runA11y.exec(ReportPath,"Google Home",driver);

```

`Note:` Before invoking the above method, pass the required url in the driver.get() method. e.g. driver.get("www.google.com");

5.**Change `PageName` in the runA11y.exec() method**

## Development / Contribution

We welcome contributions from the community to enhance the capabilities of the Cognizant CxA Viewer 2.0. If you would like to contribute, please follow our guidelines outlined in the CONTRIBUTING.md file.

## Code of Conduct

To provide clarity on what is expected of our members,Cognizant Customer Experience Assurance Viewer 2.0 has adopted the code of conduct defined by the Contributor Covenant. This document is used across many open source communities and we think it articulates our values well. For more, see the Code of Conduct.

## License

Cognizant Customer Experience Assurance Viewer 2.0 is licensed under Apache License, Version 2.0. Feel free to use, modify, and distribute this software as per the terms of the license.

Thank you for your interest in our project! We hope you find the Cognizant CxA Viewer 2.0 valuable for your business needs.
