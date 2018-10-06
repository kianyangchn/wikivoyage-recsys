package me.yang.recsys.wikivoyage.models;

import java.util.ArrayList;
import me.yang.recsys.wikivoyage.utils.CalcMath;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralVectorModel {
    private static Logger logger = LoggerFactory.getLogger(GeneralVectorModel.class);

    private int dim = 128;
    private String seperator;
    private int threadnum = 10;
    private Map<String, float[]> wordVecModel;

    /**
     * load word2vec model into memory
     * file format: word elem1 elem2 elem3 ... elem128
     * use multithread to speed up
     * @param vectorModelFile word2vec model file
     * @param dim dimension
     * @param seperator selerator of the file line
     */
    public GeneralVectorModel(String vectorModelFile, int dim, String seperator) {
        this.dim = dim;
        this.seperator = seperator;
        loadVectorModelFromFile(vectorModelFile);
    }

    private void loadVectorModelFromFile(String wordVecModelFile) {
        final Map<String, float[]> concurrentWordVecModel = new ConcurrentHashMap<>();
        try {
            logger.info(">>> Start multi threads loading: [word-vec] [" + wordVecModelFile + "] ...");
            ExecutorService executorService = Executors.newFixedThreadPool(threadnum);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(wordVecModelFile),"utf-8"));
            String line;
            while((line = reader.readLine()) != null) {
                final String finalLine = line.trim();
                executorService.execute(() -> {
                    String[] elems = finalLine.split(seperator);
                    if (elems.length == (dim + 1)) {
                        String word = elems[0];
                        float[] vector = parseVector(elems);
                        concurrentWordVecModel.put(word, vector);
                    }
                });
            }
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
        } finally {
            wordVecModel = new HashMap<>(concurrentWordVecModel);
        }
    }


    public float[] get(String word) {
        return this.wordVecModel.get(word);
    }

    /**
     * avg-sum of vector of words to build the vector of sentence
     * @param wordList
     * @return
     */
    public float[] calculateSentenceVector(List<String> wordList) {
        try {
            List<Pair<String, Float>> weightedWordList = new ArrayList<>();
            for (String word: wordList) weightedWordList.add(Pair.of(word, (float) 1));
            return calculateWeightedVector(weightedWordList);
        } catch (Exception e) {
            logger.error("calculate sentence vector error: ", e);
        }
        return null;
    }

    /**
     * @param wordList
     * @return
     */
    private float[] calculateWeightedVector(List<Pair<String, Float>> wordList) {
        try {
            int count = 0;
            float[] sentenceVec = new float[dim];
            for (Pair<String, Float> pair : wordList) {
                String word = pair.getLeft();
                float weight = pair.getRight();
                float[] vector = get(word);
                if (null != vector) {
                    ++count;
                    CalcMath.vecMul(vector, weight);
                    sentenceVec = CalcMath.vecAdd(sentenceVec, vector);
                }
            }
            if (count > 0) {
                CalcMath.vecAvg(sentenceVec, count);
                return CalcMath.vecNorm(sentenceVec);
            }
            else
                return null;
        } catch (Exception e) {
            logger.error("calculate weighted vector error: ", e);
        }
        return null;
    }


    private float[] parseVector(String[] elems) {
        float[] vector = new float[dim];
        for (int i=0;i<dim;i++) {
            vector[i] = Float.parseFloat(elems[i+1]);
        }
        return vector;
    }



}
