package github.com.mgrzeszczak.reactor.protocol;

import github.com.mgrzeszczak.reactor.core.Protocol;
import github.com.mgrzeszczak.reactor.core.ProtocolFactory;

public class StringProtocolFactory implements ProtocolFactory<String> {

    @Override
    public Protocol<String> create() {
        return new StringProtocol();
    }

}
