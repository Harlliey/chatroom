package core;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class Connect {
    private UUID key = UUID.randomUUID();
    private SocketChannel channel;
    private Sender sender;
    private Receiver receiver;

    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;
    }
}
