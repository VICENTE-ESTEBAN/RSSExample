package com.rss.inteligenzrss;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.rss.inteligenzrss.fragments.PrefFragment;

public class PrefActivity extends Activity implements PrefFragment.OnPrefFragmentListener{

	
	boolean mPreferenceChanged = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PrefFragment _prefFragment = new PrefFragment();
		
		FragmentTransaction _transaction = this.getFragmentManager().beginTransaction();
		_transaction.replace(android.R.id.content, _prefFragment);
		_transaction.commit();
		
	}

	@Override
	public void onPreferenceChanged() {
		mPreferenceChanged = true;
	}
	
	
	@Override
	public void finish() {
	
		setResult(mPreferenceChanged?Activity.RESULT_OK:Activity.RESULT_CANCELED);
		
		super.finish();
	}
}
