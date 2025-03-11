package services;

import exceptions.GitException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubAPIService.class.getName());
    private final HttpClient client;
    private final String accessToken;

    public GitHubAPIService(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token must not be null or empty.");
        }
        this.accessToken = accessToken;
        this.client = HttpClient.newHttpClient();
    }

    public List<String> getChangedFiles(String owner, String repository, String branchA, String mergeBase) throws GitException {
        String url = String.format("https://api.github.com/repos/%s/%s/compare/%s...%s", owner, repository, mergeBase, branchA);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.error("GitHub API request failed: {} - {}", response.statusCode(), response.body());
                throw new GitException("GitHub API request failed with status: " + response.statusCode());
            }
            return parseChangedFiles(response.body());

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error fetching changed files from GitHub API", e);
            Thread.currentThread().interrupt();
            throw new GitException("Error fetching changed files from GitHub API", e);
        }
    }

    private List<String> parseChangedFiles(String responseBody) {
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray filesArray = jsonResponse.optJSONArray("files");

        List<String> changedFiles = new ArrayList<>();
        if (filesArray != null) {
            for (int i = 0; i < filesArray.length(); i++) {
                JSONObject fileObject = filesArray.getJSONObject(i);
                changedFiles.add(fileObject.getString("filename"));
            }
        }
        return changedFiles;
    }
}
