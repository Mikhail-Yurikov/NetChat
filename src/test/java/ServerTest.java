import netchat.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.Socket;

public class ServerTest {
    @Mock
    Socket socket;

    @Test
    void readFromSettingsTest() {
        int port = Server.readFromSettings();
        Assertions.assertEquals(8080, port);
    }
}
