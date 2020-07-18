package com.example.heyochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 1:
                ContacsFragment contacsFragment=new ContacsFragment();
                return contacsFragment;
            case 2:
                GroupsFragment groupsFragment=new GroupsFragment();
                return groupsFragment;

                case 3:
                RequestsFragment requestsFragment=new RequestsFragment();
                return requestsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
                case 1:
                    return "Contacs";

            case 2:
                 return "Groups";
            case 3:
                return "Requests";
            default:
                return null;
        }
    }
}
