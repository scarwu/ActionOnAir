/**
 * Font Manager
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

public class FontManager {

    private static final String TAG = "AoA-" + FontManager.class.getSimpleName();

    public static final String FONTAWESOME = "fonts/fontawesome-webfont.ttf";

    public static Typeface getTypeface(Context context, String font) {
        Log.i(TAG, "Get Typeface");

        return Typeface.createFromAsset(context.getAssets(), font);
    }
}