/*
 * Copyright (C) 2016 Pedro Paulo de Amorim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.folioreader.android.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.Config;
import com.folioreader.FolioReader;
import com.folioreader.model.HighLight;
import com.folioreader.model.locators.ReadLocator;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadLocatorListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity
        implements OnHighlightListener, ReadLocatorListener,FolioReader.OnClosedListener,
        FolioReader.OnAddWordListener, FolioReader.TranslateAndCheckWordListener, FolioReader.TextToSpeechListener,
        FolioReader.OnDismissPopupListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private FolioReader folioReader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        folioReader = FolioReader.get()
                .setOnHighlightListener(this)
                .setReadLocatorListener(this)
                .setOnClosedListener(this)
                .setOnAddWordListener(this)
                .setTranslateAndCheckListener(this)
                .setTextToSpeechListener(this)
                .setOnDismissPopupListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getHighlightsAndSave();

        findViewById(R.id.btn_raw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Config config = AppUtil.getSavedConfig(getApplicationContext());
                if (config == null)
                    config = new Config();
                config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);

                folioReader.setConfig(config, true)
                        .openBook(R.raw.four);
            }
        });

        findViewById(R.id.btn_assest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReadLocator readLocator = getLastReadLocator();

                Config config = AppUtil.getSavedConfig(getApplicationContext());
                if (config == null)
                    config = new Config();
                config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);
                config.setNightThemeColorInt(Color.parseColor("#FFFFFF"));
                config.setShowRemainingIndicator(true);
                config.setShowTextSelection(false);

                folioReader.setReadLocator(readLocator);
                folioReader.setConfig(config, true)
                        .openBook("file:///android_asset/john.epub");
            }
        });

        Config config = AppUtil.getSavedConfig(getApplicationContext());
        if (config == null)
            config = new Config();
        config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);

        ReadLocator readLocator = getLastReadLocator();
        folioReader.setReadLocator(readLocator);
        folioReader.setConfig(config, true)
                .openBook(R.raw.accessible_epub_3);
    }

    private ReadLocator getLastReadLocator() {

        String jsonString = "{\"bookId\":\"urn:isbn:9781449328030\",\"href\":\"/EPUB/ch02.xhtml\",\"created\":1680065091630,\"locations\":{\"cfi\":\"epubcfi(/0!/4/2[building_a_better_epub]/10/28/10/5:0)\"},\"title\":\"\"}";
//                loadAssetTextAsString("Locators/LastReadLocators/last_read_locator_1.json");
        return ReadLocator.Companion.fromJson(jsonString);
    }

    @Override
    public void saveReadLocator(ReadLocator readLocator) {
//        Log.i(LOG_TAG, "-> saveReadLocator -> " + readLocator.toJson());
    }

    /*
     * For testing purpose, we are getting dummy highlights from asset. But you can get highlights from your server
     * On success, you can save highlights to FolioReader DB.
     */

    private void getHighlightsAndSave() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<HighLight> highlightList = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    highlightList = objectMapper.readValue(
                            loadAssetTextAsString("highlights/highlights_data.json"),
                            new TypeReference<List<HighLight>>() {}
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (highlightList != null) {
                    folioReader.saveReceivedHighLights(highlightList, new OnSaveHighlight() {
                        @Override
                        public void onFinished() {
                            // Do something on success
                        }
                    });
                }
            }
        }).start();
    }

    private String loadAssetTextAsString(String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e("HomeActivity", "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("HomeActivity", "Error closing asset " + name);
                }
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FolioReader.clear();
    }

    @Override
    public void onHighlight(HighLight highlight, HighLight.HighLightAction type) {
        Toast.makeText(this,
                "highlight id = " + highlight.getUUID() + " type = " + type,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFolioReaderClosed(int currentPage, int totalPage, String locator) {
        Log.v(LOG_TAG, "-> onFolioReaderClosed" + currentPage + totalPage);
        final Map<String, Object> mapd = new HashMap<String, Object>();

        mapd.put("currentPage", currentPage);
        mapd.put("totalPage", totalPage);
        mapd.put("readLocator", locator);
        Log.v(LOG_TAG, "-> onFolioReaderClosed" + mapd);
    }

    @Override
    public void onAddWordListener(String word) {
        Log.v(LOG_TAG, "-> onAddWordListener ->" + word);
    }

    @Override
    public void translateAndCheckWordListener(String word) {
        Log.v(LOG_TAG, "-> translateAndCheckWordListener ->" + word);
    }

    @Override
    public void textToSpeechListener(String word) {
        Log.v(LOG_TAG, "-> textToSpeechListener ->" + word);
    }

    @Override
    public void onDismissPopupListener() {
        Log.v(LOG_TAG, "-> onDismissPopupListener ->");
    }

    public void sendTranslateAndCheck(String translate, boolean wordExist) {
        Log.i(LOG_TAG, "-> sendTranslateAndCheck ->" + translate + wordExist);
        folioReader.sendTranslateAndCheckWord(translate, wordExist);
    }
}