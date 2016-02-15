

package fr.pagesjaunes.mdm.core.core;

import org.junit.Ignore;
import org.junit.Test;

import fr.pagesjaunes.mdm.core.BootstrapService;
import fr.pagesjaunes.mdm.core.Device;

import static org.junit.Assert.assertThat;

/**
 * Unit tests of client API
 */
public class BootstrapApiClientUtilTest {

    @Test
    @Ignore("Requires the API to use basic authentication. Parse.com api does not. See BootstrapService for more info.")
    public void shouldCreateClient() throws Exception {
        List<Device> users = new BootstrapService("demo@androidbootstrap.com", "foobar").getDevices();

        assertThat("no device", users.get(0).getModel(), notNullValue());
    }
}
