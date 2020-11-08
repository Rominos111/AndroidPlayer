package fr.r_thd.player.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.r_thd.player.R;
import fr.r_thd.player.adapter.MusicAdapter;
import fr.r_thd.player.adapter.AdapterListener;
import fr.r_thd.player.dialog.MusicDeleteDialog;
import fr.r_thd.player.dialog.MusicEditDialog;
import fr.r_thd.player.dialog.UpdatableFromDialog;
import fr.r_thd.player.objects.Music;
import fr.r_thd.player.objects.OnBitmapUpdateListener;
import fr.r_thd.player.objects.Playlist;
import fr.r_thd.player.service.MusicPlayerService;
import fr.r_thd.player.storage.MusicDatabaseStorage;
import fr.r_thd.player.storage.PlaylistDatabaseStorage;
import fr.r_thd.player.util.UriUtility;

/**
 * Activité de playlist
 */
public class PlaylistActivity extends AppCompatActivity implements UpdatableFromDialog {
    private static final int REQUEST_GET_FILE = 0;

    /**
     * Playlist
     */
    private static Playlist playlist;

    /**
     * Adapter
     */
    private MusicAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        int id = getIntent().getIntExtra(HomeActivity.EXTRA_DETAILS_PLAYLIST_ID, -1);

        if (id == -1) {
            Toast.makeText(getApplicationContext(), getString(R.string.err_null_id), Toast.LENGTH_LONG).show();
            return;
        }

        playlist = PlaylistDatabaseStorage.get(getApplicationContext()).find(id);

        if (playlist == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.err_null_playlist), Toast.LENGTH_LONG).show();
            return;
        }

        buildRecyclerView();
        bindButtons();

        SearchView searchView = findViewById(R.id.search_bar_music);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listAdapter.getFilter().filter(newText);
                return true;
            }
        });

        listAdapter.notifyDataSetChanged();
    }

    private void buildRecyclerView() {
        final RecyclerView list = findViewById(R.id.playlist);
        list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listAdapter = new MusicAdapter(playlist.getArray(), new AdapterListener() {
            @Override
            public void onLongClick() {
                Toast.makeText(getApplicationContext(), getString(R.string.hint_detail_playlist), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClick(int pos) {
                Intent intent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
                intent.putExtra(MusicPlayerActivity.EXTRA_CURRENT_PLAYLIST_ID, playlist.getId());
                intent.putExtra(MusicPlayerActivity.EXTRA_SELECTED_MUSIC_INDEX, pos);
                startActivity(intent);
            }

            @Override
            public void onEditButtonClick(int pos) {
                // TODO:
                /*
                Intent serviceIntent = new Intent(getApplicationContext(), NotificationService.class);
                serviceIntent.setAction(NotificationService.STARTFOREGROUND_ACTION);
                startService(serviceIntent);
                 */

                new MusicEditDialog(PlaylistActivity.this, playlist, pos).show(getSupportFragmentManager(), "");
            }

            @Override
            public void onDeleteButtonClick(int pos) {
                new MusicDeleteDialog(PlaylistActivity.this, playlist, listAdapter, pos).show(getSupportFragmentManager(), "");
            }
        });

        listAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                onChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                onChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                onChanged();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                onChanged();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                onChanged();
            }

            @Override
            public void onChanged() {
                super.onChanged();
                View noResultView = findViewById(R.id.no_result);
                if (listAdapter.getItemCount() == 0) {
                    noResultView.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
                }
                else {
                    noResultView.getLayoutParams().height = 0;
                }
                noResultView.requestLayout();
            }
        });

        list.setAdapter(listAdapter);
    }

    private void bindButtons() {
        findViewById(R.id.button_shuffle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playlist.size() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.warn_empty_playlist), Toast.LENGTH_SHORT).show();
                }
                else {
                    playlist.shuffle();
                    playlist.setCurrentIndex(0);
                    Intent intent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
                    intent.putExtra(MusicPlayerActivity.EXTRA_CURRENT_PLAYLIST_ID, playlist.getId());
                    intent.putExtra(MusicPlayerActivity.EXTRA_SELECTED_MUSIC_INDEX, playlist.getRandomIndex());
                    intent.putExtra(MusicPlayerActivity.EXTRA_SHOULD_PLAY, Boolean.valueOf(MusicPlayerService.isPlaying()));
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.button_shuffle).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.hint_shuffle_playlist), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.chooser_music_title)), REQUEST_GET_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GET_FILE && resultCode == RESULT_OK && data != null) {
            List<Uri> uris = new ArrayList<>();

            if(data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                int currentItem = 0;
                while (currentItem < count) {
                    uris.add(data.getClipData().getItemAt(currentItem).getUri());
                    currentItem ++;
                }
            }
            else if (data.getData() != null) {
                uris.add(data.getData());
            }

            for (Uri fileUri : uris) {
                String name = UriUtility.getFileName(fileUri, getContentResolver());
                String path = fileUri.toString();

                final Music music = new Music(playlist.getId(), name, path);

                final int id = MusicDatabaseStorage.get(getApplicationContext()).insert(music);

                music.setOnBitmapUpdateListener(new OnBitmapUpdateListener() {
                    @Override
                    public void onBitmapUpdate() {
                        listAdapter.notifyDataSetChanged();
                        MusicDatabaseStorage.get(getApplicationContext()).update(id, music);
                    }
                });

                if (id == -1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.err_general), Toast.LENGTH_LONG).show();
                }
                else {
                    music.setId(id);
                    listAdapter.add(music);
                    listAdapter.notifyItemInserted(playlist.size() - 1);
                }
            }
        }
    }

    @Override
    public void updateFromDialog(int pos, @NonNull UpdatableFromDialog.UpdateType type) {
        if (type == UpdateType.EDIT) {
            listAdapter.notifyItemChanged(pos);
        }
        else if (type == UpdateType.DELETE) {
            listAdapter.notifyItemRemoved(pos);
        }
    }
}
