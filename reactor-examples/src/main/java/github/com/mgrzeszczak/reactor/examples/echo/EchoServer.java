package github.com.mgrzeszczak.reactor.examples.echo;

import github.com.mgrzeszczak.reactor.core.Reactor;
import github.com.mgrzeszczak.reactor.protocol.StringProtocolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class EchoServer {

    private final static Logger logger = LogManager.getLogger(EchoServer.class);
    private final static int PORT = 8080;

    public static void main(String[] args) throws Exception {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
        Reactor<String> reactor = Reactor.<String>builder()
                .address(address)
                .eventHandler(new EchoEventHandler())
                .protocolFactory(new StringProtocolFactory())
                .threads(10)
                .build();
        reactor.run();
    }

}
