package server;

import utils.CloseUtil;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientHandler {
    private final Socket client;
    private final ClientReadHandler clientReadHandler;
    private final ClientSendHandler clientSendHandler;
    private final ClientHandlerCallback clientHandlerCallBack;
    private final String clientInfo;

    ClientHandler(Socket client, ClientHandlerCallback clientHandlerCallBack) throws IOException {
        this.client = client;
        this.clientReadHandler = new ClientReadHandler(client.getInputStream());
        this.clientSendHandler = new ClientSendHandler(client.getOutputStream());
        this.clientHandlerCallBack = clientHandlerCallBack;
        this.clientInfo = "A[" + client.getInetAddress() + "] " + "P[" + client.getPort() + "]";
        System.out.println("New client connection: " + clientInfo);
    }

    String getClientInfo() {
        return clientInfo;
    }

    void exit() {
        clientReadHandler.exit();
        clientSendHandler.exit();
        CloseUtil.close(client);
        System.out.println("Client is closed IP: " + client.getInetAddress() + " port: " + client.getPort());
    }

    void send(String str) {
        clientSendHandler.send(str);
    }

    void readToPrint() {
        new Thread(clientReadHandler).start();
    }

    void selfExit() {
        exit();
        clientHandlerCallBack.onSelfClosed(this);
    }

    public interface ClientHandlerCallback {
        void onSelfClosed(ClientHandler handler);
        void onNewMessageReceived(ClientHandler handler, String msg);
    }

    class ClientReadHandler implements Runnable{
        private boolean flag = false;
        private final InputStream inputStream;

        ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    String str = socketInput.readLine();
                    if (str == null) {
                        System.out.println("Client cannot read the data!");
                        ClientHandler.this.selfExit();
                        break;
                    }
                    clientHandlerCallBack.onNewMessageReceived(ClientHandler.this, str);
                } while (!flag);

            } catch (Exception e) {
                if (!flag) {
                    System.out.println("Connection missed!");
                    ClientHandler.this.selfExit();
                }
            } finally {
                CloseUtil.close(inputStream);
            }
        }

        void exit() {
            flag = true;
            CloseUtil.close(inputStream);
        }
    }

    class ClientSendHandler {
        private boolean flag = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        ClientSendHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            flag = true;
            CloseUtil.close(printStream);
            executorService.shutdownNow();
        }

        void send(String str) {
            if (flag) {
                return;
            }
            executorService.execute(new SendRunnable(str));
        }

        class SendRunnable implements Runnable {
            private final String msg;

            SendRunnable(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if (ClientSendHandler.this.flag) {
                    return;
                }

                try {
                    ClientSendHandler.this.printStream.println(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
