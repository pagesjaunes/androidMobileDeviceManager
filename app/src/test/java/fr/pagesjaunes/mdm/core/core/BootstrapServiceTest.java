

package fr.pagesjaunes.mdm.core.core;

import org.apache.http.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import fr.pagesjaunes.mdm.core.BootstrapService;
import fr.pagesjaunes.mdm.core.Device;
import fr.pagesjaunes.mdm.core.UserAgentProvider;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests of {@link fr.pagesjaunes.mdm.core.BootstrapService}
 */
@RunWith(MockitoJUnitRunner.class)
public class BootstrapServiceTest {

    /**
     * Create reader for string
     *
     * @param value
     * @return input stream reader
     * @throws IOException
     */
    private static BufferedReader createReader(String value) throws IOException
    {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                value.getBytes(HttpRequest.CHARSET_UTF8))));
    }

    @Mock
    private HttpRequest request;

    private BootstrapService service;

    /**
     * Set up default mocks
     *
     * @throws IOException
     */
    @Before
    public void before() throws IOException {
        service = new BootstrapService("foo", new UserAgentProvider()) {
            protected HttpRequest execute(HttpRequest request) throws IOException {
                return BootstrapServiceTest.this.request;
            }
        };
        doReturn(true).when(request).ok();
    }



    /**
     * Verify getting checkins with an empty response
     *
     * @throws IOException
     */
    @Test
    public void getDeviceEmptyResponse() throws IOException {
        doReturn(createReader("")).when(request).bufferedReader();
        List<Device> referrers = service.getDevices();
        assertNotNull(referrers);
        assertTrue(referrers.isEmpty());
    }
}
