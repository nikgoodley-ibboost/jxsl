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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.scenario.RunReport;
import com.servicelibre.jxsl.scenario.XslScenario;
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

	public XspecTestScenarioRunner(File xspecTestsGeneratorFile) {
		super();
		this.xspecTestsGeneratorScenario = new XslScenario(xspecTestsGeneratorFile);

	}

	public XspecTestScenarioRunner(XslScenario xspecTestsGeneratorScenario) {
		super();
		this.xspecTestsGeneratorScenario = xspecTestsGeneratorScenario;
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
		// TODO implement
		return null;
	}

	/**
	 * 
	 * Return in runReport:
	 * mainOutput = XML file test results
	 * first otherOutputFiles =  HTML file test report
	 * 
	 */
	public RunReport run(File xspecFile) {

		RunReport testRunReport = null;

		// Generate custom test XSL
		File generatedTestFile = generateTestFile(xspecFile).mainOutputFile;

		if (generatedTestFile != null && generatedTestFile.exists()) {

			// Execute the xspec test
			testRunReport = executeTest(xspecFile, generatedTestFile);
			
			// Produce HTML report if transformation scenario provided
			if (xspecResultHtmlConvertorScenario != null) {
			    RunReport htmlrunReport = generateHtmlReport(testRunReport.mainOutputFile);
			    if(testRunReport != null) {
			        testRunReport.otherOutputFiles.add(htmlrunReport.mainOutputFile);
			    }
			}

		} else {
			logger.error("Unable to find Xspec generated test file.");
		}

		return testRunReport;
	}

	private RunReport generateHtmlReport(File xmlResultFile) {

		RunReport runReport = new RunReport();

		if (xspecResultHtmlConvertorScenario != null) {
			xspecResultHtmlConvertorScenario.getTransformer().reset();
			xspecResultHtmlConvertorScenario.setSaveOutputOnDisk(true);
			xspecResultHtmlConvertorScenario.setMainOutputDir(xmlResultFile.getParentFile());
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
		xspecTests.setSaveRunReport(true);
		xspecTests.setSaveXmlSource(true);
		xspecTests.setMainOutputName(xspecFile.getName().replace(".xspec", "-result.xml"));
		xspecTests.setInitialTemplate("{http://www.jenitennison.com/xslt/xspec}main");
		xspecTests.setName("xmlResults");

		// FIXME xspecFile could be omitted since initialTemplate has been
		// set?  Create an apply() method without arg?
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
		xspecTestsGeneratorScenario.setSubDirTimeStamp(true);
		xspecTestsGeneratorScenario.setName("xspec");
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

}
