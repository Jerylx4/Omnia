package com.prograavanzada.omnia.update;

import java.io.File;

public interface DownloadCallback {
    void onDownloadComplete(File apkFile);
    void onDownloadFailed(String reason);
}
