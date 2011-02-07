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
import com.servicelibre.jxsl.dstest.XslDataSetRunner;
import com.servicelibre.jxsl.dstest.sources.DocumentSource;
import com.servicelibre.jxsl.dstest.sources.FolderDocumentSource;
import com.servicelibre.jxsl.dstest.validations.AlwaysFalseValidator;
import com.servicelibre.jxsl.dstest.validations.AlwaysTrueValidator;
import com.servicelibre.jxsl.dstest.validations.JavaXslOutputValidation;
import com.servicelibre.jxsl.dstest.validations.OutputValidator;
import com.servicelibre.jxsl.dstest.validations.XslOutputValidation;
import com.servicelibre.jxsl.scenario.XslScenario;

public class XslTestEngineTest
{

    private static File rootFolder;

    @BeforeClass
    public static void init()
    {
        URL rootFolderUrl = ClassLoader.getSystemResource("xsltestengine-data");

        try
        {
            rootFolder = new File(rootFolderUrl.toURI());
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void basicDocumentSourceTest()
    {

        DocumentSource docSource = getDocumentSource();
        List<DocumentId> documentIds = docSource.getDocumentIds();

        assertNotNull("documentIds cannot be null", documentIds);
        assertTrue("There must be at least one file in " + rootFolder, documentIds.size() > 0);

        for (DocumentId id : documentIds)
        {
            System.out.println(id);
        }

    }

    @Test
    public void basicXslTestEngine()
    {

        XslDataSetRunner engine = new XslDataSetRunner(getDocumentSource(), getXslOutputValidation());

        // TODO return a list of runReport or something similar?
        int run = engine.runAll();

        assertNotNull(run);
        
        System.err.println(run);

    }

    public DocumentSource getDocumentSource()
    {
        // Create a new DocumentSource
        return new FolderDocumentSource(rootFolder, new String[] { "xml" }, true);

    }

    public XslOutputValidation getXslOutputValidation()
    {

        URL xslUrl = ClassLoader.getSystemResource("xsltestengine-data/toHtmlWithIds.xsl");
        XslScenario scenario = new XslScenario(xslUrl);

        List<OutputValidator> outputValidators = new ArrayList<OutputValidator>();
        outputValidators.add(new AlwaysTrueValidator());
        outputValidators.add(new AlwaysFalseValidator());

        XslOutputValidation outputValidation = new JavaXslOutputValidation(scenario, outputValidators);

        return outputValidation;
    }

}
