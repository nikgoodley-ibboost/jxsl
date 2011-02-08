/**
 * Java XSL code library
 *
 * Copyright (C) 2010 Benoit Mercier <info@servicelibre.com> — All rights reserved.
 *
 * This file is part of jxsl.
 *
 * jxsl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * jxsl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jxsl.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.servicelibre.jxsl.scenario.test.xspec;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.xml.sax.SAXException;

import com.servicelibre.jxsl.dstest.Document;
import com.servicelibre.jxsl.scenario.RunReport;
import com.servicelibre.jxsl.scenario.XslScenario;
import com.servicelibre.jxsl.scenario.test.FailureReport;
import com.servicelibre.jxsl.scenario.test.TestReport;
import com.servicelibre.jxsl.scenario.test.XslTestScenarioRunner;

/**
 * Run XML testdoc => generate Xspec XSL to apply on the fly then apply!
 * 
 */
public class XspecTestScenarioRunner implements XslTestScenarioRunner {

    static Logger logger = LoggerFactory.getLogger(XspecTestScenarioRunner.class);

    static {
	System.setProperty("javax.xml.transform.TransformerFactory", XslScenario.SAXON_TRANSFORMER_FACTORY_FQCN);
    }

    private File outputDir = new File(System.getProperty("java.io.tmpdir"));
    private XslScenario xspecTestsGeneratorScenario;
    private XslScenario xspecResultHtmlConvertorScenario;

    private boolean storeResultsInSubDir = true;
    private boolean resultsSubDirWithTimeStamp = true;
    private XPathExpression successXpath;
    private XPathExpression testFailedCount;
    private XPathExpression testCount;
    private TestReport lastRunReport;

    private DocumentBuilder xmlBuilder;

    public XspecTestScenarioRunner(File xspecTestsGeneratorFile) {
	super();
	this.xspecTestsGeneratorScenario = new XslScenario(xspecTestsGeneratorFile);
	init();
    }

    public XspecTestScenarioRunner(XslScenario xspecTestsGeneratorScenario) {
	super();
	this.xspecTestsGeneratorScenario = xspecTestsGeneratorScenario;
	init();
    }

    private void init() {

	XPath xpath = XPathFactory.newInstance().newXPath();

	SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
	namespaceContext.bindNamespaceUri("x", "http://www.jenitennison.com/xslt/xspec");
	xpath.setNamespaceContext(namespaceContext);

	try {

	    successXpath = xpath.compile("count(//x:test[@successful ='false'] ) = 0");
	    testFailedCount = xpath.compile("count(//x:test[@successful ='false'] )");
	    testCount = xpath.compile("count(//x:test)");
	} catch (XPathExpressionException e) {
	    logger.error("Error while initializing {}.", this.getClass().getName(), e);
	}

	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	docFactory.setNamespaceAware(true);

	try {
	    xmlBuilder = docFactory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
	    logger.error("Error while configuring XML parser", e);
	}

    }

    public void reset() {
	xspecTestsGeneratorScenario.getTransformer().reset();
    }

    public void setOutputDir(File outputDir) {
	this.outputDir = outputDir;

    }

    public File getOutputDir() {
	return outputDir;
    }

    public TestReport getLastRunReport() {
	return this.lastRunReport;
    }

    @Override
    public TestReport run(File xspecFile) {
	return run(xspecFile, outputDir, null);
    }

    @Override
    public TestReport run(File xspecFile, File testOutputDir) {
	return run(xspecFile, testOutputDir, null);
    }    
    
    
    /**
     * 
     * TODO add optional Document parameter
     * 
     * // TODO improve performance
     * In order to improve performance, we should not regenerate/recompile xspec xsl.  We should instead compile it once and change/set
     * parameter (document URL) at each execution.  This implies that we have to keep a compiled version of the Xsl for all runs 
     * of the testScenario with the same xspec file.
     */
    @Override
    public TestReport run(File xspecFile, File outputDir, Document xmlDoc) {

	this.outputDir = outputDir;

	RunReport testRunReport = null;
	TestReport testReport = new TestReport();

	// Generate custom test XSL
	File generatedTestFile = generateTestFile(xspecFile).mainOutputFile;

	if (generatedTestFile != null && generatedTestFile.exists()) {

	    // TODO if Document parameter != null => filter content  => replace ${jxslTestDocument} by current Document path
	    if(xmlDoc != null) {
		xmlDoc.getFile();
	    }
	    
	    // Execute the xspec test
	    testRunReport = executeTest(xspecFile, generatedTestFile);

	    // Produce HTML report if transformation scenario provided
	    if (xspecResultHtmlConvertorScenario != null) {
		RunReport htmlrunReport = generateHtmlReport(testRunReport.mainOutputFile);

		if (testRunReport != null) {
		    testRunReport.otherOutputFiles.add(htmlrunReport.mainOutputFile);
		}
	    }

	    if (testRunReport != null)

	    {

		testReport.executionTime = testRunReport.executionTime;
		testReport.executionDate = testRunReport.executionDate;

		testReport.success = getSuccess(testRunReport.mainOutputFile);

		if (!testReport.success) {
		    testReport.failureReport = getFailureReport(testRunReport.mainOutputFile);
		}

		if (testRunReport.otherOutputFiles.size() > 0) {
		    try {
			testReport.reportUrl = testRunReport.otherOutputFiles.get(0).toURI().toURL();
		    } catch (MalformedURLException e) {
			logger.error("Error while converting test report File to URL.", e);
		    }
		}

		try {

		    org.w3c.dom.Document xspecResultDoc = xmlBuilder.parse(testRunReport.mainOutputFile);

		    testReport.testCount = ((Double) testCount.evaluate(xspecResultDoc, XPathConstants.NUMBER)).intValue();
		    testReport.testFailedCount = ((Double) testFailedCount.evaluate(xspecResultDoc, XPathConstants.NUMBER)).intValue();

		} catch (SAXException e) {
		    logger.error("Error while creating failure report", e);
		} catch (IOException e) {
		    logger.error("Error while creating failure report", e);
		} catch (XPathExpressionException e) {
		    logger.error("Error while evaluating XPath during failure report creation", e);
		}

	    } else {
		testReport.success = false;

	    }

	} else {
	    logger.error("Unable to find Xspec generated test file.");
	}

	return testReport;

    }

