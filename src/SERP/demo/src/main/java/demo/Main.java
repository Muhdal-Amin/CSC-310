package demo;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Main {

    // --- Constants for Google Custom Search ---
    private static final String GOOGLE_API_KEY = "AIzaSyAXYtqHUjLpZNhIukfgFmdcqfebI-HLwNw";
    private static final String GOOGLE_CX = "73ac8a41da3f3487d";
    private static final int RESULTS_PER_CALL = 10;

    // --- Global Data Structures ---
    // We use ConcurrentHashMap for thread safety.
    private static final Map<String, Set<String>> crimeFeatures = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> dlFeatures = new ConcurrentHashMap<>();

    // --- Retrieve search results via Google Custom Search API ---
    public static List<String> getSearchResults(String query, int numResults) {
        List<String> urls = new ArrayList<>();
        int startIndex = 1;
        Gson gson = new Gson();

        while (urls.size() < numResults) {
            try {
                String urlStr = "https://www.googleapis.com/customsearch/v1"
                        + "?key=" + URLEncoder.encode(GOOGLE_API_KEY, "UTF-8")
                        + "&cx=" + URLEncoder.encode(GOOGLE_CX, "UTF-8")
                        + "&q=" + URLEncoder.encode(query, "UTF-8")
                        + "&start=" + startIndex;
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("Error: Received response code " + responseCode);
                    break;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder responseStr = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    responseStr.append(inputLine);
                }
                in.close();
                connection.disconnect();

                // Parse the JSON response.
                JsonObject jsonObject = gson.fromJson(responseStr.toString(), JsonObject.class);
                JsonArray items = jsonObject.has("items") ? jsonObject.getAsJsonArray("items") : null;
                if (items == null || items.size() == 0) {
                    break;
                }

                for (JsonElement element : items) {
                    JsonObject itemObj = element.getAsJsonObject();
                    if (itemObj.has("link")) {
                        String link = itemObj.get("link").getAsString();
                        urls.add(link);
                        if (urls.size() >= numResults) {
                            break;
                        }
                    }
                }
                startIndex += RESULTS_PER_CALL;
                // Respect rate limits by sleeping 1 second.
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Error retrieving search results for query '" + query + "': " + e.getMessage());
                break;
            }
        }
        return urls;
    }

    public static List<String> getCrimeReportingUrls(int numResults) {
        String crimeQuery = "crime report";
        System.out.println("Retrieving crime reporting URLs for query: '" + crimeQuery + "'");
        return getSearchResults(crimeQuery, numResults);
    }

    public static List<String> getDlJournalUrls(int numResults) {
        String dlQuery = "deep learning model papers";
        System.out.println("Retrieving deep learning journal URLs for query: '" + dlQuery + "'");
        return getSearchResults(dlQuery, numResults);
    }

    // --- Extract crime features from text ---
    public static Set<String> extractCrimeFeatures(String text) {
        Set<String> features = new HashSet<>();
        // Predefined set of crime-related keywords.
        Set<String> crimeKeywords = new HashSet<>(Arrays.asList(
                "murder", "robbery", "assault", "arson", "burglary", "homicide",
                "fraud", "kidnapping", "theft", "shooting", "crime", "incident",
                "weapon", "police", "suspect", "victim", "evidence", "trial", "court",
                "investigation", "officer", "riot", "vandalism"
        ));

        // Tokenize the text (simple split by non-word characters).
        String[] tokens = text.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (crimeKeywords.contains(token)) {
                features.add(token);
            }
        }
        // Date detection: add "date" if a date pattern is found.
        String datePattern = "\\b(?:\\d{1,2}[-/\\s]*(?:th|st|nd|rd)?\\s*)?(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)[\\s\\-.,]+\\d{1,2}[,\\s]+\\d{4}\\b";
        Pattern pattern = Pattern.compile(datePattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            features.add("date");
        }
        return features;
    }

    // --- Process a crime-reporting page ---
    public static void processCrimePage(String url) {
        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            String text = doc.text();
            Set<String> features = extractCrimeFeatures(text);
            for (String feature : features) {
                crimeFeatures.computeIfAbsent(feature, k -> ConcurrentHashMap.newKeySet()).add(url);
            }
            System.out.println("[Crime] Processed: " + url);
        } catch (Exception e) {
            System.out.println("[Crime] Error processing " + url + ": " + e.getMessage());
        }
    }

    // --- Extract deep learning features from text ---
    public static Set<String> extractDlFeatures(String text) {
        Set<String> features = new HashSet<>();
        // Predefined deep learning–related keywords.
        Set<String> dlKeywords = new HashSet<>(Arrays.asList(
                "deep", "learning", "neural", "network", "cnn", "rnn", "lstm", "transformer", "attention",
                "gan", "autoencoder", "backpropagation", "optimizer", "gradient", "convolution", "pooling",
                "activation", "relu", "sigmoid", "tanh", "dropout", "batchnorm", "embedding", "sequence",
                "model", "architecture", "classification", "regression", "unsupervised", "supervised",
                "reinforcement", "transfer", "finetuning", "pretraining", "bert", "gpt", "selfsupervised"
        ));
        String[] tokens = text.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (dlKeywords.contains(token)) {
                features.add(token);
            }
        }
        return features;
    }

    // --- Process a deep learning journal page ---
    public static void processDlPage(String url) {
        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            String text = doc.text();
            Set<String> features = extractDlFeatures(text);
            for (String feature : features) {
                dlFeatures.computeIfAbsent(feature, k -> ConcurrentHashMap.newKeySet()).add(url);
            }
            System.out.println("[DL] Processed: " + url);
        } catch (Exception e) {
            System.out.println("[DL] Error processing " + url + ": " + e.getMessage());
        }
    }

    // --- Visualize crime features using JFreeChart ---
    public static void visualizeCrimeFeatures() {
        Map<String, Integer> featureCounts = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : crimeFeatures.entrySet()) {
            featureCounts.put(entry.getKey(), entry.getValue().size());
        }

        // Sort the features by count in descending order.
        List<Map.Entry<String, Integer>> sortedFeatures = new ArrayList<>(featureCounts.entrySet());
        sortedFeatures.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedFeatures) {
            if (count >= 10) break;
            dataset.addValue(entry.getValue(), "Features", entry.getKey());
            count++;
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Distinctive Features in Crime Reporting Pages",
                "Features",
                "Number of Pages",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        JFrame frame = new JFrame("Crime Reporting Features");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(barChart));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // --- Visualize deep learning features using JFreeChart ---
    public static void visualizeDlFeatures() {
        Map<String, Integer> featureCounts = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : dlFeatures.entrySet()) {
            featureCounts.put(entry.getKey(), entry.getValue().size());
        }

        List<Map.Entry<String, Integer>> sortedFeatures = new ArrayList<>(featureCounts.entrySet());
        sortedFeatures.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedFeatures) {
            if (count >= 10) break;
            dataset.addValue(entry.getValue(), "Deep Learning Features", entry.getKey());
            count++;
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Deep Learning Related Features in Journal Pages",
                "Deep Learning Features",
                "Number of Pages",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        JFrame frame = new JFrame("Deep Learning Journal Features");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(barChart));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // --- Main Method ---
    public static void main(String[] args) {
        int numUrlsToFetch = 20; // Adjust as needed
        List<String> crimeUrls = getCrimeReportingUrls(numUrlsToFetch);
        List<String> dlUrls = getDlJournalUrls(numUrlsToFetch);

        System.out.println("Retrieved " + crimeUrls.size() + " crime reporting URLs.");
        System.out.println("Retrieved " + dlUrls.size() + " deep learning journal URLs.");

        int maxWorkers = 5;
        ExecutorService executor = Executors.newFixedThreadPool(maxWorkers);

        System.out.println("Starting processing of crime reporting pages...");
        List<Future<?>> futures = new ArrayList<>();
        for (String url : crimeUrls) {
            futures.add(executor.submit(() -> processCrimePage(url)));
        }
        // Wait for all crime pages to be processed.
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                System.out.println("Error waiting for crime page processing: " + e.getMessage());
            }
        }

        System.out.println("Starting processing of deep learning journal pages...");
        futures.clear();
        for (String url : dlUrls) {
            futures.add(executor.submit(() -> processDlPage(url)));
        }
        // Wait for all deep learning pages to be processed.
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                System.out.println("Error waiting for deep learning page processing: " + e.getMessage());
            }
        }

        executor.shutdown();

        System.out.println("Visualizing crime reporting features...");
        visualizeCrimeFeatures();

        System.out.println("Visualizing deep learning journal features...");
        visualizeDlFeatures();
    }
}
