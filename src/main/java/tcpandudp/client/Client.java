package tcpandudp.client;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {
        ServerInfo serverInfo = ClientSearcher.searchAndGetServer(10000);
        System.out.println("Server: " + serverInfo);

        if (serverInfo != null) {
            try {
                TCPClient.linkWithServer(serverInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
