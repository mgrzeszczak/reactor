package github.com.mgrzeszczak.reactor.core;

import java.net.SocketAddress;

public interface ConnectionData {

    long id();

    SocketAddress remoteAddress();

    SocketAddress localAddress();

}
