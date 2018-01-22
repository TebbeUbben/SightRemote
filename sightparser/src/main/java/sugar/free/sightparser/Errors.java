package sugar.free.sightparser;

import java.util.HashMap;
import java.util.Map;

import sugar.free.sightparser.error.BolusDurationLimitExceededError;
import sugar.free.sightparser.error.BolusAmountLimitExceededError;
import sugar.free.sightparser.error.NotAvailableError;
import sugar.free.sightparser.error.PumpAlreadyInThatStateError;
import sugar.free.sightparser.error.SightError;

public class Errors {

    public static final Map<Short, Class<? extends SightError>> ERRORS = new HashMap<>();

    static {
        ERRORS.put((short) 0x6A0C, NotAvailableError.class);
        ERRORS.put((short) 0x8117, BolusAmountLimitExceededError.class);
        ERRORS.put((short) 0x7E17, BolusDurationLimitExceededError.class);
        ERRORS.put((short) 0xFC0C, PumpAlreadyInThatStateError.class);
    }

}
