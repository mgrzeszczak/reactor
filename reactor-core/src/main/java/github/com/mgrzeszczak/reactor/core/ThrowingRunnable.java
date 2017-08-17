package github.com.mgrzeszczak.reactor.core;

@FunctionalInterface
public interface ThrowingRunnable {

    void run() throws Throwable;

}
