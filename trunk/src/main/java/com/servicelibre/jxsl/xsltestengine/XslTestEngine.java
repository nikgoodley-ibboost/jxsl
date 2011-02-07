package com.servicelibre.jxsl.xsltestengine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.scenario.XslScenario;


// DataSetXslSuiteRunner ??? run(documentId), runAll()
// TODO how to embed in Junit test??
public class XslTestEngine {

	static Logger logger = LoggerFactory.getLogger(XslScenario.class);

	private DocumentSource docSource;

	private  List<XslOutputValidation> xslOutputValidations;
	
	public XslTestEngine() {
		super();
	}

	public XslTestEngine(DocumentSource docSource, XslOutputValidation xslOutputValidation) {
		super();
		this.docSource = docSource;
		xslOutputValidations = new ArrayList<XslOutputValidation>(1);
		xslOutputValidations.add(xslOutputValidation);
	}
	
	public XslTestEngine(DocumentSource docSource, List<XslOutputValidation> xslOutputValidations) {
		super();
		this.docSource = docSource;
		this.xslOutputValidations= xslOutputValidations;
	}	

	public DocumentSource getDocSource() {
		return docSource;
	}

	public void setDocSource(DocumentSource docSource) {
		this.docSource = docSource;
	}

	public int run() {
		int processedFilesCount = 0;

		// Get all files Ids to process
		if (docSource != null) {

			if (xslOutputValidations != null) {

				for (DocumentId documentId : docSource.getDocumentIds()) {
				    
					for(XslOutputValidation outputValidation :xslOutputValidations)

					{
						processedFilesCount += outputValidation.run(docSource.getDocument(documentId));
					}

				}
			} else {
				logger.warn("There is no outputValidator available for this XslOutputValidation.");
			}

		} else {
			logger.error("DocumentSource is NULL!");
		}

		return processedFilesCount;
	}

	public List<XslOutputValidation> getXslOutputValidations() {
		return xslOutputValidations;
	}

	public void setXslOutputValidations(List<XslOutputValidation> xslOutputValidations) {
		this.xslOutputValidations = xslOutputValidations;
	}

}
