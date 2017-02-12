package webhook.teamcity.payload.format.convertor;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import webhook.teamcity.payload.convertor.ExtraParametersMapToJsonConvertor;
import webhook.teamcity.payload.convertor.ExtraParametersMapToXmlConvertor;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class ConvertorTest {
    HierarchicalStreamReader hsr = mock(HierarchicalStreamReader.class);
    UnmarshallingContext umc = mock(UnmarshallingContext.class);


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void TestJsonUnmarshall() {
        ExtraParametersMapToJsonConvertor jsonconv = new ExtraParametersMapToJsonConvertor();
        assertNull(jsonconv.unmarshal(hsr, umc));
    }

    @Test
    public void TestXmlUnmarshall() {
        ExtraParametersMapToXmlConvertor xmlconv = new ExtraParametersMapToXmlConvertor();
        assertNull(xmlconv.unmarshal(hsr, umc));
    }
}
