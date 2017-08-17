package github.com.mgrzeszczak.reactor.core;

public interface ProtocolFactory<T> {

    Protocol<T> create();

}
