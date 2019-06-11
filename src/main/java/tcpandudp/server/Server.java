package tcpandudp.server;

import tcpandudp.constants.TCPConstants;

public class Server {
    public static void main(String[] args) {
        ServerProvider.start(TCPConstants.SERVER_PORT);
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ServerProvider.stop();
    }
}
