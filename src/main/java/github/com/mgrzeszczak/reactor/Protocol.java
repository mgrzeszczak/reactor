package github.com.mgrzeszczak.reactor;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface Protocol<T> {

    void write(ByteBuffer buffer, Consumer<T> msgConsumer);
    ByteBuffer convert(T msg);

}
