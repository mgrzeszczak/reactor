package github.com.mgrzeszczak.reactor.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.LinkedList;
import java.util.List;

public final class Reactor<T> {

    private final static Logger logger = LogManager.getLogger(Reactor.class);
    private final static int DEFAULT_BACKLOG = 100;
    private final static int DEFAULT_THREAD_COUNT = 8;
    private final static String KERNEL_NAME = "KERNEL-";

    private final List<Kernel<T>> kernels;
    private final int backlog;
    private final int threads;
    private final InetSocketAddress address;
    private final ServerSocketChannel channel;
    private final ProtocolFactory<T> protocolFactory;
    private final EventHandler<T> eventHandler;

    private Reactor(EventHandler<T> eventHandler, ProtocolFactory<T> protocolFactory, InetSocketAddress address, int threads, int backlog) throws IOException {
        this.checkArguments(eventHandler, protocolFactory, address, threads);
        this.eventHandler = eventHandler;
        this.protocolFactory = protocolFactory;
        this.address = address;
        this.backlog = backlog;
        this.threads = threads;
        this.channel = openChannel();
        this.kernels = createKernels();
    }

    private void checkArguments(EventHandler<T> eventHandler, ProtocolFactory<T> protocolFactory, InetSocketAddress address, int threads) {
        if (address == null) {
            throw new IllegalArgumentException("address cannot be null");
        }
        if (eventHandler == null) {
            throw new IllegalArgumentException("eventHandler cannot be null");
        }
        if (protocolFactory == null) {
            throw new IllegalArgumentException("protocolFactory cannot be null");
        }
        if (threads <= 0) {
            throw new IllegalArgumentException("expected positive threads number");
        }
    }

    private ServerSocketChannel openChannel() throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(address, backlog);
        channel.configureBlocking(false);
        return channel;
    }

    private List<Kernel<T>> createKernels() throws IOException {
        List<Kernel<T>> kernels = new LinkedList<>();
        for (int i = 0; i < threads; i++) {
            Kernel<T> kernel = createKernel(i + 1);
            kernels.add(kernel);
        }
        return kernels;
    }

    private Kernel<T> createKernel(int nr) throws IOException {
        String name = KERNEL_NAME + nr;
        return new Kernel<>(channel, eventHandler, protocolFactory, name);
    }

    public void run() throws IOException {
        kernels.forEach(Kernel::start);
        for (Kernel kernel : kernels) {
            try {
                kernel.join();
            } catch (InterruptedException e) {
                logger.warn(e, e);
            }
        }
        channel.close();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {

        private int threads = DEFAULT_THREAD_COUNT;
        private int backlog = DEFAULT_BACKLOG;
        private InetSocketAddress address = null;
        private ProtocolFactory<T> protocolFactory = null;
        private EventHandler<T> eventHandler = null;

        private Builder() {

        }

        public Builder<T> threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder<T> backlog(int backlog) {
            this.backlog = backlog;
            return this;
        }

        public Builder<T> address(InetSocketAddress address) {
            this.address = address;
            return this;
        }

        public Builder<T> eventHandler(EventHandler<T> eventHandler) {
            this.eventHandler = eventHandler;
            return this;
        }

        public Builder<T> protocolFactory(ProtocolFactory<T> protocolFactory) {
            this.protocolFactory = protocolFactory;
            return this;
        }

        public Reactor<T> build() throws IOException {
            return new Reactor<>(eventHandler, protocolFactory, address, threads, backlog);
        }

    }

}
