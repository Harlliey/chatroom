import java.util.UUID;

public class MessageCreator {

    private final static String SN_HEADER = "My unique identifier is:";
    private final static String PORT_HEADER = "Please reply to the port:";

    public static String buildWithPort(int port) {
        return PORT_HEADER + port;
    }

    public static int parsePort(String msg) {
        if (msg.startsWith(PORT_HEADER)) {
            String strPort = msg.substring(PORT_HEADER.length());
            return Integer.parseInt(strPort);
        } else {
            return -1;
        }
    }

    public static String buildWithSn(String sn) {
        return SN_HEADER + sn;
    }

    public static String parseSn(String msg) {
        if (msg.startsWith(SN_HEADER)) {
            return msg.substring(SN_HEADER.length());
        } else {
            return null;
        }
    }
}
