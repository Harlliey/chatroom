package core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

public interface IoProvider extends Closeable {
    boolean registerInput(SocketChannel socketChannel, HandleInputCallback handleInputCallback);
    boolean registerOutput(SocketChannel socketChannel, HandleOutputCallback handleOutputCallback);
    void unRegisterInput(SocketChannel socketChannel);
    void unRegisterOutput(SocketChannel socketChannel);

    abstract class HandleInputCallback implements Runnable{
        @Override
        public final void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    abstract class HandleOutputCallback implements Runnable{
        private Object attach;

        @Override
        public final void run() {
            canProviderOutput(attach);
        }

        public void setAttach(Object attach) {
            this.attach = attach;
        }

        protected abstract void canProviderOutput(Object attach);
    }
}
