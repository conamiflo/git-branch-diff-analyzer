package service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitHubAPIService {

    private final HttpClient client;
    private final String accessToken;
    private static final Logger LOGGER = Logger.getLogger(GitHubAPIService.class.getName());

    public GitHubAPIService(String accessToken) {
        this.accessToken = accessToken;
        this.client = HttpClient.newHttpClient();
    }

    public List<String> getChangedFiles(String owner, String repository, String branchA, String mergeBase) throws IOException, InterruptedException {

        String url = "https://api.github.com/repos/" + owner + "/" + repository + "/compare/" + mergeBase + "..." + branchA;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.log(Level.SEVERE, "GitHub API request failed: {0}", response.body());
                throw new IOException("GitHub API request failed with status code: " + response.statusCode());
            }

            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray filesArray = jsonResponse.getJSONArray("files");
            List<String> changedFiles = new ArrayList<>();

            for (int i = 0; i < filesArray.length(); i++) {
                JSONObject fileObject = filesArray.getJSONObject(i);
                changedFiles.add(fileObject.getString("filename"));
            }
            return changedFiles;

        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Error fetching changed files from GitHub API", e);
            return List.of();
        }

    }








}
