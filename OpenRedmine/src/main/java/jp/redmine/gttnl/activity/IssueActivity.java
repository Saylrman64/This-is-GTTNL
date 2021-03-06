package jp.redmine.gttnl.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.redmine.gttnl.R;
import jp.redmine.gttnl.activity.handler.IssueActionInterface;
import jp.redmine.gttnl.activity.pager.CorePage;
import jp.redmine.gttnl.db.cache.DatabaseCacheHelper;
import jp.redmine.gttnl.db.cache.RedmineIssueModel;
import jp.redmine.gttnl.db.cache.RedmineProjectModel;
import jp.redmine.gttnl.entity.RedmineIssue;
import jp.redmine.gttnl.entity.RedmineProject;
import jp.redmine.gttnl.fragment.ActivityInterface;
import jp.redmine.gttnl.fragment.IssueEdit;
import jp.redmine.gttnl.fragment.IssueView;
import jp.redmine.gttnl.fragment.IssueWatcherList;
import jp.redmine.gttnl.fragment.TimeEntryEdit;
import jp.redmine.gttnl.param.IssueArgument;
import jp.redmine.gttnl.param.TimeEntryArgument;

public class IssueActivity extends TabActivity<DatabaseCacheHelper>
	implements ActivityInterface {
	private static final String TAG = IssueActivity.class.getSimpleName();
	public IssueActivity(){
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected List<CorePage> getTabs(){

		IssueArgument intent = new IssueArgument();
		intent.setIntent(getIntent());

		// setup navigation
		boolean isEditable = true;
		try {
			RedmineProject proj = null;
			if(intent.getProjectId() < 0){
				RedmineIssueModel mIssue = new RedmineIssueModel(getHelper());
				RedmineIssue issue = mIssue.fetchById(intent.getConnectionId(), intent.getIssueId());
				proj = issue.getProject();
			}
			if(proj == null){
				RedmineProjectModel mProject = new RedmineProjectModel(getHelper());
				proj = mProject.fetchById(intent.getProjectId());
			}
			if(proj != null && proj.getId() != null){
				setTitle(proj.getName());
				isEditable = proj.getStatus().isUpdateable();
			}
		} catch (SQLException e) {
			Log.e(TAG, "getTabs", e);
		}

		boolean isValidIssue = intent.getIssueId() > 0;

		List<CorePage> list = new ArrayList<CorePage>();
		if(isValidIssue) {
			// Issue view
			IssueArgument argList = new IssueArgument();
			argList.setArgument();
			argList.importArgument(intent);
			list.add((new CorePage<IssueArgument>() {
						@Override
						public Fragment getRawFragment(IssueArgument param) {
							return IssueView.newInstance(param);
						}

					})
							.setParam(argList)
							.setName(getString(R.string.ticket_issue))
							.setIcon(R.drawable.ic_tag)
			);
			// Watchers
			IssueArgument argWatchers = new IssueArgument();
			argWatchers.setArgument();
			argWatchers.importArgument(intent);
			list.add((new CorePage<IssueArgument>() {
						@Override
						public Fragment getRawFragment(IssueArgument param) {
							return IssueWatcherList.newInstance(param);
						}
					})
					.setParam(argWatchers)
					.setName(getString(R.string.ticket_watcher))
					.setIcon(R.drawable.ic_watchers)
			);
		}
		if(isValidIssue && isEditable){
			// Time Entry
			TimeEntryArgument argTimeentry = new TimeEntryArgument();
			argTimeentry.setArgument();
			argTimeentry.importArgument(intent);
			list.add((new CorePage<TimeEntryArgument>() {
						@Override
						public Fragment getRawFragment(TimeEntryArgument param) {
							return TimeEntryEdit.newInstance(param);
						}
				})
				.setParam(argTimeentry)
				.setName(getString(R.string.ticket_time))
				.setIcon(R.drawable.ic_time)
			);
		}

		if(isEditable) {
			IssueArgument argEdit = new IssueArgument();
			argEdit.setArgument();
			argEdit.importArgument(intent);
			list.add((new CorePage<IssueArgument>() {
						@Override
						public Fragment getRawFragment(IssueArgument param) {
							return IssueEdit.newInstance(param);
						}
					})
							.setParam(argEdit)
							.setName(getString(R.string.edit))
							.setIcon(R.drawable.ic_mode_edit)
			);
		}


		return list;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:

				IssueArgument intent = new IssueArgument();
				intent.setIntent(getIntent());
				if(intent.getProjectId() < 0){
					RedmineProject proj = null;
					try {
						RedmineIssueModel mIssue = new RedmineIssueModel(getHelper());
						RedmineIssue issue = mIssue.fetchById(intent.getConnectionId(), intent.getIssueId());
						proj = issue.getProject();
					} catch (SQLException e) {
						Log.e(TAG, "onOptionsItemSelected", e);
					}
					if(proj != null && proj.getId() != null){
						IssueActionInterface handler = getHandler(IssueActionInterface.class);
						handler.onIssueList(intent.getConnectionId(),  proj.getId());
						finish();
					}
				} else {
					finish();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
