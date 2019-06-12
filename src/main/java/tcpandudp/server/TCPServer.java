package tcpandudp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private int tcpPort;
    private TCPListener tcpListener;

    public TCPServer(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    boolean start() {
        try {
            TCPListener listener = new TCPListener(tcpPort);
            tcpListener = listener;
            new Thread(tcpListener).start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void stop() {
        if (tcpListener != null) {
            tcpListener.exit();
            tcpListener = null;
        }
    }

    private static class TCPListener implements Runnable{
        private ServerSocket serverSocket;
        private boolean flag = false;

        public TCPListener(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server info:");
            System.out.println("IP: " + serverSocket.getInetAddress());
            System.out.println("port: " + serverSocket.getLocalPort());
        }


        @Override
        public void run() {
            System.out.println("TCP Server is ready!");
            do {
                Socket client;
                try {
                    client = serverSocket.accept();
                } catch (IOException e) {
                    continue;
                }
                ClientHandler clientHandler = new ClientHandler(client);
                new Thread(clientHandler).start();
            } while (!flag);
        }

        void exit() {
            flag = true;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler implements Runnable{
        private Socket client;
        private boolean flag = false;

        public ClientHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            System.out.println("New client connection: " + client.getInetAddress() + " port: " + client.getPort());
            try {
                PrintStream socketOutput = new PrintStream(client.getOutputStream());
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(client.getInputStream()));

                do {
                    String str = socketInput.readLine();
                    if ("bye".equalsIgnoreCase(str)) {
                        flag = true;
                        System.out.println(str);
                        socketOutput.println("bye");
                    } else {
                        System.out.println(str);
                        socketOutput.println("response: " + str.length());
                    }
                } while (!flag);

                socketInput.close();
                socketOutput.close();
            } catch (IOException e) {
                System.out.println("Connection error!");
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
