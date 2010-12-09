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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.SimpleNamespaceContext;

import com.servicelibre.jxsl.scenario.RunReport;
import com.servicelibre.jxsl.scenario.test.FailureReport;
import com.servicelibre.jxsl.scenario.test.TestReport;
import com.servicelibre.jxsl.scenario.test.XslTestSuiteRunner;

/**
 * * All XslScenario constructed from files/directories are using the same
 * XslScenario : only xslPath will change
 * 
 */
public class XspecTestSuiteRunner implements XslTestSuiteRunner

{
    private static final Logger logger = LoggerFactory.getLogger(XspecTestSuiteRunner.class);

    private List<File> testFiles = new ArrayList<File>();
    private List<File> testDirectoryFiles = new ArrayList<File>();

    private IOFileFilter fileFilter = TrueFileFilter.TRUE;
    private IOFileFilter directoryFilter = TrueFileFilter.TRUE;

    XspecTestScenarioRunner xspecRunner;

    private XPathExpression successXpath;

    private XPathExpression testFailedCount;

    private XPathExpression testCount;

    public XspecTestSuiteRunner(File xspecTestsGeneratorFile)
    {
        this(new XspecTestScenarioRunner(xspecTestsGeneratorFile));
    }

    public XspecTestSuiteRunner(XspecTestScenarioRunner xspecTestRunner)
    {
        this.xspecRunner = xspecTestRunner;
        init();
    }

    public void init()
    {

        setFileFilter(FileFilterUtils.suffixFileFilter("xspec"));
        XPath xpath = XPathFactory.newInstance().newXPath();

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.bindNamespaceUri("x", "http://www.jenitennison.com/xslt/xspec");
        xpath.setNamespaceContext(namespaceContext);

        try
        {

            successXpath = xpath.compile("count(//x:test[@successful ='false'] ) = 0");
            testFailedCount = xpath.compile("count(//x:test[@successful ='false'] )");
            testCount = xpath.compile("count(//x:test)");
        }
        catch (XPathExpressionException e)
        {
            logger.error("Error while initializing {}.", this.getClass().getName(), e);
        }

    }

    public TestReport run(File xspecTestFile)
    {
        TestReport testReport = new TestReport();

        RunReport runReport = xspecRunner.run(xspecTestFile);

        if (runReport != null)

        {
            testReport.executionTime = runReport.executionTime;
            testReport.executionDate = runReport.executionDate;

            testReport.success = getSuccess(runReport.mainOutputFile);

            if (!testReport.success)
            {
                testReport.failureReport = getFailureReport(runReport.mainOutputFile);
            }

            try
            {
                testReport.reportUrl = runReport.otherOutputFiles.get(0).toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                logger.error("Error while converting test report File to URL.", e);
            }

        }
        else
        {
            testReport.success = false;

        }

        return testReport;
    }


    private FailureReport getFailureReport(File mainOutputFile)
    {
        FailureReport failureReport = new FailureReport("Test failed to run.  See error log.");

        // TODO implement  
        // failureReport.testFailedCount;

        // count(//x:test)
        // failureReport.testCount;

        return failureReport;
    }

    private boolean getSuccess(File mainOutputFile)
    {
        String success = "false";

        try
        {
            success = successXpath.evaluate(new StreamSource(mainOutputFile));
        }
        catch (XPathExpressionException e)
        {
            logger.error("Error while retrieving success/failure in test report {}", mainOutputFile, e);
        }

        return Boolean.parseBoolean(success);
    }

    public List<TestReport> runAll()
    {
        List<TestReport> testReports = new ArrayList<TestReport>();

        for (File xspecFile : getTestFiles())
        {
            testReports.add(run(xspecFile));
        }

        return testReports;
    }

    public void setFiles(List<File> files)
    {
        testFiles.clear();
        testFiles.addAll(files);
    }

    /**
     * Recursively load all *.xspec files under each given directories.
     * 
     * @param directories
     */
    public void setDirectories(List<File> directories)
    {
        testDirectoryFiles.clear();

        for (File dir : directories)
        {
            testDirectoryFiles.addAll(FileUtils.listFiles(dir, fileFilter, directoryFilter));
        }
    }

    public List<File> getTestFiles()
    {

        List<File> allFiles = new ArrayList<File>(testFiles.size() + testDirectoryFiles.size());
        allFiles.addAll(testFiles);
        allFiles.addAll(testDirectoryFiles);
        return allFiles;
    }

    public IOFileFilter getFileFilter()
    {
        return fileFilter;
    }

    public void setFileFilter(IOFileFilter fileFilter)
    {
        this.fileFilter = fileFilter;
    }

    public IOFileFilter getDirectoryFilter()
    {
        return directoryFilter;
    }

    public void setDirectoryFilter(IOFileFilter directoryFilter)
    {
        this.directoryFilter = directoryFilter;
    }

}
