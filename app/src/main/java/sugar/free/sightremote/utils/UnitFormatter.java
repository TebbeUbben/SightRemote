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

}
