package github.com.mgrzeszczak.reactor.protocol;

import github.com.mgrzeszczak.reactor.core.Protocol;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class StringProtocol implements Protocol<String> {

    private final int INITIAL_SIZE = 10240;
    private final ByteArrayOutputStream stream;

    StringProtocol() {
        this.stream = new ByteArrayOutputStream(INITIAL_SIZE);
    }

    @Override
    public void write(ByteBuffer buffer, Consumer<String> msgConsumer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        // TODO: implement
        msgConsumer.accept("hello world");
    }

    @Override
    public ByteBuffer convert(String msg) {
        byte[] body = msg.getBytes(StandardCharsets.UTF_8);
        byte[] header = ByteBuffer.allocate(Integer.BYTES).putInt(body.length).array();
        byte[] payload = new byte[body.length + header.length];
        return ByteBuffer.wrap(payload);
    }

}
