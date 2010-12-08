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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.servicelibre.jxsl.scenario.test.TestReport;

/**
 * JUnit test scenario for xspec files.
 * 
 * XspecScenarioJUnitTest is configured via a Spring application context file
 * that MUST be named <strong>xspec&#8209;context.xml</strong> and MUST be
 * located at the classpath root.
 * 
 * To enable XSL unit testing with Xspec in your own project (build with Ant,
 * Maven, etc.), simply create a new class in your test package that extends
 * XspecScenarioJUnitTest. Here is a complete and functional implementation.
 * 
 * <pre>
 *package com.mycompany.test.xspec;
 *
 *import com.servicelibre.jxsl.scenario.test.xspec.XspecScenarioJUnitTest;
 *import java.io.File;
 *
 *public class XspecUnitTesting extends XspecScenarioJUnitTest
 *{
 *   
 *  public XspecUnitTesting(File xspecFile)
 *  {
 *     super(xspecFile);
 *  }
 *   
 *}
 * 
 * </pre>
 * 
 * @author benoitm
 * 
 */
@RunWith(Parameterized.class)
public class XspecScenarioJUnitTest
{

    File xspecFile;

    private static XspecTestSuiteRunner xspecSuiteRunner;

    public final static String ctxFileName = "classpath:/xspec-context.xml";

    private static TestReport testReport;

    static
    {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(ctxFileName);
        xspecSuiteRunner = (XspecTestSuiteRunner) ctx.getBean("xspecTestSuiteRunner");

    }

    public XspecScenarioJUnitTest(File xspecFile)
    {
        this.xspecFile = xspecFile;
    }

    @Test
    public void executeXspecTest()
    {
        testReport = xspecSuiteRunner.run(xspecFile);

        assertNotNull(testReport);

    }

    @Test
    @Ignore  //TODO reactivate!
    public void assertXspecResults()
    {

        assertNotNull(testReport);

        assertTrue(testReport.success);

        assertTrue("X tests sur Y ont échoués.  Rapport détaillé disponible ici:", testReport.success);

        // TODO show testReport (toString) and assert results
        // TEST

    }

    @Parameters
    public static Collection<Object[]> getXspecFiles()
    {

        List<Object[]> xspecFiles = new ArrayList<Object[]>(4);

        for (File file : xspecSuiteRunner.getTestFiles())
        {
            xspecFiles.add(new Object[] { file });
        }

        return xspecFiles;
    }

    public static XspecTestSuiteRunner getXspecSuiteRunner()
    {
        return XspecScenarioJUnitTest.xspecSuiteRunner;
    }

    public static void setXspecSuiteRunner(XspecTestSuiteRunner xspecSuiteRunner)
    {
        XspecScenarioJUnitTest.xspecSuiteRunner = xspecSuiteRunner;
    }

}
