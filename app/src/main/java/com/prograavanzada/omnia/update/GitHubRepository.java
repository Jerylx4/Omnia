package com.prograavanzada.omnia.update;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GitHubRepository {
    private final String url;
    private final String currentVersion;
    private final ExecutorService executor;

    public GitHubRepository(String repoOwner, String repoName, String currentVersion){
        this.url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/releases/latest";
        this.currentVersion = currentVersion;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void checkLatestVersion(VersionCheck callback){
        executor.execute(()-> {
            try{
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String fetchedTag = jsonResponse.getString("tag_name").replace("v", "");

                    if(isNewerVersion(fetchedTag, currentVersion)){
                        String downloadUrl = jsonResponse.getJSONArray("assests")
                                .getJSONObject(0)
                                .getString("browser_download_url");

                        callback.onUpdateAvailable(downloadUrl, fetchedTag);
                    } else {
                        callback.onUpToDate();
                    }

                } else {
                    callback.onError(new RuntimeException("Http error: " + connection.getResponseCode()));

                }

            }catch (Exception e){
                callback.onError(e);
            }
        });
    }

    private boolean isNewerVersion(String fetchedVersion, String currentVersion){
        return !fetchedVersion.equals(currentVersion);
    }
}
