package com.servicelibre.jxsl.xsltestengine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FolderDocumentSource  implements DocumentSource {

	private File rootDir;
	private boolean recursive;
	private String[] extensions;
	
	
	public FolderDocumentSource(File rootFolder) {
		this(rootFolder, null, false);
	}
	

	public FolderDocumentSource(File rootFolder, String[] extensions, boolean recursive) {
		this.rootDir = rootFolder;
		this.extensions = extensions;
		this.recursive = recursive;
	}


	@Override
	public List<DocumentId> getDocumentIds() {
		
		List<DocumentId> documentIds = new ArrayList<DocumentId>(); 
		
		Collection<File> files = FileUtils.listFiles(rootDir, extensions, recursive);
		
		Iterator<File> fileIt = files.iterator();
		
		while(fileIt.hasNext()) {
			
			File file = fileIt.next();
			documentIds.add(new DocumentId(file.getAbsolutePath()));
		}
		
		return documentIds;
	}

	@Override
	public Document getDocument(DocumentId docId) {
		return new Document(docId, new File(docId.getId()));
	}

	public File getrootDir() {
		return rootDir;
	}

	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}


	public String[] getExtensions() {
		return extensions;
	}


	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}
	
	

}
