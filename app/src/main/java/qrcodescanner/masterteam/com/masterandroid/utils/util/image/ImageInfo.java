package qrcodescanner.masterteam.com.masterandroid.utils.util.image;

import android.net.Uri;

public class ImageInfo {
    private Uri mImageUri;
    private boolean mTakenByCamera;

    public ImageInfo(Uri imageUri, boolean takenByCamera) {
        mImageUri = imageUri; 
        mTakenByCamera = takenByCamera;
    }

    public Uri getImageUri() {
        return mImageUri;
    }

}
