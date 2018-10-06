package me.yang.recsys.wikivoyage.rec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.yang.recsys.wikivoyage.base.Listing;
import me.yang.recsys.wikivoyage.docbase.DocBase;
import me.yang.recsys.wikivoyage.models.GeneralVectorModel;
import me.yang.recsys.wikivoyage.relevance.Relevance;
import me.yang.recsys.wikivoyage.utils.MinHeap;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Recommendation Strategy
 */
public class RecForSearch {
    private static Logger logger = LoggerFactory.getLogger(RecForSearch.class);
    private static final int REC_NUM = 3;
    private static GeneralVectorModel generalVectorModel;

    public static void setGeneralVectorModel(
        GeneralVectorModel generalVectorModel) {
        RecForSearch.generalVectorModel = generalVectorModel;
    }

    /**
     * Entry point of the recommend module
     * @param searchedTerm what user search
     * @return 3 recommended POIs
     */
    public static List<String> recommend(String searchedTerm) {
        searchedTerm = searchedTerm.toLowerCase();
        float[] searchedTermVector = generalVectorModel.calculateSentenceVector(Arrays.asList(searchedTerm.split(" ")));
        // city name matched
        if (DocBase.isCity(searchedTerm))
            return recommendForCity(searchedTerm, "", searchedTermVector);
        // POI name matched
        else if (DocBase.isPoi(searchedTerm))
            return recommendForPoi(searchedTerm);
        // none of above
        else
            return recommendForOther(searchedTerm, searchedTermVector);
    }

    /**
     * Strategy when user search a city name
     * 1. recall all POIs in the city
     * 2. calculate relevance score of the city name and the POI
     * 3. get Top 3 as recommend POIs of the query
     * @param city searched term
     * @param searchedPoi if a POI is searched, then this POI name should not be recommended
     * @param searchedTermVector city semantic vector
     * @return
     */
    private static List<String> recommendForCity(String city, String searchedPoi, float[] searchedTermVector) {
        List<String> candidates = new ArrayList<>();
        try {
            // 1. recall all POIs of the city
            List<Listing> listingsInCity = DocBase.getListingsInCity(city);
            MinHeap<Listing> minHeap = new MinHeap<>(REC_NUM);
            // 2. calculate relevant score of POI and city name
            for (Listing listing : listingsInCity) {
                if (!listing.getName().equalsIgnoreCase(searchedPoi)) {
                    double relevanceScore = Relevance
                        .calculateRelevance(searchedPoi, searchedTermVector, listing);
                    minHeap.update(listing, relevanceScore);
                }
            }
            // 3. get Top 3 as result
            List<Pair<Listing, Double>> recResultList = minHeap.pollAll();
            for (Pair<Listing, Double> rec : recResultList) {
                Listing listing = rec.getLeft();
                String poi = listing.getName();
                candidates.add(poi);
            }
            return candidates;
        } catch (Exception e) {
            logger.error("", e);
        }
        return candidates;
    }

    /**
     * Strategy when user search POI name
     * 1. select the POI with highest property score as the potential(different POI may have same name)
     * high property score means the POI is important to the city
     * 2. get the city of the selected POI
     * 3. transfer to Strategy 1, as user search this city
     * 4. use the pre-calculated POI vector as searchedTermVector
     * @param poi
     * @return
     */
    private static List<String> recommendForPoi(String poi) {
        List<String> candidates = new ArrayList<>();
        try {
            List<Listing> listingsOfPoi = DocBase.getListingsOfPoi(poi);
            Listing listing = selectListingsWithSameName(listingsOfPoi);
            String city = listing.getPageTitle();
            candidates = recommendForCity(city, poi, listing.getVector());
            return candidates;
        } catch (Exception e) {
            logger.error("", e);
        }
        return candidates;
    }

    /**
     * Strategy when user neither search a city nor a POI
     * 1. if there is a city name in the searched term, transfer to Strategy 1
     * 2. else if there is a POI name in the searched term, transfer to Strategy 2
     * 3. otherwise return empty list
     * @param term
     * @param searchedTermVector
     * @return
     */
    private static List<String> recommendForOther(String term, float[] searchedTermVector) {
        List<String> candidates = new ArrayList<>();
        try {
            String matchedCity = DocBase.acRetri(term, DocBase.getCityTrie());
            if (null != matchedCity && DocBase.isCity(matchedCity)) {
                candidates = recommendForCity(matchedCity, "", searchedTermVector);
                return candidates;
            }
            String matchedPoi = DocBase.acRetri(term, DocBase.getPoiTrie());
            if (null != matchedPoi && DocBase.isPoi(matchedPoi)) {
                candidates = recommendForPoi(matchedPoi);
                return candidates;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return candidates;
    }

    private static Listing selectListingsWithSameName(List<Listing> listings) {
        if (listings.size() == 1)
            return listings.get(0);

        double maxPropertyScore = -1;
        Listing bestListing = null;
        for (Listing listing : listings) {
            double propertyScore = Relevance.calculatePropertyScore(listing);
            if (propertyScore > maxPropertyScore) {
                bestListing = listing;
                maxPropertyScore = propertyScore;
            }
        }
        return bestListing;
    }

}
