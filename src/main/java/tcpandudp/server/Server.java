package tcpandudp.server;

import tcpandudp.constants.TCPConstants;

public class Server {
    public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer(TCPConstants.SERVER_PORT);
        boolean isSuccess = tcpServer.start();
        if (!isSuccess) {
            System.out.println("Failed to start tcp server!");
            return;
        }

        ServerProvider.start(TCPConstants.SERVER_PORT);
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tcpServer.stop();
        ServerProvider.stop();
    }
}
