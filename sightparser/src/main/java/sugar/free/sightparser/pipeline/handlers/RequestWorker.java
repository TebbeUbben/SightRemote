package sugar.free.sightparser.pipeline.handlers;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.messages.connection.ActivateServiceMessage;
import sugar.free.sightparser.applayer.messages.connection.ServiceChallengeMessage;
import sugar.free.sightparser.crypto.Cryptograph;
import sugar.free.sightparser.exceptions.DisconnectedException;
import sugar.free.sightparser.errors.InvalidServicePasswordError;
import sugar.free.sightparser.handling.MessageRequest;
import sugar.free.sightparser.handling.MessageStatus;
import sugar.free.sightparser.pipeline.DuplexHandler;
import sugar.free.sightparser.pipeline.Pipeline;

public class RequestWorker implements DuplexHandler {

    private final List<MessageRequest> messageRequests = new ArrayList<>();
    private MessageRequest requested = null;

    @Override
    public void onInboundMessage(final Object message, Pipeline pipeline) throws Exception {
        synchronized (messageRequests) {
            if (requested == null) return;
            if (message instanceof DisconnectedException) {
                for (MessageRequest messageRequest : new ArrayList<>(messageRequests)) {
                    sendError(messageRequest, (Exception) message);
                    requested = null;
                }
                return;
            }
            if (message instanceof Exception) {
                sendError(requested, (Exception) message);
                requested = null;
                requestNext(pipeline);
                return;
            }
            if (requested.getMessageStatus() == MessageStatus.ACTIVATING_SERVICE) {
                Service service = requested.getAppLayerMessage().getService();
                if (message instanceof ServiceChallengeMessage) {
                    byte[] password = Cryptograph.getServicePasswordHash(service.getServicePassword(), ((ServiceChallengeMessage) message).getRandomData());
                    ActivateServiceMessage activateService = new ActivateServiceMessage();
                    activateService.setServicePassword(password);
                    activateService.setServiceID(service.getServiceID());
                    activateService.setVersion(service.getVersion());
                    pipeline.send(activateService);
                } else if (message instanceof ActivateServiceMessage) {
                    pipeline.getActivatedServices().add(service);
                    requested.setMessageStatus(MessageStatus.PENDING);
                    pipeline.send(requested.getAppLayerMessage());
                }
            } else if (requested.getMessageStatus() == MessageStatus.PENDING && message instanceof AppLayerMessage) {
                sendMessage(requested, (AppLayerMessage) message);
                requested = null;
                requestNext(pipeline);
            }
        }
    }

    private void sendError(MessageRequest messageRequest, Exception exception) {
        try {
            messageRequest.getMessageCallback().onError(SerializationUtils.serialize((Serializable) exception));
        } catch (Exception e) {
        }
    }

    private void sendMessage(MessageRequest messageRequest, AppLayerMessage message) {
        try {
            messageRequest.getMessageCallback().onMessage(SerializationUtils.serialize(message));
        } catch (Exception e) {
        }
    }

    private void requestNext(Pipeline pipeline) {
        if (messageRequests.size() == 0) return;
        requested = null;
        while (messageRequests.size() != 0) {
            requested = messageRequests.get(0);
            if (!requested.getBinder().isBinderAlive()) {
                messageRequests.remove(requested);
                if (messageRequests.size() == 0) return;
                else continue;
            } else break;
        }
        messageRequests.remove(requested);
        Service service = requested.getAppLayerMessage().getService();
        if (!pipeline.getActivatedServices().contains(service)) {
            requested.setMessageStatus(MessageStatus.ACTIVATING_SERVICE);
            if (service.getServicePassword() != null) {
                if (service.getServicePassword().length() != 16)
                    pipeline.receive(new InvalidServicePasswordError(requested.getAppLayerMessage().getClass(), (short) 0x99F0));
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
            requested.setMessageStatus(MessageStatus.PENDING);
            if (service.getServicePassword() != null && service.getServicePassword().length() != 16)
                pipeline.receive(new InvalidServicePasswordError(requested.getAppLayerMessage().getClass(), (short) 0x99F0));
            else pipeline.send(requested.getAppLayerMessage());
        }
    }

    @Override
    public void onOutboundMessage(Object message, Pipeline pipeline) throws Exception {
        synchronized (messageRequests) {
            if (messageRequests.size() == 0) return;
            if (message instanceof DisconnectedException) {
                for (MessageRequest messageRequest : messageRequests) {
                    sendError(messageRequest, (Exception) message);
                    messageRequests.remove(messageRequest);
                }
            }
            if (requested.getMessageStatus() == MessageStatus.PENDING && message instanceof Exception)
                sendError(requested, (Exception) message);
        }
    }

    public void requestMessage(Pipeline pipeline, MessageRequest messageRequest) {
        synchronized (messageRequests) {
            messageRequests.add(messageRequest);
            Collections.sort(messageRequests, (o1, o2) -> o1.getAppLayerMessage().getMessagePriority().getValue() - o2.getAppLayerMessage().getMessagePriority().getValue());
            if (requested == null) {
                requestNext(pipeline);
            }
        }
    }
}
