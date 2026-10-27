package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.utils.ResUtil;

public class ColorPickerView extends View {

    public interface Listener {

        void onColorChanged(int color);
    }

    private final RectF panel = new RectF();
    private final RectF hueBar = new RectF();
    private final Path clip = new Path();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float radius;
    private final float thumbRadius;
    private final int hueHeight;
    private final int gap;
    private Bitmap panelBitmap;
    private Listener listener;
    private float hue;
    private float saturation = 1f;
    private float value = 1f;
    private float panelHue = -1f;

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        radius = ResUtil.dp2px(8);
        thumbRadius = ResUtil.dp2px(8);
        hueHeight = ResUtil.dp2px(16);
        gap = ResUtil.dp2px(16);
        int pad = ResUtil.dp2px(10);
        setPadding(pad, pad, pad, pad);
        thumbFill.setStyle(Paint.Style.FILL);
        thumbFill.setColor(Color.WHITE);
        thumbStroke.setStyle(Paint.Style.STROKE);
        thumbStroke.setStrokeWidth(ResUtil.dp2px(2));
        thumbStroke.setColor(Color.BLACK);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (hsv[1] > 0f) hue = hsv[0];
        saturation = hsv[1];
        value = hsv[2];
        panelHue = -1f;
        invalidate();
    }

    public int getColor() {
        return Color.HSVToColor(new float[]{hue, saturation, value});
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recyclePanel();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        layoutRects();
        if (panel.width() <= 0 || panel.height() <= 0) return;
        drawPanel(canvas);
        drawHue(canvas);
        drawThumb(canvas, panel.left + saturation * panel.width(), panel.top + (1f - value) * panel.height());
        drawThumb(canvas, hueBar.left + hue / 360f * hueBar.width(), hueBar.centerY());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN && getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) return action == MotionEvent.ACTION_UP;
        layoutRects();
        if (panel.width() <= 0f || hueBar.width() <= 0f) return true;
        float x = event.getX();
        float y = event.getY();
        if (y < hueBar.top - gap / 2f) movePanel(x, y);
        else moveHue(x);
        if (listener != null) listener.onColorChanged(getColor());
        invalidate();
        return true;
    }

    private void layoutRects() {
        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = getWidth() - getPaddingRight();
        float bottom = getHeight() - getPaddingBottom();
        panel.set(left, top, right, bottom - hueHeight - gap);
        hueBar.set(left, bottom - hueHeight, right, bottom);
    }

    private void drawPanel(Canvas canvas) {
        ensurePanel();
        canvas.save();
        clip.reset();
        clip.addRoundRect(panel, radius, radius, Path.Direction.CW);
        canvas.clipPath(clip);
        canvas.drawBitmap(panelBitmap, panel.left, panel.top, null);
        canvas.restore();
    }

    private void drawHue(Canvas canvas) {
        int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};
        paint.setShader(new LinearGradient(hueBar.left, hueBar.centerY(), hueBar.right, hueBar.centerY(), colors, null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(hueBar, hueBar.height() / 2f, hueBar.height() / 2f, paint);
        paint.setShader(null);
    }

    private void drawThumb(Canvas canvas, float x, float y) {
        canvas.drawCircle(x, y, thumbRadius, thumbFill);
        canvas.drawCircle(x, y, thumbRadius, thumbStroke);
    }

    private void ensurePanel() {
        int width = Math.max(1, (int) panel.width());
        int height = Math.max(1, (int) panel.height());
        if (panelBitmap != null && !panelBitmap.isRecycled() && panelBitmap.getWidth() == width && panelBitmap.getHeight() == height && panelHue == hue) return;
        recyclePanel();
        panelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas bitmapCanvas = new Canvas(panelBitmap);
        int hueColor = Color.HSVToColor(new float[]{hue, 1f, 1f});
        paint.setShader(new LinearGradient(0, 0, width, 0, Color.WHITE, hueColor, Shader.TileMode.CLAMP));
        bitmapCanvas.drawRect(0, 0, width, height, paint);
        paint.setShader(new LinearGradient(0, 0, 0, height, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP));
        bitmapCanvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
        panelHue = hue;
    }

    private void movePanel(float x, float y) {
        saturation = clamp((x - panel.left) / panel.width());
        value = 1f - clamp((y - panel.top) / panel.height());
    }

    private void moveHue(float x) {
        hue = clamp((x - hueBar.left) / hueBar.width()) * 360f;
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private void recyclePanel() {
        if (panelBitmap == null) return;
        panelBitmap.recycle();
        panelBitmap = null;
    }
}
