package sugar.free.sightparser.handling;

public class HistoryBroadcast {

    //Sent to service
    public static final String ACTION_START_SYNC = "sugar.free.sightremote.services.HistorySyncService.START_SYNC";
    public static final String ACTION_START_RESYNC = "sugar.free.sightremote.services.HistorySyncService.START_RESYNC";

    //Sent from service
    public static final String ACTION_BOLUS_DELIVERED = "sugar.free.sightremote.services.HistorySyncService.BOLUS_DELIVERED";
    public static final String ACTION_PUMP_STATUS_CHANGED = "sugar.free.sightremote.services.HistorySyncService.PUMP_STATUS_CHANGED";
    public static final String ACTION_BOLUS_PROGRAMMED = "sugar.free.sightremote.services.HistorySyncService.BOLUS_PROGRAMMED";
    public static final String ACTION_END_OF_TBR = "sugar.free.sightremote.services.HistorySyncService.END_OF_TBR";
    public static final String ACTION_TIME_CHANGED = "sugar.free.sightremote.services.HistorySyncService.TIME_CHANGED";
    public static final String ACTION_CANNULA_FILLED = "sugar.free.sightremote.services.HistorySyncService.CANNULA_FILLED";
    public static final String ACTION_DAILY_TOTAL = "sugar.free.sightremote.services.HistorySyncService.DAILY_TOTAL";
    public static final String ACTION_SYNC_STARTED = "sugar.free.sightremote.services.HistorySyncService.SYNC_STARTED";
    public static final String ACTION_STILL_SYNCING = "sugar.free.sightremote.services.HistorySyncService.STILL_SYNCING";
    public static final String ACTION_SYNC_FINISHED = "sugar.free.sightremote.services.HistorySyncService.SYNC_SYNC_FINISHED";

    public static final String EXTRA_BOLUS_ID = "BOLUS_ID";
    public static final String EXTRA_BOLUS_TYPE = "BOLUS_TYPE";
    public static final String EXTRA_DURATION = "DURATION";
    public static final String EXTRA_TBR_AMOUNT = "TBR_AMOUNT";
    public static final String EXTRA_EVENT_NUMBER = "EVENT_NUMBER";
    public static final String EXTRA_EXTENDED_AMOUNT = "EXTENDED_AMOUNT";
    public static final String EXTRA_IMMEDIATE_AMOUNT = "IMMEDIATE_AMOUNT";
    public static final String EXTRA_PUMP_SERIAL_NUMBER = "PUMP_SERIAL_NUMBER";
    public static final String EXTRA_EVENT_TIME = "EVENT_TIME";
    public static final String EXTRA_START_TIME = "START_TIME";
    public static final String EXTRA_OLD_STATUS = "OLD_STATUS";
    public static final String EXTRA_NEW_STATUS = "NEW_STATUS";
    public static final String EXTRA_TIME_BEFORE = "TIME_BEFORE";
    public static final String EXTRA_FILL_AMOUNT = "FILL_AMOUNT";
    public static final String EXTRA_BASAL_TOTAL = "BASAL_TOTAL";
    public static final String EXTRA_BOLUS_TOTAL = "BOLUS_TOTAL";
    public static final String EXTRA_TOTAL_DATE = "TOTAL_DATE";
    public static final String EXTRA_RESYNC = "RESYNC";

}
