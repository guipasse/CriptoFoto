package tfm.uoc.edu.criptofoto.model;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String title;
    private String path;

    public ImageItem(Bitmap image, String title, String path) {
        super();
        this.image = image;
        this.title = title;
        this.path = path;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getPath() { return path; }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
