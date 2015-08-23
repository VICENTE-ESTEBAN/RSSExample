package com.rss.inteligenzrss;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.rss.inteligenzrss.entities.Item;
import com.rss.inteligenzrss.fragments.DetalleFragment;

public class DetalleActivity extends Activity{
	
	public static String REQUEST_PARAM_ITEM = "item";
	
	private String TAGNAME_DETALLE = "fragmentDetalle";
	
	private Item mItem = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detalle);
		
		
		if(savedInstanceState!=null)
			mItem = savedInstanceState.getParcelable(REQUEST_PARAM_ITEM);
		else
			mItem = this.getIntent().getParcelableExtra(REQUEST_PARAM_ITEM);
		
		
		
		final FragmentManager fm = getFragmentManager();

		FragmentTransaction ft = fm.beginTransaction();
		
		DetalleFragment _fragment = DetalleFragment.newInstance(mItem);		
					
		ft.add(R.id.flContent, _fragment, TAGNAME_DETALLE);	            
		
		ft.commit();
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		
		outState.putParcelable(REQUEST_PARAM_ITEM, mItem);
	}
	
}
