package sugar.free.sightparser.handling;

import android.os.IBinder;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.AppLayerMessage;

public class MessageRequest {

    @Getter
    private AppLayerMessage appLayerMessage;
    @Getter
    private IMessageCallback messageCallback;
    @Getter
    @Setter
    private MessageStatus messageStatus = MessageStatus.NOT_ACTIVE;
    @Getter
    private IBinder binder;

    public MessageRequest(AppLayerMessage appLayerMessage, IMessageCallback messageCallback, IBinder binder) {
        this.appLayerMessage = appLayerMessage;
        this.messageCallback = messageCallback;
        this.binder = binder;
    }
}