package sugar.free.sightparser;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundingUtil {

    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Invalid decimal places");
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
