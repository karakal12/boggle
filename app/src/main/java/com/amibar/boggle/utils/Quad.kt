package com.amibar.boggle.utils;

import android.graphics.Path;

import androidx.annotation.ColorInt;

public record Quad(Path path, @ColorInt int color, double avgOoz) {
}
