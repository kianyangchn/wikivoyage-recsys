package me.yang.recsys.wikivoyage.relevance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import me.yang.recsys.wikivoyage.base.Listing;
import me.yang.recsys.wikivoyage.utils.CalcMath;
import me.yang.recsys.wikivoyage.utils.Normalization;

public class Relevance {

    /**
     * total relevance score is calculated by Property score and query-doc similarity score
     * @param term
     * @param searchedTermVector
     * @param listing
     * @return
     */
    public static double calculateRelevance(String term, float[] searchedTermVector, Listing listing) {
        double propertyScore = calculatePropertyScore(listing);
        double queryDocSimilarityScore = calculateQueryDocSimilarityScore(term, searchedTermVector, listing);
        double relevanceScore = propertyScore + queryDocSimilarityScore;
        return relevanceScore;
    }

    /**
     * importance of a POI to the city
     * if author write more description of the POI and complete the geographic information, then
     * the POI is important to the city
     * @param listing
     * @return
     */
    public static double calculatePropertyScore(Listing listing) {
        int contentLen = listing.getContentLen();
        double contentScore = Normalization.minMaxNormalization(contentLen);
        double locationInfoScore = listing.getLat().length() > 0 && listing.getLng().length() > 0 ? 1 : 0;

        double propertyScore = 0.2 * contentScore + 0.1 * locationInfoScore;
        return propertyScore;
    }

    /**
     * query-doc similarity is defined by 2 things
     * semantic similarity use fasttext based semantic embedding technology to compare the similarity
     * between two text.
     * jaccard similarity measures how many words of the two text are in common, aka how similar the
     * two text look like.
     * @param term
     * @param searchedTermVector
     * @param listing
     * @return
     */
    private static double calculateQueryDocSimilarityScore(String term, float[] searchedTermVector, Listing listing) {
        double semanticSimilarityScore = calculateSemanticSimilarityScore(searchedTermVector, listing);
        double jaccardSimilarityScore = calculateJaccardSimilarityScore(term, listing);

        double queyDocSimilarityScore = 0.35 * semanticSimilarityScore + 0.35 * jaccardSimilarityScore;
        return queyDocSimilarityScore;
    }

    /**
     * semantic similarity use fasttext based semantic embedding technology to compare the similarity
     * between two text.
     * @param searchedTermVector
     * @param listing
     * @return
     */
    private static double calculateSemanticSimilarityScore(float[] searchedTermVector, Listing listing) {
        float[] listingVector = listing.getVector();
        double vectorScore = CalcMath.cosineSim(listingVector, searchedTermVector);

        return vectorScore;
    }

    /**
     * jaccard similarity measures how many words of the two text are in common, aka how similar the
     * two text look like.
     * @param term
     * @param listing
     * @return
     */
    private static double calculateJaccardSimilarityScore(String term, Listing listing) {
        Set<String> termSet = new HashSet<>(Arrays.asList(term.trim().split(" ")));
        Set<String> listingSet = new HashSet<>(Arrays.asList(listing.getName().split(" ")));
        Set<String> intersection = new HashSet<>(termSet);
        intersection.retainAll(listingSet);
        int interSize = intersection.size();
        termSet.addAll(listingSet);
        int unionSize = termSet.size();
        double jaccardSimilarityScore = (float) interSize / (float) unionSize;
        return jaccardSimilarityScore;
    }



}
