/*
 * Copyright 2014 A.C.R. Development
 */
package acr.browser.lightning.constant;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import acr.browser.lightning.BuildConfig;

public final class Constants {

    private Constants() {
    }

    public static final boolean FULL_VERSION = BuildConfig.FULL_VERSION;
    public static final String MOBITECH_APP_KEY = BuildConfig.appKey;

    // Hardcoded user agents
    public static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30" + TextUtils.htmlEncode(BuildConfig.USER_AGENT_SUFFIX);//"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
    public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"  + TextUtils.htmlEncode(BuildConfig.USER_AGENT_SUFFIX);

    // Search query URLs
    public static final String YAHOO_SEARCH = "https://search.yahoo.com/search?p=";
    public static final String GOOGLE_SEARCH = "https://cse.google.com:443/cse/publicurl?cx=partner-pub-6728551582410897:7354642160&q=";
    public static final String BING_SEARCH = "https://www.bing.com/search?q=";
    public static final String DUCK_SEARCH = "https://duckduckgo.com/?t=lightning&q=";
    public static final String DUCK_LITE_SEARCH = "https://duckduckgo.com/lite/?t=lightning&q=";
    public static final String STARTPAGE_MOBILE_SEARCH = "https://startpage.com/do/m/mobilesearch?language=english&query=";
    public static final String STARTPAGE_SEARCH = "https://startpage.com/do/search?language=english&query=";
    public static final String ASK_SEARCH = "http://www.ask.com/web?qsrc=0&o=0&l=dir&qo=LightningBrowser&q=";
    public static final String BAIDU_SEARCH = "https://www.baidu.com/s?wd=";
    public static final String YANDEX_SEARCH = "https://yandex.ru/yandsearch?lr=21411&text=";
    public static final String MOBITECH_SEARCH = BuildConfig.SEARCH_BASE_URL;
    public static final String MOBITECH_SEARCH_WITHOUT_USER_ID = BuildConfig.SEARCH_BASE_URL;//"https://trends.mobitech-search.xyz/v1/search/%1$s?origin=APP_SEARCH_BAR&c=%2$s&keywords=";
    public static final String MOBITECH_STARTPAGE = BuildConfig.START_PAGE; //"http://www.default-browser.com/?affID=970801840";//https://browser.mobitech-search.xyz/start?p_key=%1$s&user_id=%2$s&c=%3$s&q=";
    public static final String MOBITECH_STARTPAGE_WITHOUT_USER_ID = BuildConfig.START_PAGE;//"http://www.default-browser.com/?affID=970801840";//"https://browser.mobitech-search.xyz/start?p_key=%1$s&c=%2$s&q=";
    public static final String MOBITECH_STARTPAGE_BASE_URL = BuildConfig.START_PAGE;//"https://browser.mobitech-search.xyz/start";

    public static final String TRACKING_MOBITECH_INSTALLED_APP = "http://installs.mobitech.io/v1/tracking/install"; //POST


    // Custom local page schemes
    public static final String SCHEME_HOMEPAGE = "about:home";
    public static final String SCHEME_BLANK = "about:blank";
    public static final String SCHEME_BOOKMARKS = "about:bookmarks";

    // Miscellaneous JavaScript
    public static final String JAVASCRIPT_INVERT_PAGE = "javascript:(function(){var e='img {-webkit-filter: invert(100%);'+'-moz-filter: invert(100%);'+'-o-filter: invert(100%);'+'-ms-filter: invert(100%); }',t=document.getElementsByTagName('head')[0],n=document.createElement('style');if(!window.counter){window.counter=1}else{window.counter++;if(window.counter%2==0){var e='html {-webkit-filter: invert(0%); -moz-filter: invert(0%); -o-filter: invert(0%); -ms-filter: invert(0%); }'}}n.type='text/css';if(n.styleSheet){n.styleSheet.cssText=e}else{n.appendChild(document.createTextNode(e))}t.appendChild(n)})();";
    public static final String JAVASCRIPT_TEXT_REFLOW = "javascript:document.getElementsByTagName('body')[0].style.width=window.innerWidth+'px';";
    public static final String JAVASCRIPT_THEME_COLOR = "(function () {\n" +
        "   \"use strict\";\n" +
        "    var metas, i, tag;\n" +
        "    metas = document.getElementsByTagName('meta');\n" +
        "    if (metas !== null) {\n" +
        "        for (i = 0; i < metas.length; i++) {\n" +
        "            tag = metas[i].getAttribute('name');\n" +
        "            if (tag !== null && tag.toLowerCase() === 'theme-color') {\n" +
        "                return metas[i].getAttribute('content');\n" +
        "            }\n" +
        "            console.log(tag);\n" +
        "        }\n" +
        "    }\n" +
        '\n' +
        "    return '';\n" +
        "}());";

    public static final String LOAD_READING_URL = "ReadingUrl";

    // URL Schemes
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    public static final String FILE = "file://";
    public static final String ABOUT = "about:";
    public static final String FOLDER = "folder://";

    // Application log tag
    public static final String TAG = "Lightning";

    // These should match the order of @array/proxy_choices_array
    @IntDef({NO_PROXY, PROXY_ORBOT, PROXY_I2P, PROXY_MANUAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PROXY {}

    public static final int NO_PROXY = 0;
    public static final int PROXY_ORBOT = 1;
    public static final int PROXY_I2P = 2;
    public static final int PROXY_MANUAL = 3;

    // Default text encoding we will use
    public static final String DEFAULT_ENCODING = "UTF-8";

    // Allowable text encodings for the WebView
    public static final String[] TEXT_ENCODINGS = {"ISO-8859-1", "UTF-8", "GBK", "Big5", "ISO-2022-JP", "SHIFT_JS", "EUC-JP", "EUC-KR"};

    public static final String INTENT_ORIGIN = "URL_INTENT_ORIGIN";
    public static final String INTENT_ACTION_SEARCH = "intent_action_search";
    public static final String INTENT_ACTION_WEB_SEARCH = "android.intent.action.WEB_SEARCH";


}
