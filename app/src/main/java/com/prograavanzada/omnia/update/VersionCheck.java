package com.prograavanzada.omnia.update;

public interface VersionCheck {
    void onUpdateAvailable(String downloadUrl, String versionName);
    void onUpToDate();
    void onError(Exception e);
}
