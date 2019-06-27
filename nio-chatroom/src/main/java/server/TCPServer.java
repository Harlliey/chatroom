package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandler.ClientHandlerCallback{
    private int tcpPort;
    private TCPListener tcpListener;
    private List<ClientHandler> clientHandlerList;
    private final ExecutorService forwardExecutor;

    TCPServer(int tcpPort) {
        this.tcpPort = tcpPort;
        this.clientHandlerList = new ArrayList<>();
        this.forwardExecutor = Executors.newSingleThreadExecutor();
    }

    boolean start() {
        try {
            this.tcpListener = new TCPListener(tcpPort);
            new Thread(this.tcpListener).start();
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

        synchronized (TCPServer.this) {
            for (ClientHandler handler : clientHandlerList) {
                handler.exit();
            }

            clientHandlerList.clear();
        }

        forwardExecutor.shutdownNow();
    }

    synchronized void broadcast(String str) {
        for (ClientHandler handler : clientHandlerList) {
            handler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageReceived(ClientHandler handler, String msg) {
        System.out.println("Received from: " + handler.getClientInfo() + ": " + msg);
        forwardExecutor.execute(() -> {
            synchronized (TCPServer.this) {
                for (ClientHandler clientHandler : clientHandlerList) {
                    if (clientHandler.equals(handler)) {
                        continue;
                    }
                    clientHandler.send(msg);
                }
            }
        });
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
                    clientHandler = new ClientHandler(client, TCPServer.this);

                    clientHandler.readToPrint();

                    synchronized (TCPServer.this) {
                        clientHandlerList.add(clientHandler);
                    }
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
