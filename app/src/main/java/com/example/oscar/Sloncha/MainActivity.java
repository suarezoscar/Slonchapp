package com.example.oscar.Sloncha;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PostFragment.OnFragmentInteractionListener,
        CommentFragment.OnFragmentInteractionListener,
        CategoryFragment.OnFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        ChatConversationFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener {

    private static final int RC_SIGN_IN = 10;
    private static final String TAG = "USERSTATE";
    public static int Device_Width;
    private static String USER_ID;
    private static String NOTIFICATION_ID;
    Fragment fragment = null;
    Boolean fragmentTransaction = false;
    SearchView searchView = null;
    ImageView nav_profile_picture;
    ArrayList<String> mListaux;
    ArrayList<Chat> mObjectList;
    ArrayAdapter<String> arrayAdapter;
    AlertDialog.Builder builderSingle;
    private DatabaseReference mDataReactions;
    private RecyclerView mBlogList;
    private DatabaseReference mDataPosts;
    private DatabaseReference mDataRoot;
    private Query mQueryUserPosts;
    private DatabaseReference mDataUsers;
    private Boolean mProcessReactions = false;
    private StorageReference mStorage;
    private Menu mMenu;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView nav_username;
    private TextView nav_email;
    private DatabaseReference mDataComments;
    private Query mCommentsQuery;
    private ImageView profile_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mListaux = new ArrayList<>();
        mObjectList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item);
        builderSingle = new AlertDialog.Builder(MainActivity.this);
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    USER_ID = user.getUid();
                    setUser();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(
                                    AuthUI.FACEBOOK_PROVIDER,
                                    //AuthUI.EMAIL_PROVIDER,
                                    AuthUI.GOOGLE_PROVIDER)
                            .setLogo(R.mipmap.ic_launcher)
                            .setTheme(R.style.AppThemeLogin)
                            .build(), RC_SIGN_IN);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        });

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        OneSignal.sendTag("User_ID", USER_ID);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                Log.d("debug", "User:" + userId);
                NOTIFICATION_ID = userId;

                if (registrationId != null)
                    Log.d("debug", "registrationId:" + registrationId);

            }
        });
        mBlogList = (RecyclerView) findViewById(R.id.RecyclerView_MainActivity);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mDataPosts = FirebaseDatabase.getInstance().getReference().child("Posts");

        Bundle bundle = getIntent().getExtras();

        if (bundle == null) {

            //si se pasa una categoria por parametro

            // Toast.makeText(this, "bundle null", Toast.LENGTH_SHORT).show();
            mQueryUserPosts = mDataPosts;
        } else {

            // si la categoria es null mostrar posts de amigos

            String QueryCategories = bundle.getString("categories");
            mQueryUserPosts = mDataPosts.orderByChild("categorie").equalTo(QueryCategories);
            // Toast.makeText(this, "bundle not null " + bundle.getString("categories"), Toast.LENGTH_SHORT).show();

        }

        mDataUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDataReactions = FirebaseDatabase.getInstance().getReference().child("Reactions");
        mDataComments = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDataRoot = FirebaseDatabase.getInstance().getReference();
        mDataReactions.keepSynced(true);
        mDataUsers.keepSynced(true);
        mDataReactions.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference();
        profile_pic = (ImageView) findViewById(R.id.post_user_profilePicture);

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        Device_Width = metrics.widthPixels;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        nav_username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        nav_email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_mail);
        nav_profile_picture = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageViewProfile);

        navigationView.setNavigationItemSelectedListener(this);

        /**
         * start of code configuration for color of text of your Navigation Drawer / Menu based on state
         */
        int[][] state = new int[][]{
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed

        };

        int[] color = new int[]{
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                Color.WHITE
        };

        ColorStateList colorStateList1 = new ColorStateList(state, color);


        // FOR NAVIGATION VIEW ITEM ICON COLOR
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed

        };

        int[] colors = new int[]{
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                Color.WHITE
        };
        ColorStateList colorStateList2 = new ColorStateList(states, colors);
        navigationView.setItemTextColor(colorStateList1);
        navigationView.setItemIconTintList(colorStateList2);
        /**
         * end of code configuration for color of text of your Navigation Drawer / Menu based on state
         */

    }

    @Override
    protected void onStart() {

        super.onStart();
        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(

                Post.class,
                R.layout.blog_row,
                PostViewHolder.class,
                mQueryUserPosts
        ) {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, final Post model, int position) {

                final String post_id = getRef(position).getKey().toString();

                viewHolder.setLikeBtn(post_id);
                viewHolder.setLolBtn(post_id);
                viewHolder.setSadBtn(post_id);
                viewHolder.setShitBtn(post_id);
                viewHolder.setAngryBtn(post_id);
                viewHolder.setLoveBtn(post_id);
                viewHolder.setComment(post_id);

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUsername(model.getUserId());
                viewHolder.setUserPic(getApplicationContext(), model.getUserId());
                viewHolder.setDate(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragmentTransaction = true;

                        if (fragmentTransaction) {
                            fragment = new CommentFragment();

                            Bundle args = new Bundle();
                            args.putString("UID", auth.getCurrentUser().getUid());
                            args.putString("POSTID", post_id);
                            fragment.setArguments(args);

                            getSupportFragmentManager().beginTransaction()
                                    //     .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                    .replace(R.id.content_main, fragment)
                                    .addToBackStack(null)
                                    .commit();

                        }
                        fragmentTransaction = false;
                    }
                });

                //like
                viewHolder.reaction_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessReactions = true;
                        mDataReactions.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    if (dataSnapshot.child(post_id).child(auth.getCurrentUser().getUid()).exists()) {

                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).removeValue();

                                        mProcessReactions = false;
                                    } else {
                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).setValue("like");
                                        mProcessReactions = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                // who likes?
                viewHolder.reaction_like.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mProcessReactions = true;
                        arrayAdapter.clear();
                        mObjectList.clear();

                        builderSingle.setIcon(R.drawable.like);
                        builderSingle.setTitle("who liked?..");

                        mDataReactions.child(post_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                                        if (d.getValue(String.class).equals("like")) {
                                            //OBTENER USER_ID DE LOS QUE DIERON LIKE
                                            mDataUsers.child(d.getKey()).child("Name").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (mProcessReactions) {
                                                        //BUSCAR EL NOMBRE POR EL USER_ID Y ANHADIR NOMBRE A LISTA
                                                        Chat c = new Chat();
                                                        c.setName(dataSnapshot.getValue().toString());
                                                        c.setUserId(d.getKey());
                                                        mObjectList.add(c);
                                                        arrayAdapter.add(c.getName());

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }
                                    }

                                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String strName = arrayAdapter.getItem(which);
                                            //GO TO USER PROFILE ON CLICK
                                            fragment = new ProfileFragment();

                                            Bundle args = new Bundle();
                                            args.putString("userID", mObjectList.get(which).getUserId());
                                            fragment.setArguments(args);

                                            getSupportFragmentManager().beginTransaction()
                                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                                    .replace(R.id.content_main, fragment)
                                                    .addToBackStack(null)
                                                    .commit();

                                        }
                                    });

                                    builderSingle.show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        return true;
                    }

                });

                // love
                viewHolder.reaction_love.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessReactions = true;
                        mDataReactions.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    if (dataSnapshot.child(post_id).child(auth.getCurrentUser().getUid()).exists()) {

                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).removeValue();

                                        mProcessReactions = false;
                                    } else {
                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).setValue("love");
                                        mProcessReactions = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                //who loved
                viewHolder.reaction_love.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mProcessReactions = true;
                        arrayAdapter.clear();
                        mObjectList.clear();

                        builderSingle.setIcon(R.drawable.love);
                        builderSingle.setTitle("who loved?..");

                        mDataReactions.child(post_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                                        if (d.getValue(String.class).equals("love")) {
                                            //OBTENER USER_ID DE LOS QUE DIERON LIKE
                                            mDataUsers.child(d.getKey()).child("Name").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (mProcessReactions) {
                                                        //BUSCAR EL NOMBRE POR EL USER_ID Y ANHADIR NOMBRE A LISTA
                                                        Chat c = new Chat();
                                                        c.setName(dataSnapshot.getValue().toString());
                                                        c.setUserId(d.getKey());
                                                        mObjectList.add(c);
                                                        arrayAdapter.add(c.getName());

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }
                                    }

                                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String strName = arrayAdapter.getItem(which);
                                            //GO TO USER PROFILE ON CLICK
                                            fragment = new ProfileFragment();

                                            Bundle args = new Bundle();
                                            args.putString("userID", mObjectList.get(which).getUserId());
                                            fragment.setArguments(args);

                                            getSupportFragmentManager().beginTransaction()
                                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                                    .replace(R.id.content_main, fragment)
                                                    .addToBackStack(null)
                                                    .commit();

                                        }
                                    });

                                    builderSingle.show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        return true;
                    }

                });

                //lol
                viewHolder.reaction_lol.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessReactions = true;
                        mDataReactions.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    if (dataSnapshot.child(post_id).child(auth.getCurrentUser().getUid()).exists()) {

                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).removeValue();

                                        mProcessReactions = false;
                                    } else {
                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).setValue("lol");
                                        mProcessReactions = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                //who laughed
                viewHolder.reaction_lol.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mProcessReactions = true;
                        arrayAdapter.clear();
                        mObjectList.clear();

                        builderSingle.setIcon(R.drawable.lought);
                        builderSingle.setTitle("who laughed?..");

                        mDataReactions.child(post_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                                        if (d.getValue(String.class).equals("lol")) {
                                            //OBTENER USER_ID DE LOS QUE DIERON LIKE
                                            mDataUsers.child(d.getKey()).child("Name").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (mProcessReactions) {
                                                        //BUSCAR EL NOMBRE POR EL USER_ID Y ANHADIR NOMBRE A LISTA
                                                        Chat c = new Chat();
                                                        c.setName(dataSnapshot.getValue().toString());
                                                        c.setUserId(d.getKey());
                                                        mObjectList.add(c);
                                                        arrayAdapter.add(c.getName());

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }
                                    }

                                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String strName = arrayAdapter.getItem(which);
                                            //GO TO USER PROFILE ON CLICK
                                            fragment = new ProfileFragment();

                                            Bundle args = new Bundle();
                                            args.putString("userID", mObjectList.get(which).getUserId());
                                            fragment.setArguments(args);

                                            getSupportFragmentManager().beginTransaction()
                                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                                    .replace(R.id.content_main, fragment)
                                                    .addToBackStack(null)
                                                    .commit();

                                        }
                                    });

                                    builderSingle.show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        return true;
                    }

                });
                //sad
                viewHolder.reaction_sad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessReactions = true;
                        mDataReactions.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    if (dataSnapshot.child(post_id).child(auth.getCurrentUser().getUid()).exists()) {

                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).removeValue();

                                        mProcessReactions = false;
                                    } else {
                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).setValue("sad");
                                        mProcessReactions = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                //who think it's sad
                viewHolder.reaction_sad.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mProcessReactions = true;
                        arrayAdapter.clear();
                        mObjectList.clear();

                        builderSingle.setIcon(R.drawable.sad);
                        builderSingle.setTitle("who think it's sad?..");

                        mDataReactions.child(post_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                                        if (d.getValue(String.class).equals("sad")) {
                                            //OBTENER USER_ID DE LOS QUE DIERON LIKE
                                            mDataUsers.child(d.getKey()).child("Name").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (mProcessReactions) {
                                                        //BUSCAR EL NOMBRE POR EL USER_ID Y ANHADIR NOMBRE A LISTA
                                                        Chat c = new Chat();
                                                        c.setName(dataSnapshot.getValue().toString());
                                                        c.setUserId(d.getKey());
                                                        mObjectList.add(c);
                                                        arrayAdapter.add(c.getName());

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }
                                    }

                                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String strName = arrayAdapter.getItem(which);
                                            //GO TO USER PROFILE ON CLICK
                                            fragment = new ProfileFragment();

                                            Bundle args = new Bundle();
                                            args.putString("userID", mObjectList.get(which).getUserId());
                                            fragment.setArguments(args);

                                            getSupportFragmentManager().beginTransaction()
                                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                                    .replace(R.id.content_main, fragment)
                                                    .addToBackStack(null)
                                                    .commit();

                                        }
                                    });

                                    builderSingle.show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        return true;
                    }

                });
                //shit
                viewHolder.reaction_shit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessReactions = true;
                        mDataReactions.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    if (dataSnapshot.child(post_id).child(auth.getCurrentUser().getUid()).exists()) {

                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).removeValue();

                                        mProcessReactions = false;
                                    } else {
                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).setValue("shit");
                                        mProcessReactions = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });

                //who think
                viewHolder.reaction_shit.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mProcessReactions = true;
                        arrayAdapter.clear();
                        mObjectList.clear();

                        builderSingle.setIcon(R.drawable.shit);
                        builderSingle.setTitle("who doesn't appreciate this ?..");

                        mDataReactions.child(post_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                                        if (d.getValue(String.class).equals("shit")) {
                                            //OBTENER USER_ID DE LOS QUE DIERON LIKE
                                            mDataUsers.child(d.getKey()).child("Name").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (mProcessReactions) {
                                                        //BUSCAR EL NOMBRE POR EL USER_ID Y ANHADIR NOMBRE A LISTA
                                                        Chat c = new Chat();
                                                        c.setName(dataSnapshot.getValue().toString());
                                                        c.setUserId(d.getKey());
                                                        mObjectList.add(c);
                                                        arrayAdapter.add(c.getName());

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }
                                    }

                                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String strName = arrayAdapter.getItem(which);
                                            //GO TO USER PROFILE ON CLICK
                                            fragment = new ProfileFragment();

                                            Bundle args = new Bundle();
                                            args.putString("userID", mObjectList.get(which).getUserId());
                                            fragment.setArguments(args);

                                            getSupportFragmentManager().beginTransaction()
                                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                                    .replace(R.id.content_main, fragment)
                                                    .addToBackStack(null)
                                                    .commit();

                                        }
                                    });

                                    builderSingle.show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        return true;
                    }

                });
                //angry
                viewHolder.reaction_angry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessReactions = true;
                        mDataReactions.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    if (dataSnapshot.child(post_id).child(auth.getCurrentUser().getUid()).exists()) {

                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).removeValue();

                                        mProcessReactions = false;
                                    } else {
                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).setValue("angry");
                                        mProcessReactions = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                viewHolder.reaction_angry.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mProcessReactions = true;
                        arrayAdapter.clear();
                        mObjectList.clear();

                        builderSingle.setIcon(R.drawable.angry);
                        builderSingle.setTitle("who's angry ?..");

                        mDataReactions.child(post_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                                        if (d.getValue(String.class).equals("angry")) {
                                            //OBTENER USER_ID DE LOS QUE DIERON LIKE
                                            mDataUsers.child(d.getKey()).child("Name").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (mProcessReactions) {
                                                        //BUSCAR EL NOMBRE POR EL USER_ID Y ANHADIR NOMBRE A LISTA
                                                        Chat c = new Chat();
                                                        c.setName(dataSnapshot.getValue().toString());
                                                        c.setUserId(d.getKey());
                                                        mObjectList.add(c);
                                                        arrayAdapter.add(c.getName());

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }
                                    }

                                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String strName = arrayAdapter.getItem(which);
                                            //GO TO USER PROFILE ON CLICK
                                            fragment = new ProfileFragment();

                                            Bundle args = new Bundle();
                                            args.putString("userID", mObjectList.get(which).getUserId());
                                            fragment.setArguments(args);

                                            getSupportFragmentManager().beginTransaction()
                                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                                    .replace(R.id.content_main, fragment)
                                                    .addToBackStack(null)
                                                    .commit();

                                        }
                                    });

                                    builderSingle.show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        return true;
                    }

                });


                //comments
                viewHolder.button_comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragmentTransaction = true;

                        if (fragmentTransaction) {
                            fragment = new CommentFragment();

                            Bundle args = new Bundle();
                            args.putString("UID", auth.getCurrentUser().getUid());
                            args.putString("POSTID", post_id);
                            fragment.setArguments(args);

                            getSupportFragmentManager().beginTransaction()
                                    //     .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                    .replace(R.id.content_main, fragment)
                                    .addToBackStack(null)
                                    .commit();

                        }
                        fragmentTransaction = false;
                    }
                });

                viewHolder.button_profilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        fragment = new ProfileFragment();

                        Bundle args = new Bundle();
                        args.putString("userID", model.getUserId());
                        fragment.setArguments(args);

                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                                .replace(R.id.content_main, fragment)
                                .addToBackStack(null)
                                .commit();

                    }

                });
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mBlogList.setLayoutManager(layoutManager);
        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                setUser();
            } else {
                Toast.makeText(getBaseContext(), "Error login", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        MenuItem item_post = menu.findItem(R.id.menuPost);
        searchView = (SearchView) item.getActionView();

        nav_username = (TextView) findViewById(R.id.nav_header_name);
        nav_email = (TextView) findViewById(R.id.nav_header_mail);
        nav_profile_picture = (ImageView) findViewById(R.id.imageViewProfile);

        item_post.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                fragment = new PostFragment();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.content_main, fragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString("search", query);

                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.content_main, fragment)
                        .addToBackStack(null)
                        .commit();

                searchView.setIconified(true);
                searchView.clearFocus();
                if (mMenu != null) {
                    (mMenu.findItem(R.id.menuSearch)).collapseActionView();
                }

                searchView.setQuery("", false);
                searchView.setIconified(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
        return super.
                onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action

            fragmentTransaction = true;

            if (fragmentTransaction) {
                fragment = new ProfileFragment();

                Bundle args = new Bundle();
                args.putString("userID", auth.getCurrentUser().getUid());
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.content_main, fragment)
                        .addToBackStack(null)
                        .commit();

            }
            fragmentTransaction = false;


        } else if (id == R.id.nav_people) {


            fragmentTransaction = true;

            if (fragmentTransaction) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                this.finish();
            }
            fragmentTransaction = false;


        } else if (id == R.id.nav_categories) {

            //abrir chat
            fragmentTransaction = true;
            if (fragmentTransaction) {
                fragment = new CategoryFragment();

                Bundle args = new Bundle();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.content_main, fragment)
                        .addToBackStack(null)
                        .commit();

            }
            fragmentTransaction = false;

        } else if (id == R.id.nav_chat) {

            //abrir chat
            fragmentTransaction = true;
            if (fragmentTransaction) {
                fragment = new ChatFragment();

                Bundle args = new Bundle();
                args.putString("userID", auth.getCurrentUser().getUid());
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        //.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.content_main, fragment)
                        .addToBackStack(null)
                        .commit();

            }
            fragmentTransaction = false;

        } else if (id == R.id.nav_logout) {

            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d("AUTH", "User logged out");
                    finish();
                }
            });

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void setUser() {

        DatabaseReference user_exist = mDataUsers;
        user_exist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(auth.getCurrentUser().getUid())) {
                    DatabaseReference current_user_db = mDataUsers.child(auth.getCurrentUser().getUid());
                    try {
                        current_user_db.child("Name").setValue(auth.getCurrentUser().getDisplayName());
                        current_user_db.child("Email").setValue(auth.getCurrentUser().getEmail());
                        current_user_db.child("UserId").setValue(auth.getCurrentUser().getUid());
                        if (auth.getCurrentUser().getPhotoUrl() == null) {
                            current_user_db.child("Image").setValue("https://firebasestorage.googleapis.com/v0/b/realnews-18145.appspot.com/o/noavatar.png?alt=media&token=8ef818ac-e3b3-45e9-9d73-0e065129799a");
                        } else {
                            current_user_db.child("Image").setValue(auth.getCurrentUser().getPhotoUrl().toString());
                        }

                    } catch (Exception er) {
                        System.out.println(er.toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nav_username.setText(auth.getCurrentUser().getDisplayName());
        nav_email.setText(auth.getCurrentUser().getEmail());
        if (auth.getCurrentUser().getPhotoUrl() == null) {
            Picasso.with(getBaseContext()).load("https://firebasestorage.googleapis.com/v0/b/realnews-18145.appspot.com/o/noavatar.png?alt=media&token=8ef818ac-e3b3-45e9-9d73-0e065129799a").into(nav_profile_picture);
        } else {
            Picasso.with(getBaseContext()).load(auth.getCurrentUser().getPhotoUrl()).into(nav_profile_picture);
        }
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageButton reaction_like;
        ImageButton reaction_lol;
        ImageButton reaction_shit;
        ImageButton reaction_sad;
        ImageButton reaction_love;
        ImageButton reaction_angry;
        ImageButton button_comment;
        ImageView button_profilePicture;


        View mView;
        TextView post_title;
        TextView reactions_count_like;
        TextView reactions_count_lol;
        TextView reactions_count_love;
        TextView reactions_count_sad;
        TextView reactions_count_shit;
        TextView reactions_count_angry;
        TextView comments_count;
        DatabaseReference mDatabaseReaction;
        DatabaseReference mDatabaseUsers;
        DatabaseReference mDatabaseComments;
        FirebaseAuth mAuth;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            reaction_like = (ImageButton) mView.findViewById(R.id.reaction_like);
            mDatabaseReaction = FirebaseDatabase.getInstance().getReference().child("Reactions");
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments");

            mAuth = FirebaseAuth.getInstance();
            mDatabaseReaction.keepSynced(true);

            reaction_lol = (ImageButton) mView.findViewById(R.id.reaction_lol);
            reaction_shit = (ImageButton) mView.findViewById(R.id.reaction_shit);
            reaction_sad = (ImageButton) mView.findViewById(R.id.reaction_sad);
            reaction_love = (ImageButton) mView.findViewById(R.id.reaction_love);
            reaction_angry = (ImageButton) mView.findViewById(R.id.reaction_angry);
            button_comment = (ImageButton) mView.findViewById(R.id.button_comment);
            button_profilePicture = (ImageView) mView.findViewById(R.id.post_user_profilePicture);
            reactions_count_like = (TextView) mView.findViewById(R.id.reactions_count_like);
            reactions_count_lol = (TextView) mView.findViewById(R.id.reactions_count_lol);
            reactions_count_love = (TextView) mView.findViewById(R.id.reactions_count_love);
            reactions_count_sad = (TextView) mView.findViewById(R.id.reactions_count_sad);
            reactions_count_shit = (TextView) mView.findViewById(R.id.reactions_count_shit);
            reactions_count_angry = (TextView) mView.findViewById(R.id.reactions_count_angry);
            comments_count = (TextView) mView.findViewById(R.id.comment_count);

            post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        public void setTitle(String title) {

            post_title.setText(title);
        }

        public void setDesc(String desc) {
            TextView post_desc = (TextView) mView.findViewById(R.id.post_description);
            post_desc.setText(desc);
        }

        public void setImage(Context ctx, String image) {
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);

            Picasso.with(ctx).load(image)
                    .resize(400, 400)
                    .into(post_image);
        }

        public void setUserPic(final Context ctx, final String userId) {

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ImageView profile_pic = (ImageView) mView.findViewById(R.id.post_user_profilePicture);
                    Picasso.with(ctx).load(dataSnapshot
                            .child(userId)
                            .child("Image").getValue().toString()).into(profile_pic);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setUsername(final String userId) {
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    TextView post_username = (TextView) mView.findViewById(R.id.post_username);

                    post_username.setText(dataSnapshot.child(userId).child("Name").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setDate(String post_date) {
            TextView date = (TextView) mView.findViewById(R.id.post_date);
            date.setText(post_date);
        }

        public void setLikeBtn(final String post_id) {

            try {
                mDatabaseReaction.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int aux = 0;
                        if (dataSnapshot.child(post_id).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).getValue().equals("like")) {

                            reaction_like.setImageResource(R.drawable.like);

                        } else {
                            reaction_like.setImageResource(R.drawable.bw_like);
                        }

                        for (DataSnapshot snap : dataSnapshot.child(post_id).getChildren()) {
                            if (snap.getValue().equals("like"))
                                aux++;
                        }

                        reactions_count_like.setText(String.valueOf(aux));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (Exception er) {
                System.out.println(er.toString());
            }

        }

        public void setLolBtn(final String post_id) {

            try {
                mDatabaseReaction.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int aux = 0;
                        if (dataSnapshot.child(post_id).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).getValue().equals("lol")) {

                            reaction_lol.setImageResource(R.drawable.lought);

                        } else {
                            reaction_lol.setImageResource(R.drawable.bw_lought);
                        }
                        for (DataSnapshot snap : dataSnapshot.child(post_id).getChildren()) {
                            if (snap.getValue().equals("lol"))
                                aux++;
                        }
                        reactions_count_lol.setText(String.valueOf(aux));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (Exception error) {
                System.out.println(error.toString());
            }

        }

        public void setShitBtn(final String post_id) {

            try {
                mDatabaseReaction.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int aux = 0;
                        if (dataSnapshot.child(post_id).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).getValue().equals("shit")) {

                            reaction_shit.setImageResource(R.drawable.shit);

                        } else {
                            reaction_shit.setImageResource(R.drawable.bw_shit);
                        }
                        for (DataSnapshot snap : dataSnapshot.child(post_id).getChildren()) {
                            if (snap.getValue().equals("shit"))
                                aux++;
                        }
                        reactions_count_shit.setText(String.valueOf(aux));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (Exception er) {
                System.out.println(er.toString());
            }
        }

        public void setSadBtn(final String post_id) {

            try {
                mDatabaseReaction.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int aux = 0;
                        if (dataSnapshot.child(post_id).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).getValue().equals("sad")) {


                            reaction_sad.setImageResource(R.drawable.sad);

                        } else {
                            reaction_sad.setImageResource(R.drawable.bw_sad);
                        }
                        for (DataSnapshot snap : dataSnapshot.child(post_id).getChildren()) {
                            if (snap.getValue().equals("sad"))
                                aux++;
                        }
                        reactions_count_sad.setText(String.valueOf(aux));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (Exception er) {
                System.out.println(er.toString());
            }

        }

        public void setLoveBtn(final String post_id) {

            try {
                mDatabaseReaction.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int aux = 0;
                        if (dataSnapshot.child(post_id).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).getValue().equals("love")) {
                            reaction_love.setImageResource(R.drawable.love);
                        } else {
                            reaction_love.setImageResource(R.drawable.bw_love);
                        }
                        for (DataSnapshot snap : dataSnapshot.child(post_id).getChildren()) {
                            if (snap.getValue().equals("love"))
                                aux++;
                        }
                        reactions_count_love.setText(String.valueOf(aux));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (Exception er) {
                System.out.println(er.toString());
            }

        }

        public void setAngryBtn(final String post_id) {

            try {
                mDatabaseReaction.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int aux = 0;
                        if (dataSnapshot.child(post_id).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).exists() &&
                                dataSnapshot.child(post_id).child(mAuth.getCurrentUser().getUid()).getValue().equals("angry")) {


                            reaction_angry.setImageResource(R.drawable.angry);

                        } else {
                            reaction_angry.setImageResource(R.drawable.bw_angry);
                        }
                        for (DataSnapshot snap : dataSnapshot.child(post_id).getChildren()) {
                            if (snap.getValue().equals("angry"))
                                aux++;
                        }
                        reactions_count_angry.setText(String.valueOf(aux));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (Exception er) {
                System.out.println(er.toString());
            }

        }

        public void setComment(final String post_id) {

            try {
                mDatabaseComments.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        comments_count.setText(String.valueOf(dataSnapshot.child(post_id).getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (Exception er) {
                System.out.println(er.toString());
            }
        }

    }

}
