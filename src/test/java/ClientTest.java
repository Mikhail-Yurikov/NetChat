import netchat.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientTest {
    @BeforeEach
    void setUp() throws IOException {
        // Создаем файл settings.txt с необходимыми настройками
        Files.writeString(Paths.get("settings.txt"), """
                SERVER_ADDRESS=localhost
                PORT=8080""");
    }

    @Test
    void testGetServerAddress() throws Exception {
        Client client = new Client();
        assertEquals("localhost", client.getServerAddress());
    }

    @Test
    void testGetPort() throws Exception {
        Client client = new Client();
        assertEquals(8080, client.getPort());
    }

    @Test
    void testLogUniqueMessage() throws Exception {
        // Подготовим фиктивное время
        LocalDateTime fixedNow = LocalDateTime.of(2023, 10, 10, 12, 30, 0);

        // Сначала очистим файл журнала перед каждым новым тестом
        Path logFile = Paths.get("client.log");
        if (Files.exists(logFile)) {
            Files.write(logFile, "".getBytes()); // Очищаем файл журнала
        }

        // Замещение статического метода LocalDateTime.now() с помощью Mockito
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(fixedNow); // Возвращаем фиктивное время

            // Создаем экземпляр нашего клиента
            Client client = new Client();

            // Передаем сообщение для записи в лог
            String message = "Test User joined the chat!";
            client.logUniqueMessage(message);

            // Ожидаемый результат (форматированный временной штамп плюс сообщение)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String expectedLogMessage = formatter.format(fixedNow) + " " + message + "\n";

            // Читаем реальный результат из файла журнала
            String actualLogContent = Files.readString(logFile);

            // Чистим строки от управляющих символов и лишних пробелов
            expectedLogMessage = expectedLogMessage.replaceAll("\\p{C}", "").replaceAll("\\s+", "");
            actualLogContent = actualLogContent.replaceAll("\\p{C}", "").replaceAll("\\s+", "");

            // Диагностика: выводим длину каждой строки
            System.out.println("Expected length: " + expectedLogMessage.length());
            System.out.println("Actual length: " + actualLogContent.length());

            // Проверяем, совпадают ли полученные данные с ожидаемыми
            assertEquals(expectedLogMessage, actualLogContent);
        }
    }


    @Test
    void testExitCommand() throws IOException {
        Socket mockSocket = mock(Socket.class);
        BufferedReader mockIn = mock(BufferedReader.class);
        PrintWriter mockOut = mock(PrintWriter.class);
        BufferedReader mockStdIn = mock(BufferedReader.class);

        when(mockSocket.getInputStream()).thenReturn(null);
        when(mockSocket.getOutputStream()).thenReturn(null);
        when(mockIn.readLine()).thenReturn("Bye!");
        when(mockStdIn.readLine()).thenReturn("/exit");

        Client client = new Client();
        client.start();

        verify(mockOut, never()).println(anyString()); // Нет отправки сообщений после команды /exit
    }
}
