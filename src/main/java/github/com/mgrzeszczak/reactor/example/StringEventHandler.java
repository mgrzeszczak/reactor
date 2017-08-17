package github.com.mgrzeszczak.reactor.example;

import github.com.mgrzeszczak.reactor.Connection;
import github.com.mgrzeszczak.reactor.ConnectionData;
import github.com.mgrzeszczak.reactor.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringEventHandler implements EventHandler<String> {

    private final static Logger logger = LogManager.getLogger(StringEventHandler.class);

    @Override
    public void onOpen(Connection<String> connection) {
        logger.info("New connection: {}", connection);
    }

    @Override
    public void onMessage(Connection<String> connection, String message) {
        logger.info("New message {} from {}", message, connection);
    }

    @Override
    public void onError(ConnectionData data, Exception error) {
        logger.error("Connection {} error {}", data, error);
    }

    @Override
    public void onClose(ConnectionData data) {
        logger.error("Connection closed {}", data);
    }

}
