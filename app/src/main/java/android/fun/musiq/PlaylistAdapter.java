package android.fun.musiq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * Created by samohan on 8/11/15.
 */
public class PlaylistAdapter extends ArrayAdapter<PlaylistSimple>{

    public PlaylistAdapter(Context context, ArrayList<PlaylistSimple> userPlaylists) {
        super(context, 0, userPlaylists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlaylistSimple playlistSimple = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlist_item, parent, false);
        }

        TextView playlistName = (TextView) convertView.findViewById(R.id.playlistName);
        playlistName.setText(playlistSimple.name);
        return convertView;
    }
}
