package ttblue_android.com.ttblue_android;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DevicesAdapter<BluetoothDevice> extends BaseAdapter {
    private final LayoutInflater mInflater;
    private List<BluetoothDevice> mData;

    public DevicesAdapter(Context context, Set<BluetoothDevice> data) {
        mInflater = LayoutInflater.from(context);
        setData(data);
    }

    public void setData(Set<BluetoothDevice> data) {
        if (data != null) {
            mData.addAll(data);
        } else {
            mData = new ArrayList();
        }
    }

    public void append(BluetoothDevice device) {
        mData.add(device);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mData != null ) {
            return mData.size();
        }
        return 0;
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView result = (TextView) convertView;
        if (result == null) {
            result = (TextView) mInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        BluetoothDevice bt = getItem(position);
        result.setText(bt.toString());
        return result;
    }
}