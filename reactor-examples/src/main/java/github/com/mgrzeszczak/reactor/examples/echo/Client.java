package github.com.mgrzeszczak.reactor.examples.echo;

import github.com.mgrzeszczak.reactor.core.Protocol;
import github.com.mgrzeszczak.reactor.protocol.StringProtocolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {

    private static final Logger logger = LogManager.getLogger(Client.class);

    public static void main(String[] args) throws Exception {
        Protocol<String> protocol = new StringProtocolFactory().create();
        for (int i = 0; i < 100; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        logger.info("Running");
                        SocketChannel channel = SocketChannel.open();
                        logger.info("Connecting");
                        channel.connect(new InetSocketAddress(InetAddress.getLocalHost(), 8080));
                        logger.info("Connected");
                        while (true) {
                            String message = "hello world";
                            ByteBuffer serialized = protocol.serialize(message);
                            int sent = 0;
                            while (serialized.hasRemaining()) {
                                sent += channel.write(serialized);
                            }
                            logger.info("Sent {} bytes", sent);
                            logger.info("Sent {}", message);
                            ByteBuffer header = ByteBuffer.allocate(Integer.BYTES);
                            int read = 0;
                            while (read < Integer.BYTES) {
                                read += channel.read(header);
                            }
                            header.flip();
                            int length = header.getInt();
                            logger.info("length: {}", length);
                            ByteBuffer body = ByteBuffer.allocate(length);
                            read = 0;
                            while (read < length) {
                                int readNow = channel.read(body);
                                read += readNow;
                                if (readNow <= 0) {
                                    throw new RuntimeException("disconnected");
                                }
                            }
                            body.flip();
                            logger.info("Received {}", new String(body.array(), StandardCharsets.UTF_8));
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
            }.start();
        }


    }

}
