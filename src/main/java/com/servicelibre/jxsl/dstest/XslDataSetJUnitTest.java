package com.servicelibre.jxsl.dstest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

        int run = runner.run(documentId);
        System.err.println(run);
        assertTrue((run*-1) + " validations failed on this document (" + documentId + ")", run > 0);

    }

}
