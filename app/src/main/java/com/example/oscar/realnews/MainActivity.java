package com.example.oscar.realnews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PostFragment.OnFragmentInteractionListener,
        CommentFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener {

    private static final int RC_SIGN_IN = 10;
    Fragment fragment = null;
    Boolean fragmentTransaction = false;
    FloatingActionButton fab;
    SearchView searchView = null;
    ImageView nav_profile_picture;
    private RecyclerView mBlogList;
    private DatabaseReference mDataPosts;
    private DatabaseReference mDataReactions;
    private DatabaseReference mDataUsers;
    private Boolean mProcessReactions = false;
    private StorageReference mStorage;
    private Menu mMenu;
    private FirebaseAuth auth;
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
        mBlogList = (RecyclerView) findViewById(R.id.RecyclerView_MainActivity);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mDataPosts = FirebaseDatabase.getInstance().getReference().child("Posts");
        mDataUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDataReactions = FirebaseDatabase.getInstance().getReference().child("Reactions");
        mDataComments = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDataReactions.keepSynced(true);
        mDataUsers.keepSynced(true);
        mDataReactions.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        profile_pic = (ImageView) findViewById(R.id.post_user_profilePicture);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentTransaction = true;

                if (fragmentTransaction) {
                    fragment = new PostFragment();
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                            .replace(R.id.content_main, fragment)
                            .addToBackStack(null)
                            .commit();
                    fab.setVisibility(View.GONE);
                }
                fragmentTransaction = false;
            }
        });

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

        if (auth.getCurrentUser() != null) {
            setUser();

        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setProviders(
                            AuthUI.FACEBOOK_PROVIDER,
                            //AuthUI.EMAIL_PROVIDER,
                            AuthUI.GOOGLE_PROVIDER)
                    .setLogo(R.mipmap.ic_launcher)
                    .setTheme(R.style.AppThemeLogin)
                    .build(), RC_SIGN_IN);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(

                Post.class,
                R.layout.blog_row,
                PostViewHolder.class,
                mDataPosts
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
                            fab.setVisibility(View.GONE);
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
                            fab.setVisibility(View.GONE);
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
                        fab.setVisibility(View.GONE);
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
        fab.setVisibility(View.VISIBLE);
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
        searchView = (SearchView) item.getActionView();
        nav_username = (TextView) findViewById(R.id.nav_header_name);
        nav_email = (TextView) findViewById(R.id.nav_header_mail);
        nav_profile_picture = (ImageView) findViewById(R.id.imageViewProfile);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

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
                fab.setVisibility(View.GONE);
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

        } else if (id == R.id.nav_share) {

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
                        current_user_db.child("Image").setValue(auth.getCurrentUser().getPhotoUrl().toString());
                        current_user_db.child("Email").setValue(auth.getCurrentUser().getEmail());
                        current_user_db.child("UserId").setValue(auth.getCurrentUser().getUid());
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
            Picasso.with(getBaseContext()).load("http://lorempixel.com/200/200/cats /").into(nav_profile_picture);
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
