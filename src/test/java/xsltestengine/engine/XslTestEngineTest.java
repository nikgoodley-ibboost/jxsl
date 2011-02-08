package xsltestengine.engine;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.servicelibre.jxsl.dstest.DocumentId;
import com.servicelibre.jxsl.dstest.XslDataSetJUnitTest;
import com.servicelibre.jxsl.dstest.XslDataSetRunner;
import com.servicelibre.jxsl.dstest.sources.DocumentSource;
import com.servicelibre.jxsl.dstest.sources.FolderDocumentSource;
import com.servicelibre.jxsl.dstest.validations.AlwaysFalseValidator;
import com.servicelibre.jxsl.dstest.validations.AlwaysTrueValidator;
import com.servicelibre.jxsl.dstest.validations.JavaXslValidation;
import com.servicelibre.jxsl.dstest.validations.OutputValidator;
import com.servicelibre.jxsl.dstest.validations.XslValidation;
import com.servicelibre.jxsl.scenario.XslScenario;
import com.servicelibre.jxsl.scenario.test.xspec.XspecTestScenarioRunner;

public class XslTestEngineTest {

    private static File rootFolder;
    private static File xspecXslGeneratorFile;
    private static File xspecFile;

    @BeforeClass
    public static void init() {
	
	URL rootFolderUrl = ClassLoader.getSystemResource("xsltestengine-data");
	URL xspecXslUrl = ClassLoader.getSystemResource("xspec/generate-xspec-tests.xsl");
	URL xspecFileUrl = ClassLoader.getSystemResource("xspec/tutorial/encoding.xspec");

	try {
	    rootFolder = new File(rootFolderUrl.toURI());
	    xspecXslGeneratorFile = new File(xspecXslUrl.toURI());
	    xspecFile = new File(xspecFileUrl.toURI());
	
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	}
	
	

    }

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

    /**
     * Test the engine with on single OutputValidation (Java)
     */
    @Test
    public void basicXslTestEngine() {

	XslDataSetRunner engine = new XslDataSetRunner(getDocumentSource(), getJavaXslOutputValidation());

	XslDataSetJUnitTest.assertEngineRun(engine.runAll());

    }

    /**
     * Test the engine with several OutputValidation (Java + Xspec)
     */
    @Test
    public void completeXslTestEngine() {
	List<XslValidation> outputValidations = new ArrayList<XslValidation>();

	outputValidations.add(getJavaXslOutputValidation());
	outputValidations.add(getXspecValidation());

	XslDataSetRunner engine = new XslDataSetRunner(getDocumentSource(), outputValidations);

	XslDataSetJUnitTest.assertEngineRun(engine.runAll());

    }

    public DocumentSource getDocumentSource() {
	// Create a new DocumentSource
	return new FolderDocumentSource(rootFolder, new String[] { "xml" }, true);

    }

    private XslValidation getXspecValidation() {

	XspecTestScenarioRunner xspecRunner = new XspecTestScenarioRunner(xspecXslGeneratorFile);

	XslValidation outputValidation = new XspecValidation(xspecRunner, xspecFile);

	return outputValidation;
    }

    public XslValidation getJavaXslOutputValidation() {

	URL xslUrl = ClassLoader.getSystemResource("xsltestengine-data/toHtmlWithIds.xsl");

	XslScenario scenario = new XslScenario(xslUrl);

	List<OutputValidator> outputValidators = new ArrayList<OutputValidator>();
	outputValidators.add(new AlwaysTrueValidator());
	outputValidators.add(new AlwaysFalseValidator());

	XslValidation outputValidation = new JavaXslValidation(scenario, outputValidators);

	return outputValidation;
    }

}
