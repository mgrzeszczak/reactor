package github.com.mgrzeszczak.reactor.protocol;

import github.com.mgrzeszczak.reactor.core.Protocol;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class StringProtocol implements Protocol<String> {

    private final static int INITIAL_SIZE = 10240;

    private final ByteArrayOutputStream stream;
    private Integer length;

    StringProtocol() {
        this.stream = new ByteArrayOutputStream(INITIAL_SIZE);
        this.length = null;
    }

    @Override
    public void write(ByteBuffer buffer, Consumer<String> msgConsumer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        stream.write(data, 0, data.length);
        if (length == null && stream.size() >= Integer.BYTES) {
            length = ByteBuffer.wrap(take(Integer.BYTES)).getInt();
        }
        if (length != null && stream.size() >= length) {
            String message = new String(take(length), StandardCharsets.UTF_8);
            msgConsumer.accept(message);
            length = null;
        }
    }

    private byte[] take(int size) {
        byte[] bytes = stream.toByteArray();
        byte[] data = new byte[size];
        System.arraycopy(bytes, 0, data, 0, size);
        stream.reset();
        stream.write(bytes, size, bytes.length - size);
        return data;
    }

    @Override
    public ByteBuffer serialize(String msg) {
        byte[] body = msg.getBytes(StandardCharsets.UTF_8);
        byte[] header = ByteBuffer.allocate(Integer.BYTES).putInt(body.length).array();
        byte[] payload = new byte[body.length + header.length];
        System.arraycopy(header, 0, payload, 0, header.length);
        System.arraycopy(body, 0, payload, header.length, body.length);
        return ByteBuffer.wrap(payload);
    }

}
