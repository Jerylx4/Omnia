package com.prograavanzada.omnia.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

public class UpdateOrchestrator {
    // Cambiamos Context por Activity. El AlertDialog necesita el contexto de una ventana
    // gráfica activa para poder renderizarse y anclarse a la pantalla.
    private final Activity activity;
    private final GitHubRepository repository;
    private final ApkDownloader downloader;
    private static final String TAG = "UpdateOrchestrator";

    public UpdateOrchestrator(Activity activity, String repoOwner, String repoName, String currentVersion) {
        this.activity = activity;
        this.repository = new GitHubRepository(repoOwner, repoName, currentVersion);
        this.downloader = new ApkDownloader(activity);
    }

    public void executeUpdateFlow() {
        repository.checkLatestVersion(new VersionCheck() {
            @Override
            public void onUpdateAvailable(String downloadUrl, String versionName) {
                Log.i(TAG, "Nueva versión detectada: " + versionName);

                // Cambio de Contexto de Hilo (Thread Context Switch):
                // Ordenamos al sistema operativo que ejecute este bloque de código
                // específicamente en el Main Thread (UI Thread).
                activity.runOnUiThread(() -> showUpdateDialog(downloadUrl, versionName));
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

    // Método encapsulado para construir y mostrar el recuadro gráfico
    private void showUpdateDialog(String downloadUrl, String versionName) {
        // Implementación del patrón Builder para construir la ventana nativa
        new AlertDialog.Builder(activity)
                .setTitle("Actualización Disponible")
                .setMessage("Hay una nueva versión (" + versionName + ") lista para descargar.\n¿Deseas instalarla ahora?")
                .setCancelable(false) // Bloquea la ventana para que el usuario no pueda cerrarla tocando fuera de ella
                .setPositiveButton("Instalar", (dialog, which) -> {
                    // Feedback visual rápido para el usuario
                    Toast.makeText(activity, "Iniciando descarga en segundo plano...", Toast.LENGTH_SHORT).show();

                    // Si acepta, delegamos la ejecución al método de descarga
                    proceedWithDownload(downloadUrl);
                })
                .setNegativeButton("Más tarde", (dialog, which) -> {
                    // Libera los recursos del diálogo de la memoria RAM
                    dialog.dismiss();
                })
                .show(); // Despacha la orden de renderizado al gestor de ventanas (Window Manager)
    }

    private void monitorDownloadProgress(long downloadId) {
        // 1. Crear la interfaz gráfica (Barra de progreso) dinámicamente
        ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);

        LinearLayout layout = new LinearLayout(activity);
        layout.setPadding(50, 30, 50, 30);
        layout.addView(progressBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // 2. Construir el recuadro que no se puede cancelar
        AlertDialog progressDialog = new AlertDialog.Builder(activity)
                .setTitle("Descargando actualización")
                .setMessage("Por favor, espera...")
                .setView(layout)
                .setCancelable(false)
                .show();

        // 3. Crear un hilo paralelo para consultar el kernel sin congelar la app
        new Thread(() -> {
            boolean isDownloading = true;
            DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

            while (isDownloading) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = manager.query(query);

                if (cursor != null && cursor.moveToFirst()) {
                    // Índices de las columnas en la base de datos interna de Android
                    int bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

                    if (bytesDownloadedIndex >= 0 && bytesTotalIndex >= 0) {
                        long bytesDownloaded = cursor.getLong(bytesDownloadedIndex);
                        long bytesTotal = cursor.getLong(bytesTotalIndex);
                        int status = cursor.getInt(statusIndex);

                        // Calculamos el porcentaje, previniendo la división por cero
                        if (bytesTotal > 0) {
                            final int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);

                            // Saltamos al hilo de la Interfaz para actualizar la barra
                            activity.runOnUiThread(() -> progressBar.setProgress(progress));
                        }

                        // Condición de salida del bucle
                        if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                            isDownloading = false;
                        }
                    }
                    cursor.close();
                }

                // Pausa de 100ms para no saturar el procesador con consultas
                try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
            }

            // Una vez que el bucle termina, cerramos la ventana
            activity.runOnUiThread(progressDialog::dismiss);
        }).start();
    }

    private void proceedWithDownload(String downloadUrl) {
        // Capturamos el ID de la descarga
        long downloadId = downloader.startDownload(downloadUrl, new DownloadCallback() {
            @Override
            public void onDownloadComplete(File apkFile) {
                Log.i(TAG, "Descarga exitosa. Iniciando instalación del paquete.");
                ApkInstaller.install(activity, apkFile);
            }

            @Override
            public void onDownloadFailed(String reason) {
                Log.e(TAG, "Fallo en la descarga: " + reason);
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Error al descargar: " + reason, Toast.LENGTH_LONG).show()
                );
            }
        });

        monitorDownloadProgress(downloadId);
    }
}