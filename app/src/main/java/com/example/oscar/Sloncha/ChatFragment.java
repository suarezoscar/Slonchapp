package com.example.oscar.Sloncha;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static Boolean HAVE_FRIENDS = false;
    private static String LOGGED_USER_ID;
    RecyclerView chat_RecyclerView;
    CardView cardView;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myFriend;
    DatabaseReference myRef;
    DatabaseReference myChatReaded;
    ProgressBar mProgress;
    LinearLayoutManager mLinearLayoutManager;
    FirebaseAuth mAuth;
    private List chatList;
    private ChatAdapter chatAdapter;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference("Users");
        myRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        LOGGED_USER_ID = mAuth.getCurrentUser().getUid();
        myFriend = firebaseDatabase.getReference("Friends");



        chatList = new ArrayList();
        chatAdapter = new ChatAdapter(getContext(), chatList, getFragmentManager());

        myFriend.child(LOGGED_USER_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (d.getValue().equals(true)) {

                        myRef.child(d.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                DataSnapshot d = dataSnapshot;
                                String desc = "";
                                if (d.child("Desc").exists())
                                    desc = d.child("Desc").getValue().toString();
                                Chat c = new Chat(
                                        d.child("Email").getValue().toString(),
                                        d.child("Image").getValue().toString(),
                                        d.child("Name").getValue().toString(),
                                        d.child("UserId").getValue().toString(),
                                        desc
                                );
                                chatList.add(c);

                                System.out.println(c.getName() + " " + c.getImage() + " " + c.getEmail() + " " +
                                        c.getUserId());
                                chatAdapter.notifyDataSetChanged();
                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mProgress.setVisibility(ProgressBar.VISIBLE);
        chat_RecyclerView.setAdapter(chatAdapter);
        System.out.println("TAMANHO CHAT " + chatList.size());
        mProgress.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        mProgress = (ProgressBar) v.findViewById(R.id.chat_progressbar);
        chat_RecyclerView = (RecyclerView) v.findViewById(R.id.chat_recyclerView);
        chat_RecyclerView.setNestedScrollingEnabled(false);
        chat_RecyclerView.setHasFixedSize(false);
        chat_RecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cardView = (CardView) v.findViewById(R.id.chat_cardview_new_message);
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


}
