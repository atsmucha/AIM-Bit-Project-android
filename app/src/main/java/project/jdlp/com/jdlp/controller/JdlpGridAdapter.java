package project.jdlp.com.jdlp.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;

import project.jdlp.com.jdlp.manager.DataHandler;
import project.jdlp.com.jdlp.model.JdlpItem;
import project.jdlp.com.jdlp.view.GridItemView;

/**
 * Created by atsmucha on 15. 7. 9.
 */
public class JdlpGridAdapter extends BaseAdapter{
    Context context;
    List<JdlpItem> items = new ArrayList<>();

    public JdlpGridAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public JdlpItem getItem(int position) {
        return items.get(position);
    }

    public List<JdlpItem> getItems() {
        return items;
    }

    public void setItems(List<JdlpItem> items) {
        this.items = items;
    }

    @Override
    public long getItemId(int position) {
        return positioning(position);
    }

    public JdlpItem getItemJdlpPosition(int position) {
        return items.get(positioning(position));
    }

    private Integer positioning(int position) {
        switch(position) {
            case 0:
                return 6;
            case 1:
                return 7;
            case 2:
                return 8;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 0;
            case 7:
                return 1;
            case 8:
                return 2;
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridItemView view;
        if(convertView != null) {
            view = (GridItemView)convertView;
        } else {
            view = new GridItemView(context);
        }

        view.setData(items.get(position));
        return view;
    }

    public void updateItem(JdlpItem jdlpItems) {
        JdlpItem item = this.getItem(jdlpItems.getItemPosition());
        item.setItemTitle(jdlpItems.getItemTitle());
        item.setItemBitmap(jdlpItems.getItemBitmap());
        notifyDataSetChanged();
    }

    public void updateItems(List<JdlpItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void updateItemTitle(int position, String title) {
        JdlpItem item = this.getItem(position);
        item.setItemTitle(title);
        notifyDataSetChanged();
    }

    public void updateItemImage(int position, Bitmap itemBitmap) {
        JdlpItem item = this.getItem(position);
        item.setItemBitmap(itemBitmap);
        notifyDataSetChanged();
    }

    public void addItem(JdlpItem data) {
        if(this.items.size() > 9) {
            Log.e("JdlpGridAdapter::", "9개이상 못넣는다");
            return;
        }

        this.items.add(data);
        notifyDataSetChanged();
    }

    public void addAll(List<JdlpItem> data) {
        if(data.size() > 9) {
            Log.e("JdlpGridAdapter::", "9개이상 못넣는다");
            return;
        }
        this.items.removeAll(items);
        this.items.addAll(data);

        notifyDataSetChanged();
    }

}
