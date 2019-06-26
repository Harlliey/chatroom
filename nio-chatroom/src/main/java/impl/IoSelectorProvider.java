package impl;

import core.IoProvider;
import utils.CloseUtil;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IoSelectorProvider implements IoProvider {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final AtomicBoolean inRegInput = new AtomicBoolean(false);
    private final AtomicBoolean inRegOutput = new AtomicBoolean(false);

    private final Selector readSelector;
    private final Selector writeSelector;

    private final ExecutorService inputHandlePool;
    private final ExecutorService outputHandlePool;

    private final Map<SelectionKey, Runnable> inputCallbackMap;
    private final Map<SelectionKey, Runnable> outputCallbackMap;

    public IoSelectorProvider() throws IOException {
        readSelector = Selector.open();
        writeSelector = Selector.open();
        inputHandlePool = Executors.newFixedThreadPool(4, new IoProviderThreadFactory("IoProvider-Input-Thread-"));
        outputHandlePool = Executors.newFixedThreadPool(4, new IoProviderThreadFactory("IoProvider-Output-Thread-"));
        inputCallbackMap = new HashMap<>();
        outputCallbackMap = new HashMap<>();

        startRead();
        startWrite();
    }

    private void startRead() {
        Thread readThread = new Thread("IoProvider ReadSelector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (readSelector.select() == 0) {
                            waitForRegFinish(inRegInput);
                            continue;
                        }

                        Set<SelectionKey> selectionKeys = readSelector.selectedKeys();

                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                handleSelection(selectionKey, SelectionKey.OP_READ, inputCallbackMap, inputHandlePool);
                            }
                        }

                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        readThread.setPriority(Thread.MAX_PRIORITY);
        readThread.start();
    }

    private void startWrite() {
        Thread writeThread = new Thread("IoProvider WriteSelector Thread"){
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (writeSelector.select() == 0) {
                            waitForRegFinish(inRegOutput);
                            continue;
                        }

                        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();

                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                handleSelection(selectionKey, SelectionKey.OP_WRITE, outputCallbackMap, outputHandlePool);
                            }
                        }

                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        writeThread.setPriority(Thread.MAX_PRIORITY);
        writeThread.run();
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private static void waitForRegFinish(final AtomicBoolean lock) {
        synchronized (lock) {
            if (lock.get()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void handleSelection(SelectionKey key, int opRead, Map<SelectionKey, Runnable> inputCallbackMap, ExecutorService inputHandlePool) {
        //取消监听注册的读事件
        key.interestOps(key.readyOps() & ~opRead);

        Runnable runnable = null;
        try {
            runnable = inputCallbackMap.get(key);
        } catch (Exception ignored) {
        }

        if (runnable != null && !inputHandlePool.isShutdown()) {
            inputHandlePool.execute(runnable);
        }
    }


    @Override
    public boolean registerInput(SocketChannel socketChannel, HandleInputCallback handleInputCallback) {
        return registerSelection(socketChannel, readSelector, SelectionKey.OP_READ, inRegInput, inputCallbackMap, handleInputCallback) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel socketChannel, HandleOutputCallback handleOutputCallback) {
        return registerSelection(socketChannel, writeSelector, SelectionKey.OP_WRITE, inRegOutput, outputCallbackMap, handleOutputCallback) != null;
    }

    @Override
    public void unRegisterInput(SocketChannel socketChannel) {
        unRegisterSelection(socketChannel, readSelector, inputCallbackMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel socketChannel) {
        unRegisterSelection(socketChannel, writeSelector, outputCallbackMap);
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            inputHandlePool.shutdownNow();
            outputHandlePool.shutdownNow();

            inputCallbackMap.clear();
            outputCallbackMap.clear();

            readSelector.wakeup();
            writeSelector.wakeup();

            CloseUtil.close(readSelector, writeSelector);
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private static SelectionKey registerSelection(SocketChannel channel, Selector selector, int registerOps, AtomicBoolean lock, Map<SelectionKey, Runnable> map, Runnable runnable) {
        synchronized (lock) {
            lock.set(true);
            try {
                selector.wakeup();

                SelectionKey key = null;
                if (channel.isRegistered()) {
                    key = channel.keyFor(selector);
                    if (key != null) {
                        key.interestOps(key.readyOps() | registerOps);
                    }
                }

                if (key == null) {
                    key = channel.register(selector, registerOps);
                    map.put(key, runnable);

                }

                return key;
            } catch (ClosedChannelException e) {
                return null;
            } finally {
                lock.set(false);
                try {
                    lock.notify();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void unRegisterSelection(SocketChannel channel, Selector selector, Map<SelectionKey, Runnable> map) {
        if (channel.isRegistered()) {
            SelectionKey key = channel.keyFor(selector);
            if (key != null) {
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }

    }

    static class IoProviderThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        IoProviderThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
