package me.yang.recsys.wikivoyage.utils;

public class Normalization {
    private static int min = 1; // min content length in all listings
    private static int max = 1006; // max content length in all listings

    /**
     * min max normalization for content length
     * @param contentLen
     * @return
     */
    public static double minMaxNormalization(int contentLen) {
        return (float)(contentLen - min) / (float)(max - min);
    }

}
