package com.boo.app.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.gcm.BooGcmListenerService;
import com.boo.app.gcm.BooGcmListenerService.CountListener;
import com.boo.app.library.CircularTextView;
import com.boo.app.model.MenuItemObject;
import com.boo.app.model.UserObject;
import com.boo.app.ui.adapter.MenuAdapter;
import com.boo.app.ui.fragment.AboutFragment;
import com.boo.app.ui.fragment.ActivityFragment;
import com.boo.app.ui.fragment.BootiqueFragment;
import com.boo.app.ui.fragment.ChangePasswordFragment;
import com.boo.app.ui.fragment.ContactUsFragment;
import com.boo.app.ui.fragment.HomeFragment;
import com.boo.app.ui.fragment.SearchFragment;
import com.boo.app.ui.fragment.TrendingFragment;

import java.util.ArrayList;

public class Main extends BaseActivity implements OnClickListener, CountListener {
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ListView mMenuList;
    private LinearLayout mLeftMenu;
    private LinearLayout mMainContent;
    private FrameLayout mContentFrame;
    private CircularTextView tvBadge;

    private ImageView ivHome, ivTrending, ivActivity, ivBootique;

    private String strQuery;

    private BooGcmListenerService mBooGcmListenerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBooGcmListenerService = new BooGcmListenerService();
        mBooGcmListenerService.registerListener(this);

        findViews();
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_search);

        int searchEditId = android.support.v7.appcompat.R.id.search_src_text;
        EditText et = (EditText) searchView.findViewById(searchEditId);
        et.setHint("Search...");
        et.setTextColor(Color.WHITE);
        et.setHintTextColor(Color.WHITE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                strQuery = query.trim();
                displayView(10);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void findViews() {
        toolbar = _findViewById(R.id.main_toolbar);

        mDrawerLayout = _findViewById(R.id.drawer_layout);
        mMenuList     = _findViewById(R.id.menu_list);
        mLeftMenu     = _findViewById(R.id.left_menu);
        mContentFrame = _findViewById(R.id.content_frame);
        mMainContent  = _findViewById(R.id.main_content);

        ivHome     = _findViewById(R.id.iv_main_home);
        ivTrending = _findViewById(R.id.iv_main_trending);
        ivActivity = _findViewById(R.id.iv_main_activity);
        ivBootique = _findViewById(R.id.iv_main_bootique);

        ivHome    .setTag(6);
        ivTrending.setTag(7);
        ivActivity.setTag(8);
        ivBootique.setTag(9);

        tvBadge = _findViewById(R.id.tv_main_badge);
    }

    private void initUI() {
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.isLoading) return;

                if (mDrawerLayout.isDrawerOpen(mLeftMenu))
                    mDrawerLayout.closeDrawer(mLeftMenu);
                else
                    mDrawerLayout.openDrawer(mLeftMenu);
            }
        });

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.str_navigation_drawer_open, // nav drawer open - description for accessibility
                R.string.str_navigation_drawer_close // nav drawer close - description for accessibility
        );
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        setLeftMenu();
        setFragmentSliding();

        ivHome    .setOnClickListener(this);
        ivTrending.setOnClickListener(this);
        ivActivity.setOnClickListener(this);
        ivBootique.setOnClickListener(this);

        tvBadge.setSolidColor("#ff2b4b");
        tvBadge.setStrokeColor("#ffffff");
        tvBadge.setStrokeWidth(1);
        tvBadge.setVisibility(View.INVISIBLE);

        if (App.receivedNotification) {
            App.receivedNotification = false;
            displayView(8);
        } else {
            displayView(6);
        }
    }

    @Override
    public void onClick(View v) {
        if (App.isLoading) return;

        int position = (int) v.getTag();
        displayView(position);
    }

    private void displayView(int position) {
        mDrawerLayout.closeDrawer(mLeftMenu);

        changeButtonColor(position);

        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new AboutFragment();
                break;
//            case 1:
//                fragment = new HistoryFragment();
//                break;
//            case 2:
//                fragment = new ContactUsFragment();
//                break;
            case 3:
                fragment = new ContactUsFragment();
                break;
            case 4:
                fragment = new ChangePasswordFragment();
                break;
            case 5:
                ((App)getApplication()).getCurrentUser().deleteFromDisk(this);
                startActivity(new Intent(this, Welcome.class));
                finish();
                break;
            case 6:
                fragment = new HomeFragment();
                break;
            case 7:
                fragment = new TrendingFragment();
                break;
            case 8:
                BooGcmListenerService.gcm_count = 0;
                tvBadge.setVisibility(View.INVISIBLE);
                fragment = new ActivityFragment();
                break;
            case 9:
                UserObject refUser = ((App)getApplication()).getCurrentUser();
                fragment = BootiqueFragment.newInstance(refUser);
                break;
            case 10:
                Bundle bundle = new Bundle();
                bundle.putString("query", strQuery);
                fragment = new SearchFragment();
                fragment.setArguments(bundle);
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(mContentFrame.getId(), fragment).commit();
        }
    }

    private void setLeftMenu() {
        ArrayList<MenuItemObject> mMenuItems = new ArrayList<>();
        String[] mMenuTitles = getResources().getStringArray(R.array.menu_titles);
        TypedArray mMenuIcons = getResources().obtainTypedArray(R.array.menu_icons);

        for (int i = 0; i < mMenuTitles.length; i++) {
            mMenuItems.add(new MenuItemObject(mMenuIcons.getResourceId(i, -1), mMenuTitles[i]));
        }

        mMenuIcons.recycle();

        mMenuList.setAdapter(new MenuAdapter(getApplicationContext(), mMenuItems));
        mMenuList.setOnItemClickListener(new DrawerListClickListener());
    }

    @Override
    public void getCount(final int count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText(String.valueOf(count));
            }
        });

    }

    private class DrawerListClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }

    private void setFragmentSliding() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (drawerView.equals(mLeftMenu)) {
                    float moveFactor = (mLeftMenu.getWidth() * slideOffset);
                    mMainContent.setTranslationX(moveFactor);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    public void changeButtonColor(int position) {
        boolean bHome, bTrending, bActivity, bBootique;

        bHome     = (int)ivHome    .getTag() == position;
        bTrending = (int)ivTrending.getTag() == position;
        bActivity = (int)ivActivity.getTag() == position;
        bBootique = (int)ivBootique.getTag() == position;

        ivHome    .setColorFilter(ContextCompat.getColor(this, bHome     ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));
        ivTrending.setColorFilter(ContextCompat.getColor(this, bTrending ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));
        ivActivity.setColorFilter(ContextCompat.getColor(this, bActivity ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));
        ivBootique.setColorFilter(ContextCompat.getColor(this, bBootique ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));
    }
}
