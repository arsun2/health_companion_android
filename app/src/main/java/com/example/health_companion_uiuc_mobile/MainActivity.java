package com.example.health_companion_uiuc_mobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.health_companion_uiuc_mobile.utils.NetworkHelper;
import com.fitbit.api.exceptions.MissingScopesException;
import com.fitbit.api.loaders.ResourceLoaderResult;
import com.fitbit.api.models.User;
import com.fitbit.api.models.UserContainer;
import com.fitbit.api.services.UserService;
import com.fitbit.authentication.AuthenticationManager;
import com.google.common.base.Joiner;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                   StatsFragment.OnFragmentInteractionListener,
                   AboutFragment.OnFragmentInteractionListener,
                   PlanFragment.OnFragmentInteractionListener,
                   LoaderManager.LoaderCallbacks<ResourceLoaderResult<UserContainer>>{

    private boolean networkOK;
    private User user;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Contact Us for More Information", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cascade.cs.illinois.edu/"));
                startActivity(browserIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        networkOK = NetworkHelper.checkNetworkAccess(this);
        Toast.makeText(this, "Network ok: " + networkOK, Toast.LENGTH_SHORT).show();

        // load user profile and then start the stat fragment
        getLoaderManager().initLoader(1, null, this).forceLoad();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displayView(item.getItemId());
        return true;
    }

    public void displayView(int viewId) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        Bundle bundle = new Bundle();
        bundle.putString("userID", user.getEncodedId());

        switch (viewId) {
            case R.id.nav_stats:
                fragment = new StatsFragment();
                title  = getString(R.string.stats);
                break;
            case R.id.nav_plan:
                fragment = new PlanFragment();
                title = getString(R.string.plan);
                break;
            case R.id.nav_about:
                fragment = new AboutFragment();
                title = getString(R.string.about);
                break;
            case R.id.nav_logout:
                AuthenticationManager.logout(this);
                break;
        }

        if (fragment != null) {
            fragment.setArguments(bundle);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // load user profile

    @Override
    public Loader<ResourceLoaderResult<UserContainer>> onCreateLoader(int i, Bundle bundle) {
        return UserService.getLoggedInUserLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ResourceLoaderResult<UserContainer>> loader, ResourceLoaderResult<UserContainer> data) {
        switch (data.getResultType()) {
            case ERROR:
                Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_LONG).show();
                break;
            case EXCEPTION:
                Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_LONG).show();
                break;
        }

        if (data.isSuccessful()) {
            user = data.getResult().getUser();

            // display the stat view only after loading the user
//            displayView(R.id.nav_stats);

            displayView(R.id.nav_stats);
        } else {
            AuthenticationManager.logout(this);
        }

    }

    @Override
    public void onLoaderReset(Loader<ResourceLoaderResult<UserContainer>> loader) {
    }
}
