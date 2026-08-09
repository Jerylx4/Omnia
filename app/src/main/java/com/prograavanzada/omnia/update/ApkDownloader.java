package com.prograavanzada.omnia.update;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

public class ApkDownloader {
    private final Context context;
    private final DownloadManager downloadManager;
    private long activeDownloadId;
    private DownloadCallback activeCallback;
    private final String fileName = "update_temp.apk";

    public ApkDownloader(Context context){
        this.context = context.getApplicationContext();
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void startDownload(String url, DownloadCallback callback){
        this.activeCallback = callback;
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

        if(file.exists()) file.delete();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle("Actualización requerida")
                .setDescription("Descargando...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);

        context.registerReceiver(downloadReciver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);

        activeDownloadId = downloadManager.enqueue(request);
    }

    private final BroadcastReceiver downloadReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (activeDownloadId == id) {
                ctx.unregisterReceiver(this);

                File apkFile = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
                if (apkFile.exists() && activeCallback != null) {
                    activeCallback.onDownloadComplete(apkFile);
                } else if (activeCallback != null) {
                    activeCallback.onDownloadFailed("Archivo no encontrado en sistema de ficheros.");
                }
            }
        }
    };

}
