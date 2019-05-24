package com.anull.youchat;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionsPageAdapter extends FragmentPagerAdapter {

    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int pos) {

        switch(pos)
        {
            case 0:
                Requests req = new Requests();
                return req;
            case 1:
                Chats chat = new Chats();
                return  chat;
            case 2:
                Friends friend = new Friends();
                return  friend;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);

        switch (position)
        {
            case 0:
                return "REQUESTS";
            case 1:
                return  "CHATS";
            case 2:
                return  "FRIENDS";
            default:
                return null;
        }
    }

}

