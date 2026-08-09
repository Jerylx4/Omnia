package com.prograavanzada.omnia.update;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class UpdateOrchestador {
    private final Context context;
    private final GitHubRepository repository;
    private final ApkDownloader downloader;
    private static final String TAG = "UpdateOrchestador";

    public UpdateOrchestador(Context context, String repoOwner, String repoName, String currentVersion){
        this.context = context;
        this.repository = new GitHubRepository(repoOwner, repoName, currentVersion);
        this.downloader = new ApkDownloader(context);
    }

    public void executedUpdateFlow(){
        repository.checkLatestVersion(new VersionCheck() {
            @Override
            public void onUpdateAvailable(String downloadUrl, String versionName) {
                Log.i(TAG, "Nueva versión detectada: " + versionName);

                downloader.startDownload(downloadUrl, new DownloadCallback() {
                    @Override
                    public void onDownloadComplete(File apkFile) {
                        Log.i(TAG, "Descarga exitosa. Iniciando instalación de " + apkFile.getName());

                        ApkInstaller.install(context, apkFile);
                    }

                    @Override
                    public void onDownloadFailed(String reason) {
                        Log.e(TAG, "Fallo en la descarga: " + reason);
                    }
                });
            }

            @Override
            public void onUpToDate() {
                Log.i(TAG, "El sistema se encuentra en la versión más reciente.");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Fallo en el protocolo de red: ", e);
            }
        });
    }
}
