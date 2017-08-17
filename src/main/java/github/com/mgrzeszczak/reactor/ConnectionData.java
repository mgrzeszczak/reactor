package github.com.mgrzeszczak.reactor;

import java.net.SocketAddress;

public interface ConnectionData {

    long id();

    SocketAddress remoteAddress();

    SocketAddress localAddress();

}
