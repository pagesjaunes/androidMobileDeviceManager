package fr.pagesjaunes.mdm.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

import butterknife.Bind;
import fr.pagesjaunes.mdm.R;

import static fr.pagesjaunes.mdm.core.Constants.Extra.USER;

public class UserActivity extends BootstrapActivity {

    @Bind(R.id.iv_avatar) protected ImageView avatar;
    @Bind(R.id.tv_name) protected TextView name;

    private ParseUser user;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_view);

        if (getIntent() != null && getIntent().getExtras() != null) {
            user = (ParseUser) getIntent().getExtras().getSerializable(USER);
        }

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        Picasso.with(this).load(user.getAvatarUrl())
//                .placeholder(R.drawable.gravatar_icon)
//                .into(avatar);

        name.setText(user.getUsername());

    }


}
