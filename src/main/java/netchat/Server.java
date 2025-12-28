package netchat;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Server implements Runnable {
    private static final int PORT = readFromSettings();
    private static final String configPath = "settings.txt";
    private static List<Socket> clients = new ArrayList<>();


    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен!");
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    clients.add(clientSocket);
                    Thread t = new Thread(new ClientHandler(clientSocket, clients));
                    t.start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int readFromSettings() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
            String portStr = props.getProperty("PORT");
            return Integer.parseInt(portStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
