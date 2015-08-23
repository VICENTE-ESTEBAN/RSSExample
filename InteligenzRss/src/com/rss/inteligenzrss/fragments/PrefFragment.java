package com.rss.inteligenzrss.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rss.inteligenzrss.R;

public class PrefFragment extends PreferenceFragment  implements OnSharedPreferenceChangeListener{
	
	private OnPrefFragmentListener mlistener = null;
	public interface OnPrefFragmentListener {
		public void onPreferenceChanged();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.addPreferencesFromResource(R.xml.preference);
		
		ListPreference _lp = (ListPreference) findPreference(getResources().getString(R.string.KEY_PREFERENCE));
		CharSequence currText = _lp.getEntry();
		//String currValue = _lp.getValue();
				
        _lp.setSummary(getResources().getString(R.string.PreferenceSummary).concat(currText.toString()));
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		if (activity instanceof OnPrefFragmentListener) {
			mlistener = (OnPrefFragmentListener) activity;
		}
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
	    //view.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));

	    return view;
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
	    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	    super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		ListPreference _lp = (ListPreference) findPreference(key);
		CharSequence currText = _lp.getEntry();
		//String currValue = _lp.getValue();
		
		_lp.setSummary(currText); // required or will not update
        _lp.setSummary(getResources().getString(R.string.PreferenceSummary).concat(currText.toString()));
        
        if(mlistener!=null)
        	mlistener.onPreferenceChanged();
        
	}
}