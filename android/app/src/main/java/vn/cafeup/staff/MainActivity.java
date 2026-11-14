package vn.cafeup.staff;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/** SUSHI GO staff screen in a WebView. Print jobs (intent:/rawbt: links) go straight to RawBT, no tap needed. */
public class MainActivity extends Activity {
    static final String START = "https://pokesushinhatrang.github.io/staff/";
    WebView web;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);   // order sound without a tap
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        web.addJavascriptInterface(new Bridge(), "cafeupApp");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) { return open(r.getUrl().toString()); }
            @SuppressWarnings("deprecation") @Override public boolean shouldOverrideUrlLoading(WebView v, String url) { return open(url); }
        });
        setContentView(web);
        if (b != null) web.restoreState(b); else web.loadUrl(START);
    }

    /** intent:, rawbt:, tel:, zalo… open in other apps; the staff site stays in the WebView */
    boolean open(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) return false;
        startExternal(url);
        return true;
    }

    void startExternal(String url) {
        try {
            Intent i = url.startsWith("intent:") ? Intent.parseUri(url, Intent.URI_INTENT_SCHEME) : new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.setComponent(null);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "RawBT: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** window.cafeupApp.print(url) from the page */
    class Bridge {
        @JavascriptInterface public void print(final String url) { runOnUiThread(new Runnable() { public void run() { startExternal(url); } }); }
        @JavascriptInterface public String version() { return "1.0"; }
    }

    @Override protected void onSaveInstanceState(Bundle b) { super.onSaveInstanceState(b); web.saveState(b); }
    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else moveTaskToBack(true); }
}
