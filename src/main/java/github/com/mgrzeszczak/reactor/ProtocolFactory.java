package github.com.mgrzeszczak.reactor;

public interface ProtocolFactory<T> {

    Protocol<T> create();

}
