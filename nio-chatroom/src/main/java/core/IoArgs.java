package core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoArgs {
    private byte[] buffer = new byte[256];
    private ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

    public int read(SocketChannel socketChannel) throws IOException {
        byteBuffer.clear();
        return socketChannel.read(byteBuffer);
    }

    public int write(SocketChannel socketChannel) throws IOException {
        return socketChannel.write(byteBuffer);
    }

    public String bufToStr() {
        return new String(buffer, 0, byteBuffer.position() - 1);
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs ioArgs);

        void onCompleted(IoArgs ioArgs);
    }
}
