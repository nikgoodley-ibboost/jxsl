package com.servicelibre.jxsl.xsltestengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.scenario.XslScenario;

public class XslTestEngine {

	static Logger logger = LoggerFactory.getLogger(XslScenario.class);

	private DocumentSource docSource;

	private XslTestSuite xslTestSuite;

	public XslTestEngine(DocumentSource docSource, XslTestSuite xslTestSuite) {
		super();
		this.docSource = docSource;
		this.xslTestSuite = xslTestSuite;
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

			if (xslTestSuite != null) {

				for (DocumentId documentId : docSource.getDocumentIds()) {

					processedFilesCount += xslTestSuite.run(docSource.getDocument(documentId));

				}
			} else {
				logger.warn("There is no outputValidator available for this XslTestSuite.");
			}

		} else {
			logger.error("DocumentSource is NULL!");
		}

		return processedFilesCount;
	}

	public XslTestSuite getXslTestSuite() {
		return xslTestSuite;
	}

	public void setXslTestSuite(XslTestSuite xslTestSuite) {
		this.xslTestSuite = xslTestSuite;
	}

}
