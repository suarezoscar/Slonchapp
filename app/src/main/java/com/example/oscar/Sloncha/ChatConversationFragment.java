package com.example.oscar.Sloncha;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.media.MediaRecorder.VideoSource.CAMERA;
import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatConversationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatConversationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatConversationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "RECIEVER_ID";
    private static final String ARG_PARAM2 = "param2";
    private static final int GALLERY_INTENT = 2;
    private static final int READ_EXTERNAL_STORAGE = 0;
    private static final int MULTIPLE_PERMISIONS = 10;
    static String Sender_Name;
    private static String USER_ID;
    private static String SENDER_NAME;
    // TODO: Rename and change types of parameters
    private static String RECIEVER_ID;
    private static DatabaseReference mDatabaseUsers;
    final CharSequence[] options = {"Camera", "Gallery"};
    public LinearLayoutManager mLinearLayoutManager;
    public DatabaseReference myref1, myref2, myChatRead, myChatReaded;
    FirebaseDatabase firebaseDatabase;
    ImageView attach_icon, send_icon, no_data_avaiable_image;
    EditText message_area;
    TextView no_chat;
    ProgressBar progressBar;
    Uri mImageUri = Uri.EMPTY;

    private FirebaseAuth mAuth;
    private ImageView profile_pic;
    private String pictureImagePath = "";
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<ChatConversation, ChatConversation_ViewHolder> mFirebaseAdapter;
    private ProgressDialog mProgressDialog;
    private String PictureImagePath = "";

    public ChatConversationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ChatConversationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatConversationFragment newInstance(String param1) {
        ChatConversationFragment fragment = new ChatConversationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        //  args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Log.d("LOGGED", "On Start : " );
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatConversation, ChatConversationFragment.ChatConversation_ViewHolder>(ChatConversation.class, R.layout.chat_conversation_row, ChatConversationFragment.ChatConversation_ViewHolder.class, myref1) {


            @Override
            protected void populateViewHolder(final ChatConversation_ViewHolder chatConversation_viewHolder, ChatConversation chatConversation, final int i) {

                chatConversation_viewHolder.getSender(chatConversation.getSender(), mAuth.getCurrentUser().getUid(), chatConversation.getDateTime());
                chatConversation_viewHolder.getMessage(chatConversation.getMessage(), chatConversation.getDateTime());
                System.out.println("DATETIME " + chatConversation.getDateTime());
                //Log.d("LOGGED", "Sender : " + model.getSender());
                //Log.d("LOGGED", "Message : " + model.getMessage());


                chatConversation_viewHolder.mView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        final DatabaseReference ref = mFirebaseAdapter.getRef(i);
                        ref.keepSynced(true);
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String retrieve_image_url = dataSnapshot.child("message").getValue(String.class);
                                if (retrieve_image_url.startsWith("https")) {
                                    //Toast.makeText(ChatConversationActivity.this, "URL : " + retrieve_image_url, Toast.LENGTH_SHORT).show();
                                    Intent intent = (new Intent(getContext(), com.example.oscar.Sloncha.EnlargeImageView.class));
                                    intent.putExtra("url", retrieve_image_url);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };
        Log.d("LOGGED", "Set Layout : ");
        recyclerView.setAdapter(mFirebaseAdapter);


        myref1.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    //Log.d("LOGGED", "Data SnapShot : " +dataSnapshot.toString());
                    progressBar.setVisibility(ProgressBar.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    no_data_avaiable_image.setVisibility(View.GONE);
                    no_chat.setVisibility(View.GONE);
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 500);
                    recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v,
                                                   int left, int top, int right, int bottom,
                                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            if (bottom < oldBottom) {
                                recyclerView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                                    }
                                }, 100);
                            }
                        }
                    });
                } else {
                    //Log.d("LOGGED", "NO Data SnapShot : " +dataSnapshot.toString());
                    progressBar.setVisibility(ProgressBar.GONE);
                    recyclerView.setVisibility(View.GONE);
                    no_data_avaiable_image.setVisibility(View.VISIBLE);
                    no_chat.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            RECIEVER_ID = getArguments().getString(ARG_PARAM1);

            // mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAuth = FirebaseAuth.getInstance();
        USER_ID = mAuth.getCurrentUser().getUid();
        SENDER_NAME = mAuth.getCurrentUser().getDisplayName();

        firebaseDatabase = FirebaseDatabase.getInstance();

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        myref1 = FirebaseDatabase.getInstance().getReference().child("Chat").child(USER_ID).child(RECIEVER_ID);
        myref1.keepSynced(true);

        myref2 = FirebaseDatabase.getInstance().getReference().child("Chat").child(RECIEVER_ID).child(USER_ID);
        myref2.keepSynced(true);

        myChatRead = FirebaseDatabase.getInstance().getReference().child("ChatRead").child(RECIEVER_ID).child(USER_ID);

        myChatReaded = FirebaseDatabase.getInstance().getReference().child("ChatRead").child(USER_ID).child(RECIEVER_ID);

        myChatReaded.setValue(true);

        mProgressDialog = new ProgressDialog(getContext());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat_conversation, container, false);


        recyclerView = (RecyclerView) v.findViewById(R.id.fragment_chat_recycler_view);
        attach_icon = (ImageView) v.findViewById(R.id.attachButton);
        send_icon = (ImageView) v.findViewById(R.id.sendButton);
        no_data_avaiable_image = (ImageView) v.findViewById(R.id.no_data_available_image);
        message_area = (EditText) v.findViewById(R.id.messageArea);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar3);
        no_chat = (TextView) v.findViewById(R.id.no_chat_text);
        mLinearLayoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(mLinearLayoutManager);
        mLinearLayoutManager.setStackFromEnd(true);

        send_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = message_area.getText().toString().trim();

                if (!messageText.equals("")) {

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    ArrayMap<String, String> map = new ArrayMap<>();
                    map.put("message", messageText);
                    map.put("Sender", mAuth.getCurrentUser().getUid());
                    map.put("DateTime", dateFormat.format(date));
                    myref1.push().setValue(map);
                    myref2.push().setValue(map);
                    myChatRead.setValue(false);
                    message_area.setText("");
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 500);

                    Notification.SendChat(RECIEVER_ID, messageText);
                }
            }
        });

        attach_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose Source");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (options[which].equals("Camera")) {
                            if (ContextCompat.checkSelfPermission(getContext(),
                                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.CAMERA},
                                        CAMERA);

                            } else {
                                callCamera();
                            }
                        }
                        if (options[which].equals("Gallery")) {

                            if (ContextCompat.checkSelfPermission(getContext(),
                                    Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        READ_EXTERNAL_STORAGE);

                            } else {
                                callGallery();
                            }
                        }
                    }
                });
                builder.show();
            }

        });

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Permission Required");
                alertDialog.setMessage("The app need permission to access the storage");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MULTIPLE_PERMISIONS);
                            }
                        });

                alertDialog.show();
            }
        }
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {

                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Permission Required");
                alertDialog.setMessage("The app need permission to access the camera");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.CAMERA},
                                        MULTIPLE_PERMISIONS);
                            }
                        });

                alertDialog.show();
            }
        }


        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callGallery();
                }
                return;

            case MULTIPLE_PERMISIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callCamera();
                }

        }
    }

    private void callCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        Log.d("LOGGED", "imageFileName :  " + imageFileName);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;


        File file = new File(pictureImagePath);

        Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", file);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        cameraIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


        Log.d("LOGGED", "pictureImagePath :  " + pictureImagePath);
        Log.d("LOGGED", "outputFileUri :  " + outputFileUri);

        startActivityForResult(cameraIntent, 5);
    }

    private void callGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_INTENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("LOGGED", "InSIDE onActivityResult : ");
        Log.d("LOGGED", " requestCode : " + requestCode + " resultCode : " + resultCode + " DATA " + data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {

            mImageUri = data.getData();
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Chat_Images").child(mImageUri.getLastPathSegment());
            Log.d("LOGGED", "ImageURI : " + mImageUri);


            mProgressDialog.setMessage("Uploading...");
            mProgressDialog.show();

            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUri = taskSnapshot.getDownloadUrl();

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();

                    ArrayMap<String, String> map = new ArrayMap<>();
                    map.put("message", downloadUri.toString());
                    map.put("sender", mAuth.getCurrentUser().getUid());
                    map.put("DateTime", dateFormat.format(date));
                    myref1.push().setValue(map);
                    myref2.push().setValue(map);
                    myChatRead.setValue(false);
                    mProgressDialog.dismiss();
                }
            });
        } else if (requestCode == 5 && resultCode == RESULT_OK) {


            File imgFile = new File(pictureImagePath);
            if (imgFile.exists()) {
                Log.d("LOGGED", "imgFile : " + imgFile);

                Uri fileUri = Uri.fromFile(imgFile);
                Log.d("LOGGED", "fileUri : " + fileUri);

                StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Chat_Images").child(fileUri.getLastPathSegment());

                mProgressDialog.setMessage("Uploading...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") Uri downloadUri = taskSnapshot.getDownloadUrl();
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date date = new Date();

                        ArrayMap<String, String> map = new ArrayMap<>();
                        map.put("message", downloadUri.toString());
                        map.put("sender", mAuth.getCurrentUser().getUid());
                        map.put("DateTime", dateFormat.format(date));
                        myref1.push().setValue(map);
                        myref2.push().setValue(map);
                        myChatRead.setValue(false);
                        mProgressDialog.dismiss();
                    }
                });
            }
        } else if (requestCode == 5) {
            Toast.makeText(getContext(), "resultCode : " + resultCode, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                getActivity().onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

    //View Holder For Recycler View
    public static class ChatConversation_ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout.LayoutParams params, text_params;
        private final TextView message, sender, datetime;
        private final ImageView chat_image_incoming, chat_image_outgoing, chat_image_profilepic;
        View mView;
        LinearLayout layout;
        LinearLayout layoutdir;
        FirebaseAuth mAuth;


        public ChatConversation_ViewHolder(final View itemView) {
            super(itemView);
            //Log.d("LOGGED", "ON Chat_Conversation_ViewHolder : " );
            mAuth = FirebaseAuth.getInstance();
            mView = itemView;
            message = (TextView) mView.findViewById(R.id.fetch_chat_messgae);
            sender = (TextView) mView.findViewById(R.id.fetch_chat_sender);
            datetime = (TextView) mView.findViewById(R.id.fetch_chat_datetime);
            chat_image_incoming = (ImageView) mView.findViewById(R.id.chat_uploaded_image_incoming);
            chat_image_outgoing = (ImageView) mView.findViewById(R.id.chat_uploaded_image_outgoing);
            chat_image_profilepic = (ImageView) mView.findViewById(R.id.fetch_chat_profile_pic);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            text_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout = (LinearLayout) mView.findViewById(R.id.chat_linear_layout);
            layoutdir = (LinearLayout) mView.findViewById(R.id.linearLayout_chat_dir);
        }

        private void getSender(String title, String uid, final String DateTime) {

            try {

                if (uid.equals(title)) {
                    //Log.d("LOGGED", "getSender: ");
                    params.setMargins((MainActivity.Device_Width / 8), 5, 10, 20);
                    mView.setLayoutParams(params);
                    mView.setBackgroundResource(R.drawable.shape_outcoming_message);
                    mDatabaseUsers.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            System.out.println(dataSnapshot.child("Image").getValue(String.class));
                            Picasso.with(getApplicationContext()).load(dataSnapshot.child("Image").getValue().toString()).transform(new CircleTransform()).into(chat_image_profilepic);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    sender.setText(R.string.you_chat_message);
                    datetime.setText(DateTime);
                    chat_image_outgoing.setVisibility(View.VISIBLE);
                    chat_image_incoming.setVisibility(View.GONE);

                } else {
                    params.setMargins(10, 5, (MainActivity.Device_Width / 8), 20);
                    sender.setGravity(Gravity.START);
                    mView.setLayoutParams(params);
                    mView.setBackgroundResource(R.drawable.shape_incoming_message);
                    mDatabaseUsers.child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            sender.setText(dataSnapshot.child("Name").getValue().toString().split(" ")[0]);
                            datetime.setText(DateTime);
                            Picasso.with(getApplicationContext()).load(dataSnapshot.child("Image").getValue().toString()).transform(new CircleTransform()).into(chat_image_profilepic);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    chat_image_outgoing.setVisibility(View.GONE);
                    chat_image_incoming.setVisibility(View.VISIBLE);
                }

            } catch (Exception er) {
                System.out.println("GETSENDER ERROR: " + er);
            }
        }

        private void getMessage(String title, String datetime) {

            if (!title.startsWith("https")) {
                if (!sender.getText().equals(Sender_Name)) {
                    text_params.setMargins(15, 10, 22, 15);
                } else {
                    text_params.setMargins(65, 10, 22, 15);
                }

                message.setLayoutParams(text_params);
                message.setText(title);
                System.out.println("DATETIME INSIDE " + datetime);
                this.datetime.setText(datetime);

                //   message.setTextColor(Color.parseColor("#FFFFFF"));
                message.setVisibility(View.VISIBLE);

                chat_image_incoming.setVisibility(View.GONE);
                chat_image_outgoing.setVisibility(View.GONE);
            } else {
                if (chat_image_outgoing.getVisibility() == View.VISIBLE && chat_image_incoming.getVisibility() == View.GONE) {
                    chat_image_outgoing.setVisibility(View.VISIBLE);
                    message.setVisibility(View.GONE);

                    Glide.with(itemView.getContext())
                            .load(title)
                            .crossFade()
                            .fitCenter()
                            .placeholder(R.drawable.loading)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(chat_image_outgoing);
                } else {
                    chat_image_incoming.setVisibility(View.VISIBLE);
                    message.setVisibility(View.GONE);

                    Glide.with(itemView.getContext())
                            .load(title)
                            .crossFade()
                            .fitCenter()
                            .placeholder(R.drawable.loading)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(chat_image_incoming);
                }
            }

        }


    }
}
