package tcpandudp.client;

import tcpandudp.utils.CloseUtil;

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
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            new Thread(readHandler).start();

            write(socket);

            readHandler.exit();
        } catch (Exception e) {
            System.out.println("Link with server error!");
        }

        socket.close();
        System.out.println("Connection closed!");
    }

    private static void write(Socket client) throws IOException {
        BufferedReader keyInputReader = new BufferedReader(new InputStreamReader(System.in));
        PrintStream output = new PrintStream(client.getOutputStream());

        do {
            String str = keyInputReader.readLine();
            output.println(str);

            if ("byebye".equalsIgnoreCase(str)) {
                break;
            }
        } while (true);

        keyInputReader.close();
        output.close();
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
