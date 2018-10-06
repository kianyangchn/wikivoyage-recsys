package me.yang.recsys.wikivoyage.utils;

public class CalcMath {

    /**
     * cosine similarity between vectors
     * @param vec1
     * @param vec2
     * @return
     */
    public static double cosineSim(float[] vec1, float[] vec2){
        if (null == vec1 || null == vec2 || (vec1.length != vec2.length))
            return 0;
        double fraction = 0.0;

        double s1 = 0.0;
        double s2 = 0.0;
        for (int i=0; i<vec1.length; i++){
            fraction += vec1[i] * vec2[i];
            s1 += vec1[i]*vec1[i];
            s2 += vec2[i]*vec2[i];
        }

        double denominator = Math.sqrt(s1*s2);

        if(0 == denominator){
            return 0;
        }else {
            return fraction/denominator;
        }
    }

    /**
     * a * vector
     * @param vector
     * @param weight
     */
    public static void vecMul(float[] vector, float weight) {
        if (null == vector)
        for (int i=0;i<vector.length;i++) vector[i] = FormatMath.validnum(weight * vector[i], 3);
    }

    /**
     * vec1 + vec2
     * @param vec1
     * @param vec2
     * @return
     */
    public static float[] vecAdd(float[] vec1, float[] vec2) {
        if (null == vec1 && null == vec2) return null;
        if (null == vec1) return vec2;
        if (null == vec2) return vec1;
        int dim = vec1.length;
        float[] vec = new float[dim];
        for (int i = 0; i<dim; i++) vec[i] = vec1[i]+vec2[i];
        return vec;
    }

    /**
     * normalization of vector
     * @param vec
     * @return
     */
    public static float[] vecNorm(float[] vec) {
        if (vec == null) return null;
        double norm = 0;
        for (int i=0;i<vec.length;i++) norm += (vec[i] * vec[i]);
        norm = Math.sqrt(norm);
        if (norm==0) return vec;
        else {
            for (int i=0;i<vec.length;i++) vec[i] = (float) (vec[i]/norm);
            return vec;
        }
    }


    /**
     * vector / count
     * @param vec
     * @param count
     */
    public static void vecAvg(float[] vec, int count) {
        if (null != vec && count > 0) {
            float weight = 1/count;
            vecMul(vec, weight);
        }
    }
}
