package com.example.oscar.Sloncha;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {


    List<Chat> chatList;
    private Context mContext;
    private FragmentManager fragmentManager;
    private DatabaseReference myChatReaded;
    private FirebaseAuth mAuth;

    public ChatAdapter(Context ctx, List<Chat> chatList, FragmentManager fragmentManager) {
        mContext = ctx;
        this.chatList = chatList;
        this.fragmentManager = fragmentManager;
        mAuth = FirebaseAuth.getInstance();
        myChatReaded = FirebaseDatabase.getInstance().getReference().child("ChatRead").child(mAuth.getCurrentUser().getUid());
    }

    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_row, viewGroup, false);
        ChatAdapter.ChatViewHolder holder = new ChatAdapter.ChatViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ChatAdapter.ChatViewHolder chatViewHolder, int i) {
        final Chat chat = chatList.get(i);
        System.out.println("CHAT GET USERID " + chat.getUserId());
        myChatReaded.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(chat.getUserId()).exists() && dataSnapshot.child(chat.getUserId()).getValue().equals(false)) {

                    System.out.println("mostrar icono card");
                    chatViewHolder.card.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chatViewHolder.person_name.setText(chat.getName());
        chatViewHolder.person_email.setText(chat.getEmail());
        Picasso.with(mContext).load(chat.getImage()).resize(200, 200).transform(new CircleTransform()).into(chatViewHolder.person_image);
        chatViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new ChatConversationFragment();
                Bundle args = new Bundle();
                System.out.println("RECIEVER_ID " + chat.getUserId());
                args.putString("RECIEVER_ID", chat.getUserId());
                fragment.setArguments(args);
                // Return the fragment manager
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.content_main, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView person_name;
        ImageView person_image;
        TextView person_email;
        LinearLayout linearLayout;
        CardView card;

        public ChatViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.chat_row_layout);
            person_name = (TextView) itemView.findViewById(R.id.chat_nameprofile);
            person_email = (TextView) itemView.findViewById(R.id.chat_emailprofile);
            person_image = (ImageView) itemView.findViewById(R.id.chat_imageprofilepic);
            card = (CardView) itemView.findViewById(R.id.chat_cardview_new_message);
        }
    }

}
