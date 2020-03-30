package minhgiang.tmgiang.savestoryfb.previewscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import minhgiang.tmgiang.savestoryfb.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadedListAdapter extends RecyclerView.Adapter<DownloadedListAdapter.DownloadedListViewHolder> {
    private List<FileDownloaded> fileList = new ArrayList<>();
    private Context context;
    public DownloadedListAdapter(Context context, List<FileDownloaded> fileList) {
        this.fileList = fileList;
        this.context = context;
    }

    @NonNull
    @Override
    public DownloadedListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_preview, parent, false);
        return new DownloadedListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadedListViewHolder holder, int position) {
        FileDownloaded fileDownloaded = fileList.get(position);
        File file = fileDownloaded.getFile();
        if (FilenameUtils.getExtension(file.getName()).equals("jpg") ||
                FilenameUtils.getExtension(file.getName()).equals("jpeg") ||
                FilenameUtils.getExtension(file.getName()).equals("png")) {
            holder.ivPlayVideo.setVisibility(View.GONE);
            if(fileDownloaded.getBitmap() != null){
                Glide.with(context)
                        .load(fileDownloaded.getBitmap())
                        .apply(new RequestOptions().override(500, 500))
                        .into(holder.ivThumbnail);
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                fileList.get(position).setBitmap(bitmap);
                Glide.with(context)
                        .load(bitmap)
                        .apply(new RequestOptions().override(500, 500))
                        .into(holder.ivThumbnail);
            }

        } else {
            holder.ivPlayVideo.setVisibility(View.VISIBLE);
            if(fileDownloaded.getBitmap() != null){
                Glide.with(context)
                        .load(fileDownloaded.getBitmap())
                        .apply(new RequestOptions().override(500, 500))
                        .into(holder.ivThumbnail);
            } else {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(file.getPath(),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                fileList.get(position).setBitmap(thumb);
                Glide.with(context)
                        .load(thumb)
                        .apply(new RequestOptions().override(500, 500))
                        .into(holder.ivThumbnail);

            }


        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class DownloadedListViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        ImageView ivPlayVideo;

        public DownloadedListViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivPlayVideo = itemView.findViewById(R.id.ivPlayVideo);
        }
    }
}
