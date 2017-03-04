package com.adnaga;

import org.apache.cordova.*;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;

public class AdnagaChartboost implements IPlugin {
    private static final String LOG_TAG = "Adnaga-Chartboost";

    private Adnaga _adnaga;

    public String getNetworkName() {
        return "cb";
    }

    public void init(final String pid, Adnaga adnaga) {
        _adnaga = adnaga;
        Log.w(LOG_TAG, "chartboost ads is enabled. initing. appId_signatureId=" + pid);

        if ((pid != null) && (!"".equals(pid)) && (!"null".equals(pid))) {
            _adnaga.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] tokens = pid.split("_");
                    Activity act = _adnaga.getActivity();
                    Chartboost.setAutoCacheAds(false);
                    Chartboost.startWithAppId(act, tokens[0] /* appid */, tokens[1] /* signature */);
                    Chartboost.setDelegate(new MyChartboostListener());
                    Chartboost.onCreate(act);
                    Chartboost.onStart(act);
                }
            });
        }
        Log.i(LOG_TAG, "admaga-cb inited");
    }

    public void loadAds(final String pid) {
        _adnaga.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "Trying to load Chartboost ads");
                Chartboost.cacheInterstitial(CBLocation.LOCATION_DEFAULT);
            }
        });
    }

    public void showAds(final CallbackContext callbackContext) {
        _adnaga.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "Trying to show chartboost ads");
                if (Chartboost.hasInterstitial(CBLocation.LOCATION_DEFAULT)) {
                    Chartboost.showInterstitial(CBLocation.LOCATION_DEFAULT);
                } else {
                    Log.e(LOG_TAG, "Chartboost interstitial not ready, cannot show");
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, "chartboost interstitial not ready, cannot show");
                    callbackContext.sendPluginResult(result);
                }
            }
        });
    }

    public void onPause() {

    }

    public void onResume() {

    }

    private class MyChartboostListener extends ChartboostDelegate {
        @Override
        public void didDisplayInterstitial(String location) {
            _adnaga.sendAdsEventToJs("cb", "START", location);
        }
        // when ads is loaded, this will be called
        @Override
        public void didCacheInterstitial(String location) {
            _adnaga.sendAdsEventToJs("cb", "READY", location);
        }
        @Override
        public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
            _adnaga.sendAdsEventToJs("cb", "LOADERROR", String.valueOf(error));
        }
        @Override
        public void didDismissInterstitial(String location) {
            _adnaga.sendAdsEventToJs("cb", "FINISH", location);
        }
        @Override
        public void didClickInterstitial(String location) {
            _adnaga.sendAdsEventToJs("cb", "CLICK", location);
        }
    }
}