package fr.r_thd.player.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.r_thd.player.R;
import fr.r_thd.player.adapter.PlaylistAdapter;
import fr.r_thd.player.model.Music;
import fr.r_thd.player.model.Playlist;

public class HomeActivity extends AppCompatActivity {
    private static List<Playlist> playlistList = new ArrayList<>();

    static {
        Playlist p1 = new Playlist("Ma playlist");
        p1.add(new Music("test", R.raw.abc));
        p1.add(new Music("test2", R.raw.abc));

        Playlist p2 = new Playlist("Autre playlist");
        p2.add(new Music("test3", R.raw.abc));
        p2.add(new Music("test4", R.raw.abc));
        p2.add(new Music("test5", R.raw.abc));

        playlistList.add(p1);
        playlistList.add(p2);
    }

    public static final String EXTRA_DETAILS_PLAYLIST = "EXTRA_DETAILS_PLAYLIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final RecyclerView list = findViewById(R.id.playlist_list);
        list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        list.setAdapter(new PlaylistAdapter(playlistList) {
            @Override
            public void onItemClick(View v) {
                Playlist playlist = playlistList.get(list.getChildViewHolder(v).getAdapterPosition());
                Intent intent = new Intent(getApplicationContext(), PlaylistActivity.class);
                intent.putExtra(EXTRA_DETAILS_PLAYLIST, playlist);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Détails d'une playlist", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        findViewById(R.id.button_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Nom de la playlist");

                final EditText input = new EditText(HomeActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        playlistList.add(new Playlist(name));
                    }
                });

                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        findViewById(R.id.button_create).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Créer une playlist", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
