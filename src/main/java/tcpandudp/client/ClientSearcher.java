package tcpandudp.client;

import tcpandudp.constants.UDPConstants;
import tcpandudp.utils.ByteUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientSearcher {

    private static final int LISTEN_PORT = UDPConstants.CLIENT_RECEIVE_PORT;
    static ServerInfo searchAndGetServer(int timeout) {
        System.out.println("Client searcher is starting...");

        CountDownLatch receiceLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = listen(receiceLatch);
            sendBroadCast();
            receiceLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        System.out.println("Client searcher finished!");
        if (listener == null) return null;
        List<ServerInfo> servers = listener.getAndClose();
        if (servers.size() > 0) return servers.get(0);
        return null;
    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("Client searcher is listening...");
        CountDownLatch startLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startLatch, receiveLatch);
        new Thread(listener).start();
        startLatch.await();
        return listener;
    }

    private static void sendBroadCast() throws IOException {
        System.out.println("Client searcher is broadcasting...");
        DatagramSocket broadcastSocket = new DatagramSocket();
        ByteBuffer broadcastBuffer = ByteBuffer.allocate(128);
        broadcastBuffer.put(UDPConstants.HEADER);
        broadcastBuffer.putShort((short)1);
        broadcastBuffer.putInt(LISTEN_PORT);
        DatagramPacket broadcastPacket = new DatagramPacket(broadcastBuffer.array(), broadcastBuffer.position() + 1);
        broadcastPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        broadcastPacket.setPort(UDPConstants.SERVER_PORT);
        broadcastSocket.send(broadcastPacket);
        broadcastSocket.close();
        System.out.println("Client searcher broadcast finished!");
    }

    static class Listener implements Runnable{
        private final int listenPort;
        private final CountDownLatch startLatch;
        private final CountDownLatch receiveLatch;
        private final byte[] buffer = new byte[128];
        private final List<ServerInfo> servers = new ArrayList<>();
        private final int minLen = UDPConstants.HEADER.length + 6;
        private DatagramSocket ds = null;
        private boolean flag = false;

        public Listener(int listenPort, CountDownLatch startLatch, CountDownLatch receiveLatch) {
            this.listenPort = listenPort;
            this.startLatch = startLatch;
            this.receiveLatch = receiveLatch;
        }

        @Override
        public void run() {
            startLatch.countDown();
            System.out.println("Listener is running...");
            try {
                ds = new DatagramSocket(listenPort);
                DatagramPacket recPac = new DatagramPacket(buffer, buffer.length);
                while (!flag) {
                    ds.receive(recPac);

                    String serverIP = recPac.getAddress().getHostAddress();
                    int serverPort = recPac.getPort();
                    int serverDataLen = recPac.getLength();
                    byte[] serverData = recPac.getData();
                    boolean isValid = (serverDataLen > minLen) &&
                            (ByteUtils.startWith(serverData, UDPConstants.HEADER));

                    System.out.println("Client searcher receive from: ");
                    System.out.println("ip: " + serverIP);
                    System.out.println("port: " + serverPort);
                    System.out.println("valid: " + isValid);

                    if (!isValid) continue;

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, serverDataLen);
                    short cmd = byteBuffer.getShort();
                    int tcpPort = byteBuffer.getInt();
                    if (cmd != 2 || tcpPort < 0) {
                        System.out.println("Client searcher cannot recognize the command: " + cmd + " or the tcp port: " + tcpPort);
                        continue;
                    }
                    String sn = new String(buffer, minLen, serverDataLen - minLen);
                    ServerInfo serverInfo = new ServerInfo(sn, tcpPort, serverIP);
                    servers.add(serverInfo);
                    receiveLatch.countDown();
                }
            } catch (IOException ignored) {
            } finally {
                close();
            }
        }

        List<ServerInfo> getAndClose() {
            flag = true;
            close();
            return servers;
        }

        private void close() {
            if (ds != null) {
                ds.close();
            }
        }
    }
}
