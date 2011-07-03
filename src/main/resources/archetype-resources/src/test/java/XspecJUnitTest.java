#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import java.io.File;
import com.servicelibre.jxsl.scenario.test.xspec.XspecScenarioJUnitTest;

public class XspecJUnitTest extends XspecScenarioJUnitTest
{

    public XspecJUnitTest(File xspecFile)
    {
        super(xspecFile);
    }

}