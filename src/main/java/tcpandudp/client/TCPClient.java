package tcpandudp.client;

import java.io.*;
import java.net.*;

public class TCPClient {
    static void linkWithServer(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);

        socket.connect(new InetSocketAddress(Inet4Address.getByName(serverInfo.getIpAddr()), serverInfo.getTcpPort()), 3000);

        System.out.println("Connection established: ");
        System.out.println("Client info: (ip: " + socket.getLocalAddress() + " port: " + socket.getLocalPort() + ")");
        System.out.println("Server info: (ip: " + socket.getInetAddress() + " port: " + socket.getPort() + ")");

        try {
            sendMsg(socket);
        } catch (Exception e) {
            System.out.println("Link with server error!");
        }

        socket.close();
        System.out.println("Connection closed!");
    }

    private static void sendMsg(Socket client) throws IOException {
        BufferedReader keyInputReader = new BufferedReader(new InputStreamReader(System.in));
        PrintStream output = new PrintStream(client.getOutputStream());
        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

        boolean flag = true;
        do {
            String str = keyInputReader.readLine();
            output.println(str);

            String echo = input.readLine();
            if ("bye".equalsIgnoreCase(echo)) {
                flag = false;
            } else {
                System.out.println(echo);
            }
        } while (flag);

        keyInputReader.close();
        input.close();
        output.close();
    }
}
