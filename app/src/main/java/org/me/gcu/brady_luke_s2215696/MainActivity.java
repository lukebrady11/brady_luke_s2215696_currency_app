package org.me.gcu.brady_luke_s2215696;

/*  Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

// Name                 Luke Brady
// Student ID           S2215696
// Programme of Study   Computing


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;

import android.view.View.OnClickListener;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView rawDataDisplay;
    private Button startButton;

    private RecyclerView rvRates;
    private RatesAdapter adapter;

    private LinearLayout keyRatesLayout;
    private androidx.appcompat.widget.SearchView searchView;

    private String result = "";
    private String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";

    // Data collections
    private final List<CurrencyRate> allRates = new ArrayList<>();
    private final List<CurrencyRate> shownRates = new ArrayList<>();
    private final List<CurrencyRate> rates = new ArrayList<>();

    // Threading
    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService scheduler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Link views from xml
        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        keyRatesLayout = findViewById(R.id.keyRatesLayout);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        // RecyclerView setup
        rvRates = findViewById(R.id.rvRates);
        adapter = new RatesAdapter();
        if (rvRates != null) {
            rvRates.setLayoutManager(new LinearLayoutManager(this));
            rvRates.setAdapter(adapter);
            adapter.setOnItemClick(rate -> {
                android.content.Intent i = new android.content.Intent(this, ConverterActivity.class);
                i.putExtra("code", rate.code);
                i.putExtra("name", rate.name);
                i.putExtra("rateToGBP", rate.rateToGBP);
                startActivity(i);
            });
        }

        // SearchView setup
        searchView = findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { applyFilter(query); return true; }
                @Override public boolean onQueryTextChange(String query) { applyFilter(query); return true; }
            });
        }
    }

    @Override
    public void onClick(View aview) {
        // Manual refresh button
        startProgress();
    }
    @Override
    protected void onStart() {
        super.onStart();

        // Automatic refresh: calls startProgress() once at startup and then every 15 minutes
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleWithFixedDelay(
                    this::startProgress,
                    0,
                    15,
                    TimeUnit.MINUTES
            );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop when Activity is no longer visible
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop when Activity is destroyed
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (exec != null) {
            exec.shutdownNow();
        }
    }
    // Starts a background task to download and parse the data
    private void startProgress() {
        if (exec == null || exec.isShutdown()) {
            exec = Executors.newSingleThreadExecutor();
        }
        exec.execute(new Task(urlSource));
    }

    // Filter list (allRates) into shownRates based on query, Matches code or name
    private void applyFilter(String q) {
        String query = q == null ? "" : q.trim().toUpperCase();
        shownRates.clear();
        if (query.isEmpty()) {
            shownRates.addAll(allRates);
        } else {
            for (CurrencyRate r : allRates) {
                if (r.code.toUpperCase().contains(query) || r.name.toUpperCase().contains(query)) {
                    shownRates.add(r);
                }

            }
        }
        adapter.setData(shownRates);

    };


    // Creates key rates section, using white text and main button colour
    private void updateKeyRatesBoxes() {
        if (keyRatesLayout == null) return;
        keyRatesLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        String[] mainCodes = {"USD", "EUR", "JPY"};

        for (String code : mainCodes) {
            CurrencyRate match = null;
            for (CurrencyRate r : allRates) {
                if (code.equalsIgnoreCase(r.code)) {
                    match = r;
                    break;
                }
            }
            if (match == null) continue;

            View v = inflater.inflate(R.layout.item_key_rate, keyRatesLayout, false);
            TextView tvCode = v.findViewById(R.id.tvKeyCode);
            TextView tvRate = v.findViewById(R.id.tvKeyRate);
            TextView tvName = v.findViewById(R.id.tvKeyName);

            tvCode.setText(match.code);
            tvRate.setText("1 GBP = " + match.rateToGBP + " " + match.code);
            tvName.setText(match.name);

            // make text white
            tvCode.setTextColor(Color.WHITE);
            tvRate.setTextColor(Color.WHITE);
            tvName.setTextColor(Color.WHITE);

            // match button colour for background
            int primary = ContextCompat.getColor(this, R.color.primaryButton);
            v.setBackgroundColor(primary);

            final CurrencyRate item = match;
            // key box also opens converter
            v.setOnClickListener(view -> {
                android.content.Intent i =
                        new android.content.Intent(MainActivity.this, ConverterActivity.class);
                i.putExtra("code", item.code);
                i.putExtra("name", item.name);
                i.putExtra("rateToGBP", item.rateToGBP);
                startActivity(i);
            });

            keyRatesLayout.addView(v);
        }
    }

    // downloads RSS XML and parses it with XMLPullParser, builds CurrencyRate objects
    private class Task implements Runnable {
        private String url;
        public Task(String aurl) { url = aurl; }

        @Override
        public void run() {
            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine;

            Log.d("MyTask", "in run");

            // local fetch time for display
            final String localFetchTime = new SimpleDateFormat(
                    "EEE MMM dd yyyy HH:mm:ss z", Locale.UK
            ).format(new Date());

            try {
                result = "";
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                while ((inputLine = in.readLine()) != null) {
                    result = result + inputLine;
                }
                in.close();
            } catch (IOException ae) {
                Log.e("MyTask", "ioexception: " + ae.getMessage());
            }

            if (result == null || result.isEmpty()) {
                postToUi("No data downloaded. Check your internet connection or the feed URL.");
                return;
            }

            // Cleaning
            int i = result.indexOf("<?");
            if (i >= 0) result = result.substring(i);
            i = result.indexOf("</rss>");
            if (i >= 0 && i + 6 <= result.length()) result = result.substring(0, i + 6);

            try {
                // Set up XML pull parser
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(result));

                rates.clear();

                List<String> lines = new ArrayList<>();
                String channelLastBuildDate = null;

                boolean insideItem = false;
                String text = null;
                String itemTitle = null;
                String itemDescription = null;

                Pattern codePattern = Pattern.compile("\\(([A-Z]{3})\\)\\s*$");
                Pattern ratePattern = Pattern.compile("=\\s*([0-9]+(?:\\.[0-9]+)?)");

                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if ("item".equalsIgnoreCase(xpp.getName())) {
                                insideItem = true;
                                itemTitle = null;
                                itemDescription = null;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            text = xpp.getText();
                            break;

                        case XmlPullParser.END_TAG:
                            String name = xpp.getName();

                            if (!insideItem) {
                                if ("lastBuildDate".equalsIgnoreCase(name)) {
                                    channelLastBuildDate = (text == null) ? null : text.trim();
                                }
                            } else {
                                if ("title".equalsIgnoreCase(name)) {
                                    itemTitle = (text == null) ? "" : text.trim();
                                } else if ("description".equalsIgnoreCase(name)) {
                                    itemDescription = (text == null) ? "" : text.trim();
                                } else if ("item".equalsIgnoreCase(name)) {
                                    // Process one currency item

                                    // Title format
                                    String right = itemTitle;
                                    int slash = itemTitle.indexOf('/');
                                    if (slash >= 0 && slash + 1 < itemTitle.length()) {
                                        right = itemTitle.substring(slash + 1);
                                    }

                                    // Extract 3-letter code from ("XXX") at end
                                    String code = null;
                                    Matcher mCode = codePattern.matcher(right);
                                    if (mCode.find()) code = mCode.group(1);

                                    // Remove ("XXX") from title
                                    String displayName = right.replaceAll("\\([A-Z]{3}\\)", "").trim();

                                    // Extract rate from description
                                    double rate = 0.0;
                                    Matcher mRate = ratePattern.matcher(itemDescription);
                                    if (mRate.find()) {
                                        try { rate = Double.parseDouble(mRate.group(1)); }
                                        catch (NumberFormatException ignore) {}
                                    }

                                    if (code != null && rate > 0) {
                                        // Create CurrencyRate object
                                        CurrencyRate cr = new CurrencyRate();
                                        cr.code = code;
                                        cr.name = displayName;
                                        cr.rateToGBP = rate;
                                        cr.pubDate = channelLastBuildDate;
                                        rates.add(cr);

                                        lines.add(code + "  |  1 GBP = " + rate + " " + displayName);
                                    }

                                    insideItem = false;
                                }
                            }
                            break;
                    }
                    eventType = xpp.next();
                }

                // Sort textual list for readability
                lines.sort(String::compareTo);

                // Build summary (feed + local fetch time)
                StringBuilder sb = new StringBuilder();
                if (channelLastBuildDate != null) {
                    sb.append("Feed last updated: ").append(channelLastBuildDate).append("\n");
                }
                sb.append("Fetched: ").append(localFetchTime).append("\n\n");

                // Build key rates
                String[] main = {"USD", "EUR", "JPY"};
                for (String m : main) {
                    for (CurrencyRate r : rates) {
                        if (m.equalsIgnoreCase(r.code)) {
                            sb.append(m).append("  |  1 GBP = ")
                                    .append(r.rateToGBP).append(" ").append(r.name).append("\n");
                            break;
                        }
                    }
                }
                sb.append("\n");

                // Show first 25 lines
                int show = Math.min(25, lines.size());
                for (int k = 0; k < show; k++) sb.append(lines.get(k)).append("\n");
                if (lines.isEmpty()) sb.append("No currency items parsed. Check feed contents/format.");

                result = sb.toString();

            } catch (XmlPullParserException e) {
                Log.e("Parsing", "EXCEPTION " + e);
                result = "Parsing error: " + e.getMessage();
            } catch (IOException e) {
                Log.e("Parsing", "I/O EXCEPTION " + e);
                result = "I/O error during parse: " + e.getMessage();
            }

            // Update UI: summary text + list
            if (rawDataDisplay != null) {
                rawDataDisplay.post(() -> {
                    rawDataDisplay.setText(result);

                    allRates.clear();
                    allRates.addAll(rates);

                    updateKeyRatesBoxes();

                    String q = (searchView != null) ? searchView.getQuery().toString() : "";
                    applyFilter(q);
                });
            }
        }
        // post an error message to UI thread
           private void postToUi(String msg) {
            if (rawDataDisplay == null) return;
            rawDataDisplay.post(() -> {
                rawDataDisplay.setText(msg);
                adapter.setData(new ArrayList<>()); // clear list on error
        });
    }
    }
}

