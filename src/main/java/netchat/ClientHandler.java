package netchat;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private final List<Socket> clients;
    private String userName;
    private final PrintWriter printWriter;




    private BufferedReader input;

    public ClientHandler(Socket clientSocket, List<Socket> clients) throws FileNotFoundException {
        this.clientSocket = clientSocket;
        this.clients = clients;
        printWriter = new PrintWriter(new FileOutputStream("server.log", true));
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


            userName = input.readLine();
            if (userName == null || userName.isEmpty()) {
                System.err.println("Имя пользователя пустое!");
                return;
            }


            broadcastMessage(userName + " присоединился к чату.");
            logOfServer(userName + " присоединился к чату.");
            String message;
            while (true) {
                message = input.readLine();


                if ("/exit".equalsIgnoreCase(message) || message == null) {
                    synchronized (clients) {
                        broadcastMessage(userName + " покинул чат.");
                        logOfServer(userName + " покинул чат.");
                        clients.remove(clientSocket);
                        break;
                    }
                } else {
                    logOfServer(userName + ": " + message);
                    broadcastMessage(userName + ": " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Не удалось закрыть соединение с клиентом: " + e.getMessage());
            } finally {
                try {
                    input.close();
                    printWriter.close();
                    clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public void broadcastMessage(String message) throws IOException {
        synchronized (clients) {
            for (Socket client : clients) {
                if (!client.isClosed()) {
                    PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                    writer.println(message);
                }
            }
        }
    }

    public synchronized void logOfServer(String message) throws FileNotFoundException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedNow = now.format(formatter);
        printWriter.println(formattedNow + " " + message);
        printWriter.flush();

    }
}
