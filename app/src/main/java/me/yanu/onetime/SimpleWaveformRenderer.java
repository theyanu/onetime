package me.yanu.onetime;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;

/**
 * Created by yannickpulver on 09.05.17.
 */

class SimpleWaveformRenderer implements WaveformRenderer {
    private final Paint paintFill;
    private final Paint paintStroke;
    private final Path waveformPath;
    private static final int Y_FACTOR = 0xFF;
    private static final float HALF_FACTOR = 0.5f;

    static SimpleWaveformRenderer newInstance(@ColorInt int foregroundColour) {
        Paint paintFill = new Paint();
        paintFill.setColor(foregroundColour);
        paintFill.setAlpha(40);
        paintFill.setAntiAlias(true);
        paintFill.setStyle(Paint.Style.FILL);

        Paint paintStroke = new Paint();
        paintStroke.setColor(foregroundColour);
        paintStroke.setAlpha(80);
        paintStroke.setAntiAlias(true);
        paintStroke.setStyle(Paint.Style.STROKE);

        Path waveformPath = new Path();
        return new SimpleWaveformRenderer(paintFill, paintStroke, waveformPath);
    }

    private SimpleWaveformRenderer(Paint paintFill, Paint paintStroke, Path waveformPath) {
        this.paintFill = paintFill;
        this.paintStroke = paintStroke;
        this.waveformPath = waveformPath;
    }

    @Override
    public void render(Canvas canvas, byte[] waveform) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        waveformPath.reset();
        if (waveform != null) {
            renderWaveform(waveform, width, height);
        }
        canvas.drawPath(waveformPath, paintFill);
        canvas.drawPath(waveformPath, paintStroke);

    }

    private void renderWaveform(byte[] waveform, float width, float height) {
        float xIncrement = width / (float) (waveform.length);
        float yIncrement = height / Y_FACTOR;
        int halfHeight = (int) (height * HALF_FACTOR);
        waveformPath.moveTo(0, halfHeight);
        for (int i = 1; i < waveform.length; i++) {
            float yPosition = waveform[i] > 0 ? height - (yIncrement * waveform[i]) : -(yIncrement * waveform[i]);
            waveformPath.lineTo(xIncrement * i, yPosition);
        }
        waveformPath.lineTo(width, halfHeight);
        waveformPath.lineTo(width, height);
        waveformPath.lineTo(0, height);
        waveformPath.lineTo(0, halfHeight);

    }

}
