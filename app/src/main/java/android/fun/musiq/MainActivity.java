package android.fun.musiq;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends ActionBarActivity {
    private static final String CLIENT_ID = "55b994fa86d84e5d8847d58f1b3b1707";
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "add-that-song://callback";
    private final SpotifyApi spotifyApi = new SpotifyApi();
    private final SpotifyService service = spotifyApi.getService();
    private AtomicBoolean hasDoresoPlaylist = new AtomicBoolean(false);
    private String currentUserId = "";
    private static String accessToken;
    Button logoutBtn;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{
            "playlist-modify-public"
        });

        final AuthenticationRequest request = builder.build();

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticationClient.openLoginActivity(MainActivity.this, REQUEST_CODE, request);
            }
        });

        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticationClient.logout(getApplicationContext());
            }
        });

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
                    accessToken = response.getAccessToken();
                    spotifyApi.setAccessToken(accessToken);
                    getCurrentUserId();
                    break;

                case ERROR:
                    Toast.makeText(getApplicationContext(),
                            "Please verify your login credentials", Toast.LENGTH_SHORT)
                            .show();
                    break;

                default:
                    // do nothing
            }
        }
    }

    private void getCurrentUserId() {
        service.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate user, Response response) {
                currentUserId = user.id;
                Intent i = new Intent(MainActivity.this, RecognizeSong.class);
                i.putExtra("user ID", currentUserId);
                i.putExtra("access token", accessToken);
                checkForPlaylist(currentUserId);
                startActivity(i);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });
    }

    private void createPlaylist(String userId) {
        Map<String, Object> playlistObj = new HashMap<>();
        playlistObj.put("name", "ACRCloud songs");
        playlistObj.put("public", true);
        service.createPlaylist(userId, playlistObj, new Callback<Playlist>() {
            @Override
            public void success(Playlist playlist, Response response) {
                Log.d("Playlist created", response.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Error in playlist", error.toString());
            }
        });
    }

    private void checkForPlaylist(final String userId) {
        hasDoresoPlaylist.set(false);
        service.getPlaylists(userId, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                List<PlaylistSimple> playlists = playlistSimplePager.items;
                for (PlaylistSimple playlist : playlists) {
                    if (playlist.name.equals("ACRCloud songs")) {
                        hasDoresoPlaylist.set(true);
                        Log.d("Playlist exists", response.toString());
                        break;
                    }
                }

                if (!hasDoresoPlaylist.get()) {
                    createPlaylist(userId);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Couldn't get playlists", error.toString());
            }
        });
    }
}