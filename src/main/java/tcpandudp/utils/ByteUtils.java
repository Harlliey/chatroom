package tcpandudp.utils;

import java.util.Arrays;

public class ByteUtils {

    public static boolean startWith(byte[] origin, byte[] header) {

        byte[] originCut = Arrays.copyOf(origin, header.length);
        if (Arrays.equals(originCut, header)) {
            return true;
        } else {
            return false;
        }
    }
}
