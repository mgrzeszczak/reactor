package github.com.mgrzeszczak.reactor.core;

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

    Kernel(ServerSocketChannel server, ConnectionFactory<T> connectionFactory, String name) throws IOException {
        this.channel = server;
        this.setName(name);
        this.selector = Selector.open();
        this.connectionFactory = connectionFactory;
        this.channelKey = channel.register(selector, SelectionKey.OP_ACCEPT);
        this.connections = new ArrayList<>();
    }

    @Override
    public void run() {
        running = true;
        logger.info("Kernel started");
        try {
            doRun();
            selector.close();
        } catch (Exception e) {
            logger.error("Fatal kernel error", e);
        }
        cleanup();
    }

    private void doRun() throws Exception {
        while (running) {
            logger.info("active connections: {}", connections.size());
            int select = selector.select(TIMEOUT_MS);
            if (select != 0) {
                handleKeys(selector.selectedKeys().iterator());
            }
            doSend();
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        running = false;
    }

    private void handleKeys(Iterator<SelectionKey> iterator) {
        while (iterator.hasNext()) {
            SelectionKey next = iterator.next();
            if (next.isAcceptable()) {
                doAccept(next);
            } else if (next.isReadable()) {
                doRead(next);
            }
            iterator.remove();
        }
    }

    private void doAccept(SelectionKey key) {
        try {
            SocketChannel accept = channel.accept();
            if (accept != null) {
                handleNew(accept);
            }
        } catch (IOException e) {
            logger.error(e, e);
            cleanup();
            throw new ReactorException(e);
        }
    }

    private void cleanup() {
        Iterator<ConnectionImpl<T>> iterator = connections.iterator();
        while (iterator.hasNext()) {
            ConnectionImpl<T> next = iterator.next();
            next.doClose();
            iterator.remove();
        }
        channelKey.cancel();
    }

    private void handleNew(SocketChannel channel) {
        ConnectionImpl<T> connection = null;
        try {
            connection = connectionFactory.create(channel, selector::wakeup);
        } catch (IOException e) {
            logger.warn(e, e);
            return;
        }
        connections.add(connection);
        try {
            channel.configureBlocking(false);
            connection.register(selector);
        } catch (IOException e) {
            logger.warn(e, e);
            closeConnection(connection);
        }
        connection.onOpen();
    }

    @SuppressWarnings("unchecked")
    private void doRead(SelectionKey key) {
        ConnectionImpl<T> connection = (ConnectionImpl<T>) key.attachment();
        ioAction(connection, () -> connection.doRead(MAX_READ_COUNT));
    }

    private void doSend() {
        connections.forEach(c -> ioAction(c, () -> c.doSend(MAX_WRITE_COUNT)));
    }

    private void ioAction(ConnectionImpl<T> connection, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            logger.warn(t, t);
            closeConnection(connection);
        }
    }

    private void closeConnection(ConnectionImpl<T> connection) {
        connection.doClose();
        connections.remove(connection);
    }

}
