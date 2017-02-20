/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package acr.browser.lightning.utils;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import acr.browser.lightning.BuildConfig;
import acr.browser.lightning.constant.BookmarkPage;
import acr.browser.lightning.constant.Constants;
import acr.browser.lightning.constant.HistoryPage;
import acr.browser.lightning.constant.StartPage;

/**
 * Utility methods for Url manipulation
 */
public class UrlUtils {
    private static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile(
            "(?i)" + // switch on case insensitive matching
                    '(' +    // begin group for schema
                    "(?:http|https|file)://" +
                    "|(?:inline|data|about|javascript):" +
                    "|(?:.*:.*@)" +
                    ')' +
                    "(.*)");
    // Google search
    public final static String QUERY_PLACE_HOLDER = "%s";
    // Regular expression to strip http:// and optionally
    // the trailing slash
    private static final Pattern STRIP_URL_PATTERN =
            Pattern.compile("^http://(.*?)/?$");

    private UrlUtils() { /* cannot be instantiated */ }

    /**
     * Strips the provided url of preceding "http://" and any trailing "/". Does not
     * strip "https://". If the provided string cannot be stripped, the original string
     * is returned.
     * <p/>
     * TODO: Put this in TextUtils to be used by other packages doing something similar.
     *
     * @param url a url to strip, like "http://www.google.com/"
     * @return a stripped url like "www.google.com", or the original string if it could
     * not be stripped
     */
    @Nullable
    public static String stripUrl(@Nullable String url) {
        if (url == null) return null;
        Matcher m = STRIP_URL_PATTERN.matcher(url);
        if (m.matches()) {
            return m.group(1);
        } else {
            return url;
        }
    }

    /**
     * Attempts to determine whether user input is a URL or search
     * terms.  Anything with a space is passed to search if canBeSearch is true.
     * <p/>
     * Converts to lowercase any mistakenly uppercased schema (i.e.,
     * "Http://" converts to "http://"
     *
     * @param canBeSearch If true, will return a search url if it isn't a valid
     *                    URL. If false, invalid URLs will return null
     * @return Original or modified URL
     */
    @NonNull
    public static String smartUrlFilter(@NonNull String url, boolean canBeSearch, String searchUrl) {
        String inUrl = url.trim();
        boolean hasSpace = inUrl.indexOf(' ') != -1;
        Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(inUrl);
        if (matcher.matches()) {
            // force scheme to lowercase
            String scheme = matcher.group(1);
            String lcScheme = scheme.toLowerCase();
            if (!lcScheme.equals(scheme)) {
                inUrl = lcScheme + matcher.group(2);
            }
            if (hasSpace && Patterns.WEB_URL.matcher(inUrl).matches()) {
                inUrl = inUrl.replace(" ", "%20");
            }
            return inUrl;
        }
        if (!hasSpace) {
            if (Patterns.WEB_URL.matcher(inUrl).matches()) {
                return URLUtil.guessUrl(inUrl);
            }
        }
        if (canBeSearch) {
            return URLUtil.composeSearchUrl(inUrl,
                    searchUrl, QUERY_PLACE_HOLDER);
        }
        return "";
    }

    /* package */
    @NonNull
    static String fixUrl(@NonNull String inUrl) {
        // FIXME: Converting the url to lower case
        // duplicates functionality in smartUrlFilter().
        // However, changing all current callers of fixUrl to
        // call smartUrlFilter in addition may have unwanted
        // consequences, and is deferred for now.
        int colon = inUrl.indexOf(':');
        boolean allLower = true;
        for (int index = 0; index < colon; index++) {
            char ch = inUrl.charAt(index);
            if (!Character.isLetter(ch)) {
                break;
            }
            allLower &= Character.isLowerCase(ch);
            if (index == colon - 1 && !allLower) {
                inUrl = inUrl.substring(0, colon).toLowerCase()
                        + inUrl.substring(colon);
            }
        }
        if (inUrl.startsWith("http://") || inUrl.startsWith("https://"))
            return inUrl;
        if (inUrl.startsWith("http:") ||
                inUrl.startsWith("https:")) {
            if (inUrl.startsWith("http:/") || inUrl.startsWith("https:/")) {
                inUrl = inUrl.replaceFirst("/", "//");
            } else inUrl = inUrl.replaceFirst(":", "://");
        }
        return inUrl;
    }

    // Returns the filtered URL. Cannot return null, but can return an empty string
    /* package */
    @Nullable
    static String filteredUrl(@Nullable String inUrl) {
        if (inUrl == null) {
            return "";
        }
        if (inUrl.startsWith("content:")
                || inUrl.startsWith("browser:")) {
            return "";
        }
        return inUrl;
    }

    /**
     * Returns whether the given url is the bookmarks/history page or a normal website
     */
    public static boolean isSpecialUrl(@Nullable String url) {
        return url != null && url.startsWith(Constants.FILE) &&
                (url.endsWith(BookmarkPage.FILENAME) ||
                        url.endsWith(HistoryPage.FILENAME) ||
                        url.endsWith(StartPage.FILENAME));
    }

    /**
     * Determines if the url is a url for the bookmark page.
     *
     * @param url the url to check, may be null.
     * @return true if the url is a bookmark url, false otherwise.
     */
    public static boolean isBookmarkUrl(@Nullable String url) {
        return url != null && url.startsWith(Constants.FILE) && url.endsWith(BookmarkPage.FILENAME);
    }

    /**
     * Determines if the url is a url for the history page.
     *
     * @param url the url to check, may be null.
     * @return true if the url is a history url, false otherwise.
     */
    public static boolean isHistoryUrl(@Nullable String url) {
        return url != null && url.startsWith(Constants.FILE) && url.endsWith(HistoryPage.FILENAME);
    }

    /**
     * Determines if the url is a url for the start page.
     *
     * @param url the url to check, may be null.
     * @return true if the url is a start page url, false otherwise.
     */
    public static boolean isStartPageUrl(@Nullable String url) {
        return url != null && url.startsWith(Constants.FILE) && url.endsWith(StartPage.FILENAME);
    }

    public static String makeMobitechSearchUrl(String userId, String publicKey) {
        if (BuildConfig.DEBUG) {
            publicKey = "TESTC36B5A";
        }
        return String.format(Constants.MOBITECH_SEARCH, publicKey, userId);
    }

    public static String makeMobitechSearchStartPage(String userId, String publicKey) {
        if (BuildConfig.DEBUG) {
            publicKey = "TESTC36B5A";
        }
        return String.format(Constants.STARTPAGE_MOBITECH_SEARCH, publicKey, userId);
    }

    public static String getReferrerIdForInstalledApp(String referrer, String referrerField) {
        if (referrerField != null && !referrerField.isEmpty() && referrer.indexOf(referrerField) != -1) {
            referrer = referrer.substring(referrer.indexOf(referrerField) + referrerField.length());
            int startIndex = referrer.indexOf("=") + 1;
            int endIndex = referrer.indexOf("&") != -1 ? referrer.indexOf("&") : referrer.length();
            referrer = referrer.substring(startIndex, endIndex);
            try {
                referrer = URLDecoder.decode(referrer, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return Constants.TRECKING_MOBITECH_INSTALLED_APP + referrer;
    }

    public static void putHttpParameters(Map<String, String> params, HttpURLConnection connection) {
        Uri.Builder builder = new Uri.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        final String query = builder.build().getEncodedQuery();
        OutputStream os = null;
        BufferedWriter writer = null;
        try {
            os = connection.getOutputStream();
            writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}