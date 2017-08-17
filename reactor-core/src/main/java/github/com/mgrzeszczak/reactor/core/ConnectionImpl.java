package github.com.mgrzeszczak.reactor.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

final class ConnectionImpl<T> implements Connection<T> {

    private final Logger logger = LogManager.getLogger(ConnectionImpl.class);
    private final int BUFFER_SIZE = 10240;

    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private final long id;
    private final Protocol<T> protocol;
    private final EventHandler<T> eventHandler;
    private final SocketChannel channel;
    private final BlockingQueue<T> messageQueue;
    private final Runnable notify;

    private boolean connected;
    private SelectionKey channelKey;

    ConnectionImpl(long id, SocketChannel channel, Protocol<T> protocol, EventHandler<T> eventHandler, Runnable notify) {
        this.id = id;
        this.protocol = protocol;
        this.eventHandler = eventHandler;
        this.channel = channel;
        this.notify = notify;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.connected = true;
    }

    void doSend(int max) throws IOException {
        int sent = 0;
        if (currentBuffer != null && !writeBuffer(currentBuffer)) {
            return;
        }
        while (sent < max) {
            T message = messageQueue.poll();
            if (message == null) {
                return;
            }
            ByteBuffer payload = protocol.convert(message);
            if (!writeBuffer(payload)) {
                return;
            }
            sent++;
        }
    }

    private ByteBuffer currentBuffer;

    private boolean writeBuffer(ByteBuffer buffer) throws IOException {
        currentBuffer = buffer;
        while (buffer.hasRemaining()) {
            int wrote = channel.write(buffer);
            if (wrote == 0) {
                return false;
            }
        }
        currentBuffer = null;
        return true;
    }

    void doRead(int max) throws IOException {
        int count = 0;
        int read;
        while ((read = channel.read(buffer)) > 0 && count < max) {
            buffer.flip();
            protocol.write(buffer, m -> eventHandler.onMessage(this, m));
            buffer.flip();
            count++;
        }
        if (read < 0) {
            // TODO: connection closed
            logger.info("connection closed {}", this);
            return;
        }
    }

    void register(Selector selector) throws IOException {
        if (channelKey == null) {
            SelectionKey channelKey = channel.register(selector, SelectionKey.OP_READ, this);
        }
    }

    private void cancel() {
        if (channelKey != null) {
            channelKey.cancel();
            channelKey = null;
        }
    }

    void doClose() {
        if (connected) {
            eventHandler.onClose(this);
            connected = false;
            cancel();
            close();
        }
    }

    @Override
    public void send(T message) {
        try {
            messageQueue.put(message);
            notify.run();
        } catch (InterruptedException e) {
            logger.warn(e, e);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            logger.warn(e, e);
        }
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public SocketAddress remoteAddress() {
        try {
            return channel.getRemoteAddress();
        } catch (IOException e) {
            logger.warn(e, e);
            return null;
        }
    }

    @Override
    public SocketAddress localAddress() {
        try {
            return channel.getLocalAddress();
        } catch (IOException e) {
            logger.warn(e, e);
            return null;
        }
    }

}
