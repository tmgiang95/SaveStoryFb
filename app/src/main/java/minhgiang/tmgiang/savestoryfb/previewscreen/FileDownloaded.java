package minhgiang.tmgiang.savestoryfb.previewscreen;

import android.graphics.Bitmap;

import java.io.File;

public class FileDownloaded {
    private File file;
    private Bitmap bitmap;

    public FileDownloaded(File file, Bitmap bitmap) {
        this.file = file;
        this.bitmap = bitmap;
    }

    public FileDownloaded() {
    }

    public FileDownloaded(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
