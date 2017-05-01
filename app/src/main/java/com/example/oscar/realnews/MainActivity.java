package com.example.oscar.realnews;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NewPostFragment.OnFragmentInteractionListener {

    Fragment fragment = null;
    Boolean fragmentTransaction = false;
    FloatingActionButton fab;
    private static final int RC_SIGN_IN = 10;
    private RecyclerView mBlogList;
    private DatabaseReference mDataPosts;
    private DatabaseReference mDataReactions;
    private DatabaseReference mDataUsers;
    private Boolean mProcessReactions = false;
    private int mReactionCount = 0;
    private StorageReference mStorage;
    private Menu mMenu;
    private FirebaseAuth auth;
    private TextView nav_username;
    private TextView nav_email;

    SearchView searchView = null;
    ImageView nav_profile_picture;

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

        mDataReactions.keepSynced(true);
        mDataUsers.keepSynced(true);
        mDataReactions.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentTransaction = true;

                if (fragmentTransaction) {
                    fragment = new NewPostFragment();
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
            protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position) {

                final String post_id = getRef(position).getKey().toString();

                viewHolder.setLikeBtn(post_id);

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUsername(model.getUserName());
                viewHolder.setUserPic(getApplicationContext(), model.getUserPic());
                viewHolder.setDate(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Post Id: " + post_id, Toast.LENGTH_SHORT).show();
                    }
                });


                viewHolder.reaction_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessReactions = true;
                        mDataReactions.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessReactions) {
                                    if (dataSnapshot.child(post_id).hasChild(auth.getCurrentUser().getUid())) {

                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).removeValue();
                                        Toast.makeText(getApplicationContext(), "Like Removed", Toast.LENGTH_SHORT).show();

                                        mProcessReactions = false;
                                    } else {

                                        mDataReactions.child(post_id).child(auth.getCurrentUser().getUid()).setValue("like");
                                        Toast.makeText(getApplicationContext(), "Like", Toast.LENGTH_SHORT).show();

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
            }
        };

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

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageButton reaction_like;
        ImageButton reaction_lol;
        ImageButton reaction_shit;
        ImageButton reaction_sad;
        ImageButton reaction_love;
        ImageButton reaction_angry;
        ImageButton button_comment;

        View mView;
        TextView post_title;
        TextView reactions_count;
        DatabaseReference mDatabaseReaction;
        FirebaseAuth mAuth;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            reaction_like = (ImageButton) mView.findViewById(R.id.reaction_like);
            mDatabaseReaction = FirebaseDatabase.getInstance().getReference().child("Reactions");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseReaction.keepSynced(true);

            reaction_lol = (ImageButton) mView.findViewById(R.id.reaction_lol);
            reaction_shit = (ImageButton) mView.findViewById(R.id.reaction_shit);
            reaction_sad = (ImageButton) mView.findViewById(R.id.reaction_sad);
            reaction_love = (ImageButton) mView.findViewById(R.id.reaction_love);
            reaction_angry = (ImageButton) mView.findViewById(R.id.reaction_angry);
            button_comment = (ImageButton) mView.findViewById(R.id.button_comment);
            reactions_count = (TextView) mView.findViewById(R.id.reactions_count);

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

            Picasso.with(ctx).load(image).into(post_image);
        }

        public void setUserPic(Context ctx, String image) {
            ImageView post_profilePic = (ImageView) mView.findViewById(R.id.post_user_profilePicture);

            Picasso.with(ctx).load(image).into(post_profilePic);
        }

        public void setUsername(String username) {
            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }

        public void setDate(String username) {
            TextView post_username = (TextView) mView.findViewById(R.id.post_date);
            post_username.setText(username);
        }

        public void setLikeBtn(final String post_id) {

            mDatabaseReaction.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    reactions_count.setText(String.valueOf(dataSnapshot.child(post_id).getChildrenCount()));

                    if (dataSnapshot.child(post_id).hasChild(mAuth.getCurrentUser().getUid())) {

                        reaction_like.setImageResource(R.drawable.like);

                    } else {
                        reaction_like.setImageResource(R.drawable.bw_like);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

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
        } else if (id == R.id.nav_people) {

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

}
