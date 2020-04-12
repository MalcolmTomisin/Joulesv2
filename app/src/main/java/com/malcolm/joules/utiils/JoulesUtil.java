package com.malcolm.joules.utiils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import com.malcolm.joules.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JoulesUtil {
    public static Uri getAlbumArtUri(long albumId){
        if (ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId) != null)
            return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);

        Context context = null;
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        tempDir.mkdir();
        String albumArtTitle = "albumart";
        File tempFile = null;
        try {
            tempFile = File.createTempFile(albumArtTitle,".png",tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap defaultBitMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.exo_icon_vr);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        defaultBitMap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        byte[] bitMapData = bytes.toByteArray();

        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitMapData);
            fos.flush();
            fos.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return Uri.fromFile(tempFile);
    }
}
