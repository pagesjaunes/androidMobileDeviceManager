package fr.pagesjaunes.mdm.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.pagesjaunes.mdm.Injector;
import fr.pagesjaunes.mdm.R;
import fr.pagesjaunes.mdm.R.id;
import fr.pagesjaunes.mdm.R.layout;
import fr.pagesjaunes.mdm.R.string;
import fr.pagesjaunes.mdm.core.BootstrapService;
import fr.pagesjaunes.mdm.core.Constants;
import fr.pagesjaunes.mdm.events.UnAuthorizedErrorEvent;
import fr.pagesjaunes.mdm.ui.TextWatcherAdapter;
import fr.pagesjaunes.mdm.util.Ln;
import fr.pagesjaunes.mdm.util.SafeAsyncTask;
import fr.pagesjaunes.mdm.wishlist.Toaster;
import retrofit.RetrofitError;

import static android.R.layout.simple_dropdown_item_1line;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

/**
 * Activity to authenticate the user against an API (example API on Parse.com)
 */
public class BootstrapAuthenticatorActivity extends ActionBarAccountAuthenticatorActivity {

    /**
     * PARAM_CONFIRM_CREDENTIALS
     */
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    /**
     * PARAM_PASSWORD
     */
    public static final String PARAM_PASSWORD = "password";

    /**
     * PARAM_USERNAME
     */
    public static final String PARAM_USERNAME = "username";

    /**
     * PARAM_AUTHTOKEN_TYPE
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    @Bind(id.et_username) protected AutoCompleteTextView usernameText;
    @Bind(id.et_email) protected AutoCompleteTextView emailText;
    @Bind(id.et_password) protected EditText passwordText;
    @Bind(id.b_signin) protected Button signInButton;
    private final TextWatcher watcher = validationTextWatcher();
	@Bind(id.b_signup_ui) protected Button signUpUIButton;
	@Bind(id.b_signup) protected Button signUpButton;
	private final TextWatcher signupWatcher = signupTextWatcher();
    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean requestNewAccount = false;
    @Inject BootstrapService bootstrapService;
    @Inject Bus bus;
    private AccountManager accountManager;
    private SafeAsyncTask<Boolean> authenticationTask;
    private String authToken;
    private String authTokenType;
    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean confirmCredentials = false;
    private String email;
    private String password;
    /**
     * In this instance the token is simply the sessionId returned from Parse.com. This could be a
     * oauth token or some other type of timed token that expires/etc. We're just using the parse.com
     * sessionId to prove the example of how to utilize a token.
     */
    private String token;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Injector.inject(this);

        accountManager = AccountManager.get(this);

