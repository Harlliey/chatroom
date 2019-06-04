import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSearcher {
    public static void main(String[] args) throws IOException {
        System.out.println("UDP Searcher is starting...");

        DatagramSocket ds = new DatagramSocket();

        String requestMsg = "Hello world!";
        byte[] requestMsgByte = requestMsg.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(requestMsgByte,
                requestMsgByte.length);
        requestPacket.setAddress(InetAddress.getLocalHost());
        requestPacket.setPort(20000);

        ds.send(requestPacket);

        final byte[] buffer = new byte[512];

        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        ds.receive(receivePacket);

        String ipAddr = receivePacket.getAddress().getHostAddress();
        int port = receivePacket.getPort();
        int dataLen = receivePacket.getLength();
        String data = new String(receivePacket.getData(), 0, dataLen);
        System.out.println("UDP Searcher get the message from: ");
        System.out.println("ip: " + ipAddr + " port: " + port + " data: " + data);

        System.out.println("UDP Searcher finished!");
        ds.close();
    }
}
