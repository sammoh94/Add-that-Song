package android.fun.musiq;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MainActivity extends ActionBarActivity {
    private static final String CLIENT_ID = "55b994fa86d84e5d8847d58f1b3b1707";
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "add-that-song://callback";
    private BackendMethods backendMethods= new BackendMethods();
    Button logoutBtn;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{
                "user-read-private",
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
                    String accessToken = response.getAccessToken();
                    backendMethods.setAccessToken(accessToken);
                    backendMethods.getCurrentUserId();
                    Intent i = new Intent(MainActivity.this, RecognizeSong.class);
                    startActivity(i);
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
}
