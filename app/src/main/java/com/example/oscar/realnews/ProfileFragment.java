package com.example.oscar.realnews;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    private static final String ARG_PARAM1 = "userID";
    //<editor-fold desc="Variables">
    private static Boolean IS_FRIEND = false;
    private static String LOGGEDUSERID;
    private static InputMethodManager imm;
    private FirebaseAuth mAuth;
    private FriendAdapter friendRequestAdapter;
    private FriendAdapter friendAdapter;
    private List friendRequest;
    private List friends;
    private DatabaseReference mDatabaseUserProfile;
    private DatabaseReference mDatabaseUserPosts;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseFriends;
    private DatabaseReference mDatabaseFriendRequest;
    private RecyclerView mRecyclerViewUserPost;
    private RecyclerView mRecyclerViewFriends;
    private RecyclerView mRecyclerViewRequest;
    private boolean clicked = false;
    private Query mQueryUserPosts;
    private Query mQueryFriend;
    private LinearLayout linearLayout_friends;
    private LinearLayout linearLayout_requests;
    private LinearLayout linear_contactprofile;
    private LinearLayout linear_gallery;
    private String userID;
    private TextView username;
    private TextView userdesc;
    private ImageView userpic;
    private ProgressDialog mProgress;
    private EditText edit_desc;
    private ImageButton button_desc;
    private ImageButton button_addfriend;
    private ImageButton button_privatemessage;
    private ImageButton button_acceptFriend;
    private ImageButton button_declineFriend;
    private ImageButton button_cancelfriend;
    private LinearLayout linear_desc;
    private OnFragmentInteractionListener mListener;
    private ImageButton mButtonProfileDesc;
    //</editor-fold>

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Post, ProfileViewHolder> firebaseRecyclerAdapterPosts = new FirebaseRecyclerAdapter<Post, ProfileViewHolder>(

                Post.class,
                R.layout.pic_row,
                ProfileViewHolder.class,
                mQueryUserPosts
        ) {
            @Override
            protected void populateViewHolder(ProfileViewHolder viewHolder, final Post model, int position) {
                viewHolder.setImage(getContext(), model.getImage());
                viewHolder.image_picrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //comments fragments

                        mDatabaseUserPosts.orderByChild("image").equalTo(model.getImage()).addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot post : dataSnapshot.getChildren()) {
                                    Fragment fragment = new CommentFragment();
                                    Bundle args = new Bundle();
                                    args.putString("UID", mAuth.getCurrentUser().getUid());
                                    args.putString("POSTID", post.getRef().getKey());
                                    fragment.setArguments(args);

                                    getFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                            .replace(R.id.content_main, fragment)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                viewHolder.image_picrow.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mAuth.getCurrentUser().getUid().equals(model.getUserId())) {

                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            //Yes button clicked
                                            Toast.makeText(getContext(), "Correctly Deleted", Toast.LENGTH_SHORT).show();
                                            mDatabaseUserPosts.orderByChild("image").equalTo(model.getImage()).addListenerForSingleValueEvent(new ValueEventListener() {

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
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Are you sure you want to delete?").setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();
                        }
                        return true;
                    }

                });
            }

        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        if (LOGGEDUSERID.equals(userID) || IS_FRIEND) {
            linear_gallery.setVisibility(View.VISIBLE);
            mRecyclerViewUserPost.setLayoutManager(layoutManager);
            mRecyclerViewUserPost.setNestedScrollingEnabled(false);
            mRecyclerViewUserPost.setHasFixedSize(false);
            mRecyclerViewUserPost.setLayoutManager(new GridLayoutManager(getContext(), 3));
            mRecyclerViewUserPost.setAdapter(firebaseRecyclerAdapterPosts);
        }

        mRecyclerViewRequest.setLayoutManager(layoutManager);
        mRecyclerViewRequest.setNestedScrollingEnabled(false);
        mRecyclerViewRequest.setHasFixedSize(false);
        mRecyclerViewRequest.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewRequest.setAdapter(friendRequestAdapter);

        mRecyclerViewFriends.setLayoutManager(layoutManager);
        mRecyclerViewFriends.setNestedScrollingEnabled(false);
        mRecyclerViewFriends.setHasFixedSize(false);
        mRecyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewFriends.setAdapter(friendAdapter);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userID = getArguments().getString(ARG_PARAM1);
        }

        mDatabaseFriends = FirebaseDatabase.getInstance().getReference().child("Friends");
        mDatabaseUserProfile = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUserPosts = FirebaseDatabase.getInstance().getReference().child("Posts");
        mQueryUserPosts = mDatabaseUserPosts.orderByChild("userId").equalTo(userID);

        mDatabaseFriendRequest = FirebaseDatabase.getInstance().getReference().child("Friends").child(userID);


        //if this profile is the actual logged user profile.
        if (userID.equals(LOGGEDUSERID)) {

            mDatabaseFriendRequest.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    friendRequest.removeAll(friendRequest);
                    friends.removeAll(friends);
                    for (DataSnapshot d : dataSnapshot.getChildren()) {

                        //if some request peding
                        if (d.getValue().equals("pending")) {
                            linearLayout_requests.setVisibility(View.VISIBLE);
                            friendRequest.add(new Friend(d.getKey()));
                            System.out.println(friendRequest.size() + " peticiones");
                        }
                        if (d.getValue().equals(true)) {
                            friends.add(new Friend(d.getKey()));
                            linearLayout_friends.setVisibility(View.VISIBLE);
                            System.out.println(friends.size() + " amigos");
                        }
                    }
                    friendRequestAdapter.notifyDataSetChanged();
                    friendAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            mDatabaseFriendRequest.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(LOGGEDUSERID).exists() && dataSnapshot.child(LOGGEDUSERID).getValue().equals(true))
                    {
                        IS_FRIEND = true;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        LOGGEDUSERID = mAuth.getCurrentUser().getUid();
        friendRequest = new ArrayList();
        friends = new ArrayList();
        friendRequestAdapter = new FriendAdapter(getContext(), friendRequest, getFragmentManager());
        friendAdapter = new FriendAdapter(getContext(), friends, getFragmentManager());
        linearLayout_friends = (LinearLayout) view.findViewById(R.id.Linearlayout_friends);
        linearLayout_requests = (LinearLayout) view.findViewById(R.id.Linearlayout_requests);
        linear_gallery = (LinearLayout) view.findViewById(R.id.Linearlayout_gallery);
        username = (TextView) view.findViewById(R.id.username_profileFragment);
        userdesc = (TextView) view.findViewById(R.id.userdesc_profileFragment);
        userpic = (ImageView) view.findViewById(R.id.pic_profileFragment);
        linear_desc = (LinearLayout) view.findViewById(R.id.linearlayout_editdesc_profileFragment);
        button_desc = (ImageButton) view.findViewById(R.id.button_editDesc_profileFragment);
        linear_contactprofile = (LinearLayout) view.findViewById(R.id.layout_contactprofile);
        button_addfriend = (ImageButton) view.findViewById(R.id.button_add_friend);
        button_acceptFriend = (ImageButton) view.findViewById(R.id.button_accept_friend);
        button_declineFriend = (ImageButton) view.findViewById(R.id.button_decline_friend);
        button_cancelfriend = (ImageButton) view.findViewById(R.id.button_cancel_friend);
        button_privatemessage = (ImageButton) view.findViewById(R.id.button_private_message);

        mRecyclerViewRequest = (RecyclerView) view.findViewById(R.id.Recyclerview_requests);
        mRecyclerViewRequest.setHasFixedSize(true);
        mRecyclerViewRequest.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerViewFriends = (RecyclerView) view.findViewById(R.id.Recyclerview_friends);
        mRecyclerViewFriends.setHasFixedSize(true);
        mRecyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerViewUserPost = (RecyclerView) view.findViewById(R.id.Recyclerview_profile);
        mRecyclerViewUserPost.setHasFixedSize(true);
        mRecyclerViewUserPost.setLayoutManager(new LinearLayoutManager(getContext()));


        mProgress = new ProgressDialog(getContext());
        edit_desc = (EditText) view.findViewById(R.id.editText_description_profileFragment);
        button_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDesc();
            }
        });


        button_cancelfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clicked = true;

                mDatabaseFriends.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        if (clicked) {
                            if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals("waiting") ||
                                    dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals(true)) {
/*
                                            CANCELAR UNA SOLICITUD DE AMISTAD
 */
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //Yes button clicked
                                                mDatabaseFriends.child(userID)
                                                        .child(mAuth.getCurrentUser().getUid())
                                                        .setValue(false);

                                                mDatabaseFriends.child(mAuth.getCurrentUser().getUid())
                                                        .child(userID)
                                                        .setValue(false);

                                                Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                if (dataSnapshot.child(userID).child(mAuth.getCurrentUser().getUid()).getValue().equals("waiting")) {
                                    builder.setMessage("Cancel friendship\nAre you sure?").setPositiveButton("Yes", dialogClickListener)
                                            .setNegativeButton("No", dialogClickListener).show();
                                } else {
                                    builder.setMessage("Cancel friend request?").setPositiveButton("Yes", dialogClickListener)
                                            .setNegativeButton("No", dialogClickListener).show();
                                }
                            }

                        }
                        clicked = false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println(databaseError.toString());
                    }
                });

            }
        });

        button_acceptFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clicked = true;

                mDatabaseFriends.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (clicked) {
                            if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals("pending")) {
/*
                                            ACEPTAR UNA SOLICITUD DE AMISTAD
 */
                                mDatabaseFriends.child(mAuth.getCurrentUser().getUid())
                                        .child(userID)
                                        .setValue(true);

                                mDatabaseFriends.child(userID)
                                        .child(mAuth.getCurrentUser().getUid())
                                        .setValue(true);


                                Toast.makeText(getContext(), "Request accepted", Toast.LENGTH_SHORT).show();
                            }

                        }
                        clicked = false;
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println(databaseError.toString());
                    }
                });
            }
        });

        button_declineFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = true;
                mDatabaseFriends.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (clicked) {
                            if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals("pending")) {
/*
                                            RECHAZAR UNA SOLICITUD DE AMISTAD
 */
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //Yes button clicked

                                                mDatabaseFriends.child(mAuth.getCurrentUser().getUid())
                                                        .child(userID)
                                                        .setValue(false);

                                                mDatabaseFriends.child(userID)
                                                        .child(mAuth.getCurrentUser().getUid())
                                                        .setValue("declined");

                                                Toast.makeText(getContext(), "Request declined", Toast.LENGTH_SHORT).show();
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage("Decline friend request\nAre you sure?").setPositiveButton("Yes", dialogClickListener)
                                        .setNegativeButton("No", dialogClickListener).show();
                            }

                        }
                        clicked = false;
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println(databaseError.toString());
                    }
                });
            }
        });

        button_addfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = true;
                //TODO add friend script
                mDatabaseFriends.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (clicked) {

                            if (!dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).exists() ||
                                    (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals(false))) {
/*
                                            ENVIAR UNA SOLICITUD DE AMISTAD
 */

                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //Yes button clicked
                                                mDatabaseFriends.child(mAuth.getCurrentUser().getUid())
                                                        .child(userID)
                                                        .setValue("waiting");

                                                mDatabaseFriends.child(userID)
                                                        .child(mAuth.getCurrentUser().getUid())
                                                        .setValue("pending");

                                                Toast.makeText(getContext(), "Friend request Sent", Toast.LENGTH_SHORT).show();
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage("Send a friend request?").setPositiveButton("Yes", dialogClickListener)
                                        .setNegativeButton("No", dialogClickListener).show();


                            }
                        }
                        clicked = false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println(databaseError.toString());
                    }
                });
            }
        });

        button_privatemessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO script send private message
            }
        });
        mAuth = FirebaseAuth.getInstance();


        mButtonProfileDesc = (ImageButton) view.findViewById(R.id.button_editDesc_profileFragment);

        Boolean aux = true;
        mDatabaseUserProfile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username.setText(dataSnapshot.child("Name").getValue().toString());
                if (dataSnapshot.child("Desc").exists()) {
                    userdesc.setText(dataSnapshot.child("Desc").getValue().toString());
                } else {
                    //      userdesc.setVisibility(View.GONE);
                }
                Picasso.with(getContext()).load(dataSnapshot.child("Image").getValue().toString()).into(userpic);
                if (!String.valueOf(userID).equals(String.valueOf(mAuth.getCurrentUser().getUid()))) {
                    linear_desc.setVisibility(View.GONE);
                } else {
                    linear_contactprofile.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        button_cancelfriend.setVisibility(View.GONE);
        button_acceptFriend.setVisibility(View.GONE);
        button_declineFriend.setVisibility(View.GONE);
        button_addfriend.setVisibility(View.GONE);

        mDatabaseFriends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!mAuth.getCurrentUser().getUid().equals(userID)) {
                    if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).exists() &&
                            dataSnapshot.child(userID).child(mAuth.getCurrentUser().getUid()).exists()) {
                        //relacion en bdat creada


                        if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals("waiting")) {
                            //solicitud  pendiente de acceptar por el otro usuario

                            button_cancelfriend.setVisibility(View.VISIBLE);
                            button_acceptFriend.setVisibility(View.GONE);
                            button_declineFriend.setVisibility(View.GONE);
                            button_addfriend.setVisibility(View.GONE);

                        }
                        if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals("declined")) {
                            //solicitud rechazada por el otro usuario

                            button_cancelfriend.setVisibility(View.GONE);
                            button_acceptFriend.setVisibility(View.GONE);
                            button_declineFriend.setVisibility(View.GONE);
                            button_addfriend.setVisibility(View.GONE);

                        }
                        if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals(true)) {
                            //solicitud aceptada ambas partes (usuarios amigos)

                            button_cancelfriend.setVisibility(View.VISIBLE);
                            button_acceptFriend.setVisibility(View.GONE);
                            button_declineFriend.setVisibility(View.GONE);
                            button_addfriend.setVisibility(View.GONE);

                        }

                        if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals(false)) {
                            //solicitud cancelada por usuario actual

                            button_cancelfriend.setVisibility(View.GONE);
                            button_acceptFriend.setVisibility(View.GONE);
                            button_declineFriend.setVisibility(View.GONE);
                            button_addfriend.setVisibility(View.VISIBLE);

                        }
                        if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child(userID).getValue().equals("pending")) {
                            //usuario actual tiene solicitud pendiente

                            button_cancelfriend.setVisibility(View.GONE);
                            button_acceptFriend.setVisibility(View.VISIBLE);
                            button_declineFriend.setVisibility(View.VISIBLE);
                            button_addfriend.setVisibility(View.GONE);

                        }

                    } else {
                        button_addfriend.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void sendDesc() {
        mProgress.setMessage("Posting ...");
        mProgress.setCancelable(false);
        mProgress.show();


        final String txt_desc = edit_desc.getText().toString().trim();
        if (!TextUtils.isEmpty(txt_desc)) {

            mDatabaseUserProfile.child("Desc").setValue(txt_desc);

            mProgress.dismiss();
            edit_desc.getText().clear();

            //  edit_desc.setVisibility(View.VISIBLE);
            // edit_desc.setText(txt_desc);

        } else {
            mProgress.dismiss();
            Toast.makeText(getContext(), "Error try again", Toast.LENGTH_LONG).show();
        }
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {

        ImageButton image_picrow;
        View mView;
        private DatabaseReference mDatabaseUsers;

        public ProfileViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Posts");
            image_picrow = (ImageButton) mView.findViewById(R.id.imagebutton_pic_picrow);
        }

        public void setImage(Context ctx, String image) {
            ImageButton post_image = (ImageButton) mView.findViewById(R.id.imagebutton_pic_picrow);

            Picasso.with(ctx).load(image)
                    .centerCrop()
                    .resize(100, 100)
                    .into(post_image);
        }
    }
}
