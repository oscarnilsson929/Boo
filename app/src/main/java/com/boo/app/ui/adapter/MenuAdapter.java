package com.boo.app.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boo.app.R;
import com.boo.app.model.MenuItemObject;

import java.util.ArrayList;

public class MenuAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<MenuItemObject> menuItems;

    public MenuAdapter(Context context, ArrayList<MenuItemObject> drawerItems){
        this.context = context;
        this.menuItems = drawerItems;
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_drawer_item, null);
        }

        TextView mMenuTitle = (TextView) convertView.findViewById(R.id.tv_menu_title);
        mMenuTitle.setText(menuItems.get(position).getTitle());

        ImageView mMenuIcon = (ImageView) convertView.findViewById(R.id.iv_menu_icon);
        mMenuIcon.setImageResource(menuItems.get(position).getIcon());

        return convertView;
    }
}
