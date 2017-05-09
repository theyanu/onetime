package me.yanu.onetime;

import android.graphics.Canvas;

/**
 * Created by yannickpulver on 09.05.17.
 */

interface WaveformRenderer {
    void render(Canvas canvas, byte[] waveform);
}
