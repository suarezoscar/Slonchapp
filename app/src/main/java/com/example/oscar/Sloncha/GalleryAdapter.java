package com.example.oscar.Sloncha;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by oscar on 11/06/2017.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {


    private static String LOGGED_USER_ID;
    List<Gallery> galleryList;
    private Context mContext;
    private FragmentManager fragmentManager;
    private DatabaseReference mDatabaseUserPosts;
    private DatabaseReference mDatabaseUsers;

    public GalleryAdapter(Context ctx, List<Gallery> galleryList, FragmentManager fragmentManager, String LOGGED_USER_ID) {
        mContext = ctx;
        this.galleryList = galleryList;
        this.fragmentManager = fragmentManager;
        this.LOGGED_USER_ID = LOGGED_USER_ID;

    }

    @Override
    public GalleryAdapter.GalleryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pic_row, viewGroup, false);
        GalleryAdapter.GalleryViewHolder holder = new GalleryAdapter.GalleryViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final GalleryAdapter.GalleryViewHolder galleryViewHolder, int i) {
        final Gallery gallery = galleryList.get(i);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUserPosts = FirebaseDatabase.getInstance().getReference().child("Posts");

        Picasso.with(mContext).load(gallery.getImage()).fit().into(galleryViewHolder.person_image);

        galleryViewHolder.person_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new CommentFragment();
                Bundle args = new Bundle();
                args.putString("UID", LOGGED_USER_ID);
                args.putString("POSTID", gallery.getPostId());
                fragment.setArguments(args);

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .add(R.id.content_main, fragment)
                        .addToBackStack(null)
                        .commit();

            }
        });

        galleryViewHolder.person_image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (LOGGED_USER_ID.equals(gallery.getUserId())) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    Toast.makeText(mContext, "Correctly Deleted", Toast.LENGTH_SHORT).show();
                                    mDatabaseUserPosts.orderByChild("image").equalTo(gallery.getImage()).addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot post : dataSnapshot.getChildren()) {
                                                post.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure you want to delete?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {

        ImageButton person_image;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            person_image = (ImageButton) itemView.findViewById(R.id.imagebutton_pic_picrow);
        }
    }

}
