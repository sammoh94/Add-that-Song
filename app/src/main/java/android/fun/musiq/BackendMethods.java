package android.fun.musiq;

import android.util.Log;

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

/**
 * Created by samohan on 8/10/15.
 */
public class BackendMethods {
    private final SpotifyApi spotifyApi = new SpotifyApi();
    private final SpotifyService service = spotifyApi.getService();
    private AtomicBoolean hasDoresoPlaylist = new AtomicBoolean(false);

    public void getCurrentUserId() {
        service.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate user, Response response) {
                Log.d("User id is: ", user.id);
                checkForPlaylist(user.id);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });
    }

    public void setAccessToken(String newAccessToken) {
        spotifyApi.setAccessToken(newAccessToken);
    }

    private void createPlaylist(String userId) {
        Map<String, Object> playlistObj = new HashMap<>();
        playlistObj.put("name", "Doreso songs");
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
                    if (playlist.name.equals("Doreso songs")) {
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
