package sugar.free.sightremote.adapters;

/**
 * Created by jamorham on 04/10/2017.
 *
 * Interface between preferences and view
 */

public interface PrefsView {

    boolean getbool(String name);

    void setbool(String name, boolean value);

    void togglebool(String name);
}
