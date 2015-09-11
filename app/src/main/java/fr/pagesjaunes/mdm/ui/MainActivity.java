

package fr.pagesjaunes.mdm.ui;

import android.accounts.OperationCanceledException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import fr.pagesjaunes.mdm.BootstrapServiceProvider;
import fr.pagesjaunes.mdm.R;
import fr.pagesjaunes.mdm.core.BootstrapService;
import fr.pagesjaunes.mdm.core.Device;
import fr.pagesjaunes.mdm.events.NavItemSelectedEvent;
import fr.pagesjaunes.mdm.util.Ln;
import fr.pagesjaunes.mdm.util.SafeAsyncTask;
import fr.pagesjaunes.mdm.util.UIUtils;


/**
 * Initial activity for the application.
 *
 * If you need to remove the authentication from the application please see
 * {@link fr.pagesjaunes.mdm.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends BootstrapFragmentActivity {

    @Inject protected BootstrapServiceProvider serviceProvider;

    private boolean userHasAuthenticated = false;
	public static final String PREFS_NAME = "settings";

	private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence title;
    private NavigationDrawerFragment navigationDrawerFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        if(isTablet()) {
            setContentView(R.layout.main_activity_tablet);
        } else {
            setContentView(R.layout.main_activity);
        }

        // View injection with Butterknife
        ButterKnife.bind(this);

        // Set up navigation drawer
        title = drawerTitle = getTitle();

        if(!isTablet()) { // was
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerToggle = new ActionBarDrawerToggle(
                    this,                    /* Host activity */
                    drawerLayout,           /* DrawerLayout object */
                    R.drawable.ic_drawer,    /* nav drawer icon to replace 'Up' caret */
                    R.string.navigation_drawer_open,    /* "open drawer" description */
                    R.string.navigation_drawer_close) { /* "close drawer" description */

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle(title);
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle(drawerTitle);
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            // Set the drawer toggle as the DrawerListener
            drawerLayout.setDrawerListener(drawerToggle);

            navigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

            // Set up the drawer.
            navigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        checkAuth();

    }




    private boolean isTablet() {
        return UIUtils.isTablet(this);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
getCurrentUser();
        if(!isTablet()) { // was
            // Sync the toggle state after onRestoreInstanceState has occurred.
            drawerToggle.syncState();
        }
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(!isTablet()) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }


    private void initScreen() {
        if (userHasAuthenticated) {

            Ln.d("Foo");
            final FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new CarouselFragment())
                    .commit();
        }

    }

    private void checkAuth() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final BootstrapService svc = serviceProvider.getService(MainActivity.this);
                return svc != null;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                if (e instanceof OperationCanceledException) {
                    // User cancelled the authentication process (back button, etc).
                    // Since auth could not take place, lets finish this activity.
                    finish();
                }
            }

            @Override
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
                userHasAuthenticated = true;
				initScreen();
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (!isTablet() && drawerToggle.onOptionsItemSelected(item)) { // was
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                //menuDrawer.toggleMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void showVersion()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        CarouselFragment carousel = (CarouselFragment) fragmentManager.findFragmentById(R.id.container);
        carousel.pager.setCurrentItem(1);

    }
    @Subscribe
    public void onNavigationItemSelected(NavItemSelectedEvent event) {

        Ln.d("Selected: %1$s", event.getItemPosition());

        switch(event.getItemPosition()) {
            case 0:
                // Home
                // do nothing as we're already on the home screen.
				getCurrentUser();
                break;
        }
    }

	private void getCurrentUser()
	{
		new SafeAsyncTask<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return updateCurrentDevice();
			}

			@Override
			protected void onException(final Exception e) throws RuntimeException
			{
				super.onException(e);
				if (e instanceof OperationCanceledException)
				{
					// User cancelled the authentication process (back button, etc).
					// Since auth could not take place, lets finish this activity.
				}
			}

			@Override
			protected void onSuccess(Boolean result) throws Exception
			{
				super.onSuccess(result);
			}
		}.execute();

		ItemListFragment currentFragment = (ItemListFragment)this.getSupportFragmentManager().findFragmentById(R.id.vp_pages);
		if (currentFragment != null)
		{
			currentFragment.forceRefresh();
		}
	}

	private Boolean updateCurrentDevice()
	{
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
		Device currentDevice;
		String deviceId = prefs.getString("deviceId", null);

		currentDevice = new Device();
		if (deviceId != null)
		{
			currentDevice = (Device) Device.createWithoutData("Device", deviceId);
			currentDevice.fetchIfNeededInBackground(new GetCallback<ParseObject>()
			{
				@Override
				public void done(ParseObject object, ParseException e)
				{
                    if (object != null)
                    {
                        Device currentDevice = (Device) object;
                        currentDevice.setManufacturer(android.os.Build.BRAND);
                        currentDevice.setModel(android.os.Build.MODEL);
                        currentDevice.setType("");
                        currentDevice.setOsVersion(android.os.Build.VERSION.RELEASE);
                        currentDevice.setUser(ParseUser.getCurrentUser());
                        currentDevice.saveInBackground();
                    }
                    else
                    {
                        e.printStackTrace();
                    }
				}
			});
		} else
		{

			currentDevice.setManufacturer(android.os.Build.BRAND);
			currentDevice.setModel(android.os.Build.MODEL);
			currentDevice.setType("");
			currentDevice.setOsVersion(android.os.Build.VERSION.RELEASE);
			currentDevice.setUser(ParseUser.getCurrentUser());

			currentDevice.saveInBackground(new SaveCallback()
			{
				@Override
				public void done(ParseException e)
				{
					Ln.d("Save ");
				}
			});
		}
		if (deviceId == null)
		{
			SharedPreferences.Editor editor = prefs.edit();
			deviceId = currentDevice.getObjectId();
			editor.putString("deviceId", deviceId);
			editor.commit();
		}

		//		Toast.makeText(getActivity().getApplicationContext(), "" + Build.BRAND + Build.DEVICE + Build.MANUFACTURER + "etc..", Toast.LENGTH_LONG).show();
		return deviceId != null;

	}

}
