package android.fun.musiq;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UserPlaylists extends ActionBarActivity {
    private final SpotifyApi spotifyApi = new SpotifyApi();
    private final SpotifyService service = spotifyApi.getService();
    private String currentUserId = "";
    private String songUri = "";
    private final ArrayList<PlaylistSimple> userPlaylists = new ArrayList<>();
    private PlaylistAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_playlists);
        songUri = getIntent().getStringExtra("Song URI");
        currentUserId = getIntent().getStringExtra("user ID");
        String accessToken = getIntent().getStringExtra("access token");
        spotifyApi.setAccessToken(accessToken);

        ListView playlistView = (ListView) findViewById(R.id.playlistView);
        playlistAdapter = new PlaylistAdapter(this, userPlaylists);
        playlistView.setAdapter(playlistAdapter);
        getUserPlaylists();

        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaylistSimple selectedPlaylist = playlistAdapter.getItem(position);
                String playlistId = selectedPlaylist.id;
                addSongToPlaylist(currentUserId, playlistId, songUri);
            }
        });
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

    private void addSongToPlaylist(String userId, String playlistId, String trackUri) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("uris", trackUri);

        service.addTracksToPlaylist(userId, playlistId, queryParameters, new HashMap<String, Object>(), new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                Toast.makeText(UserPlaylists.this, "Song successfully added", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(UserPlaylists.this, "Could not add song", Toast.LENGTH_SHORT)
                        .show();
                error.printStackTrace();
            }
        });
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
