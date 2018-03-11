package sugar.free.sightparser;

import java.math.BigDecimal;

public class Helpers {

    public static double roundDouble(double value) {
        BigDecimal bg = new BigDecimal(value);
        bg = bg.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bg.doubleValue();
    }

    public static int roundDoubleToInt(double value) {
        BigDecimal bg = new BigDecimal(value);
        bg = bg.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bg.intValue();
    }

}
