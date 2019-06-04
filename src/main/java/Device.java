public class Device {
    String ipAddr;
    int port;
    String sn;

    public Device(String ipAddr, int port, String sn) {
        this.ipAddr = ipAddr;
        this.port = port;
        this.sn = sn;
    }

    @Override
    public String toString() {
        return "Device{" +
                "ipAddr='" + ipAddr + '\'' +
                ", port=" + port +
                ", sn='" + sn + '\'' +
                '}';
    }
}
