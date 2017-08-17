package github.com.mgrzeszczak.reactor.examples.echo;

import github.com.mgrzeszczak.reactor.core.Protocol;
import github.com.mgrzeszczak.reactor.protocol.StringProtocolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

    private static final Logger logger = LogManager.getLogger(Client.class);

    public static void main(String[] args) throws Exception {
        Protocol<String> protocol = new StringProtocolFactory().create();
        logger.info("Running");
        SocketChannel channel = SocketChannel.open();
        logger.info("Connecting");
        channel.connect(new InetSocketAddress(InetAddress.getLocalHost(), 8080));
        logger.info("Connected");

        while (true) {
            ByteBuffer convert = protocol.convert("hello world");
            System.out.println("Sending ...");
            while (convert.hasRemaining()) {
                channel.write(convert);
            }
            Thread.sleep(1000);
        }
    }

}
