package sugar.free.sightparser.crypto;

import lombok.Getter;
import lombok.Setter;

public class DerivedKeys {

    @Getter
    @Setter
    byte[] incomingKey;
    @Getter
    @Setter
    byte[] outgoingKey;
}
