/**
 * @file
 * @brief menu
 * @note
 * - AndroidManifest.xmlにActivity登録が必要(.MyPrefs)
 *
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */


package jp.aws.test;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MyPrefs extends PreferenceActivity {
	public static final String AMI_FILTER = "ami_filter";
	public static final String REGION = "region";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefes);
	}
}
