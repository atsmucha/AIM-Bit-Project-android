package project.jdlp.com.jdlp.manager;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by atsmucha on 15. 7. 28.
 * @author atsmucha
 * @since 20150728
 */
public class JdlpSocketChannel {
    private LinkedBlockingQueue<Object> dataQueue = new LinkedBlockingQueue<>();
    private ExecutorService executor = Executors.newFixedThreadPool(3);
    private HashMap<String, Integer> uuidToSize = new HashMap<String, Integer>();
    private DataHandler dataHandler;
    private Thread clientExecutor;

    private Boolean keyMode = Constants.SELECT_KEYMODE_READ;

    private String addr = "192.168.0.12";
//    192.168.1.39
//    192.168.0.6
//    192.168.1.120
//    192.168.0.5
//    192.168.0.9
    private int port = 9734;
    private final static int BUFFER_SIZE = 4096;

    ResponseHandler responseHandler;

    private class SocketWriteTask implements Runnable {
        private ByteBuffer buffer;
        private SelectionKey key;
        private Selector selector;
        private SocketChannel sc;

        private SocketWriteTask(ByteBuffer buffer, SocketChannel sc, SelectionKey key, Selector selector) {
            this.buffer = buffer;
            this.key = key;
            this.selector = selector;
            this.sc = sc;
        }

        @Override
        public void run() {
            sc = (SocketChannel)key.channel();
            byte[] data = (byte[])key.attachment();

            buffer.clear();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(data);
            buffer.flip();
            int results = 0;
            while(buffer.hasRemaining()) {
                try {
                    if(sc.isConnected()) {
                        results = sc.write(buffer);
                    }
                } catch (IOException e) {
                    disConnect();
                    responseHandler.onFail(e);
                    e.printStackTrace();
                }
                if(results == 0){
                    buffer.compact();
                    buffer.flip();
                    data = new byte[buffer.remaining()];
                    buffer.get(data);
                    key.interestOps(SelectionKey.OP_WRITE);
                    key.attach(data);
                    selector.wakeup();
                    responseHandler.onSuccess(buffer);
                    return;
                }
            }
            getKeyMode(key);
            buffer.rewind();
            selector.wakeup();
        }
    }   //SocketWriterTask

    private class SocketReadTask implements Runnable {
        private ByteBuffer buffer;
        private SelectionKey key;
        private Selector selector;
        private SocketChannel sc;

        private SocketReadTask(ByteBuffer buffer, SocketChannel sc, SelectionKey key, Selector selector) {
            this.buffer = buffer;
            this.key = key;
            this.selector = selector;
            this.sc = sc;
        }

        private boolean checkUUID(byte[] data) {
            return uuidToSize.containsKey(new String(data));
        }

        @Override
        public void run() {
            sc = (SocketChannel)key.channel();
            buffer.clear();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            byte[] data = (byte[])key.attachment();
            if(data != null) {
                buffer.put(data);
            }
            int count = 0;
            int readAttempts = 0;
            try {
                while((count = sc.read(buffer)) > 0) {
                    readAttempts++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (count == 0) {
                buffer.flip();
                data = new byte[buffer.limit()];
                buffer.get(data);
                if(checkUUID(data)) {
                    key.interestOps(SelectionKey.OP_READ);
                    key.attach(data);
                } else {
                    if(buffer != null) {
                        Log.v("jdlpSocketChannel::", data.length+"");
                        dataHandler.recvData(buffer);
                        responseHandler.onSuccess(buffer);
                    }
                    getKeyMode(key);
                }
            }
            if(count == -1) {
                try {
                    sc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    responseHandler.onFail(e);
                }
            }

            selector.wakeup();
            Log.e("Thread task", "exit");
        }
    }   //SocketReadTask

    private class ClientExecutor implements Runnable {

        @Override
        public void run() {
            try {
                dataHandler = DataHandler.getInstance();
                Selector selector = Selector.open();

                SocketChannel sc = SocketChannel.open();
                InetAddress inetAddr = InetAddress.getByName(addr);
                SocketAddress socketAddr = new InetSocketAddress(inetAddr, port);
                sc.configureBlocking(false);
                sc.connect(socketAddr);
//                sc.socket().setSoTimeout(5000);
                sc.socket().setKeepAlive(true);
                sc.socket().setTcpNoDelay(true);
                sc.register(selector, SelectionKey.OP_CONNECT);
                ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);


                while(selector.isOpen()) {
                    int count = selector.select(10);
                    if (count == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                    while(it.hasNext()) {
                        final SelectionKey key = it.next();
                        it.remove();
                        if(!key.isValid()) {
                            continue;
                        }

                        if(key.isConnectable()) {
                            sc = (SocketChannel)key.channel();
                            if(!sc.finishConnect()) {
                                continue;
                            }
                            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }

                        if(key.isReadable()) {
                            key.interestOps(0);
                            SocketReadTask socketReadTask = new SocketReadTask(buffer, sc, key, selector);
                            executor.execute(socketReadTask);
                        }

                        if(key.isWritable()) {
                            key.interestOps(0);
                            if(key.attachment() == null) {
                                key.attach(dataQueue.take());
                            }
                            executor.execute(new SocketWriteTask(buffer, sc, key, selector));
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
//                responseHandler.onFail(e);
                disConnect();
            } catch (InterruptedException e) {
                e.printStackTrace();
                responseHandler.onFail(e);
                disConnect();
            }
        }
    }   //clientExecutor

    public JdlpSocketChannel() {
        clientExecutor = new Thread(new ClientExecutor());
    }   //instantation

    public void socketStart() {
        if(clientExecutor.isAlive()) return;
        else {
            try {
                clientExecutor.start();
            } catch (Exception e) {
                clientExecutor.interrupt();
            }
        }
    }

    public void disConnect() {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.socket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedBlockingQueue<Object> getDataQueue() {
        return dataQueue;
    }

    public interface ResponseHandler {
        void onSuccess(ByteBuffer buffer);
        void onFail(Exception e);
    }

    public void setResponseHandler(ResponseHandler handler) {
        this.responseHandler = handler;
    }

    private void getKeyMode(SelectionKey key) {
        if(this.keyMode == Constants.SELECT_KEYMODE_READ) {
            key.interestOps(SelectionKey.OP_READ);
            key.attach(null);
        } else {
            key.interestOps(SelectionKey.OP_WRITE);
            key.attach(null);
        }
    }
    public void setKeyMode(boolean keyMode) {
        this.keyMode = keyMode;
    }

}   //JdlpSocketTest
