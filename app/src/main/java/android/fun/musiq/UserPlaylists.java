package android.fun.musiq;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UserPlaylists extends ActionBarActivity {
    private final SpotifyApi spotifyApi = new SpotifyApi();
    private final SpotifyService service = spotifyApi.getService();
    private String currentUserId = "";
    private String songUri = "";
    private String accessToken;
    private final ArrayList<PlaylistSimple> userPlaylists = new ArrayList<>();
    private PlaylistAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_playlists);
        songUri = getIntent().getStringExtra("Song URI");
        currentUserId = getIntent().getStringExtra("user ID");
        accessToken = getIntent().getStringExtra("access token");
        spotifyApi.setAccessToken(accessToken);

        ListView playlistView = (ListView) findViewById(R.id.playlistView);
        playlistAdapter = new PlaylistAdapter(this, userPlaylists);
        playlistView.setAdapter(playlistAdapter);
        getUserPlaylists();
        System.out.println(songUri + "........" + currentUserId);
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_playlists, menu);
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

    private void getUserPlaylists() {
        service.getPlaylists(currentUserId, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                List<PlaylistSimple> playlists = playlistSimplePager.items;
                for (PlaylistSimple playlist : playlists) {
                    playlistAdapter.add(playlist);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Couldn't get playlists", error.toString());
            }
        });
    }
}
