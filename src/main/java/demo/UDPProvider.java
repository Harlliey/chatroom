package demo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

public class UDPProvider {
    public static void main(String[] args) throws IOException {
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        new Thread(provider).start();

        System.in.read();
        provider.stop();
    }

    private static class Provider implements Runnable{
        private final String sn;
        private boolean flag = false;
        private DatagramSocket ds;

        public Provider(String sn) {

            this.sn = sn;
        }

        @Override
        public void run() {
            System.out.println("UDP Provider is starting...");
            try {
                ds = new DatagramSocket(20000);

                while (!flag) {
                    final byte[] buffer = new byte[512];

                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    ds.receive(receivePacket);

                    String ipAddr = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    int dataLen = receivePacket.getLength();
                    String data = new String(receivePacket.getData(), 0, dataLen);
                    System.out.println("UDP Provider get the message from: ");
                    System.out.println("ip: " + ipAddr + " port: " + port + " data: " + data);

                    int responsePort = MessageCreator.parsePort(data);

                    if (responsePort != -1) {
                        String responseMsg = MessageCreator.buildWithSn(sn);
                        byte[] responseMsgByte = responseMsg.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseMsgByte,
                                responseMsgByte.length,
                                receivePacket.getAddress(),
                                responsePort);

                        ds.send(responsePacket);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                exit();
            }

            System.out.println("UDP Provider finished!");
        }

        public void stop() {
            flag = true;
            exit();
        }

        public void exit() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
    }
}