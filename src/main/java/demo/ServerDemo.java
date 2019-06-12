package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by administer on 24/5/19.
 */
public class ServerDemo {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2000);

        System.out.println("Server is started!");
        System.out.println("Server message: " + serverSocket.getInetAddress() + " port: " + serverSocket.getLocalPort());

        for (;;) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            new Thread(clientHandler).start();
        }
    }

    static class ClientHandler implements Runnable{
        Socket clientSocket;
        boolean flag = true;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            System.out.println("New client connection: " + clientSocket.getInetAddress() + " port: " + clientSocket.getPort());

            try {
                PrintStream serverOutput  = new PrintStream(clientSocket.getOutputStream());
                BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                do {
                    String str = clientInput.readLine();
                    if (str.equalsIgnoreCase("bye")) {
                        flag = false;
                        serverOutput.println("bye");
                    } else {
                        System.out.println(str);
                        serverOutput.println(str.length());
                    }
                } while (flag);

                clientInput.close();
                serverOutput.close();

            } catch (Exception e) {
                System.out.println("Connection Error!");
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Connection cannot close!");
                }
            }

            System.out.println("Client: " + clientSocket.getInetAddress() + clientSocket.getPort() + " is closed");
        }
    }
}
