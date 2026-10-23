/*
 * Decompiled with CFR 0.152.
 */
package dev.daamky;

import dev.daamky.RgbaColor;
import dev.daamky.Setting;
import java.awt.Color;

/*
 * Restored as the color picker setting
 */
public final class ColorSetting
extends Setting<Integer> {
    private float hue;
    private float saturation;
    private float brightness;
    private float pickerOpenProgress;
    private boolean pickerOpen;

    public ColorSetting(String name, int n) {
        super(name, 0xFF000000 | n);
        this.updateHsbFromArgb();
    }

    @Override
    public void setValue(Integer n) {
        super.setValue(0xFF000000 | n);
        this.updateHsbFromArgb();
    }

    private void updateHsbFromArgb() {
        int n = (Integer)this.getValue();
        float[] fArray = Color.RGBtoHSB(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, null);
        if (fArray[1] > 0.0f && fArray[2] > 0.0f) {
            this.hue = fArray[0];
        }
        if (fArray[2] > 0.0f) {
            this.saturation = fArray[1];
        }
        this.brightness = fArray[2];
    }

    public void setHsb(float f, float f2, float f3) {
        this.hue = Math.clamp(f, 0.0f, 1.0f);
        this.saturation = Math.clamp(f2, 0.0f, 1.0f);
        this.brightness = Math.clamp(f3, 0.0f, 1.0f);
        super.setValue(0xFF000000 | Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 0xFFFFFF);
    }

    public float getHue() {
        return this.hue;
    }

    public float getSaturation() {
        return this.saturation;
    }

    public float getBrightness() {
        return this.brightness;
    }

    public RgbaColor getRgbaColor() {
        return RgbaColor.fromArgb((Integer)this.getValue());
    }

    public int getArgbColor() {
        return (Integer)this.getValue();
    }

    public float getRed() {
        return (float)((Integer)this.getValue() >> 16 & 0xFF) / 255.0f;
    }

    public float getGreen() {
        return (float)((Integer)this.getValue() >> 8 & 0xFF) / 255.0f;
    }

    public float getBlue() {
        return (float)((Integer)this.getValue() & 0xFF) / 255.0f;
    }

    public String getHexColor() {
        return String.format("#%06X", (Integer)this.getValue() & 0xFFFFFF);
    }

    public boolean isPickerOpen() {
        return this.pickerOpen;
    }

    public void togglePickerOpen() {
        this.pickerOpen = !this.pickerOpen;
    }
    public void setPickerOpen(boolean bl) {
        this.pickerOpen = bl;
    }
    public void updatePickerAnimation(float f) {
        float f2 = this.pickerOpen ? 1.0f : 0.0f;
        this.pickerOpenProgress += (f2 - this.pickerOpenProgress) * (1.0f - (float)Math.exp(-9.0f * f));
        if (Math.abs(f2 - this.pickerOpenProgress) < 0.002f) {
            this.pickerOpenProgress = f2;
        }
    }

    public float getPickerOpenProgress() {
        return this.pickerOpenProgress;
    }
}

