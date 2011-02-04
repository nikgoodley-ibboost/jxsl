package com.servicelibre.jxsl.xsltestengine;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.servicelibre.jxsl.scenario.XslScenario;

public class XslTestEngineTest {

	private File rootFolder = new File("/tmp");

	@Test
	public void basicDocumentSourceTest() {

		DocumentSource docSource = getDocumentSource();
		List<DocumentId> documentIds = docSource.getDocumentIds();

		assertNotNull("documentIds cannot be null", documentIds);
		assertTrue("There must be at least one file in " + rootFolder, documentIds.size() > 0);

		for (DocumentId id : documentIds) {
			System.out.println(id);
		}

	}

	@Test
	public void basicXslTestEngine() {

		XslTestEngine engine = new XslTestEngine(getDocumentSource(), getXslTestSuite());
		
		// TODO return a list of runReport or something similar?
		int run = engine.run();
		
		assertNotNull(run);
		assertTrue(run >= 0);
		
	}

	public DocumentSource getDocumentSource() {
		// Create a new DocumentSource
		return new FolderDocumentSource(rootFolder, new String[] { "txt" }, true);

	}
	
	public XslTestSuite getXslTestSuite(){
		
		//TODO
		XslScenario scenario = new XslScenario("");
		
		List<OutputValidator> outputValidators = new ArrayList<OutputValidator>();
		outputValidators.add(new AlwaysTrueValidator());
		outputValidators.add(new AlwaysFalseValidator());
		
		XslTestSuite suite = new XslJavaTestSuite(scenario, outputValidators); 
		
		
		return suite;
	}

}
