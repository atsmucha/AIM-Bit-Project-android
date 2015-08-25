package project.jdlp.com.jdlp.controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by atsmucha on 15. 7. 9.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter{
    private List<Fragment> jdlpFragmentList;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> jdlpFragmentList) {
        super(fm);
        this.jdlpFragmentList = jdlpFragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return jdlpFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return jdlpFragmentList.size();
    }
}
