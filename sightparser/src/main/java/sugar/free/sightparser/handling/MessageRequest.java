package sugar.free.sightparser.handling;

import java.io.Serializable;

import sugar.free.sightparser.applayer.AppLayerMessage;

public class MessageRequest {

    private AppLayerMessage appLayerMessage;
    private IMessageCallback messageCallback;
    private MessageStatus messageStatus = MessageStatus.NOT_ACTIVE;

    public MessageRequest(AppLayerMessage appLayerMessage, IMessageCallback messageCallback) {
        this.appLayerMessage = appLayerMessage;
        this.messageCallback = messageCallback;
    }

    public MessageRequest(Serializable deserialize, MessageCallback messageCallback) {

    }

    public AppLayerMessage getAppLayerMessage() {
        return appLayerMessage;
    }

    public IMessageCallback getMessageCallback() {
        return messageCallback;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }
}