package github.com.mgrzeszczak.reactor;

public interface EventHandler<T> {

    void onOpen(Connection<T> connection);

    void onMessage(Connection<T> connection, T message);

    void onError(ConnectionData data, Exception error);

    void onClose(ConnectionData data);

}
