package github.com.mgrzeszczak.reactor.example.protocols;

import github.com.mgrzeszczak.reactor.Protocol;
import github.com.mgrzeszczak.reactor.ProtocolFactory;

public class StringProtocolFactory implements ProtocolFactory<String> {

    @Override
    public Protocol<String> create() {
        return new StringProtocol();
    }

}
