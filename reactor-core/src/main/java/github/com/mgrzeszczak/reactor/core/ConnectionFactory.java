package github.com.mgrzeszczak.reactor.core;

import java.nio.channels.SocketChannel;

final class ConnectionFactory<T> {

    private final ProtocolFactory<T> protocolFactory;
    private final EventHandler<T> eventHandler;
    private final Runnable notify;

    ConnectionFactory(ProtocolFactory<T> protocolFactory, EventHandler<T> eventHandler, Runnable notify) {
        this.protocolFactory = protocolFactory;
        this.eventHandler = eventHandler;
        this.notify = notify;
    }

    ConnectionImpl<T> create(SocketChannel channel) {
        long id = nextId();
        return new ConnectionImpl<>(id, channel, protocolFactory.create(), eventHandler, notify);
    }

    private static long currentId = 0;

    private static synchronized long nextId() {
        return ++currentId;
    }
}
