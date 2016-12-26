package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;


public class NewsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<News>> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = NewsActivity.class.getName();

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    /**
     * Constant value for the news loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;

    /**
     * Stores the user-selected date range option.
     */
    private String mSelectedStartDateOption = "Today";

    /**
     * Segments of the URL for retrieving news data from the server
     * LIMIT = 10 results because of the "test" API key
     * Sorting by "oldest" to show difference in selected date
     */
    private static final String NEWS_REQUEST_URL_START =
            "https://content.guardianapis.com/search?section=world&&order-by=oldest&format=json&api-key=test&from-date=";
    private String mDateForUrl = null;

    /**
     * URL for the news query based on user selected start date button
     */
    private String mNewsRequestUrl = null;

    /**
     * Adapter for the list of news articles
     */
    private NewsAdapter mAdapter;

    /**
     * Color values for button background based on their selection status
     */
    private static final int SELECTED_BUTTON_COLOR = Color.parseColor("#fdd835");
    private static final int NOT_SELECTED_BUTTON_COLOR = Color.parseColor("#fff59d");

    /**
     * Resource ID for the start date button selected
     */
    int mSelectedButtonResourceIdValue = 0;

    /**
     * Represents if the activity is starting for the first time
     */
    boolean mFirstStart = true;

    /**
     * Tags to be used to save activity state
     */
    private static final String SELECTED_START_DATE_OPTION = "theSelectedStartDateOption";
    private static final String SELECTED_BUTTON_RESOURCE_ID = "theSelectedButton";
    private static final String IS_FIRST_START = "isItFirstAppStart";

    /**
     * Saves the selected button's resource ID and handle activity restarts due to events
     * such as screen rotation
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the user's current news start date button selection
        outState.putString(SELECTED_START_DATE_OPTION, mSelectedStartDateOption);
        outState.putInt(SELECTED_BUTTON_RESOURCE_ID, mSelectedButtonResourceIdValue);
        outState.putBoolean(IS_FIRST_START, mFirstStart);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list);

        // Set the empty view
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of news as input
        mAdapter = new NewsAdapter(NewsActivity.this, new ArrayList<News>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        // Restore default color for all buttons
        setButtonDefaultColor();

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            mSelectedStartDateOption = savedInstanceState.getString(SELECTED_START_DATE_OPTION);
            mSelectedButtonResourceIdValue = savedInstanceState.getInt(SELECTED_BUTTON_RESOURCE_ID);
            mFirstStart = savedInstanceState.getBoolean(IS_FIRST_START);
        } else {
            // Initialize members with default values for a new instance
            mSelectedStartDateOption = getResources().getString(R.string.today);
            mSelectedButtonResourceIdValue = R.id.button_today;
            mFirstStart = true;
        }

        // Change selected button's color to active
        findViewById(mSelectedButtonResourceIdValue).setBackgroundColor(SELECTED_BUTTON_COLOR);

        // Generate the request URL for application startup query using "Today" as default
        // date option
        makeServerRequestUrl();

        // Method call to perform background network tasks using Loader
        runLoaderIfNetworkAvailable();

        // Find a reference to the Today {@link Button} in the layout
        Button todayButton = (Button) findViewById(R.id.button_today);
        // Find a reference to the Week {@link Button} in the layout
        Button weekButton = (Button) findViewById(R.id.button_week);
        // Find a reference to the Month {@link Button} in the layout
        Button monthButton = (Button) findViewById(R.id.button_month);
        // Find a reference to the Year {@link Button} in the layout
        Button yearButton = (Button) findViewById(R.id.button_year);

        // Find reference to the loading indicator
        final View loadingIndicator = findViewById(R.id.loading_indicator);

        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingIndicator.setVisibility(View.VISIBLE);
                functionsOnButtonClick(getApplicationContext().getString(R.string.today));
            }
        });

        weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingIndicator.setVisibility(View.VISIBLE);
                functionsOnButtonClick(getApplicationContext().getString(R.string.week));
            }
        });

        monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingIndicator.setVisibility(View.VISIBLE);
                functionsOnButtonClick(getApplicationContext().getString(R.string.month));
            }
        });

        yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingIndicator.setVisibility(View.VISIBLE);
                functionsOnButtonClick(getApplicationContext().getString(R.string.year));
            }
        });

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news article.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current news article that was clicked on
                News currentNews = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getArticleUrl());

                // Create a new intent to view the news article URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    private void functionsOnButtonClick(String selectedStartDateOption) {
        // Update relevant variables
        mSelectedStartDateOption = selectedStartDateOption;
        switch (selectedStartDateOption) {
            case "Today":
                mSelectedButtonResourceIdValue = R.id.button_today;
                break;
            case "Week":
                mSelectedButtonResourceIdValue = R.id.button_week;
                break;
            case "Month":
                mSelectedButtonResourceIdValue = R.id.button_month;
                break;
            case "Year":
                mSelectedButtonResourceIdValue = R.id.button_year;
                break;
        }

        setButtonDefaultColor();
        // Set button color to selected button's color value
        findViewById(mSelectedButtonResourceIdValue).setBackgroundColor(SELECTED_BUTTON_COLOR);

        makeServerRequestUrl();

        runLoaderIfNetworkAvailable();
    }

    private void runLoaderIfNetworkAvailable() {

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager mConnMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo mNetworkInfo = mConnMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (mNetworkInfo != null && mNetworkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Restart the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            // Get the user typed text from the EditText view
            if (mFirstStart) {
                mFirstStart = false;
                loaderManager.initLoader(NEWS_LOADER_ID, null, NewsActivity.this);
            } else {
                loaderManager.restartLoader(NEWS_LOADER_ID, null, NewsActivity.this);
            }
        } else {
            //Hide the progress indicator
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(GONE);
            // Otherwise, display error
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    // Method to set default background color for all buttons
    private void setButtonDefaultColor() {

        // Find a reference to the Today {@link Button} in the layout
        Button todayButton = (Button) findViewById(R.id.button_today);
        // Find a reference to the Week {@link Button} in the layout
        Button weekButton = (Button) findViewById(R.id.button_week);
        // Find a reference to the Month {@link Button} in the layout
        Button monthButton = (Button) findViewById(R.id.button_month);
        // Find a reference to the Year {@link Button} in the layout
        Button yearButton = (Button) findViewById(R.id.button_year);

        todayButton.setBackgroundColor(NOT_SELECTED_BUTTON_COLOR);
        weekButton.setBackgroundColor(NOT_SELECTED_BUTTON_COLOR);
        monthButton.setBackgroundColor(NOT_SELECTED_BUTTON_COLOR);
        yearButton.setBackgroundColor(NOT_SELECTED_BUTTON_COLOR);
    }


    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        // Create a new loader for the given URL
        return new NewsLoader(this, mNewsRequestUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {

        //Hide the progress indicator
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(GONE);

        // Set empty state text to display "No news found."
        mEmptyStateTextView.setText(R.string.no_news);

        // Clear the adapter of previous news data
        mAdapter.clear();

        // If there is a valid list of {@link News}, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {

        // Clear the adapter of previous news data
        mAdapter.clear();
    }


    // Method to make a final URL combining the different URL parts and the user selected date range
    private void makeServerRequestUrl() {

        getDateForUrl(mSelectedStartDateOption);

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder = urlBuilder.append(NEWS_REQUEST_URL_START)
                .append(mDateForUrl);

        // First encode into UTF-8, then back to a form easily processed by the API
        // This is mainly to avoid issues with spaces and other special characters in the URL
        String finalUrl = Uri.encode(urlBuilder.toString()).replaceAll("\\+", "%20")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%3A", ":")
                .replaceAll("\\%2F", "/")
                .replaceAll("\\%3F", "?")
                .replaceAll("\\%26", "&")
                .replaceAll("\\%3D", "=")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%20", "\\+")
                .replaceAll("\\%7E", "~");

        mNewsRequestUrl = finalUrl;
    }

    private void getDateForUrl(String selectedDateOption) {
        // Variable to store range start date
        long dateSelected = 0;

        // get today and clear time of day
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        switch (selectedDateOption) {
            case "Today":
                // get today's date in milliseconds
                dateSelected = cal.getTimeInMillis() - TimeUnit.DAYS.toMillis(1); // subtraction done to avoid issue if there is no world news for today
                break;

            case "Week":
                // get start of this week
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                // This week's start date in milliseconds
                dateSelected = cal.getTimeInMillis();
                break;

            case "Month":
                // get start of the month
                cal.set(Calendar.DAY_OF_MONTH, 1);
                // This month's start date in milliseconds
                dateSelected = cal.getTimeInMillis();
                break;

            case "Year":
                // get start of the year
                cal.set(Calendar.DAY_OF_YEAR, 1);
                // This month's start date in milliseconds
                dateSelected = cal.getTimeInMillis();
                break;
        }

        Date date = new Date(dateSelected);
        DateFormat df = new SimpleDateFormat("yyyy-LL-dd");
        mDateForUrl = df.format(date);
    }

}
