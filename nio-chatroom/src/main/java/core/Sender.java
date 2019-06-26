package core;

import java.io.IOException;

public interface Sender {
    boolean sendAsync(IoArgs ioArgs, IoArgs.IoArgsEventListener listener) throws IOException;
}
