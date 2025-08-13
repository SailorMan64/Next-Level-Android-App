package jp.redmine.redmineclient.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import java.util.ArrayList;
import java.util.List;

import jp.redmine.redmineclient.R;
import jp.redmine.redmineclient.activity.pager.CorePage;
import jp.redmine.redmineclient.db.cache.DatabaseCacheHelper;
import jp.redmine.redmineclient.fragment.ActivityInterface;
import jp.redmine.redmineclient.fragment.ConnectionList;
import jp.redmine.redmineclient.fragment.ProjectFavoriteList;
import jp.redmine.redmineclient.fragment.RecentIssueList;

public class ConnectionListActivity extends TabActivity<DatabaseCacheHelper>
	implements ActivityInterface {
	public ConnectionListActivity(){
		super();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		if(getSupportActionBar()!=null) {
			ActionBar actionBar = getSupportActionBar();
			actionBar.setTitle(R.string.title_home);

		}

	}
	@Override
	protected List<CorePage> getTabs(){

		List<CorePage> list = new ArrayList<CorePage>();
		list.add((new CorePage<Void>() {
					@Override
					public Fragment getRawFragment(Void param) {
						return ConnectionList.newInstance();
					}
				})
						.setParam(null)
						.setName(getString(R.string.connection))
						.setIcon(R.drawable.ic_cloud)
		);

		list.add((new CorePage<Void>() {
					@Override
					public Fragment getRawFragment(Void param) {
						return ProjectFavoriteList.newInstance();
					}
				})
				.setParam(null)
				.setName(getString(R.string.favorite))
				.setIcon(R.drawable.ic_star)
		);
		list.add((new CorePage<Void>() {
					@Override
					public Fragment getRawFragment(Void param) {
						return RecentIssueList.newInstance();
					}
				})
						.setParam(null)
						.setName(getString(R.string.recent_issues))
						.setIcon(R.drawable.ic_tags)
		);
		return list;
	}
}
