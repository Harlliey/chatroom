package impl;

import core.IoArgs;
import core.IoProvider;
import core.Receiver;
import core.Sender;
import utils.CloseUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdapter implements Sender, Receiver, Closeable {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final IoProvider ioProvider;
    private final SocketChannel channel;
    private final onChannelStatusChangedListener onChannelStatusChangedListener;

    private IoArgs.IoArgsEventListener sendListener;
    private IoArgs.IoArgsEventListener receiveListener;

    public SocketChannelAdapter(IoProvider ioProvider, SocketChannel channel, SocketChannelAdapter.onChannelStatusChangedListener onChannelStatusChangedListener) throws IOException {
        this.ioProvider = ioProvider;
        this.channel = channel;
        this.onChannelStatusChangedListener = onChannelStatusChangedListener;

        channel.configureBlocking(false);
    }


    @Override
    public boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }

        receiveListener = listener;
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public boolean sendAsync(IoArgs ioArgs, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }

        sendListener = listener;
        outputCallback.setAttach(ioArgs);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);

            CloseUtil.close(channel);

            onChannelStatusChangedListener.onChannelClosed(channel);
        }
    }

    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }

            IoArgs args = new IoArgs();
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiveListener;

            if (listener != null) {
                listener.onStarted(args);
            }

            try {
                if (args.read(channel) > 0 && listener != null) {
                    listener.onCompleted(args);
                } else {
                    throw new IOException("Cannot read data currently!");
                }
            } catch (IOException ignored) {
                CloseUtil.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get()) {
                return;
            }

            //TODO
            sendListener.onCompleted(null);
        }
    };

    public interface onChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
