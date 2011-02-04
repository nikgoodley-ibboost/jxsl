package xsltestengine.source;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.servicelibre.jxsl.xsltestengine.FolderDocumentSource;

/**
 * Unit tests for the FolderDocumentSource class that verify
 * FolderDocumentSource behavior works in isolation.
 */
public class FolderDocumentSourceTest
{
    // The object to test
    private FolderDocumentSource source = new FolderDocumentSource(rootFolder, new String[] { "xml" }, true);

    private static File rootFolder;

    @BeforeClass
    public static void init()
    {
        URL rootFolderUrl = ClassLoader.getSystemResource("xsltestengine");

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
    public void sourceRootDirIsValid()
    {
        File dir = source.getrootDir();
        assertNotNull(dir);
        assertTrue(dir.exists());

    }

}
