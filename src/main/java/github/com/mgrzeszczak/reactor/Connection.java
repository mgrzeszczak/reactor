package github.com.mgrzeszczak.reactor;

public interface Connection<T> extends ConnectionData {

    void send(T message);
    void close();

}
