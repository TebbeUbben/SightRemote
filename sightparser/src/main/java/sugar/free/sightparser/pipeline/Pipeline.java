package sugar.free.sightparser.pipeline;

import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import sugar.free.sightparser.DataStorage;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.messages.connection.ActivateServiceMessage;
import sugar.free.sightparser.applayer.messages.connection.BindMessage;
import sugar.free.sightparser.applayer.messages.connection.ConnectMessage;
import sugar.free.sightparser.applayer.messages.connection.DeactivateAllServicesMessage;
import sugar.free.sightparser.applayer.messages.connection.DisconnectMessage;
import sugar.free.sightparser.applayer.messages.connection.ServiceChallengeMessage;
import sugar.free.sightparser.authlayer.ConnectionRequest;
import sugar.free.sightparser.authlayer.DisconnectRequest;
import sugar.free.sightparser.authlayer.SynRequest;
import sugar.free.sightparser.crypto.DerivedKeys;
import sugar.free.sightparser.exceptions.DisconnectedException;
import sugar.free.sightparser.handling.MessageRequest;
import sugar.free.sightparser.handling.StatusCallback;
import sugar.free.sightparser.pipeline.handlers.AppLayerProcessor;
import sugar.free.sightparser.pipeline.handlers.AuthLayerProcessor;
import sugar.free.sightparser.pipeline.handlers.ByteProcessor;
import sugar.free.sightparser.pipeline.handlers.ConnectionEstablisher;
import sugar.free.sightparser.pipeline.handlers.PairingEstablisher;
import sugar.free.sightparser.pipeline.handlers.RequestWorker;

public class Pipeline {

    private StatusCallback statusCallback;

    private ByteBuf byteBuf = new ByteBuf(4096);
    private List<Handler> handlers = new ArrayList<>();

    private DataStorage dataStorage;
    @Getter
    private OutputStream outputStream;
    private InputStream inputStream;

    @Getter
    private DerivedKeys derivedKeys;
    @Getter
    private long commID = 0;

    @Getter
    private BigInteger lastNonceSent = BigInteger.ZERO;
    @Getter
    private BigInteger lastNonceReceived = null;

    @Getter
    private Status status = Status.DISCONNECTED;
    @Getter
    private List<Service> activatedServices = new ArrayList<>(Arrays.asList(Service.CONNECTION));
    private RequestWorker requestWorker = new RequestWorker();

    public Pipeline(DataStorage dataStorage, StatusCallback statusCallback) {
        this.dataStorage = dataStorage;
        this.statusCallback = statusCallback;
        if (dataStorage.contains("INCOMINGKEY") && dataStorage.contains("OUTGOINGKEY")) {
            derivedKeys = new DerivedKeys();
            derivedKeys.setIncomingKey(Hex.decode(dataStorage.get("INCOMINGKEY")));
            derivedKeys.setOutgoingKey(Hex.decode(dataStorage.get("OUTGOINGKEY")));
        }
        if (dataStorage.contains("COMMID"))
            commID = Long.parseLong(dataStorage.get("COMMID"));
        if (dataStorage.contains("LASTNONCESENT"))
            lastNonceSent = new BigInteger(Hex.decode(dataStorage.get("LASTNONCESENT")));
        if (dataStorage.contains("LASTNONCERECEIVED"))
            lastNonceReceived = new BigInteger(Hex.decode(dataStorage.get("LASTNONCERECEIVED")));
        setupPipeline();
    }

    private void setupPipeline() {
        handlers.add(new ByteProcessor(byteBuf));
        handlers.add(new AuthLayerProcessor());
        handlers.add(new AppLayerProcessor());
        handlers.add(new PairingEstablisher());
        handlers.add(new ConnectionEstablisher());
        handlers.add(requestWorker);
    }

    public void receive(Object message) {
        if (message instanceof Exception) {
            Exception exception = (Exception) message;
            Log.d("SightService", "EXCEPTION: " + exception.getClass().getName() + ": " + exception.getMessage());;
        }
        for (Handler handler : handlers) {
            if (handler instanceof InboundHandler) {
                InboundHandler inboundHandler = (InboundHandler) handler;
                try {
                    inboundHandler.onInboundMessage(message, this);
                } catch (IOException e) {
                    setStatus(Status.DISCONNECTED);
                } catch (Exception e) {
                    receive(e);
                }
            }
        }
    }

    public void send(Object message) {
        if (message instanceof Exception) {
            Exception exception = (Exception) message;
            Log.d("SightService", "EXCEPTION: " + exception.getClass().getName() + ": " + exception.getMessage());
        }
        for (Handler handler : handlers) {
            if (handler instanceof OutboundHandler) {
                OutboundHandler outboundHandler = (OutboundHandler) handler;
                try {
                    outboundHandler.onOutboundMessage(message, this);
                } catch (IOException e) {
                    setStatus(Status.DISCONNECTED);
                }  catch (Exception e) {
                    send(e);
                }
            }
        }
    }

    public void loopCall() {
        try {
            if (inputStream.available() > 0) {
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                receive(bytes);
            }
        } catch (IOException e) {
            setStatus(Status.DISCONNECTED);
        }
    }

    public void setDerivedKeys(DerivedKeys derivedKeys) {
        this.derivedKeys = derivedKeys;
        dataStorage.set("INCOMINGKEY", Hex.toHexString(derivedKeys.getIncomingKey()));
        dataStorage.set("OUTGOINGKEY", Hex.toHexString(derivedKeys.getOutgoingKey()));
    }

    public void setCommID(long commID) {
        dataStorage.set("COMMID", commID + "");
        this.commID = commID;
    }


    public void setLastNonceSent(BigInteger lastNonceSent) {
        this.lastNonceSent = lastNonceSent;
        dataStorage.set("LASTNONCESENT", Hex.toHexString(lastNonceSent.toByteArray()));
    }


    public void setLastNonceReceived(BigInteger lastNonceReceived) {
        this.lastNonceReceived = lastNonceReceived;
        dataStorage.set("LASTNONCERECEIVED", Hex.toHexString(lastNonceReceived.toByteArray()));
    }

    public void setStatus(Status status) {
        this.status = status;
        statusCallback.onStatusChange(status);
    }

    public void establishPairing() {
        send(new ConnectionRequest());
    }

    public void establishConnection() {
        send(new SynRequest());
    }

    public void disconnect() {
        if (status == Status.CONNECTED) {
            send(new DeactivateAllServicesMessage());
            activatedServices.clear();
            send(new DisconnectMessage());
            send(new DisconnectRequest());
        }
    }

    public void requestMessage(MessageRequest messageRequest) {
        AppLayerMessage message = messageRequest.getAppLayerMessage();
        if (status == Status.CONNECTED
            && !(message instanceof ActivateServiceMessage)
            && !(message instanceof BindMessage)
            && !(message instanceof ConnectMessage)
            && !(message instanceof DisconnectMessage)
            && !(message instanceof DeactivateAllServicesMessage)
            && !(message instanceof ServiceChallengeMessage))
                requestWorker.requestMessage(this, messageRequest);
    }

    public void setChannels(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
}
