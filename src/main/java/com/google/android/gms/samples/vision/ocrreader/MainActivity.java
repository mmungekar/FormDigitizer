/*
 * Copyright (C) The Android Open Source Project
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

package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import android.speech.RecognizerIntent;

import java.util.Arrays;
import java.util.Locale;
import android.content.ActivityNotFoundException;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * recognizes text.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    // Use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView textValue;
    private Button readTextButton;
    private Button speechButton;

    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        textValue = (TextView)findViewById(R.id.text_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        readTextButton = (Button) findViewById(R.id.read_text);
        readTextButton.setOnClickListener(this);
        speechButton = (Button) findViewById(R.id.speech_button);
        speechButton.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_text) {
            // launch Ocr capture activity.
            Intent intent = new Intent(this, OcrCaptureActivity.class);
            intent.putExtra(OcrCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_OCR_CAPTURE);
        }
        if (v.getId() == R.id.speech_button) {
            // launch Ocr capture activity.
            startSpeechToText();
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    statusMessage.setText(R.string.ocr_success);
                    textValue.setText(text);
                    Log.d(TAG, "Text read: " + text);
                } else {
                    statusMessage.setText(R.string.ocr_failure);
                    Log.d(TAG, "No Text captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.ocr_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else if (requestCode==1) {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    List<String> listofcrap = Arrays.asList("black bean burger","carrots", "ham sandwich","chicken foccacia","black bean soup");
                    String s = (String) findClosestMatch(listofcrap, text);
                    textValue.setText(s);
                }

        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Start speech to text intent. This opens up Google Speech Recognition API dialog box to listen the speech input.
     * */
    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak something...");
        try {
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Speech recognition is not supported in this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public static Object findClosestMatch(Collection<?> collection, Object target) {
        int distance = Integer.MAX_VALUE;
        Object closest = null;
        for (Object compareObject : collection) {
            int currentDistance = StringUtils.getLevenshteinDistance(compareObject.toString(), target.toString());
            if(currentDistance < distance) {
                distance = currentDistance;
                closest = compareObject;
            }
        }
        return closest;
    }
}
