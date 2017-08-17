# reactor
[![Build Status](https://travis-ci.org/mgrzeszczak/reactor.png)](https://travis-ci.org/mgrzeszczak/reactor)

Ultra lightweight, efficient and scalable Java TCP server with simple API.

# How to use

Starting TCP server:
```
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
        Reactor<String> reactor = Reactor.<String>builder()
                .address(address)
                .eventHandler(new EchoEventHandler())
                .protocolFactory(new StringProtocolFactory())
                .threads(10)
                .backlog(100)
                .build();
        reactor.run();
```
where:

* `address` - server's InetSocketAddress
* `eventHandler` - implementation of EventHandler interface, see an example below
* `protocolFactory` - factory of Protocol implementations, see an example in *reactor-protocol-string* module
* `threads` - number of threads  that will handle connections, each connection is always run on the same thread, default = 8
* `backlog` - server socket's backlog, default = 100
* `reactor.run()` - blocks the calling thread, runs the main loop

Example eventHandler:
```
public class EchoEventHandler implements EventHandler<String> {

    private final static Logger logger = LogManager.getLogger(EchoEventHandler.class);

    @Override
    public void onOpen(Connection<String> connection) {
        logger.info("New connection: {}", connection);
    }

    @Override
    public void onMessage(Connection<String> connection, String message) {
        logger.info("New message {} from {}", message, connection);
        connection.send(message);
    }

    @Override
    public void onClose(ConnectionData data) {
        logger.error("Connection closed {}", data);
    }

}
```

# Download

[![](https://jitpack.io/v/mgrzeszczak/reactor.svg)](https://jitpack.io/#mgrzeszczak/reactor)

To get a specific module use `com.github.mgrzeszczak.reactor` as group id, and module name as artifact id.

For example, in order to only use reactor-core in your project:

**Gradle**
```
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.mgrzeszczak.reactor:reactor-core:latest-version'
}
```

**Maven**
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
	
<dependency>
    <groupId>com.github.mgrzeszczak.reactor</groupId>
    <artifactId>reactor-core</artifactId>
    <version>latest-version</version>
</dependency>
```

# License
```
MIT License

Copyright (c) 2017 Maciej Grzeszczak

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```