package jp.redmine.redmineclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

import com.j256.ormlite.android.apptools.OrmLiteFragmentActivity;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

import java.util.List;

import jp.redmine.redmineclient.R;
import jp.redmine.redmineclient.activity.handler.AttachmentActionHandler;
import jp.redmine.redmineclient.activity.handler.AttachmentActionInterface;
import jp.redmine.redmineclient.activity.handler.ConnectionActionInterface;
import jp.redmine.redmineclient.activity.handler.ConnectionListHandler;
import jp.redmine.redmineclient.activity.handler.Core.ActivityRegistry;
import jp.redmine.redmineclient.activity.handler.IssueActionInterface;
import jp.redmine.redmineclient.activity.handler.IssueViewHandler;
import jp.redmine.redmineclient.activity.handler.TimeEntryHandler;
import jp.redmine.redmineclient.activity.handler.TimeentryActionInterface;
import jp.redmine.redmineclient.activity.handler.WebviewActionInterface;
import jp.redmine.redmineclient.activity.helper.ActivityHelper;
import jp.redmine.redmineclient.activity.pager.CorePage;
import jp.redmine.redmineclient.activity.pager.CorePager;
import jp.redmine.redmineclient.fragment.ActivityInterface;

import static jp.redmine.redmineclient.R.id.pager;

abstract class TabActivity<T extends OrmLiteSqliteOpenHelper> extends OrmLiteFragmentActivity<T>
	implements ActivityInterface {
	abstract protected List<CorePage> getTabs();
	public TabActivity(){
		super();
	}

	//private Toolbar toolbar;
	//private TabLayout tabLayout;
	//private ViewPager viewPager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ActivityHelper.setupTheme(this);
		super.onCreate(savedInstanceState);

		//Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		//setSupportActionBar(toolbar);

		//getSupportActionBar().setTitle("Tis Is GTTNL");


		final ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.page_fragment_pager);

		List<CorePage> list = getTabs();

		//toolbar = (Toolbar) findViewById(R.id.toolbar);
		//setSupportActionBar(toolbar);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);



		//viewPager = (ViewPager) findViewById(R.id.mPager);
		//setupViewPager(mPager);

		//tabLayout = (TabLayout) findViewById(R.id.tabs);
		//tabLayout.setupWithViewPager(viewPager);

		TabLayout   tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
		//setSupportActionBar(toolbar);


//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#1fa0e3")));
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setLogo(R.drawable.ic_logo_toolbar_padded);  // <-- square in ActionBar
		actionBar.setDisplayUseLogoEnabled(true);
		//actionBar.setDisplayHomeAsUpEnabled(true);

		final ViewPager mPager = (ViewPager) findViewById(pager);
		//final ViewPager view_pager = (ViewPager) findViewById(R.id.pager);
		//final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
		//mPager.setAdapter(adapter);
		mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));




		/** Defining a listener for pageChange */
		/*ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				TabLayout.setSelectedNavigationItem(position);
			}
		};*/

		/** Setting the pageChange listner to the viewPager */
		//mPager.setOnPageChangeListener(pageChangeListener);

		mPager.setAdapter(new CorePager(getSupportFragmentManager(), list));





		tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
// called when tab selected
				mPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
// called when tab unselected
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
// called when a tab is reselected
			}
		});

	//	TabLayout.TabListener listener = new TabLayout.TabListener() {
		//	@Override
		//	public void onTabSelected(TabLayout.Tab tab, FragmentTransaction fragmentTransaction) {
		//		mPager.setCurrentItem(tab.getPosition());
		//	}

		//	@Override
		//	public void onTabUnselected(TabLayout.Tab tab, FragmentTransaction fragmentTransaction) {

		//	}

		//	@Override
			//public void onTabReselected(TabLayout.Tab tab, FragmentTransaction fragmentTransaction) {

			//}
	//	};




		for(CorePage item : list){

			TabLayout.Tab tab = tabLayout.newTab();
			//ActionBar.Tab tab = actionBar.newTab();
			tab.setText(item.getName());
			//tab.setTabListener(listener);

			if (item.getIcon() != null)
				tab.setIcon(item.getIcon());

			tabLayout.addTab(tab, item.isDefault());
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T getHandler(Class<T> cls){
		ActivityRegistry registry = new ActivityRegistry(){

			@Override
			public FragmentManager getFragment() {
				return getSupportFragmentManager();
			}

			@Override
			public Intent getIntent(Class<?> activity) {
				return new Intent(getApplicationContext(),activity);
			}

			@Override
			public void kickActivity(Intent intent) {
				startActivity(intent);
			}

		};
		if(cls.equals(ConnectionActionInterface.class))
			return (T) new ConnectionListHandler(registry);
		if(cls.equals(WebviewActionInterface.class))
			return (T) new IssueViewHandler(registry);
		if(cls.equals(IssueActionInterface.class))
			return (T) new IssueViewHandler(registry);
		if(cls.equals(TimeentryActionInterface.class))
			return (T) new TimeEntryHandler(registry);
		if(cls.equals(AttachmentActionInterface.class))
			return (T) new AttachmentActionHandler(registry);
		return null;
	}
}
