package demo;

import java.io.*;
import java.net.*;

/**
 * Created by administer on 24/5/19.
 */
public class ClientDemo {
    public static void main(String[] args) throws IOException{
        Socket clientSocket = new Socket();
        clientSocket.setSoTimeout(5000);

        clientSocket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 2000), 5000);

        System.out.println("Client message: " + clientSocket.getLocalAddress() + " port: " + clientSocket.getLocalPort());
        System.out.println("Server message: " + clientSocket.getInetAddress() + " port: " + clientSocket.getPort());

        try {
            sendMsg(clientSocket);

        } catch (Exception e) {
            System.out.println("Connection error!");

        } finally {
            clientSocket.close();
        }
    }

    private static void sendMsg(Socket clientSocket) throws IOException{
        InputStream keyIn = System.in;
        BufferedReader keyInput = new BufferedReader(new InputStreamReader(keyIn));

        OutputStream outputStream = clientSocket.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        InputStream serverRet = clientSocket.getInputStream();
        BufferedReader serverBufferReader = new BufferedReader(new InputStreamReader(serverRet));

        boolean flag = true;

        do {
            String keyStr = keyInput.readLine();
            socketPrintStream.println(keyStr);

            String serverEcho = serverBufferReader.readLine();
            if (serverEcho.equalsIgnoreCase("bye")) {
                flag = false;
            } else {
                System.out.println(serverEcho);
            }
        } while (flag);

        socketPrintStream.close();
        serverBufferReader.close();
    }
}
