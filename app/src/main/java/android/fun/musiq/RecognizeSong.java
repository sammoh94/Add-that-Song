package android.fun.musiq;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Powered by ACRCloud Android SDK
 */

public class RecognizeSong extends ActionBarActivity implements IACRCloudListener {
    private final SpotifyApi spotifyApi = new SpotifyApi();
    private final SpotifyService service = spotifyApi.getService();
    private ACRCloudClient mClient;
    private TextView mVolume, mResult, tv_time;
    private String trackUri = "";

    private boolean mProcessing = false;
    private boolean initState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_song);

        String path = Environment.getExternalStorageDirectory().toString()
                + "/acrcloud/model";

        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }

        mVolume = (TextView) findViewById(R.id.volume);
        mResult = (TextView) findViewById(R.id.result);
        tv_time = (TextView) findViewById(R.id.time);

        Button startBtn = (Button) findViewById(R.id.start);
        Button cancelBtn = (Button) findViewById(R.id.cancel);

        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                start();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

    }

    public void start() {
        mVolume.setText("");
        mResult.setText("");
        if(!this.initState) {
            ACRCloudConfig mConfig = new ACRCloudConfig();

            mConfig.acrcloudListener = this;
            mConfig.context = this;
            mConfig.host = "ap-southeast-1.api.acrcloud.com";
            mConfig.accessKey = "6a0296d0e6d98782693393599b38b6cb";
            mConfig.accessSecret = "eSU07iIpCnL2NC9jDgCv8mjBFmVKSaaGDigzKUML";


            mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
            this.mClient = new ACRCloudClient();
            this.initState = this.mClient.initWithConfig(mConfig);
            if (!this.initState) {
                Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!mProcessing) {
            mProcessing = true;
            if (!this.mClient.startRecognize()) {
                mProcessing = false;
                mResult.setText("start error!");
            }
        }
    }

    protected void cancel() {
        //if (mProcessing) {
        this.mClient.stop();
        tv_time.setText("");
        mResult.setText("");
        //}
        mProcessing = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recognize_song, menu);
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
    public void onResult(String result) {
        try {
            String trackId;
            // Find a smarter way to do this. Super dumb right now
            JSONObject parsedResult = new JSONObject(result);
            JSONObject status = parsedResult.getJSONObject("status");
            JSONObject metadata = parsedResult.getJSONObject("metadata");
            JSONArray music = metadata.getJSONArray("music");
            JSONObject songInfo = (JSONObject) music.get(0);
            JSONObject external_metadata = (JSONObject) songInfo.get("external_metadata");
            // TODO: Handle case if song is not in Spotify's database
            JSONObject spotifyData = (JSONObject) external_metadata.get("spotify");
            JSONObject track = (JSONObject) spotifyData.get("track");
            trackId = track.get("id").toString();

            if (status.get("msg").equals("Success")) {
                System.out.println(trackId);
                getTrackURI(trackId);
                mProcessing = false;

                if (this.mClient != null) {
                    this.mClient.stop();
                    mProcessing = false;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity", "release");
        if (this.mClient != null) {
            this.mClient.release();
            this.mClient = null;
        }
    }

    @Override
    public void onVolumeChanged(double volume) {
        mVolume.setText("volume: " + volume);
    }

    private void getTrackURI(String trackId) {
        service.getTrack(trackId, new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                trackUri = track.uri;
                Intent playlist = new Intent(RecognizeSong.this, UserPlaylists.class);
                playlist.putExtra("Song URI", trackUri);
                startActivity(playlist);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Track not found", error.toString());
            }
        });
    }
}
