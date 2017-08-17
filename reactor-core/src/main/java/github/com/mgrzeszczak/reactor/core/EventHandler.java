package github.com.mgrzeszczak.reactor.core;

public interface EventHandler<T> {

    void onOpen(Connection<T> connection);

    void onMessage(Connection<T> connection, T message);

    void onClose(ConnectionData data);

}
