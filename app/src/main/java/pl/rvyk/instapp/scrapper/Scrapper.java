package pl.rvyk.instapp.scrapper;

import okhttp3.OkHttpClient;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Scrapper {
    public static OkHttpClient client = new OkHttpClient().newBuilder().followRedirects(false).build();

    static {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }

    public static final String mozillaUserAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.125 Safari/537.36";

    public enum Methods {
        LOGIN_PASSWORD,
        PHPSESSIONID
    }

}