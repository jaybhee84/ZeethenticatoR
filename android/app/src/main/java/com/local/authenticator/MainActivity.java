package com.local.authenticator;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.webkit.WebViewAssetLoader;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        // Block screenshots and the recent-apps preview from showing codes.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        // Serve assets from an https origin so crypto.subtle (secure context only) works and localStorage is stable.
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();

        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebChromeClient(new WebChromeClient()); // enables alert/confirm
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return loader.shouldInterceptRequest(r.getUrl());
            }
        });
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void copy(String text) {
                ClipData clip = ClipData.newPlainText("code", text);
                // Hides the clipboard preview pop-up on Android 13+, so codes and backups aren't shown on screen.
                if (Build.VERSION.SDK_INT >= 33) {
                    PersistableBundle extras = new PersistableBundle();
                    extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true);
                    clip.getDescription().setExtras(extras);
                }
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(clip);
            }

            // Google's scanner runs in Play services: no camera permission or internet needed in this app.
            @JavascriptInterface
            public void scan() {
                GmsBarcodeScannerOptions qrOnly = new GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
                runOnUiThread(() -> GmsBarcodeScanning.getClient(MainActivity.this, qrOnly).startScan()
                        .addOnSuccessListener(b -> web.evaluateJavascript("onScan(" + JSONObject.quote(b.getRawValue()) + ")", null))
                        .addOnFailureListener(e -> web.evaluateJavascript(
                                "msg(" + JSONObject.quote("Scanner unavailable: " + e.getMessage()) + ")", null)));
            }
        }, "Android");
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
        setContentView(web);
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.evaluateJavascript("tick()", null); // refresh codes right away after coming back
    }
}
