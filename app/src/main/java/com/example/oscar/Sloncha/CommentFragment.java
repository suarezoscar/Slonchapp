package com.example.oscar.Sloncha;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class CommentFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static InputMethodManager imm;
    private ImageView mImage;
    private TextView mTextViewTitle;
    private TextView mTextViewDesc;
    private String userpostID;
    private ImageButton mDeletePost;
    private String uId;
    private String postId;
    private RecyclerView mComments;
    private DatabaseReference mDatabaseComments;
    private DatabaseReference mDatabasePosts;
    private OnFragmentInteractionListener mListener;
    private ImageButton sendComment;
    private EditText textbox_comments;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;


    public CommentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CommentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentFragment newInstance(String param1, String param2) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comment, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(

                Comment.class,
                R.layout.comment_row,
                CommentViewHolder.class,
                mDatabaseComments
        ) {
            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder, final Comment model, final int position) {

                final String comment_id = getRef(position).getKey().toString();
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setImageUserProfile(getContext(), model.getUid());
                viewHolder.setName(model.getUid());
                if (mAuth.getCurrentUser().getUid().equals(model.getUid())) {
                    viewHolder.mDeleteComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            //Yes button clicked
                                            mDatabaseComments.child(comment_id).removeValue();
                                            Toast.makeText(getContext(), "Correctly Deleted", Toast.LENGTH_SHORT).show();
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
                    });
                } else {
                    viewHolder.mDeleteComment.setVisibility(View.GONE);
                }
                viewHolder.mUserPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment fragment = new ProfileFragment();
                        Bundle args = new Bundle();
                        args.putString("userID", model.getUid());
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
        mComments.setLayoutManager(layoutManager);
        mComments.setAdapter(firebaseRecyclerAdapter);
        mComments.setNestedScrollingEnabled(false);
        mComments.setHasFixedSize(false);
        mComments.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, true));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uId = getArguments().getString("UID");
            postId = getArguments().getString("POSTID");
        }
        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        mDatabasePosts = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(getContext());
        mComments = (RecyclerView) view.findViewById(R.id.RecyclerView_comments);
        mComments.setHasFixedSize(true);
        mComments.setLayoutManager(new LinearLayoutManager(getContext()));

        textbox_comments = (EditText) view.findViewById(R.id.edittext_comment);
        sendComment = (ImageButton) view.findViewById(R.id.button_sendComment);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
        mImage = (ImageView) view.findViewById(R.id.imageview_comment);
        mDeletePost = (ImageButton) view.findViewById(R.id.imageButton_delete_post_commentfragment);
        mTextViewDesc = (TextView) view.findViewById(R.id.textview_desc_commentfragment);
        mTextViewTitle = (TextView) view.findViewById(R.id.textview_title_commentfragment);
        mDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if (USER_ID.equals(mAuth.getCurrentUser().getUid())) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    mDatabasePosts.addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            dataSnapshot.getRef().removeValue();
                                            Toast.makeText(getContext(), "Correctly Deleted", Toast.LENGTH_SHORT).show();
                                            getFragmentManager().popBackStack();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                } catch (Exception er) {
                                    System.out.println(er.toString());
                                }

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
                //        }
            }
        });

        mDatabasePosts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    userpostID = dataSnapshot.child("userId").getValue(String.class);
                    System.out.println("userID: " + userpostID);
                    Picasso.with(getContext()).load(dataSnapshot.child("image").getValue().toString())
                            .resize(450, 450)
                            .into(mImage);
                    mTextViewDesc.setText(dataSnapshot.child("desc").getValue().toString());
                    mTextViewTitle.setText(dataSnapshot.child("title").getValue().toString());
                    if (userpostID.equals(mAuth.getCurrentUser().getUid()))
                        mDeletePost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Database Error: " + databaseError.toString());
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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

    private void startPosting() {
        mProgress.setMessage("Posting ...");
        mProgress.setCancelable(false);
        mProgress.show();


        final String txt_comment = textbox_comments.getText().toString().trim();
        if (!TextUtils.isEmpty(txt_comment)) {

            DatabaseReference newPost = mDatabaseComments.push();

            Calendar calendar = new GregorianCalendar();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY); // 24 hour clock
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // Jan = 0, dec = 11
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            String date = dayOfMonth + "/" + month + "/" + year + "\t  " + hourOfDay + ":" + minute + ":" + second;

            Comment c = new Comment(txt_comment, date, mAuth.getCurrentUser().getUid());

            newPost.setValue(c);

            mProgress.dismiss();
            textbox_comments.getText().clear();

        } else {
            mProgress.dismiss();
            Toast.makeText(getContext(), "Error try again", Toast.LENGTH_LONG).show();
        }

        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

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

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton mDeleteComment;
        ImageButton mUserPic;
        private DatabaseReference mDatabaseUsers;
        private DatabaseReference mDatabasePosts;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabasePosts = FirebaseDatabase.getInstance().getReference().child("Posts");
            mDeleteComment = (ImageButton) mView.findViewById(R.id.imageButton_delete_comment);
            mUserPic = (ImageButton) mView.findViewById(R.id.comment_user_profilePicture);
        }


        public void setComment(String comment) {

            TextView comment_text = (TextView) mView.findViewById(R.id.TextView_comment);
            comment_text.setText(comment);
        }

        public void setDate(String date) {

            TextView comment_date = (TextView) mView.findViewById(R.id.textView_fecha_comentario);
            comment_date.setText(date);
        }

        public void setName(final String userid) {

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    TextView comment_name = (TextView) mView.findViewById(R.id.textView_nombre_comentario);
                    comment_name.setText(dataSnapshot.child(userid).child("Name").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setImageUserProfile(final Context ctx, final String userid) {
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ImageButton post_profilePic = (ImageButton) mView.findViewById(R.id.comment_user_profilePicture);
                    Picasso.with(ctx).load(dataSnapshot.child(userid).child("Image").getValue().toString()).into(post_profilePic);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
