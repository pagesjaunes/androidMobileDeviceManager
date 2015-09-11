

package fr.pagesjaunes.mdm;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import fr.pagesjaunes.mdm.core.Device;

/**
 * mdm_DT application
 */
public class BootstrapApplication extends Application {

    private static BootstrapApplication instance;

    /**
     * Create main application
     */
    public BootstrapApplication() {
    }

    /**
     * Create main application
     *
     * @param context
     */
    public BootstrapApplication(final Context context) {
        this();
        attachBaseContext(context);
    }

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public BootstrapApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public static BootstrapApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // Perform injection
        Injector.init(getRootModule(), this);
        // Enable Local Datastore.
       // Parse.enableLocalDatastore(this);
		ParseObject.registerSubclass(Device.class);
        // Add your initialization code here
		Parse.initialize(this, "qKYdojVtMPjHxhNMJZ8sIlPVdKJQZYlFcaPfhKts", "lgrLheiLQXSX5v58d3XMxgBsethavM2aQMnE27m8");
		ParseUser.enableRevocableSessionInBackground();
		//ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
         defaultACL.setPublicReadAccess(true);
		defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);


    }

    private Object getRootModule() {
        return new RootModule();
    }
}
