/*
 * Copyright (C) 2016 Shanghai Xiaohong.Inc
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
package com.xiaohong.kulian.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.SpannableString;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;

/**
 * @author andrew.wang
 * @date    2010-9-3
 *
 */
public class ClientPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Session session = Session.get(getApplicationContext());
        addPreferencesFromResource(R.xml.preferences);
        
        Preference prefTele = findPreference("tele");
        SpannableString tel = new SpannableString(getString(R.string.tele_number));
        prefTele.setSummary(tel);
        Preference prefFeedback = findPreference("feedback");
        SpannableString feedback = new SpannableString(getString(R.string.feedback_email));
        prefFeedback.setSummary(feedback);
        prefFeedback.setOnPreferenceClickListener(clickListener);
        Preference appInfo = findPreference("app_info");
        appInfo.setSummary(session.getVersionName());
    }
    
    @Override
    protected void onResume() {
		super.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    private OnPreferenceClickListener clickListener = new OnPreferenceClickListener() {
        
        @Override
        public boolean onPreferenceClick(Preference preference) {
            
            if("feedback".equals(preference.getKey())) {
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, 
                        new String[]{"kulian.support@mappn.com"});
                try {
                    // 2011/2/25 fix bug
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    // no activity can handle this intent
                }
            }
            return false;
        }
    };
}
