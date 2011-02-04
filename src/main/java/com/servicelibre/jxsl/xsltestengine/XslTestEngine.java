package com.servicelibre.jxsl.xsltestengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.scenario.XslScenario;

// TODO how to embed in Junit test??
public class XslTestEngine {

	static Logger logger = LoggerFactory.getLogger(XslScenario.class);

	private DocumentSource docSource;

	//TODO move to a List<XslOutputValidation> xslOutputValidations
	private XslOutputValidation xslOutputValidation;

	public XslTestEngine(DocumentSource docSource, XslOutputValidation xslOutputValidation) {
		super();
		this.docSource = docSource;
		this.xslOutputValidation = xslOutputValidation;
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

			if (xslOutputValidation != null) {

				for (DocumentId documentId : docSource.getDocumentIds()) {
				    
				    //TODO loop on all xslOutputValidation

					processedFilesCount += xslOutputValidation.run(docSource.getDocument(documentId));

				}
			} else {
				logger.warn("There is no outputValidator available for this XslOutputValidation.");
			}

		} else {
			logger.error("DocumentSource is NULL!");
		}

		return processedFilesCount;
	}

	public XslOutputValidation getXslOutputValidation() {
		return xslOutputValidation;
	}

	public void setXslOutputValidation(XslOutputValidation xslOutputValidation) {
		this.xslOutputValidation = xslOutputValidation;
	}

}
