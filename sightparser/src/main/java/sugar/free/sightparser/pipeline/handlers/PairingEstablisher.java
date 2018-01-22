package sugar.free.sightparser.pipeline.handlers;

import org.spongycastle.crypto.params.RSAKeyParameters;

import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;

import sugar.free.sightparser.applayer.connection.BindMessage;
import sugar.free.sightparser.authlayer.ConnectionResponse;
import sugar.free.sightparser.authlayer.KeyRequest;
import sugar.free.sightparser.authlayer.KeyResponse;
import sugar.free.sightparser.authlayer.PairingStatus;
import sugar.free.sightparser.authlayer.VerifyConfirmRequest;
import sugar.free.sightparser.authlayer.VerifyConfirmResponse;
import sugar.free.sightparser.authlayer.VerifyDisplayRequest;
import sugar.free.sightparser.authlayer.VerifyDisplayResponse;
import sugar.free.sightparser.crypto.Cryptograph;
import sugar.free.sightparser.crypto.KeyPair;
import sugar.free.sightparser.error.DisconnectedError;
import sugar.free.sightparser.pipeline.InboundHandler;
import sugar.free.sightparser.pipeline.Pipeline;
import sugar.free.sightparser.pipeline.Status;

public class PairingEstablisher implements InboundHandler {

    private byte[] randomBytes;
    private KeyPair keyPair;
    private Timer timer = new Timer();

    @Override
    public void onInboundMessage(Object message, final Pipeline pipeline) throws Exception {
        if (message instanceof ConnectionResponse) {
            KeyRequest keyRequest = new KeyRequest();
            keyRequest.setRandomBytes(getRandomBytes());
            keyRequest.setPreMasterKey(keyToBytes(getKeyPair().getPublicKey()));
            pipeline.setStatus(Status.EXCHANGING_KEYS);
            pipeline.send(keyRequest);
        } else if (message instanceof KeyResponse) {
            KeyResponse keyResponse = (KeyResponse) message;
            pipeline.setDerivedKeys(Cryptograph.deriveKeys(Cryptograph.decryptRSA(getKeyPair().getPrivateKey(), keyResponse.getPreMasterSecret()), getRandomBytes(), keyResponse.getRandomData()));
            VerifyDisplayRequest verifyDisplayRequest = new VerifyDisplayRequest();
            pipeline.setStatus(Status.WAITING_FOR_CODE_CONFIRMATION);
            pipeline.send(verifyDisplayRequest);
        } else if (message instanceof VerifyDisplayResponse) {
            VerifyConfirmRequest verifyConfirmRequest = new VerifyConfirmRequest();
            verifyConfirmRequest.setPairingStatus(PairingStatus.CONFIRMED);
            pipeline.send(verifyConfirmRequest);
        } else if (message instanceof VerifyConfirmResponse) {
            VerifyConfirmResponse verifyConfirmResponse = (VerifyConfirmResponse) message;
            PairingStatus pairingStatus = verifyConfirmResponse.getPairingStatus();
            if (pairingStatus == PairingStatus.REJECTED) {
                pipeline.setStatus(Status.CODE_REJECTED);
                throw new DisconnectedError();
            } else if (pairingStatus == PairingStatus.CONFIRMED) {
                pipeline.setStatus(Status.APP_BINDING);
                pipeline.send(new BindMessage());
            }
            else if (pairingStatus == PairingStatus.PENDING) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        VerifyConfirmRequest verifyConfirmRequest = new VerifyConfirmRequest();
                        verifyConfirmRequest.setPairingStatus(PairingStatus.CONFIRMED);
                        pipeline.send(verifyConfirmRequest);
                    }
                }, 2000);
            }
        } else if (message instanceof BindMessage) {
            pipeline.setStatus(Status.CONNECTED);
        }
    }

    private byte[] getRandomBytes() {
        if (randomBytes == null) {
            randomBytes = new byte[28];
            new SecureRandom().nextBytes(randomBytes);
        }
        return randomBytes;
    }

    private KeyPair getKeyPair() {
        if (keyPair == null) keyPair = Cryptograph.generateRSAKey();
        return keyPair;
    }

    private static byte[] keyToBytes(RSAKeyParameters publicKey) {
        byte[] modulus = publicKey.getModulus().toByteArray();
        byte[] bytes = new byte[256];
        System.arraycopy(modulus, 1, bytes, 0, 256);
        return bytes;
    }
}
