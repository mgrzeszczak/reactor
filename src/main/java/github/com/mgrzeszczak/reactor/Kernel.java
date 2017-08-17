package github.com.mgrzeszczak.reactor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class Kernel<T> extends Thread {

    private final static Logger logger = LogManager.getLogger(Kernel.class);
    private final static long TIMEOUT_MS = 100;
    private final static int MAX_WRITE_COUNT = 5;
    private final static int MAX_READ_COUNT = 5;

    private final List<ConnectionImpl<T>> connections;
    private final ConnectionFactory<T> connectionFactory;
    private final ServerSocketChannel channel;
    private final Selector selector;
    private final SelectionKey channelKey;

    private boolean running = false;

    Kernel(ServerSocketChannel server, EventHandler<T> eventHandler, ProtocolFactory<T> protocolFactory, String name) throws IOException {
        this.channel = server;
        this.setName(name);
        this.selector = Selector.open();
        this.connectionFactory = new ConnectionFactory<>(protocolFactory, eventHandler, selector::wakeup);
        this.channelKey = channel.register(selector, SelectionKey.OP_ACCEPT);
        this.connections = new ArrayList<>();
    }

    @Override
    public void run() {
        running = true;
        logger.info("Running kernel");
        try {
            doRun();
            selector.close();
        } catch (Exception e) {
            logger.error("Kernel failed: ", e);
        }
    }

    private void doRun() throws Exception {
        while (running) {
            int select = selector.select(TIMEOUT_MS);
            if (select != 0) {
                handleKeys(selector.selectedKeys().iterator());
            }
            sendMessages();
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        running = false;
        for (ConnectionImpl<T> connection : connections) {
            connection.doClose();
        }
    }

    private void handleKeys(Iterator<SelectionKey> iterator) {
        while (iterator.hasNext()) {
            SelectionKey next = iterator.next();
            if (next.isAcceptable()) {
                handleAccept(next);
            } else if (next.isReadable()) {
                handleRead(next);
            }
            iterator.remove();
        }
    }

    private void handleAccept(SelectionKey key) {
        try {
            SocketChannel accept = channel.accept();
            handleNewConnection(accept);
        } catch (IOException e) {
            logger.error(e, e);
            throw new IllegalStateException(e);
        }
    }

    private void handleNewConnection(SocketChannel channel) {
        ConnectionImpl<T> connection = connectionFactory.create(channel);
        connections.add(connection);
        try {
            channel.configureBlocking(false);
            connection.register(selector);
        } catch (IOException e) {
            logger.error(e, e);
            connection.doClose();
            connections.remove(connection);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRead(SelectionKey key) {
        ConnectionImpl<T> connection = (ConnectionImpl<T>) key.attachment();
        try {
            connection.doRead(MAX_READ_COUNT);
        } catch (IOException e) {
            logger.warn(e, e);
            connection.doClose();
            connections.remove(connection);
        }
    }

    private void sendMessages() {
        for (ConnectionImpl<T> connection : connections) {
            try {
                connection.doSend(MAX_WRITE_COUNT);
            } catch (IOException e) {
                logger.warn(e, e);
                connection.doClose();
                connections.remove(connection);
            }
        }
    }

}
