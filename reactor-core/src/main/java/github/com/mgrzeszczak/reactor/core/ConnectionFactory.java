package github.com.mgrzeszczak.reactor.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

final class ConnectionFactory<T> {

    private final ProtocolFactory<T> protocolFactory;
    private final EventHandler<T> eventHandler;

    ConnectionFactory(ProtocolFactory<T> protocolFactory, EventHandler<T> eventHandler) {
        this.protocolFactory = protocolFactory;
        this.eventHandler = eventHandler;
    }

    ConnectionImpl<T> create(SocketChannel channel, Runnable wakeup) throws IOException {
        long id = nextId();
        return new ConnectionImpl<>(id, channel, protocolFactory.create(), eventHandler, wakeup);
    }

    private static long currentId = 0;

    private static synchronized long nextId() {
        return ++currentId;
    }
}
