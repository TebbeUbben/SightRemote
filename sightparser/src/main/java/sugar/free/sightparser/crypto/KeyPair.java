package sugar.free.sightparser.crypto;

import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.params.RSAPrivateCrtKeyParameters;

import lombok.Getter;

public class KeyPair {

    protected KeyPair() {
    }

    @Getter
    RSAPrivateCrtKeyParameters privateKey;
    @Getter
    RSAKeyParameters publicKey;
}
