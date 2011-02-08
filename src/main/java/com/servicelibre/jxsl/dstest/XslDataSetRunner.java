package com.servicelibre.jxsl.dstest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.dstest.sources.DocumentSource;
import com.servicelibre.jxsl.dstest.validations.ValidationReport;
import com.servicelibre.jxsl.dstest.validations.XslValidation;
import com.servicelibre.jxsl.scenario.XslScenario;

public class XslDataSetRunner
{

    static Logger logger = LoggerFactory.getLogger(XslScenario.class);

    private DocumentSource docSource;

    private List<XslValidation> xslOutputValidations;

    public XslDataSetRunner()
    {
        super();
    }

    public XslDataSetRunner(DocumentSource docSource, XslValidation xslOutputValidation)
    {
        super();
        this.docSource = docSource;
        xslOutputValidations = new ArrayList<XslValidation>(1);
        xslOutputValidations.add(xslOutputValidation);
    }

    public XslDataSetRunner(DocumentSource docSource, List<XslValidation> xslOutputValidations)
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

                for (XslValidation outputValidation : xslOutputValidations)

                {
                	// TODO Save document if requested (saveDocuments)
                	
                	validationReports.add(outputValidation.run(docSource.getDocument(documentId)));
                	
                	// TODO append reports to csv output report (saveOutputs - All/ErrorOnly)
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

    public List<XslValidation> getXslOutputValidations()
    {
        return xslOutputValidations;
    }

    public void setXslOutputValidations(List<XslValidation> xslOutputValidations)
    {
        this.xslOutputValidations = xslOutputValidations;
    }

}
