package com.servicelibre.jxsl.dstest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import xsltestengine.engine.XslTestEngineTest;

@RunWith(Parameterized.class)
public class XslDataSetJUnitTest
{
    private static final String XSL_DATASET_RUNNER_BEAN_ID = "xslDataSetRunner";

    public final static String SPRING_CONTEXT_FILENAME = "xsldataset-context.xml";

    private static XslDataSetRunner runner;

    static
    {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(SPRING_CONTEXT_FILENAME);
        runner = (XslDataSetRunner) ctx.getBean(XSL_DATASET_RUNNER_BEAN_ID);
    }

    protected DocumentId documentId;

    public XslDataSetJUnitTest(DocumentId documentId)
    {
        super();
        this.documentId = documentId;
    }

    @Parameters
    public static Collection<Object[]> getDocuments()
    {

        List<Object[]> documents = new ArrayList<Object[]>(4);

        for (DocumentId docId : runner.getDocSource().getDocumentIds())
        {
            documents.add(new Object[] { docId });
        }

        return documents;
    }

    @Test
    public void dataSetValidationTest()
    {

		XslTestEngineTest.assertEngineRun(runner.run(documentId));

    }

}
