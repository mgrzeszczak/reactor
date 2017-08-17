package github.com.mgrzeszczak.reactor.examples.echo;

import github.com.mgrzeszczak.reactor.core.Connection;
import github.com.mgrzeszczak.reactor.core.ConnectionData;
import github.com.mgrzeszczak.reactor.core.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EchoEventHandler implements EventHandler<String> {

    private final static Logger logger = LogManager.getLogger(EchoEventHandler.class);

    @Override
    public void onOpen(Connection<String> connection) {
        logger.info("New connection: {}", connection);
    }

    @Override
    public void onMessage(Connection<String> connection, String message) {
        logger.info("New message {} from {}", message, connection);
        connection.send("response valid");
    }

    @Override
    public void onClose(ConnectionData data) {
        logger.error("Connection closed {}", data);
    }

}
