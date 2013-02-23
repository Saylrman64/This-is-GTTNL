package jp.redmine.redmineclient.adapter;

import java.sql.SQLException;

import jp.redmine.redmineclient.external.lib.LRUCache;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class RedmineBaseAdapter<T> extends BaseAdapter implements LRUCache.IFetchObject<Integer> {
	private static final String TAG = "RedmineBaseAdapter";
	protected LRUCache<Integer,T> cache = new LRUCache<Integer,T>(20);
	protected abstract View getItemView(LayoutInflater infalInflater);
	protected abstract void setupView(View view,T data);
	protected abstract int getDbCount() throws SQLException;
	protected abstract T getDbItem(int position) throws SQLException;
	protected abstract long getDbItemId(T item);

	@Override
	public void notifyDataSetChanged() {
		cache.clear();
		super.notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return getDbItemId(getItemWithCache(position));
	}
	/**
	 * Get item from database via cache
	 * This method catches SQLException.
	 * @return null or item
	 * @deprecated this method is called from BaseAdapter only
	 */
	@Override
	public Object getItem(int position) {
		return getItemWithCache(position);
	}

	/**
	 * Get item from database via cache
	 * This method catches SQLException.
	 * @return null or item
	 */
	protected T getItemWithCache(int position) {
		return cache.getValue(position, this);
	}

	/**
	 * Get item from database
	 * Called from LRUCache class.
	 * This method catches SQLException.
	 * @param position parameter
	 * @return null or item
	 * @deprecated this method is called from IFetchObject only
	 */
	@Override
	public Object getItem(Integer position) {
		try {
			return getDbItem(position);
		} catch (SQLException e) {
			Log.e(TAG,"getDbItem" , e);
			return null;
		}
	}

	/**
	 * Get count from database
	 * This method catches SQLException.
	 * @return 0 or item count
	 */
	@Override
	public int getCount() {
		try {
			return getDbCount();
		} catch (SQLException e) {
			Log.e(TAG,"getDbCount" , e);
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = getItemView(infalInflater);
		}
		if(convertView != null){
			T rec = getItemWithCache(position);
			if(rec == null){
				Log.e(TAG,"getValue returns data is null");
			} else {
				setupView(convertView,rec);
			}
		}
		return convertView;
	}

}
