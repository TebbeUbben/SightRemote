package sugar.free.sightparser.errors;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class InvalidValuesOfTwoChannelTransmissionError extends AppError {

    private static final long serialVersionUID = 1L;

    public InvalidValuesOfTwoChannelTransmissionError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
