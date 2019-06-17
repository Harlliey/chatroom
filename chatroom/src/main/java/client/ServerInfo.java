package client;

public class ServerInfo {
    private String sn;
    private int tcpPort;
    private String ipAddr;

    public ServerInfo(String sn, int tcpPort, String ipAddr) {
        this.sn = sn;
        this.tcpPort = tcpPort;
        this.ipAddr = ipAddr;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "sn='" + sn + '\'' +
                ", tcpPort=" + tcpPort +
                ", ipAddr='" + ipAddr + '\'' +
                '}';
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }
}
