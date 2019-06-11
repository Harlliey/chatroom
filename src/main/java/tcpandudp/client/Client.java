package tcpandudp.client;

public class Client {
    public static void main(String[] args) {
        ServerInfo serverInfo = ClientSearcher.searchAndGetServer(10000);
        System.out.println("Server: " + serverInfo);
    }
}
