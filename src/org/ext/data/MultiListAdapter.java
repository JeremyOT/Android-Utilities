package org.ext.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class MultiListAdapter extends BaseAdapter {

    private List<Adapter> adapters = new ArrayList<Adapter>();

    private final ArrayAdapter<String> headers;

    private final List<DataSetObserver> mObservers = new LinkedList<DataSetObserver>();

    private final static int TYPE_SECTION_HEADER = 0;

    public MultiListAdapter(Context context, int headerTextViewResourceID) {
        headers = new ArrayAdapter<String>(context, headerTextViewResourceID);
    }

    public MultiListAdapter(Context context, int headerResourceID, int headerTextViewResourceID) {
        headers = new ArrayAdapter<String>(context, headerResourceID, headerTextViewResourceID);
    }

    public MultiListAdapter(Context context, ArrayAdapter<String> arrayAdapter) {
        headers = arrayAdapter;
    }

    public void clearAdapters() {
        for (Adapter adapter : adapters) {
            for (DataSetObserver observer : mObservers) {
                adapter.unregisterDataSetObserver(observer);
            }
        }
        adapters.clear();
        headers.clear();
    }

    public void addAdapter(String header, Adapter adapter) {
        headers.add(header);
        adapters.add(adapter);
        for (DataSetObserver observer : mObservers) {
            adapter.registerDataSetObserver(observer);
        }
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return getItemViewType(position) != TYPE_SECTION_HEADER;
    }

    public int getCount() {
        int count = 0;
        for (Adapter adapter : adapters) {
            count += adapter.getCount() + 1;
        }
        return count;
    }

    public Object getItem(int position) {
        for (int i = 0; i < headers.getCount(); i++) {
            Adapter adapter = adapters.get(i);
            int size = adapter.getCount();
            if (position == 0)
                return headers.getItem(i);
            position--;
            if (position < size)
                return adapter.getItem(position);
            position -= size;
        }
        return null;
    }

    /**
     * Gets the section title for the given position.
     * 
     * @param position
     *            the position to search.
     * @return The header for the given position.
     */
    public String getSectionForPosition(int position) {
        for (int i = 0; i < headers.getCount(); i++) {
            Adapter adapter = adapters.get(i);
            int size = adapter.getCount();
            if (position == 0)
                return headers.getItem(i);
            position--;
            if (position < size)
                return headers.getItem(i);
            position -= size;
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        int type = 1;
        for (int i = 0; i < headers.getCount(); i++) {
            Adapter adapter = adapters.get(i);
            int size = adapter.getCount();
            if (position == 0)
                return TYPE_SECTION_HEADER;
            position--;
            if (position < size)
                return type + adapter.getItemViewType(position);
            position -= size;
            type += adapter.getViewTypeCount();
        }
        return -1;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionnum = 0;
        for (int i = 0; i < headers.getCount(); i++) {
            Adapter adapter = adapters.get(i);
            int size = adapter.getCount();
            if (position == 0)
                return headers.getView(sectionnum, convertView, parent);
            position--;
            if (position < size)
                return adapter.getView(position, convertView, parent);
            position -= size;
            sectionnum++;
        }
        return null;
    }

    public int getViewTypeCount() {
        int toRet = 1;
        for (Adapter adapter : adapters)
            toRet += adapter.getViewTypeCount();
        return toRet;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mObservers.add(observer);
        for (Adapter adapter : adapters)
            adapter.registerDataSetObserver(observer);
        super.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        for (Adapter adapter : adapters)
            adapter.unregisterDataSetObserver(observer);
        mObservers.remove(observer);
        super.unregisterDataSetObserver(observer);
    }
}
