package com.servicelibre.jxsl.dstest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.dstest.sources.DocumentSource;
import com.servicelibre.jxsl.dstest.validations.ValidationReport;
import com.servicelibre.jxsl.dstest.validations.XslOutputValidation;
import com.servicelibre.jxsl.scenario.XslScenario;

// TODO how to embed in Junit test??
public class XslDataSetRunner
{

    static Logger logger = LoggerFactory.getLogger(XslScenario.class);

    private DocumentSource docSource;

    private List<XslOutputValidation> xslOutputValidations;

    public XslDataSetRunner()
    {
        super();
    }

    public XslDataSetRunner(DocumentSource docSource, XslOutputValidation xslOutputValidation)
    {
        super();
        this.docSource = docSource;
        xslOutputValidations = new ArrayList<XslOutputValidation>(1);
        xslOutputValidations.add(xslOutputValidation);
    }

    public XslDataSetRunner(DocumentSource docSource, List<XslOutputValidation> xslOutputValidations)
    {
        super();
        this.docSource = docSource;
        this.xslOutputValidations = xslOutputValidations;
    }

    public DocumentSource getDocSource()
    {
        return docSource;
    }

    public void setDocSource(DocumentSource docSource)
    {
        this.docSource = docSource;
    }

    public List<ValidationReport> run(DocumentId documentId)
    {

    	List<ValidationReport> validationReports = new ArrayList<ValidationReport>();

        if (docSource != null)
        {

            if (xslOutputValidations != null)
            {

                for (XslOutputValidation outputValidation : xslOutputValidations)

                {
                	validationReports.add(outputValidation.run(docSource.getDocument(documentId)));
                }
            }
            else
            {
                logger.warn("There is no outputValidator available for this XslOutputValidation.");
            }

        }
        else
        {
            logger.error("DocumentSource is NULL!");
        }
        
        return validationReports;
        
    }

    public List<ValidationReport> runAll()
    {
    	List<ValidationReport> validationReports = new ArrayList<ValidationReport>();

        // Get all files Ids to process
        if (docSource != null)
        {

            if (xslOutputValidations != null)
            {

                for (DocumentId documentId : docSource.getDocumentIds())
                {

                	validationReports.addAll(run(documentId));

                }
            }
            else
            {
                logger.warn("There is no outputValidator available for this XslOutputValidation.");
            }

        }
        else
        {
            logger.error("DocumentSource is NULL!");
        }

        return validationReports;
    }

    public List<XslOutputValidation> getXslOutputValidations()
    {
        return xslOutputValidations;
    }

    public void setXslOutputValidations(List<XslOutputValidation> xslOutputValidations)
    {
        this.xslOutputValidations = xslOutputValidations;
    }

}
