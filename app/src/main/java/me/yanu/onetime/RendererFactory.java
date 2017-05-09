package me.yanu.onetime;

import android.support.annotation.ColorInt;

/**
 * Created by yannickpulver on 09.05.17.
 */

class RendererFactory {
    WaveformRenderer createSimpleWaveformRenderer(@ColorInt int foreground) {
        return SimpleWaveformRenderer.newInstance(foreground);
    }
}