        final Intent intent = getIntent();
        email = intent.getStringExtra(PARAM_USERNAME);
        authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        confirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);

        requestNewAccount = email == null;

        setContentView(layout.login_activity);

        ButterKnife.bind(this);

        emailText.setAdapter(new ArrayAdapter<String>(this, simple_dropdown_item_1line, userEmailAccounts()));

        passwordText.setOnKeyListener(new OnKeyListener() {

            public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
                if (event != null && ACTION_DOWN == event.getAction()
                        && keyCode == KEYCODE_ENTER && signInButton.isEnabled()) {
                    handleLogin(signInButton);
                    return true;
                }
                return false;
            }
        });

        passwordText.setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(final TextView v, final int actionId,
                                          final KeyEvent event) {
                if (actionId == IME_ACTION_DONE && signInButton.isEnabled()) {
                    handleLogin(signInButton);
                    return true;
                }
                return false;
            }
        });

        usernameText.addTextChangedListener(watcher);
        passwordText.addTextChangedListener(watcher);

        final TextView signUpText = (TextView) findViewById(id.tv_signup);
        signUpText.setMovementMethod(LinkMovementMethod.getInstance());
        signUpText.setText(Html.fromHtml(getString(string.signup_link)));
    }

    private List<String> userEmailAccounts() {
        final Account[] accounts = accountManager.getAccountsByType("com.google");
        final List<String> emailAddresses = new ArrayList<String>(accounts.length);
        for (final Account account : accounts) {
            emailAddresses.add(account.name);
        }
        return emailAddresses;
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable gitDirEditText) {
                updateUIWithValidation();
            }

        };
    }

	private TextWatcher signupTextWatcher() {
		return new TextWatcherAdapter() {
			public void afterTextChanged(final Editable gitDirEditText) {
				updateUISignupWithValidation();
			}

		};
	}

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        updateUIWithValidation();
    }

    private void updateUIWithValidation() {
		final boolean populated = populated(usernameText) && populated(passwordText);
		signInButton.setEnabled(populated);
	}

	private void updateUISignupWithValidation() {
		final boolean populated = populated(usernameText) && populated(passwordText) && populated(emailText);
		signUpButton.setEnabled(populated);
	}

    private boolean populated(final EditText editText) {
        return editText.length() > 0;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(string.message_signing_in));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			public void onCancel(final DialogInterface dialog)
			{
				if (authenticationTask != null)
				{
					authenticationTask.cancel(true);
				}
			}
		});
        return dialog;
    }

    @Subscribe
    public void onUnAuthorizedErrorEvent(UnAuthorizedErrorEvent unAuthorizedErrorEvent) {
        // Could not authorize for some reason.
        Toaster.showLong(BootstrapAuthenticatorActivity.this, R.string.message_bad_credentials);
    }

	public void handleSignupUI(final View view)
	{
		View signup = ButterKnife.findById((View) view.getParent(),R.id.v_signup);
		usernameText.addTextChangedListener(signupWatcher);
		passwordText.addTextChangedListener(signupWatcher);
		emailText.addTextChangedListener(signupWatcher);
		signup.setVisibility(View.VISIBLE);
		updateUISignupWithValidation();

		signUpUIButton.setVisibility(View.GONE);
		signInButton.setVisibility(View.GONE);

	}

	public void handleSignup(final View view)
	{
		showProgress();
		ParseUser user = ParseUser.getCurrentUser();
		user.setUsername(usernameText.getText().toString());
		user.setPassword(passwordText.getText().toString());
		user.setEmail(emailText.getText().toString());

        user.saveInBackground(new SaveCallback()
		{
			public void done(ParseException e)
			{
				if (e == null)
				{
					// Hooray! Let them use the app now.
					Ln.d("Hooray!");
					handleLogin(view);
				}
				else
				{
					// Sign up didn't succeed. Look at the ParseException
					// to figure out what went wrong
					e.printStackTrace();
				}
			}
		});
	}


    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     * <p/>
     * Specified by android:onClick="handleLogin" in the layout xml
     *
     * @param view
     */
    public void handleLogin(final View view) {
        if (authenticationTask != null) {
            return;
        }

        if (requestNewAccount) {
            email = usernameText.getText().toString();
        }

        password = passwordText.getText().toString();
        showProgress();

        authenticationTask = new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {


						bootstrapService.authenticate(email, password);
				ParseUser loginResponse = ParseUser.getCurrentUser();
                token = loginResponse.getSessionToken();

                return true;
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {
                onAuthenticationResult(authSuccess);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                // Retrofit Errors are handled inside of the {
                if(!(e instanceof RetrofitError)) {
                    final Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if(cause != null) {
                        Toaster.showLong(BootstrapAuthenticatorActivity.this, cause.getMessage());
						Ln.d("onException: failed to authenticate :%s", cause.getLocalizedMessage());
                    }
                }
            }

            @Override
            protected void onFinally() throws RuntimeException {
                hideProgress();
                authenticationTask = null;
            }
        };
        authenticationTask.execute();
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     *
     * @param result
     */
    protected void finishConfirmCredentials(final boolean result) {
        final Account account = new Account(email, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        accountManager.setPassword(account, password);

        final Intent intent = new Intent();
        intent.putExtra(KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. Also sets
     * the authToken in AccountManager for this account.
     */

    protected void finishLogin() {
        final Account account = new Account(email, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);

        if (requestNewAccount) {
            accountManager.addAccountExplicitly(account, password, null);
        } else {
            accountManager.setPassword(account, password);
        }

        authToken = token;

        final Intent intent = new Intent();
        intent.putExtra(KEY_ACCOUNT_NAME, email);
        intent.putExtra(KEY_ACCOUNT_TYPE, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);

        if (authTokenType != null
                && authTokenType.equals(Constants.Auth.AUTHTOKEN_TYPE)) {
            intent.putExtra(KEY_AUTHTOKEN, authToken);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Hide progress dialog
     */
    @SuppressWarnings("deprecation")
    protected void hideProgress() {
        dismissDialog(0);
    }

    /**
     * Show progress dialog
     */
    @SuppressWarnings("deprecation")
    protected void showProgress() {
        showDialog(0);
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param result
     */
    public void onAuthenticationResult(final boolean result) {
        if (result) {
            if (!confirmCredentials) {
                finishLogin();
            } else {
                finishConfirmCredentials(true);
            }
        } else {
            Ln.d("onAuthenticationResult: failed to authenticate");
            if (requestNewAccount) {
                Toaster.showLong(BootstrapAuthenticatorActivity.this,
                        string.message_auth_failed_new_account);
            } else {
                Toaster.showLong(BootstrapAuthenticatorActivity.this,
                        string.message_auth_failed);
            }
        }
    }
}
