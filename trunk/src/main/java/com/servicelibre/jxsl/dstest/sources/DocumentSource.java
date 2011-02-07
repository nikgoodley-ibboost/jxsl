package com.servicelibre.jxsl.dstest.sources;

import java.util.List;

import com.servicelibre.jxsl.dstest.Document;
import com.servicelibre.jxsl.dstest.DocumentId;

public interface DocumentSource {
	
	public List<DocumentId>getDocumentIds();
	public Document getDocument(DocumentId docInfo);

}
