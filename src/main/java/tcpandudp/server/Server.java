package tcpandudp.server;

import tcpandudp.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) throws IOException {
        TCPServer tcpServer = new TCPServer(TCPConstants.SERVER_PORT);
        boolean isSuccess = tcpServer.start();
        if (!isSuccess) {
            System.out.println("Failed to start tcp server!");
            return;
        }

        ServerProvider.start(TCPConstants.SERVER_PORT);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while (!"byebye".equalsIgnoreCase(str));

        tcpServer.stop();
        ServerProvider.stop();
    }
}
