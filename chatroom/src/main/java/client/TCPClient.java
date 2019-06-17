package client;

import utils.CloseUtil;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {
    private final Socket socket;
    private final ReadHandler readHandler;
    private final PrintStream printStream;

    public TCPClient(Socket socket, ReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        this.printStream = new PrintStream(socket.getOutputStream());
    }

    void exit() {
        readHandler.exit();
        CloseUtil.close(printStream);
        CloseUtil.close(socket);
    }

    void send(String msg) {
        printStream.println(msg);
    }

    static TCPClient linkWithServer(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);

        socket.connect(new InetSocketAddress(Inet4Address.getByName(serverInfo.getIpAddr()), serverInfo.getTcpPort()), 3000);

        System.out.println("Connection established: ");
        System.out.println("Client info: (ip: " + socket.getLocalAddress() + " port: " + socket.getLocalPort() + ")");
        System.out.println("Server info: (ip: " + socket.getInetAddress() + " port: " + socket.getPort() + ")");

        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            new Thread(readHandler).start();

            return new TCPClient(socket, readHandler);
        } catch (Exception e) {
            System.out.println("Link with server error!");
            CloseUtil.close(socket);
        }

        return null;
    }

    static class ReadHandler implements Runnable {
        private boolean flag = false;
        private final InputStream inputStream;

        ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    String str;
                    try {
                        str = socketInput.readLine();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }

                    if (str == null) {
                        System.out.println("Client cannot read the data!");
                        throw new Exception();
                    }

                    System.out.println(str);
                } while (!flag);
            } catch (Exception e) {
                if (!flag) {
                    System.out.println("Connection missed!");

                }
            } finally {
                CloseUtil.close(inputStream);
            }
        }

        void exit() {
            flag = true;
            CloseUtil.close(inputStream);
        }
    }
}
