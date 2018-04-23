package sugar.free.sightparser.pipeline;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public enum Status implements Serializable {

    EXCHANGING_KEYS,
    WAITING_FOR_CODE_CONFIRMATION,
    CODE_REJECTED,
    APP_BINDING,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    NOT_AUTHORIZED,
    INCOMPATIBLE,
    WAITING;

    @Getter
    @Setter
    private long statusTime;
    @Getter
    @Setter
    private long waitTime;



}
