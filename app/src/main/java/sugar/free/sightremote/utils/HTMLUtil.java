package sugar.free.sightremote.utils;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import sugar.free.sightremote.SightRemote;

public class HTMLUtil {

    public static Spanned getHTML(int stringResource, Object... args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(SightRemote.getInstance().getString(stringResource, args), Html.FROM_HTML_MODE_COMPACT);
        else
            return Html.fromHtml(SightRemote.getInstance().getString(stringResource, args));
    }

}
