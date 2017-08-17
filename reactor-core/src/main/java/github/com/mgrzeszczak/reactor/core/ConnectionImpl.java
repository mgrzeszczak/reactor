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
    private final SocketAddress remoteAddress;
    private final SocketAddress localAddress;

    private final Protocol<T> protocol;
    private final EventHandler<T> eventHandler;
    private final SocketChannel channel;
    private final BlockingQueue<T> messageQueue;
    private final Runnable wakeup;

    private boolean connected;
    private SelectionKey channelKey;

    ConnectionImpl(long id, SocketChannel channel, Protocol<T> protocol, EventHandler<T> eventHandler, Runnable wakeup) throws IOException {
        this.id = id;
        this.protocol = protocol;
        this.eventHandler = eventHandler;
        this.channel = channel;
        this.wakeup = wakeup;
        this.remoteAddress = channel.getRemoteAddress();
        this.localAddress = channel.getLocalAddress();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.connected = true;
    }

    void onOpen() {
        eventHandler.onOpen(this);
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
            logger.info("Sending: {}", message);
            ByteBuffer payload = protocol.serialize(message);
            logger.info("Serialized: {}", payload);
            if (!writeBuffer(payload)) {
                logger.info("Failed to send whole: {}", message);
                return;
            }
            logger.info("Sent whole: {}", message);
            sent++;
        }
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
            logger.info("connection closed {}", this);
            throw new ReactorException("connection closed");
        }
    }

    void register(Selector selector) throws IOException {
        if (channelKey == null) {
            channelKey = channel.register(selector, SelectionKey.OP_READ, this);
        }
    }

    private void deregister() {
        if (channelKey != null) {
            channelKey.cancel();
        }
    }

    void doClose() {
        if (connected) {
            deregister();
            connected = false;
            cancel();
            close();
            eventHandler.onClose(this);
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

    private void cancel() {
        if (channelKey != null) {
            channelKey.cancel();
            channelKey = null;
        }
    }

    @Override
    public void send(T message) {
        try {
            logger.info("Queueing {}", message);
            messageQueue.put(message);
            wakeup.run();
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
        return this.remoteAddress;
    }

    @Override
    public SocketAddress localAddress() {
        return this.localAddress;
    }

}
