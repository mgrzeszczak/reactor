package github.com.mgrzeszczak.reactor.example;


import github.com.mgrzeszczak.reactor.Reactor;
import github.com.mgrzeszczak.reactor.example.protocols.StringProtocolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Server {

    private final static Logger logger = LogManager.getLogger(Server.class);
    private final static int PORT = 8080;

    public static void main(String[] args) throws Exception {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
        Reactor<String> reactor = Reactor.<String>builder()
                .address(address)
                .eventHandler(new StringEventHandler())
                .protocolFactory(new StringProtocolFactory())
                .threads(1)
                .build();
        reactor.run();
    }

}
