package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {
    public static void main(String[] args) {
        ServerInfo serverInfo = ClientSearcher.searchAndGetServer(10000);
        System.out.println("Server: " + serverInfo);

        if (serverInfo != null) {
            TCPClient tcpClient = null;
            try {
                tcpClient = TCPClient.linkWithServer(serverInfo);
                if (tcpClient == null) {
                    return;
                }

                write(tcpClient);
            } catch (IOException e) {
                System.out.println("Linking error!");
            } finally {
                if (tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }
    }

    private static void write(TCPClient tcpClient) throws IOException {
        BufferedReader keyInputReader = new BufferedReader(new InputStreamReader(System.in));
        do {
            String str = keyInputReader.readLine();
            tcpClient.send(str);

            if ("byebye".equalsIgnoreCase(str)) {
                break;
            }
        } while (true);
    }
}
