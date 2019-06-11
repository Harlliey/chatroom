import java.util.Arrays;

public class demo {
    public static void main(String[] args) {
        byte[] a = new byte[]{7, 7, 7, 7};
        byte[] b = new byte[]{8, 7, 7, 7, 7, 1, 2, 3, 4};

        byte[] c = Arrays.copyOf(b, a.length);

        System.out.println(Arrays.equals(a, c));
    }
}
