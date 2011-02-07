package com.servicelibre.jxsl.dstest;

import java.io.File;

public class Document {

	public DocumentId id;
	private File file;

	public Document(DocumentId id){
		this(id, null);
	}

	public Document(DocumentId id, File file) {
		super();
		this.id = id;
		this.file = file;
	}

	public File getFile() {
		return file;
	}

}
