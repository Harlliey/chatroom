package tcpandudp.server;

import tcpandudp.constants.UDPConstants;
import tcpandudp.utils.ByteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ServerProvider {
    private static Provider PROVIDER_INSTANCE;
    private static final int SERVER_PORT = UDPConstants.SERVER_PORT;

    public  static void start(int tcpPort) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, tcpPort);
        new Thread(provider).start();
        PROVIDER_INSTANCE = provider;
    }

    public static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    static class Provider implements Runnable{

        private final byte[] sn;
        private final int port;
        private boolean flag = false;
        private DatagramSocket ds = null;
        final byte[] buffer = new byte[128];

        Provider(String sn, int port) {
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("Server provider is starting...");
            try {
                ds = new DatagramSocket(SERVER_PORT);
                DatagramPacket recPac = new DatagramPacket(buffer, buffer.length);

                while (!flag) {
                    ds.receive(recPac);

                    String clientIP = recPac.getAddress().getHostAddress();
                    int clientPort = recPac.getPort();
                    byte[] clientData = recPac.getData();
                    int dataLen = recPac.getLength();
                    boolean isValid = dataLen >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startWith(clientData, UDPConstants.HEADER);

                    System.out.println("Server provider receive from: ");
                    System.out.println("ip: " + clientIP);
                    System.out.println("port: " + clientPort);
                    System.out.println("valid: " + isValid);

                    if (!isValid) {
                        continue;
                    }

                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) (clientData[index++] << 8 | (clientData[index++] & 0xff));
                    int responsePort = (int) ((clientData[index++] << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            (clientData[index++] & 0xff));

                    if (cmd == 1 && responsePort > 0) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short)2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();

                        DatagramPacket resPac = new DatagramPacket(buffer, len, recPac.getAddress(), responsePort);
                        ds.send(resPac);
                        System.out.println("Server provider has responded to ip: " + clientIP + " port: " + responsePort);
                    } else {
                        System.out.println("Server provider cannot recognize the command: " + cmd + " from ip: " + clientIP + " port: " + clientPort);
                    }
                }

            } catch (IOException ignored) {
            } finally {
                close();
            }
        }

        public void exit() {
            flag = true;
            close();
        }

        public void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
    }
}
