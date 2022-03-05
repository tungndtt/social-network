package com.example.tintok.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;

public class FileUtil {
    public static MultipartBody.Part prepareImageFileBody(Context context, String name, MediaEntity media){
        RequestBody body = null;
        File mFile = null;

        if(media.uri != null){
            MediaType extension = MediaType.parse(context.getContentResolver().getType(media.uri));
            mFile = new File(context.getCacheDir(),"temp."+ extension.subtype());
            InputStream in = null;
            try {
                in = context.getContentResolver().openInputStream(media.uri);
                OutputStream out = new FileOutputStream(mFile);
                byte[] buffer = new byte[1024];
                int length;
                while((length = in.read(buffer)) >0){
                    out.write(buffer, 0, length);
                }
                out.close();
                in.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            body =  RequestBody.create(extension,
                    mFile);
        }
        else if(media.bitmap!= null) {
            mFile =  getFile(context, media.bitmap);
            body = RequestBody.create(
                    MediaType.parse("image/*"),
                    mFile);
        }
        if(mFile != null){
            mFile.deleteOnExit();
            return MultipartBody.Part.createFormData(name, mFile.getName() , body);
        }
        else return null;
    }

    public static File getFile(Context context, Bitmap bitmap){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] image = bos.toByteArray();
        File file = null;
        try {
            file = new File(context.getCacheDir(),"unknown.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(image);
            fos.close();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File[] getAllFileWithRegex(String regex){
        String myDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        Log.e("GetFileName: " , " at package: "+DataRepositoryController.applicationContext.getPackageName());
        Log.e("GetFileName: " , " at directory: "+Uri.parse("android.resource://"+DataRepositoryController.applicationContext.getPackageName()+"/").getPath());
        File f = new File(Uri.parse("android.resource://"+DataRepositoryController.applicationContext.getPackageName()+"/").toString());
        Log.e("GetFileName: " , " fodler f: "+f.exists());
        if (true ) {
            final Pattern p = Pattern.compile(regex);
            File[] flists = f.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    p.matcher(file.getName()).matches();
                    Log.e("GetFileName", "at:" + file.getName()+" res:" + p.matcher(file.getName()).matches());

                    return p.matcher(file.getName()).matches();

                }
            });
            return flists;
        }
        return null;
    }
}
