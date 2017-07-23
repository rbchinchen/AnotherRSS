package de.digisocken.anotherrss;

import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import java.util.Calendar;

/**
 * Der Einstiegspunkt des Launchers.
 * In der Applications-Klasse befinden sich auch die initalen
 * Konfigurationen der SharedPreferences sowie einige Konstanten.
 *
 * @author Jochen Peters
 */
public class AnotherRSS extends Application {
    public static boolean showAdditionalFeed = true;
    public static String query = "";

    /*
    http://feeds.bbci.co.uk/news/world/europe/rss.xml
http://news.yahoo.com/rss/
http://feeds.t-online.de/rss/nachrichten
http://www.wz.de/cmlink/wz-rss-uebersicht-1.516698
http://www.deutschlandfunk.de/die-nachrichten.353.de.rss
http://www.tagesschau.de/xml/rss2
http://www.taz.de/!p4608;rss/
https://www.heise.de/security/news/news-atom.xml
https://www.amnesty.de/rss/news
http://digisocken.de/_p/wdrWetter/?rss=true
https://www.umwelt.nrw.de/rss.xml
http://feeds.reuters.com/reuters/scienceNews?format=xml
     */
    public static final String urls =
            "http://www.tagesschau.de/xml/rss2 " +
                    "https://www.heise.de/security/news/news-atom.xml " +
                    "http://news.yahoo.com/rss/ " +
                    "https://www.amnesty.de/rss/news " +
                    "http://feeds.reuters.com/reuters/scienceNews?format=xml " +
                    "http://www.deutschlandfunk.de/die-nachrichten.353.de.rss " +
                    "http://digisocken.de/_p/wdrWetter/?rss=true " +
                    "https://www.umwelt.nrw.de/rss.xml";

    public static class Config {
        /**
         * really delete old database entries (marked as deleted)
         * older than Config.DEFAULT_expunge days
         */
        public static final int DEFAULT_expunge = 5;
        public static final String DEFAULT_rsssec = "10800";
        public static final String DEFAULT_notifyColor = "#FF00FFFF";
        public static final String DEFAULT_notifyType = "2";
        public static final int DEFAULT_NIGHT_START = 18;
        public static final int DEFAULT_NIGHT_STOP = 6;
        public static final String SEARCH_HINT_COLOR = "#FFAA00";

        /**
         * im Feed Text kann leider einen total überflüssiger Inhalt enthalten,
         * wo hinter dem Wort {@value #DEFAULT_lastRssWord} abgeschnitten werden muss.
         */
        public static final String DEFAULT_lastRssWord = "weiterlesen";

        /**
         * sets a static image size to {@value #MAX_IMG_WIDTH}
         */
        public static final int MAX_IMG_WIDTH = 120;
        public static final float IMG_ROUND = 20f;
        /**
         * sollte eine Verbindung nicht zu sande kommen, wird ein neuer
         * Alarm in {@value #RETRYSEC_AFTER_OFFLINE} sec ausgelöst
         */
        public static final long RETRYSEC_AFTER_OFFLINE = 75L;
    }

    public static Alarm alarm = null;

    /**
     * So kann der {@link Refresher} erkennen, ob er nur im Hintergrund läuft.
     * Wäre withGui auf true, wird nur eine HeadUp Notifikation gezeigt.
     * An dieser Stelle wird klar, dass der Alarm <i>doch</i> auf this zugreifen kann (?)
     */
    public static boolean withGui = false;
    public static final String TAG = AnotherRSS.class.getSimpleName();
    private static Context contextOfApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        contextOfApplication = getApplicationContext();

        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!mPreferences.contains("rss_url")) {
            mPreferences.edit().putString("rss_url", AnotherRSS.urls).commit();
        }
        if (!mPreferences.contains("nightmode_use_start")) {
            mPreferences.edit().putInt("nightmode_use_start", Config.DEFAULT_NIGHT_START).commit();
        }
        if (!mPreferences.contains("nightmode_use_stop")) {
            mPreferences.edit().putInt("nightmode_use_stop", Config.DEFAULT_NIGHT_STOP).commit();
        }

        if (alarm == null) alarm = new Alarm();
    }

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

    public static boolean inTimeSpan(int startH, int stopH) {
        int nowH = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (startH == stopH && startH == nowH) return true;
        if (startH > stopH && (nowH <= stopH || nowH >= startH)) return true;
        if (startH < stopH && nowH >= startH && nowH <= stopH) return true;
        return false;
    }
}
