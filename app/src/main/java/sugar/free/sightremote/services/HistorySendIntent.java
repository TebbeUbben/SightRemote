package sugar.free.sightremote.services;

import android.content.Context;
import android.content.Intent;

import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.database.BolusProgrammed;
import sugar.free.sightremote.database.CannulaFilled;
import sugar.free.sightremote.database.EndOfTBR;
import sugar.free.sightremote.database.PumpStatusChanged;
import sugar.free.sightremote.database.TimeChanged;

/**
 * Created by jamorham on 28/01/2018.
 */

class HistorySendIntent {

    static void sendEndOfTBR(Context context, EndOfTBR endOfTBR) {
        Intent intent = new Intent();
        intent.setAction(HistoryBroadcast.ACTION_END_OF_TBR);
        intent.putExtra(HistoryBroadcast.EXTRA_DURATION, endOfTBR.getDuration());
        intent.putExtra(HistoryBroadcast.EXTRA_TBR_AMOUNT, endOfTBR.getAmount());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, endOfTBR.getEventNumber());
        intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, endOfTBR.getPump());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, endOfTBR.getDateTime());
        intent.putExtra(HistoryBroadcast.EXTRA_START_TIME, endOfTBR.getStartTime());
        context.sendBroadcast(intent);
    }

    static void sendBolusDelivered(Context context, BolusDelivered bolusDelivered) {
        Intent intent = new Intent();
        intent.setAction(HistoryBroadcast.ACTION_BOLUS_DELIVERED);
        intent.putExtra(HistoryBroadcast.EXTRA_BOLUS_ID, bolusDelivered.getBolusId());
        intent.putExtra(HistoryBroadcast.EXTRA_BOLUS_TYPE, bolusDelivered.getBolusType().toString());
        intent.putExtra(HistoryBroadcast.EXTRA_DURATION, bolusDelivered.getDuration());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, bolusDelivered.getEventNumber());
        intent.putExtra(HistoryBroadcast.EXTRA_EXTENDED_AMOUNT, bolusDelivered.getExtendedAmount());
        intent.putExtra(HistoryBroadcast.EXTRA_IMMEDIATE_AMOUNT, bolusDelivered.getImmediateAmount());
        intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, bolusDelivered.getPump());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, bolusDelivered.getDateTime());
        intent.putExtra(HistoryBroadcast.EXTRA_START_TIME, bolusDelivered.getStartTime());
        context.sendBroadcast(intent);
    }

    static void sendBolusProgrammed(Context context, BolusProgrammed bolusProgrammed) {
        Intent intent = new Intent();
        intent.setAction(HistoryBroadcast.ACTION_BOLUS_PROGRAMMED);
        intent.putExtra(HistoryBroadcast.EXTRA_BOLUS_ID, bolusProgrammed.getBolusId());
        intent.putExtra(HistoryBroadcast.EXTRA_BOLUS_TYPE, bolusProgrammed.getBolusType().toString());
        intent.putExtra(HistoryBroadcast.EXTRA_DURATION, bolusProgrammed.getDuration());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, bolusProgrammed.getEventNumber());
        intent.putExtra(HistoryBroadcast.EXTRA_EXTENDED_AMOUNT, bolusProgrammed.getExtendedAmount());
        intent.putExtra(HistoryBroadcast.EXTRA_IMMEDIATE_AMOUNT, bolusProgrammed.getImmediateAmount());
        intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, bolusProgrammed.getPump());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, bolusProgrammed.getDateTime());
        context.sendBroadcast(intent);
    }

    static void sendPumpStatusChanged(Context context, PumpStatusChanged pumpStatusChanged) {
        Intent intent = new Intent();
        intent.setAction(HistoryBroadcast.ACTION_PUMP_STATUS_CHANGED);
        intent.putExtra(HistoryBroadcast.EXTRA_OLD_STATUS, pumpStatusChanged.getOldValue().toString());
        intent.putExtra(HistoryBroadcast.EXTRA_NEW_STATUS, pumpStatusChanged.getNewValue().toString());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, pumpStatusChanged.getEventNumber());
        intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, pumpStatusChanged.getPump());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, pumpStatusChanged.getDateTime());
        context.sendBroadcast(intent);
    }

    static void sendTimeChanged(Context context, TimeChanged timeChanged) {
        Intent intent = new Intent();
        intent.setAction(HistoryBroadcast.ACTION_TIME_CHANGED);
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, timeChanged.getDateTime());
        intent.putExtra(HistoryBroadcast.EXTRA_TIME_BEFORE, timeChanged.getTimeBefore());
        intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, timeChanged.getPump());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, timeChanged.getEventNumber());
        context.sendBroadcast(intent);
    }

    static void sendCannulaFilled(Context context, CannulaFilled cannulaFilled) {
        Intent intent = new Intent();
        intent.setAction(HistoryBroadcast.ACTION_PUMP_STATUS_CHANGED);
        intent.putExtra(HistoryBroadcast.EXTRA_FILL_AMOUNT, cannulaFilled.getAmount());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, cannulaFilled.getEventNumber());
        intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, cannulaFilled.getPump());
        intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, cannulaFilled.getDateTime());
        context.sendBroadcast(intent);
    }

}
