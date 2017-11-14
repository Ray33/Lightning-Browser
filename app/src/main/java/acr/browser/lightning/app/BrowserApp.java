package acr.browser.lightning.app;

import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.segment.analytics.Analytics;
import com.segment.analytics.android.integrations.mixpanel.MixpanelIntegration;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.annotation.ReportsCrashes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import acr.browser.lightning.BuildConfig;
import acr.browser.lightning.R;
import acr.browser.lightning.preference.PreferenceManager;
import acr.browser.lightning.utils.FileUtils;
import acr.browser.lightning.utils.MemoryLeakUtils;
import io.mobitech.commonlibrary.model.HttpResponse;
import io.mobitech.commonlibrary.utils.NetworkUtil;
import io.mobitech.commonlibrary.utils.contentParsers.StringParser;
import io.mobitech.reporting.HockeySender;

import static acr.browser.lightning.constant.Constants.MOBITECH_APP_KEY;

@ReportsCrashes(formUri = "")
public class BrowserApp extends Application {

    private static final String TAG = BrowserApp.class.getSimpleName();

    private static AppComponent mAppComponent;
    private static final Executor mIOThread = Executors.newSingleThreadExecutor();
    private static final Executor mTaskThread = Executors.newCachedThreadPool();

    @Inject Bus mBus;
    @Inject PreferenceManager mPreferenceManager;

    public static Analytics analytics;

    @Override
    public void onCreate() {
        super.onCreate();
        InitiateOnAppStartupTask initiateOnAppStartupTask = new InitiateOnAppStartupTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            initiateOnAppStartupTask
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            initiateOnAppStartupTask.execute();
        }
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        }

        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {

                if (BuildConfig.DEBUG) {
                    FileUtils.writeCrashToStorage(ex);
                }

                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, ex);
                } else {
                    System.exit(2);
                }
            }
        });

        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        mAppComponent.inject(this);

        if (mPreferenceManager.getUseLeakCanary() && !isRelease()) {
            LeakCanary.install(this);
        }
//        if (!isRelease() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//        }

       registerActivityLifecycleCallbacks(new MemoryLeakUtils.LifecycleAdapter() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG, "Cleaning up after the Android framework");
                MemoryLeakUtils.clearNextServedView(activity, BrowserApp.this);
            }
        });
    }

    @NonNull
    public static BrowserApp get(@NonNull Context context) {
        return (BrowserApp) context.getApplicationContext();
    }

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }

    @NonNull
    public static Executor getIOThread() {
        return mIOThread;
    }

    @NonNull
    public static Executor getTaskThread() {
        return mTaskThread;
    }

    public static Bus getBus(@NonNull Context context) {
        return get(context).mBus;
    }

    /**
     * Determines whether this is a release build.
     *
     * @return true if this is a release build, false otherwise.
     */
    public static boolean isRelease() {
        return !BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.toLowerCase().equals("release");
    }

    public static void copyToClipboard(@NonNull Context context, @NonNull String string) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("URL", string);
        clipboard.setPrimaryClip(clip);
    }

    private class InitiateOnAppStartupTask extends
            AsyncTask<String, Void, String> {


        public InitiateOnAppStartupTask() {
        }

        @Override
        protected String doInBackground(String... args) {

            //init bug and error reporting
            ACRA.init(BrowserApp.this);
            ACRAConfiguration conf = new ACRAConfiguration();
            conf.setBuildConfigClass(BuildConfig.class);
            ACRA.setConfig(conf);
            ACRA.getErrorReporter().setReportSender(new HockeySender("2955c756b2a44bdf9eafc02a848930d9", " 9f03c103e83eab675f0c6ff239e36d79",BrowserApp.this));

            try{
                Analytics analytics = new Analytics.Builder(BrowserApp.this, BrowserApp.this.getString(R.string.analytics_write_key))
                        // Enable this to record certain application events automatically!
                        .trackApplicationLifecycleEvents()
                        // Enable this to record screen views automatically!
                        .recordScreenViews()
                        .use(MixpanelIntegration.FACTORY)
                        .build();
                Analytics.setSingletonInstance(analytics);
            }catch (Exception e){
                Log.e(TAG,e.getMessage()!=null ? e.getMessage() : "can't initiate mixpanel", e);
            }

// Set the initialized instance as a globally accessible instance.

            String userId = "";
            if (mPreferenceManager!=null){
                userId = mPreferenceManager.getUserId();
            }


//            AnalyticsService.addEventListener(IEventCallback.EVENT_TYPE.ALL, new SegmentAnalyticsTracking(BrowserApp.this, getString(R.string.analytics_write_key)));
//
//            AccessibilityEventsReceiverService.setOnBindCallback(new ICallback() {
//                @Override
//                public void execute() {
//                    Intent reOpenShoppingBuddy = new Intent(ShoppingBuddyAppContext.this, MainWithSliderActivity.class);
//                    reOpenShoppingBuddy.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                    startActivity(reOpenShoppingBuddy);
//                }
//            });

            String userCountry="";
            if (mPreferenceManager!=null && TextUtils.isEmpty(mPreferenceManager.getCountry())){
                userCountry = getUserCountry(BrowserApp.this);
                mPreferenceManager.setCountry(userCountry);
            }


            if (mPreferenceManager!=null && !mPreferenceManager.isInstalled()) {
                String referrer = mPreferenceManager.getReferrer();
                String appId = BuildConfig.APPLICATION_ID;
                if(TextUtils.isEmpty(referrer)){
                    referrer = BuildConfig.VERSION_NAME;
                }

                try {
                    Date cDate = new Date();
                    String fDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cDate);
                    String url = "https://dashboard.mobitech.io/v1/tracking/install?p_key=" + MOBITECH_APP_KEY+"&referrer_id=" + URLEncoder.encode(referrer,"UTF-8") + "&app_id=" + URLEncoder.encode(appId,"UTF-8") +"&date="+fDate + "&user_id=" + userId + "&c=" + userCountry;
                    HttpResponse response = NetworkUtil.getContentFromURL(url, new StringParser(String.class), BrowserApp.this);
                    mPreferenceManager.setInstalled(response.responseCode<400);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (userId!=null && !TextUtils.isEmpty(userId)){
                    Analytics.with(BrowserApp.this).alias(userId);
                    Analytics.with(BrowserApp.this).identify(userId);
                }
            }

            //Track daily usage
//            Map<String, String> eventData = AnalyticsService.initResponse(IEventCallback.EVENT_TYPE.SYSTEM);
//            eventData.put(IEventCallback.EVENT_ELEMENTS.EVENT_NAME.name(), "BROWSER_OPEN");
//            eventData.put(IEventCallback.EVENT_ELEMENTS.EVENT_VALUE.name(), MOBITECH_APP_KEY);
//            AnalyticsService.raiseEvent(eventData, BrowserApp.this);
//



            return null;

        }

        /**
         * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
         * @param context Context reference to get the TelephonyManager instance from
         * @return country code or null
         */
        public String getUserCountry(Context context) {
            try {
                final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                final String simCountry = tm.getSimCountryIso();
                if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                    return simCountry.toLowerCase(Locale.US);
                }
                else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                    String networkCountry = tm.getNetworkCountryIso();
                    if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                        return networkCountry.toLowerCase(Locale.US);
                    }
                }
            }
            catch (Exception e) { }
            return null;
        }


    }
}
