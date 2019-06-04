import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UDPSearcher {
    private final static int LISTEN_PORT = 30000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("UDP Searcher starting...");

            ProviderListener providerListener = listen();
            sendBroadcast();

            System.in.read();
            List<Device> deviceList = providerListener.getDevicesAndExit();

            for (Device device : deviceList) {
                System.out.println("Device: " + device.toString());
            }

        System.out.println("UDP Searcher finished!");
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDP Searcher is broadcasting...");

        DatagramSocket ds = new DatagramSocket();

        String requestMsg = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestMsgByte = requestMsg.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(requestMsgByte,
                requestMsgByte.length);
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(20000);

        ds.send(requestPacket);
        ds.close();

        System.out.println("UDP Searcher broadcasting finished!");
    }

    private static ProviderListener listen() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ProviderListener listener = new ProviderListener(countDownLatch, LISTEN_PORT);
        new Thread(listener).start();

        countDownLatch.await();
        return listener;
    }

    private static class ProviderListener implements Runnable{
        private final CountDownLatch countDownLatch;
        private final int listenPort;
        private final List<Device> devices = new ArrayList<>();
        private boolean flag = false;
        private DatagramSocket ds = null;

        private ProviderListener(CountDownLatch countDownLatch, int listenPort) {
            this.countDownLatch = countDownLatch;
            this.listenPort = listenPort;
        }


        @Override
        public void run() {
            countDownLatch.countDown();
            try {
                ds = new DatagramSocket(listenPort);
                while (!flag) {
                    final byte[] buffer = new byte[512];

                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    ds.receive(receivePacket);

                    String ipAddr = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    int dataLen = receivePacket.getLength();
                    String data = new String(receivePacket.getData(), 0, dataLen);

                    System.out.println("UDP Searcher get the message from: ");
                    System.out.println("ip: " + ipAddr + " port: " + port + " data: " + data);

                    String sn = MessageCreator.parseSn(data);
                    if (sn != null) {
                        Device device = new Device(ipAddr, port, sn);
                        devices.add(device);
                    }

                }
            } catch (Exception ignored) {
            } finally {
                close();
            }
            System.out.println("UDP Searcher listening finished!");
        }

        private List<Device> getDevicesAndExit() {
            flag = true;
            close();
            return devices;
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
    }
}
