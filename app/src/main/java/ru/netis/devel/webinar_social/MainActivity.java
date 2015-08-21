package ru.netis.devel.webinar_social;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "myLog";
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

        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFbLogin();
            }
        });

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
                Log.d(LOG_TAG, " LoginManager.getInstance.registerCallback.onSuccess");
                accessToken = AccessToken.getCurrentAccessToken();
                contentButton.setVisibility(View.INVISIBLE);
                getFacebookData();
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, " LoginManager.getInstance.registerCallback.onCancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.d(LOG_TAG, " LoginManager.getInstance.registerCallback.onError:" + e.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getFacebookData() {

        GraphRequestBatch batch;
        batch = new GraphRequestBatch(GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                if (graphResponse.getError() != null) {
                    Log.d(LOG_TAG, "GraphRequest.newMeRequest.onCompleted: ERROR");
                } else {
                    Log.d(LOG_TAG, "GraphRequest.newMeRequest.onCompleted: SUCCESS");
                    Log.d(LOG_TAG, jsonObject.toString());
                    try {
                        String name = jsonObject.getString("name");
                        nameText.setText(name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }), new GraphRequest(accessToken, "me/feed", null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                if (graphResponse.getError() != null) {
                    Log.d(LOG_TAG, "GraphRequest.onCompleted: ERROR");
                } else {
                    Log.d(LOG_TAG, "GraphRequest.onCompleted: SUCCESS");
                    Log.d(LOG_TAG, graphResponse.getRawResponse());
                    try {
                        JSONObject jsonObject = null;
                        JSONArray jsonArray = null;
                        jsonObject = new JSONObject(graphResponse.getRawResponse());
                        jsonArray = jsonObject.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonOneStory = jsonArray.getJSONObject(i);
                            String story = jsonOneStory.optString("story");
                            if (story.isEmpty()) {
                                story = jsonOneStory.optString("message");
                            }
                            if (story.isEmpty()) {
                                story = "empty";
                            }
                            Log.d(LOG_TAG, story);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }));
        batch.executeAsync();
    }
}
