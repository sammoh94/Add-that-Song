package android.fun.musiq;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends ActionBarActivity {
    private static final String CLIENT_ID = "55b994fa86d84e5d8847d58f1b3b1707";
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "add-that-song://callback";
    private final SpotifyApi spotifyApi = new SpotifyApi();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch(response.getType()) {
                case TOKEN:
                    String accessToken = response.getAccessToken();
                    getCurrentUserId(accessToken);
                    break;

                case ERROR:
                    Toast error = Toast.makeText(getApplicationContext(),
                            "Please verify your login credentials", Toast.LENGTH_SHORT);
                    error.show();
                    break;

                default:

            }
        }
    }

    private void getCurrentUserId(String token) {
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();
        service.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate user, Response response) {
                Log.d("Album success", user.id);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });
    }
}
