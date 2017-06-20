package com.example.oscar.Sloncha;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "search";
    private static final String ARG_PARAM2 = "param2";
    private FirebaseAuth mAuth;
    private DatabaseReference myref;
    private Query mDatabaseQuery;
    private RecyclerView recyclerView;

    // TODO: Rename and change types of parameters
    private String mQuery;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param query  Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    private DatabaseReference mDatabaseUserPosts;
    private Query mQueryUserPosts;

    public SearchFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String query, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, query);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mQuery = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mDatabaseUserPosts = FirebaseDatabase.getInstance().getReference().child("Users");
        mQueryUserPosts = mDatabaseUserPosts.startAt(mQuery).orderByChild("Name");
        Toast.makeText(getContext(), "Results " + mQuery, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Search, SearchFragment.SearchViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Search, SearchFragment.SearchViewHolder>(

                Search.class,
                R.layout.search_row,
                SearchFragment.SearchViewHolder.class,
                mQueryUserPosts
        ) {
            @Override
            protected void populateViewHolder(SearchFragment.SearchViewHolder viewHolder, final Search model, final int position) {


                viewHolder.setName(model.getName());
                viewHolder.setDescription(model.getDesc());
                viewHolder.setImageUserProfile(getContext(), model.getImage());
                viewHolder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment fragment = new ProfileFragment();
                        Bundle args = new Bundle();
                        args.putString("userID", model.getUserId());
                        fragment.setArguments(args);
                        getFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                .add(R.id.content_main, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, true));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.search_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton mDeleteComment;
        ImageButton mUserPic;
        LinearLayout mLinearLayout;
        private DatabaseReference mDatabaseUsers;
        private DatabaseReference mDatabasePosts;

        public SearchViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLinearLayout = (LinearLayout) mView.findViewById(R.id.search_row_layout);
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabasePosts = FirebaseDatabase.getInstance().getReference().child("Posts");
            mDeleteComment = (ImageButton) mView.findViewById(R.id.imageButton_delete_comment);
            mUserPic = (ImageButton) mView.findViewById(R.id.comment_user_profilePicture);
        }


        public void setDescription(String desc) {
            TextView search_desc = (TextView) mView.findViewById(R.id.search_descProfile);
            search_desc.setText(desc);
        }


        public void setName(String name) {
            TextView search_name = (TextView) mView.findViewById(R.id.search_nameprofile);
            search_name.setText(name);
        }

        public void setImageUserProfile(final Context ctx, final String image) {
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ImageView search_imageprofile = (ImageView) mView.findViewById(R.id.search_imageprofilepic);
                    Picasso.with(ctx).load(image).resize(200, 200).transform(new CircleTransform()).into(search_imageprofile);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
