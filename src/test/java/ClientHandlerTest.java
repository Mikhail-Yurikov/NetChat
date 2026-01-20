import netchat.ClientHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ClientHandlerTest {
    @Mock
    private Socket mockedSocket;

    @Mock
    private BufferedReader mockedBufferedReader;

    @Mock
    private PrintWriter mockedPrintWriter;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        closeable.close();
    }

    @Test
    void testBroadcastMessage() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Socket mockedSocket = mock(Socket.class);
        when(mockedSocket.getOutputStream()).thenReturn(outputStream);

        List<Socket> clients = new ArrayList<>();
        clients.add(mockedSocket);

        ClientHandler handler = new ClientHandler(mockedSocket, clients);

        Method method = handler.getClass().getDeclaredMethod("broadcastMessage", String.class);
        method.setAccessible(true);

        String testMessage = "Привет всем!";
        method.invoke(handler, testMessage);

        byte[] data = outputStream.toByteArray();
        String result = new String(data, StandardCharsets.UTF_8);
        assertTrue(result.contains(testMessage), "Сообщение должно быть отправлено");
    }

    @Test
    void testLogOfServer() throws Exception {

        List<Socket> clients = new ArrayList<>();
        clients.add(mockedSocket);
        ClientHandler handler = new ClientHandler(mockedSocket, clients);
        when(mockedBufferedReader.readLine()).thenReturn("Иван");
        handler.logOfServer("Иван присоединился к чату.");
        String fileContent = readFile("server.log");
        assertTrue(fileContent.contains("Иван присоединился к чату."),
                "Запись должна присутствовать в файле журнала!");
    }

    private String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    void testExitCommandHandling() throws Exception {
        List<Socket> clients = new ArrayList<>();
        clients.add(mockedSocket);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(mockedSocket.getOutputStream()).thenReturn(outputStream);
        when(mockedSocket.getInputStream()).thenReturn(new ByteArrayInputStream("/exit".getBytes()));
        ClientHandler handler = new ClientHandler(mockedSocket, clients);
        handler.run();
        assertEquals(0, clients.size(), "Клиент должен быть удалён из списка после /exit");
    }
}
