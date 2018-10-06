package me.yang.recsys.wikivoyage.base;

/**
 * Listing Object
 */
public class Listing {
    private String pageId;
    private String pageTitle;
    private String category;
    private String name;
    private String alt;
    private String lat;
    private String lng;
    private int contentLen; // how many words are there in content
    private float[] vector; // fasttext based semantic embedding vector

    public Listing(String pageId, String pageTitle, String category, String name, String alt,
        String lat, String lng, int contentLen, float[] vector) {
        this.pageId = pageId;
        this.pageTitle = pageTitle;
        this.category = category;
        this.name = name;
        this.alt = alt;
        this.lat = lat;
        this.lng = lng;
        this.contentLen = contentLen;
        this.vector = vector;
    }

    public String getPageId() {
        return pageId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getAlt() {
        return alt;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public int getContentLen() {
        return contentLen;
    }

    public float[] getVector() {
        return vector;
    }
}