    private RunReport generateHtmlReport(File xmlResultFile) {

	RunReport runReport = new RunReport();

	if (xspecResultHtmlConvertorScenario != null) {
	    xspecResultHtmlConvertorScenario.getTransformer().reset();
	    xspecResultHtmlConvertorScenario.setSaveOutputOnDisk(true);
	    xspecResultHtmlConvertorScenario.setMainOutputDir(xmlResultFile.getParentFile());
	    xspecResultHtmlConvertorScenario.setStoreResultsInSubDir(false);
	    xspecResultHtmlConvertorScenario.setName("htmlConvertor");
	    xspecResultHtmlConvertorScenario.setMainOutputName(xmlResultFile.getName().replace(".xml", ".html"));
	    xspecResultHtmlConvertorScenario.apply(xmlResultFile);
	    runReport = xspecResultHtmlConvertorScenario.getLastRunReport();

	}
	return runReport;
    }

    /**
     * Execute xspec test
     * 
     * @param xspecFile
     * @param generatedTestFile
     * @return
     */
    private RunReport executeTest(File xspecFile, File generatedTestFile) {

	// Execute test XSL on xspecFile and get XML results
	XslScenario xspecTests = new XslScenario(generatedTestFile);

	xspecTests.getTransformer().reset();
	xspecTests.setSaveOutputOnDisk(true);
	xspecTests.setMainOutputDir(generatedTestFile.getParentFile());
	xspecTests.setStoreResultsInSubDir(false);
	xspecTests.setSaveRunReport(true);
	xspecTests.setSaveXmlSource(true);
	xspecTests.setMainOutputName(xspecFile.getName().replace(".xspec", "-result.xml"));
	xspecTests.setInitialTemplate("{http://www.jenitennison.com/xslt/xspec}main");
	xspecTests.setName(xspecFile.getName().replaceAll(".xspec", "_xspec"));

	// FIXME xspecFile could be omitted since initialTemplate has been
	// set? Create an apply() method without arg?
	xspecTests.apply(xspecFile);

	return xspecTests.getLastRunReport();
    }

    /**
     * Generate test XSL
     * 
     * @param xspecFile
     * @return
     */
    private RunReport generateTestFile(File xspecFile) {

	xspecTestsGeneratorScenario.getTransformer().reset();

	xspecTestsGeneratorScenario.setMainOutputDir(outputDir);

	xspecTestsGeneratorScenario.setStoreResultsInSubDir(storeResultsInSubDir);

	xspecTestsGeneratorScenario.setResultsSubDirWithTimeStamp(resultsSubDirWithTimeStamp);

	xspecTestsGeneratorScenario.setName(xspecFile.getName().replace(".xspec", ""));

	xspecTestsGeneratorScenario.setMainOutputName(xspecFile.getName().replace(".xspec", ".xslt"));

	xspecTestsGeneratorScenario.setSaveOutputOnDisk(true);

	xspecTestsGeneratorScenario.apply(xspecFile);

	return xspecTestsGeneratorScenario.getLastRunReport();
    }

    public XslScenario getXspecTestsGeneratorScenario() {
	return xspecTestsGeneratorScenario;
    }

    public void setXspecTestsGeneratorScenario(XslScenario xspecTestsGeneratorScenario) {
	this.xspecTestsGeneratorScenario = xspecTestsGeneratorScenario;
    }

    public XslScenario getXspecResultHtmlConvertorScenario() {
	return xspecResultHtmlConvertorScenario;
    }

    public void setXspecResultHtmlConvertorScenario(XslScenario xspecResultHtmlConvertorScenario) {
	this.xspecResultHtmlConvertorScenario = xspecResultHtmlConvertorScenario;
    }

    public void cleanOutputDir() {
	try {
	    FileUtils.cleanDirectory(outputDir);
	} catch (IOException e) {
	    logger.equals(e);
	}

    }

    public boolean isResultsSubDirWithTimeStamp() {
	return resultsSubDirWithTimeStamp;
    }

    public void setResultsSubDirWithTimeStamp(boolean resultsSubDirWithTimeStamp) {
	this.resultsSubDirWithTimeStamp = resultsSubDirWithTimeStamp;
    }

    public boolean isStoreResultsInSubDir() {
	return storeResultsInSubDir;
    }

    public void setStoreResultsInSubDir(boolean storeResultsInSubDir) {
	this.storeResultsInSubDir = storeResultsInSubDir;
    }

    private boolean getSuccess(File mainOutputFile) {
	String success = "false";

	try {
	    success = successXpath.evaluate(new StreamSource(mainOutputFile));
	} catch (XPathExpressionException e) {
	    logger.error("Error while retrieving success/failure in test report {}", mainOutputFile, e);
	}

	return Boolean.parseBoolean(success);
    }

    // TODO ????
    private FailureReport getFailureReport(File mainOutputFile) {
	FailureReport failureReport = new FailureReport("XSpec Test failed to run.  See error log.");

	return failureReport;
    }



}
