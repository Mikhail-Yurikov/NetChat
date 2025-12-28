package netchat;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Client {
    private static final String configPath = "settings.txt";
    private final String serverAddress = getServerAddress();
    private final int serverPort = getPort();
    public PrintWriter printWriter;


    public Client() throws FileNotFoundException {
        this.printWriter = new PrintWriter(new FileOutputStream("client.log", true));
    }


    public void start() {
        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Подключение к серверу успешно!");

            System.out.print("Введите ваше имя: ");
            String username = stdIn.readLine().trim();
            if (username == null || username.isEmpty()) {
                System.err.println("Имя не может быть пустым!");
                return;
            }
            out.println(username);

            Thread inputThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException ignored) {
                }
            });
            inputThread.setDaemon(true);
            inputThread.start();

            String userInput;
            boolean running = true;
            while (running) {
                userInput = stdIn.readLine();
                if ("/exit".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("Выход из чата...");
                    logUniqueMessage(username + " вышел из чата!");
                    running = false;
                } else if (!userInput.isEmpty()) {
                    out.println(userInput);
                    logUniqueMessage(username + " " + userInput);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    public synchronized void logUniqueMessage(String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String fullMessage = formatter.format(now) + " " + message;
        printWriter.println(fullMessage);
        printWriter.flush();
    }

    public int getPort() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
            String portStr = props.getProperty("PORT");
            return Integer.parseInt(portStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getServerAddress() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
            return props.getProperty("SERVER_ADDRESS");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
