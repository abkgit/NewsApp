package com.example.android.newsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class NewsAdapter extends ArrayAdapter<News> {

    /**
     * Custom Constructor
     *
     * @param context The current context. Used to inflate the layout file.
     * @param news    A List of News objects to display in a list
     */
    public NewsAdapter(Context context, ArrayList<News> news) {
        super(context, 0, news);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_list_item, parent, false);
        }

        // Find the news at the given position in the list of news articles
        News currentNews = getItem(position);

        // Set the news title in the news_title TextView
        TextView newsTitleView = (TextView) listItemView.findViewById(R.id.news_title);
        String newsTitle = currentNews.getTitle();
        newsTitleView.setText(newsTitle);

        // Set the news section in the news_section TextView
        TextView newsSectionView = (TextView) listItemView.findViewById(R.id.news_section);
        String newsSection = currentNews.getSection();
        newsSectionView.setText(newsSection);

        // Set the news publishing date in the news_published_date TextView
        TextView publishedDateTextView =
                (TextView) listItemView.findViewById(R.id.news_published_date);
        String publishedDate = currentNews.getPublicationDate();
        String formattedPublishedDate = formatDate(publishedDate);
        publishedDateTextView.setText(formattedPublishedDate);

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }

    private String formatDate(String date) {
        String finalDate = null;
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        originalFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date dateObject = originalFormat.parse(date);
            DateFormat finalFormat = new SimpleDateFormat("LLL. dd, yyyy");
            finalDate = finalFormat.format(dateObject);
        } catch (java.text.ParseException e) {
            System.out.println("Error formatting date.");
        }

        return finalDate;
    }

}
