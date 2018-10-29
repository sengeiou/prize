package com.mediatek.galleryfeature.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.mediatek.galleryfeature.platform.PlatformHelper;
import com.mediatek.galleryframework.base.ExtItem;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.ThumbType;
import com.mediatek.galleryframework.util.BitmapUtils;
import com.mediatek.galleryframework.util.MtkLog;
import com.mediatek.galleryframework.util.Utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class GifItem extends ExtItem {
    private static final String TAG = "MtkGallery2/GifItem";

    public GifItem(MediaData data, Context context) {
        super(context, data);
    }

    public MediaData.MediaType getType() {
        return MediaData.MediaType.GIF;
    }

    public Thumbnail getThumbnail(ThumbType thumbType) {
        if (PlatformHelper.isOutOfDecodeSpec(mMediaData.fileSize, mMediaData.width,
                mMediaData.height, mMediaData.mimeType)) {
            MtkLog.i(TAG, "<getThumbnail> " + mMediaData.filePath
                    + ", out of spec limit, abort generate thumbnail!");
            return new Thumbnail(null, false);
        }
        Bitmap bitmap = null;
        if (mMediaData.filePath != null && !mMediaData.filePath.equals("")) {
            bitmap = decodeGifThumbnail(mMediaData.filePath);
        } else if (mMediaData.uri != null) {
            bitmap = decodeGifThumbnail(mMediaData.uri);
        }
        bitmap = BitmapUtils.replaceBackgroundColor(bitmap, true);
        // if decodeGifThumbnail return null, then return null directly,
        // then decode thumbnail with google default decode routine
        return new Thumbnail(bitmap, true);
    }

    public Bitmap getOriginRatioBitmap(BitmapFactory.Options options) {
        Bitmap bitmap = super.getOriginRatioBitmap(options);
        if (bitmap != null) {
            bitmap = BitmapUtils.replaceBackgroundColor(bitmap, true);
        }
        return bitmap;
    }

    public ArrayList<SupportOperation> getNotSupportedOperations() {
        ArrayList<SupportOperation> res = new ArrayList<SupportOperation>();
        res.add(SupportOperation.FULL_IMAGE);
        res.add(SupportOperation.EDIT);
        return res;
    }

    private static Bitmap decodeGifThumbnail(String filePath) {
        GifDecoderWrapper gifDecoderWrapper = null;
        try {
            gifDecoderWrapper = GifDecoderWrapper
                    .createGifDecoderWrapper(filePath);
            if (gifDecoderWrapper == null) {
                return null;
            }
            Bitmap bitmap = gifDecoderWrapper.getFrameBitmap(0);
            return bitmap;
        } finally {
            if (null != gifDecoderWrapper) {
                gifDecoderWrapper.close();
            }
        }
    }

    private Bitmap decodeGifThumbnail(Uri uri) {
        GifDecoderWrapper gifDecoderWrapper = null;
        ParcelFileDescriptor pfd = null;
        try {
            pfd = mContext.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if (pfd == null) {
                MtkLog.w(TAG, "<decodeGifThumbnail>, pdf is null");
                return null;
            }
            FileDescriptor fd = pfd.getFileDescriptor();
            if (fd == null) {
                MtkLog.w(TAG, "<decodeGifThumbnail>, fd is null");
                return null;
            }
            gifDecoderWrapper = GifDecoderWrapper.createGifDecoderWrapper(fd);
            if (gifDecoderWrapper == null) {
                return null;
            }
            Bitmap bitmap = gifDecoderWrapper.getFrameBitmap(0);
            return bitmap;
        } catch (FileNotFoundException e) {
            MtkLog.w(TAG, "<decodeGifThumbnail>, FileNotFoundException", e);
            return null;
        } finally {
            Utils.closeSilently(pfd);
            if (null != gifDecoderWrapper) {
                gifDecoderWrapper.close();
            }
        }
    }

    @Override
    public boolean supportHighQuality() {
        return false;
    }
}