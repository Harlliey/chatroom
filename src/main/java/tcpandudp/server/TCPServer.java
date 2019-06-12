package tcpandudp.server;

import com.sun.jmx.remote.internal.ClientListenerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {
    private int tcpPort;
    private TCPListener tcpListener;
    private List<ClientHandler> clientHandlerList;

    TCPServer(int tcpPort) {
        this.tcpPort = tcpPort;
        clientHandlerList = new ArrayList<>();
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

        for (ClientHandler handler : clientHandlerList) {
            handler.exit();
        }

        clientHandlerList.clear();
    }

    void broadcast(String str) {
        for (ClientHandler handler : clientHandlerList) {
            handler.send(str);
        }
    }

    private class TCPListener implements Runnable{
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
                ClientHandler clientHandler = null;
                try {
                    clientHandler = new ClientHandler(client, clientHandler1 -> clientHandlerList.remove(clientHandler1));
                    clientHandlerList.add(clientHandler);
                    clientHandler.readToPrint();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Client connection error: " + e.getMessage());
                }
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
}
