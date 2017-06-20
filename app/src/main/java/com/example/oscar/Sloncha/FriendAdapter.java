package com.example.oscar.Sloncha;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by oscar on 23/05/2017.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {


    List<Friend> friends;
    private Context mContext;
    private FragmentManager fragmentManager;

    public FriendAdapter(Context ctx, List<Friend> friends, FragmentManager fragmentManager) {
        mContext = ctx;
        this.friends = friends;
        this.fragmentManager = fragmentManager;

    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_pic_row, viewGroup, false);
        FriendViewHolder holder = new FriendViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder friendViewHolder, int i) {
        final Friend friend = friends.get(i);

        DatabaseReference db_user = FirebaseDatabase.getInstance().getReference().child("Users").child(friend.getUserId());

        db_user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image_uri = dataSnapshot.child("Image").getValue(String.class);
                Picasso.with(mContext).load(image_uri).into(friendViewHolder.imgbutton);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        friendViewHolder.imgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new ProfileFragment();
                Bundle args = new Bundle();
                System.out.println("Friend ID" + friend.getUserId());
                args.putString("userID", friend.getUserId());
                fragment.setArguments(args);
                // Return the fragment manager
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .add(R.id.content_main, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {

        ImageButton imgbutton;

        public FriendViewHolder(View itemView) {
            super(itemView);
            imgbutton = (ImageButton) itemView.findViewById(R.id.imagebutton_profilepic_profile_picrow);
        }
    }
}
