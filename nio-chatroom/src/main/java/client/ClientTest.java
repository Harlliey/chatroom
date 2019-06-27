package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientTest {
    private static boolean done = false;

    public static void main(String[] args) throws IOException {
        ServerInfo serverInfo = ClientSearcher.searchAndGetServer(10000);
        System.out.println("Server: " + serverInfo);

        if (serverInfo == null) {
            return;
        }

        int size = 0;
        final List<TCPClient> tcpClientList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            try {
                TCPClient tcpClient = TCPClient.linkWithServer(serverInfo);
                if (tcpClient == null) {
                    System.out.println("Linking error!");
                    continue;
                }

                tcpClientList.add(tcpClient);

                System.out.println("Successfully linked: " + (++size));

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.in.read();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!done) {
                    for (TCPClient tcpClient : tcpClientList) {
                        tcpClient.send("Hello!");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

        System.in.read();

        done = true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (TCPClient tcpClient : tcpClientList) {
            tcpClient.exit();
        }
    }
}
