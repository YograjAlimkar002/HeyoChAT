package com.example.heyochat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private View grouFragmentView;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String>list_of_group=new ArrayList<>();
    private DatabaseReference GroupRef;


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        grouFragmentView= inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        InitializeFields();

        RetrieveAndDisplayGroups();

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String currentGroupName=adapterView.getItemAtPosition(position).toString(); //get the name of clcivked group from screen
                Intent groupChatIntent =new Intent(getContext(),GroupChatActivity.class);
                /////get context because of fragment..for activity put activt nam
                groupChatIntent.putExtra("groupName",currentGroupName);
                startActivity(groupChatIntent);

            }
        });


        return grouFragmentView;
    }


    private void InitializeFields()
    {
        list_view=(ListView)grouFragmentView.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_group);
        list_view.setAdapter(arrayAdapter);
    }

    private void RetrieveAndDisplayGroups()
    {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();                 //DATA DUPLICATE PRINT NA HO FOR TAT...PR ELSE IL ORINT SAME NAMES
                Iterator iterator= dataSnapshot.getChildren().iterator();
                while (iterator.hasNext())

                {
                    set.add(((DataSnapshot)iterator.next()).getKey());

                }
                list_of_group.clear();
                list_of_group.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
