package com.example.android.newsapp;

/**
 * Class to define the information related to a particular news article.
 */
public class News {

    private String mTitle;

    private String mSection;

    private String mPublicationDate;

    private String mArticleUrl;

    /**
     * Constructs a new {@link News} object.
     *
     * @param title           is the News article's title.
     * @param section         is the category section for the news article.
     * @param publicationDate is the date the article was published on.
     * @param articleUrl      is the website URL for the news article.
     */
    public News(String title, String section, String publicationDate, String articleUrl) {
        mTitle = title;
        mSection = section;
        mPublicationDate = publicationDate;
        mArticleUrl = articleUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSection() {
        return mSection;
    }

    public String getPublicationDate() {
        return mPublicationDate;
    }

    public String getArticleUrl() {
        return mArticleUrl;
    }
}
