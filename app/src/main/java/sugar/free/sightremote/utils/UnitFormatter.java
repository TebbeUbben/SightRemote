package sugar.free.sightremote.utils;

import java.text.DecimalFormat;

import sugar.free.sightremote.R;
import sugar.free.sightremote.SightRemote;

public class UnitFormatter {

    public static String format(float units) {
        DecimalFormat decimalFormat = new DecimalFormat("0");
        decimalFormat.setMinimumFractionDigits(1);
        decimalFormat.setMaximumFractionDigits(2);
        return SightRemote.getInstance().getString(R.string.unit_formatter, decimalFormat.format(units));
    }

    public static String formatBR(float units) {
        DecimalFormat decimalFormat = new DecimalFormat("0");
        decimalFormat.setMinimumFractionDigits(1);
        decimalFormat.setMaximumFractionDigits(2);
        return SightRemote.getInstance().getString(R.string.br_unit_formatter, decimalFormat.format(units));
    }

    public static String formatDuration(int duration) {
        int minutes = duration % 60;
        int hours = (duration - minutes) / 60;
        return SightRemote.getInstance().getString(R.string.duration_formatter, hours, minutes);
    }

}
