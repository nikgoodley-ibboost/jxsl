package com.servicelibre.jxsl.xsltestengine;

import java.util.List;

public interface DocumentSource {
	
	public List<DocumentId>getDocumentIds();
	public Document getDocument(DocumentId docInfo);

}
