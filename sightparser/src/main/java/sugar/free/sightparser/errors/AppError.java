package sugar.free.sightparser.errors;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;

import java.util.HashMap;
import java.util.Map;

public abstract class AppError extends Exception {

    public static final Map<Short, Class<? extends AppError>> ERRORS = new HashMap<>();

    @Getter
    private Class<? extends AppLayerMessage> clazz;
    @Getter
    private short error;

    public AppError(Class<? extends AppLayerMessage> clazz, short error) {
        this.clazz = clazz;
        this.error = error;
    }

    @Override
    public String getMessage() {
        return "Class: " + clazz.getCanonicalName() + " Error: " + error;
    }

    static {
        ERRORS.put((short) 0x6A0C, PumpStoppedError.class);
        ERRORS.put((short) 0x8117, BolusAmountNotInRangeError.class);
        ERRORS.put((short) 0xFC0C, PumpAlreadyInThatStateError.class);
        ERRORS.put((short) 0x99F0, InvalidServicePasswordError.class);
        ERRORS.put((short) 0x0FF0, UnknownCommandError.class);
        ERRORS.put((short) 0x5AF0, AlreadyConnectedError.class);
        ERRORS.put((short) 0xF0F0, WrongStateError.class);
        ERRORS.put((short) 0x66F0, ServiceIncompatibleError.class);
        ERRORS.put((short) 0x69F0, UnknownServiceError.class);
        ERRORS.put((short) 0xFFF0, NoServicePasswordNeededError.class);
        ERRORS.put((short) 0xCCF0, ServiceAlreadyActivatedError.class);
        ERRORS.put((short) 0x33F0, IncompatibleVersionError.class);
        ERRORS.put((short) 0x3CF0, InvalidPayloadLengthError.class);
        ERRORS.put((short) 0x55F0, NotConnectedError.class);
        ERRORS.put((short) 0xA5F0, ServiceCommandNotAvailableError.class);
        ERRORS.put((short) 0xAAF0, ServiceNotActivatedError.class);
        ERRORS.put((short) 0xC3F0, PumpBusyError.class);
        ERRORS.put((short) 0xD714, NotReferencedError.class);
        ERRORS.put((short) 0xE414, StepCountOutOfRangeError.class);
        ERRORS.put((short) 0xF50A, InvalidPayloadCRCError.class);
        ERRORS.put((short) 0xFA0A, InvalidParameterTypeError.class);
        ERRORS.put((short) 0x0C59, CommandExecutionFailedError.class);
        ERRORS.put((short) 0xA60C, InvalidAlertInstanceIDError.class);
        ERRORS.put((short) 0xA90C, InvalidTBRFactorError.class);
        ERRORS.put((short) 0xC00C, InvalidTBRDurationError.class);
        ERRORS.put((short) 0xDB18, InvalidTBRTemplateError.class);
        ERRORS.put((short) 0xF30C, PauseModeNotAllowedError.class);
        ERRORS.put((short) 0xCF0C, RunModeNotAllowedError.class);
        ERRORS.put((short) 0x000F, NoActiveTBRToCancelError.class);
        ERRORS.put((short) 0xB218, NoActiveTBRToChangeError.class);
        ERRORS.put((short) 0x550F, BolusTypeAndParameterMismatchError.class);
        ERRORS.put((short) 0x2417, InvalidDurationPresetError.class);
        ERRORS.put((short) 0x5A00, BolusLagTimeFeatureDisabledError.class);
        ERRORS.put((short) 0x7E17, BolusDurationNotInRangeError.class);
        ERRORS.put((short) 0x960F, InvalidValuesOfTwoChannelTransmissionError.class);
        ERRORS.put((short) 0xA50F, BolusNotFoundToCancelError.class);
        ERRORS.put((short) 0xAA0F, MaximumNumberOfBolusTypeAlreadyRunningError.class);
        ERRORS.put((short) 0x7E18, CustomBolusNotConfiguredError.class);
        ERRORS.put((short) 0xCC0F, InvalidDateParameterError.class);
        ERRORS.put((short) 0xF00F, InvalidTimeParameterError.class);
        ERRORS.put((short) 0x7711, NoConfigBlockDataError.class);
        ERRORS.put((short) 0x7811, InvalidConfigBlockIDError.class);
        ERRORS.put((short) 0x8711, InvalidConfigBlockCRCError.class);
        ERRORS.put((short) 0x8E18, InvalidConfigBlockLengthError.class);
        ERRORS.put((short) 0xBB11, WriteSessionAlreadyOpenError.class);
        ERRORS.put((short) 0xD211, WriteSessionClosedError.class);
        ERRORS.put((short) 0xDD11, ConfigMemoryAccessError.class);
        ERRORS.put((short) 0x2E12, ReadingHistoryAlreadyStartedError.class);
        ERRORS.put((short) 0x4812, ReadingHistoryNotStartedError.class);
        ERRORS.put((short) 0x4218, InvalidPayloadError.class);
        ERRORS.put((short) 0xB812, ImplausiblePortionLengthValueError.class);
        ERRORS.put((short) 0xDE12, NotAllowedToAccessPositionZeroError.class);
        ERRORS.put((short) 0xED12, PositionProtectedError.class);
    }

}
