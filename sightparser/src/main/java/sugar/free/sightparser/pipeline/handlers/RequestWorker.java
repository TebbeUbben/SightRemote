package sugar.free.sightparser.pipeline.handlers;

import android.os.RemoteException;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.applayer.connection.ActivateServiceMessage;
import sugar.free.sightparser.applayer.connection.ServiceChallengeMessage;
import sugar.free.sightparser.crypto.Cryptograph;
import sugar.free.sightparser.error.DisconnectedError;
import sugar.free.sightparser.error.InvalidServicePasswordError;
import sugar.free.sightparser.handling.MessageRequest;
import sugar.free.sightparser.handling.MessageStatus;
import sugar.free.sightparser.pipeline.DuplexHandler;
import sugar.free.sightparser.pipeline.Pipeline;

public class RequestWorker implements DuplexHandler {

    private final List<MessageRequest> messageRequests = new ArrayList<>();

    @Override
    public void onInboundMessage(final Object message, Pipeline pipeline) throws Exception {
        synchronized (messageRequests) {
            if (messageRequests.size() == 0) return;
            if (message instanceof DisconnectedError) {
                for (MessageRequest messageRequest : new ArrayList<>(messageRequests)) {
                    sendError(messageRequest, (Exception) message);
                    messageRequests.remove(messageRequest);
                }
                return;
            }
            MessageRequest messageRequest = messageRequests.get(0);
            if (message instanceof Exception) {
                sendError(messageRequest, (Exception) message);
                messageRequests.remove(messageRequest);
                requestNext(pipeline);
                return;
            }
            if (messageRequest.getMessageStatus() == MessageStatus.ACTIVATING_SERVICE) {
                Service service = messageRequest.getAppLayerMessage().getService();
                if (message instanceof ServiceChallengeMessage) {
                    byte[] password = Cryptograph.getServicePasswordHash(service.getServicePassword(), ((ServiceChallengeMessage) message).getRandomData());
                    ActivateServiceMessage activateService = new ActivateServiceMessage();
                    activateService.setServicePassword(password);
                    activateService.setServiceID(service.getServiceID());
                    activateService.setVersion(service.getVersion());
                    pipeline.send(activateService);
                } else if (message instanceof ActivateServiceMessage) {
                    pipeline.getActivatedServices().add(service);
                    requestNext(pipeline);
                }
            } else if (messageRequest.getMessageStatus() == MessageStatus.PENDING && message instanceof AppLayerMessage) {
                sendMessage(messageRequest, (AppLayerMessage) message);
                messageRequests.remove(messageRequest);
                requestNext(pipeline);
            }
        }
    }

    private void sendError(MessageRequest messageRequest, Exception exception) {
        try {
            messageRequest.getMessageCallback().onError(SerializationUtils.serialize((Serializable) exception));
        } catch (RemoteException e) {
        }
    }

    private void sendMessage(MessageRequest messageRequest, AppLayerMessage message) {
        try {
            messageRequest.getMessageCallback().onMessage(SerializationUtils.serialize(message));
        } catch (RemoteException e) {
        }
    }

    private void requestNext(Pipeline pipeline) {
        if (messageRequests.size() == 0) return;
        MessageRequest messageRequest = null;
        while (messageRequests.size() != 0) {
            messageRequest = messageRequests.get(0);
            if (!messageRequest.getBinder().isBinderAlive()) {
                messageRequests.remove(messageRequest);
                if (messageRequests.size() == 0) return;
                else continue;
            } else break;
        }
        Service service = messageRequest.getAppLayerMessage().getService();
        if (!pipeline.getActivatedServices().contains(service)) {
            messageRequest.setMessageStatus(MessageStatus.ACTIVATING_SERVICE);
            if (service.getServicePassword() != null) {
                if (service.getServicePassword().length() != 16)
                    pipeline.receive(new InvalidServicePasswordError(messageRequest.getAppLayerMessage().getClass(), (short) 0x99F0));
                else {
                    ServiceChallengeMessage serviceChallenge = new ServiceChallengeMessage();
                    serviceChallenge.setServiceID(service.getServiceID());
                    serviceChallenge.setVersion(service.getVersion());
                    pipeline.send(serviceChallenge);
                }
            } else {
                ActivateServiceMessage activateService = new ActivateServiceMessage();
                activateService.setServiceID(service.getServiceID());
                activateService.setVersion(service.getVersion());
                activateService.setServicePassword(new byte[16]);
                pipeline.send(activateService);
            }
        } else {
            messageRequest.setMessageStatus(MessageStatus.PENDING);
            if (service.getServicePassword() != null && service.getServicePassword().length() != 16)
                pipeline.receive(new InvalidServicePasswordError(messageRequest.getAppLayerMessage().getClass(), (short) 0x99F0));
            else pipeline.send(messageRequest.getAppLayerMessage());
        }
    }

    @Override
    public void onOutboundMessage(Object message, Pipeline pipeline) throws Exception {
        synchronized (messageRequests) {
            if (messageRequests.size() == 0) return;
            if (message instanceof DisconnectedError) {
                for (MessageRequest messageRequest : messageRequests) {
                    sendError(messageRequest, (Exception) message);
                    messageRequests.remove(messageRequest);
                }
                return;
            }
            MessageRequest messageRequest = messageRequests.get(0);
            if (messageRequest.getMessageStatus() == MessageStatus.PENDING && message instanceof Exception)
                sendError(messageRequest, (Exception) message);
        }
    }

    public void requestMessage(Pipeline pipeline, MessageRequest messageRequest) {
        synchronized (messageRequests) {
            messageRequests.add(messageRequest);
            if (messageRequests.size() == 1) requestNext(pipeline);
        }
    }
}
