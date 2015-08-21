package ru.netis.devel.webinar_social;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private LoginButton fbButton;
    private Button contentButton;
    private ListView dataList;
    private TextView nameText;

    public static CallbackManager callbackManager;

    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        fbButton = (LoginButton) findViewById(R.id.login_button);
        contentButton = (Button) findViewById(R.id.content_button);
        dataList = (ListView) findViewById(R.id.list_data);
        nameText = (TextView) findViewById(R.id.facebook_name);

        if (AccessToken.getCurrentAccessToken() != null) {
            contentButton.setVisibility(View.VISIBLE);
            contentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    accessToken = AccessToken.getCurrentAccessToken();
                    getFacebookData();
                }
            });
        }
    }

    private void onFbLogin() {
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_posts", "public_profile", "user_friends", "user_birthday"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println("Success");
                accessToken = AccessToken.getCurrentAccessToken();
                contentButton.setVisibility(View.INVISIBLE);
                getFacebookData();
            }

            @Override
            public void onCancel() {
                System.out.println("Cancel");
            }

            @Override
            public void onError(FacebookException e) {
                System.out.println(e.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
