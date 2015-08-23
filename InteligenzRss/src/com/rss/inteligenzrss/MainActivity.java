package com.rss.inteligenzrss;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rss.inteligenzrss.entities.Item;
import com.rss.inteligenzrss.fragments.DetalleFragment;
import com.rss.inteligenzrss.fragments.MainFragment;


public class MainActivity extends ActionBarActivity  implements MainFragment.OnItemSelectedListener{

	private static final int PREFERENCE_REQUEST = 0;

	FrameLayout mFlContent =null;
	
	private String TAGNAME_DETALLE = "fragmentDetalle";
	private MainFragment mMainFragment;
	
	private EditText mTxtSearch;
    
	private ImageView mBtnSearch;
	MenuItem mSearchItem  = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		mFlContent = (FrameLayout) this.findViewById(R.id.flContent);		
		
		ActionBar ab = getSupportActionBar();
		ab.setDisplayShowTitleEnabled(false);
		
		mMainFragment = (MainFragment) getFragmentManager().findFragmentById(R.id.main_fragment);	
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		//Overflow menu is hidden in ActionBar if your device has hardware 'Menu' button. Press it and check - your items should be there :)
		
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_menu, menu);
	    
	   mSearchItem = menu.findItem(R.id.action_search);
	    MenuItemCompat.setOnActionExpandListener(mSearchItem,
	    		new MenuItemCompat.OnActionExpandListener() {
	    		@Override
	    		public boolean onMenuItemActionExpand(MenuItem item) {
	    			mTxtSearch.requestFocus();
	    		return true;
	    		}
	    		@Override
	    		public boolean onMenuItemActionCollapse(MenuItem item) {
	    		
	    		//Must return true to have item collapse
	    		return true;
	    		}
	    		});
	    		

		mBtnSearch = (ImageView) MenuItemCompat.getActionView(mSearchItem).findViewById(R.id.btnSearch);
		mBtnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				searchRSS();
			}
		});
		mTxtSearch = (EditText) MenuItemCompat.getActionView(mSearchItem)
	    		.findViewById(R.id.txtSearch);
		
		mTxtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		        	searchRSS();
		            return true;
		        }
		        return false;
		    }
		});
		
		mTxtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    	
		        if (hasFocus) {
		        	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
		        }
		        else
		        {
		        	imm.hideSoftInputFromWindow(mTxtSearch.getWindowToken(),0); 
		        }
		    }
		});
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_search:
	           // openSearch();
	            return true;
	        case R.id.action_settings:
	           openSettings();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == PREFERENCE_REQUEST) {
			 if (resultCode == RESULT_OK) {
				 mMainFragment.loadRSS();
			 }
		}
	}
	
	
	private void openSettings(){
		Intent _intent = new Intent(this, PrefActivity.class);
		this.startActivityForResult(_intent, PREFERENCE_REQUEST);
	}
	
	@Override
	public void onRssItemSelected(Item item) {
		
		if(mFlContent!=null)
		{
			final FragmentManager fm = getFragmentManager();

			FragmentTransaction ft = fm.beginTransaction();

			DetalleFragment _fragment = DetalleFragment.newInstance(item);		
						
			ft.replace(R.id.flContent, _fragment, TAGNAME_DETALLE);	            
			
			ft.commit();
		}
		else{
			Intent _intent = new Intent(this, DetalleActivity.class);
			_intent.putExtra(DetalleActivity.REQUEST_PARAM_ITEM, item);
	
			startActivity(_intent);		
		}
		
		
	}


	@Override
	public void onHasRss(boolean hasRSS) {
		if (mSearchItem!=null)
		{
			//si no ha y resultados , ocultar el menu de b�squeda
			mSearchItem.setVisible(hasRSS);
		}
	}
	
	
	private void searchRSS(){
		if(mFlContent!=null)
		{
			final FragmentManager fm = getFragmentManager();

			FragmentTransaction ft = fm.beginTransaction();

			Fragment _fragment = fm.findFragmentByTag(TAGNAME_DETALLE);		
			if(_fragment!=null)						
				ft.remove(_fragment);	            
			
			ft.commit();
		}
		mSearchItem.collapseActionView();
		
		
		
		mMainFragment.search(mTxtSearch.getText().toString());
	}

	

}