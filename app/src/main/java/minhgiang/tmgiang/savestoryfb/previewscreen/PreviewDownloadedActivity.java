package minhgiang.tmgiang.savestoryfb.previewscreen;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import minhgiang.tmgiang.savestoryfb.MainActivity;
import minhgiang.tmgiang.savestoryfb.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PreviewDownloadedActivity extends AppCompatActivity {
    List<FileDownloaded> downloadedFiles = new ArrayList<>();
    RecyclerView rvDownloaded;
    DownloadedListAdapter downloadedListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_downloaded);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        rvDownloaded = findViewById(R.id.rvDownloaded);

        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadedFiles = getAllFileDownloaded();
                downloadedListAdapter = new DownloadedListAdapter(getBaseContext(), downloadedFiles);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
                rvDownloaded.setLayoutManager(layoutManager);
                rvDownloaded.setAdapter(downloadedListAdapter);
                int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
                rvDownloaded.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
            }
        }).start();
    }

    public List<FileDownloaded> getAllFileDownloaded() {
        File folder = new File(MainActivity.APP_FOLDER_URL);
        List<FileDownloaded> result = new ArrayList<>();
        File[] allFiles = new File[0];
        if (folder.exists()) {
            allFiles = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".mp4"));
                }
            });
            Arrays.sort(allFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return (int) (o2.lastModified() - o1.lastModified());
                }
            });
            for (File file : allFiles) {
                result.add(new FileDownloaded(file));
            }
        }
        return result;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
