package me.yang.recsys.wikivoyage.docbase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.yang.recsys.wikivoyage.base.Listing;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control all POIs (listings)
 */
public class DocBase {
    private static Logger logger = LoggerFactory.getLogger(DocBase.class);

    private static Map<String, List<Listing>> cityListingsMap = new HashMap<>();
    private static Map<String, List<Listing>> poiListingsMap = new HashMap<>();
    private static Trie cityTrie = new Trie();
    private static Trie poiTrie = new Trie();

    /**
     * load preprocessed listings docbase in memory
     * listing should have pageId, pageTitle, category, name, alt, lat, long, content_len, vector
     * @param docBaseFile
     */
    public static void loadDocBase(String docBaseFile) {
        try {
            logger.info(">>> Start loading listings docbase >>> ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(docBaseFile), "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                JSONObject info = JSON.parseObject(line.trim());
                String pageId = info.getString("pageId").toLowerCase();
                String city = info.getString("pageTitle").toLowerCase();
                String category = info.getString("category").toLowerCase();
                String poi = info.getString("name").toLowerCase();
                String alt = info.getString("alt").toLowerCase();
                String lat = info.getString("lat");
                String lng = info.getString("long");
                String location = lat+","+lng;
                int contentLen = Integer.valueOf(info.getString("content_len"));
                String[] vectorString = info.getString("vector").split(" ");
                float[] vector = new float[128];
                for (int i=0;i<vectorString.length;i++) {
                    vector[i] = Float.valueOf(vectorString[i]);
                }
                Listing listing = new Listing(pageId, city, category, poi, alt, lat, lng, contentLen, vector);
                if (!cityListingsMap.containsKey(city)) cityListingsMap.put(city, new ArrayList<>());
                cityListingsMap.get(city).add(listing);
                cityTrie.addKeyword(city);

                // POI name can be name, alt, or even (lat, long) tuple
                if (!poiListingsMap.containsKey(poi)) poiListingsMap.put(poi, new ArrayList<>());
                if (!poiListingsMap.containsKey(alt)) poiListingsMap.put(alt, new ArrayList<>());
                if (!poiListingsMap.containsKey(location)) poiListingsMap.put(location, new ArrayList<>());
                poiListingsMap.get(poi).add(listing);
                poiListingsMap.get(alt).add(listing);
                poiListingsMap.get(location).add(listing);
                poiTrie.addKeyword(poi);
                poiTrie.addKeyword(alt);
            }
            reader.close();
            logger.info(">>> Finish loading listings docbase >>> ");
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static Trie getCityTrie() {
        return cityTrie;
    }

    public static Trie getPoiTrie() {
        return poiTrie;
    }

    public static boolean isCity(String term) {
        return cityListingsMap.containsKey(term);
    }

    public static boolean isPoi(String term) {
        return poiListingsMap.containsKey(term);
    }

    public static List<Listing> getListingsInCity(String city) {
        return cityListingsMap.get(city);
    }

    public static List<Listing> getListingsOfPoi(String poi) {
        return poiListingsMap.get(poi);
    }

    /**
     * Ac Retri to check if there are any hitted word(city name or POI name) in term
     * @param term
     * @param trie
     * @return
     */
    public static String acRetri(String term, Trie trie) {
        Collection<Emit> emits = trie.onlyWholeWords().parseText(term);
        if (null != emits && emits.size() > 0) {
            String matched = null;
            for (Emit emit : emits) {
                String keyword = emit.getKeyword();
                if (null == matched) matched = keyword;
                else if (matched.length() < keyword.length()) matched = keyword;
            }
            return matched;
        }
        return null;
    }
}
