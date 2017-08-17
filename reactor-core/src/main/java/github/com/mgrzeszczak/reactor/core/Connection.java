package github.com.mgrzeszczak.reactor.core;

public interface Connection<T> extends ConnectionData {

    void send(T message);
    void close();

}
