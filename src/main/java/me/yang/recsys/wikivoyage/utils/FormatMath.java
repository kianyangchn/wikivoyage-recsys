package me.yang.recsys.wikivoyage.utils;

public class FormatMath {
    public static float validnum(float d, int n) {
        float p = (float) Math.pow(10, n);
        return Math.round(d * p) / p;
    }
}
