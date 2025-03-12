package services;

import exceptions.GitAPIException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubAPIService.class);
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
        String responseBody = sendGitHubRequest(url);
        return parseChangedFiles(responseBody);
    }

    private String sendGitHubRequest(String url) throws GitException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                LOGGER.error("GitHub API request failed: {}  {}", response.statusCode(), response.body());

                if (url.matches("https://api.github.com/repos/[^/]+/[^/]+(/.*)?")) {
                    throw new GitAPIException("Repository not found or inaccessible");
                }
                throw new GitAPIException("GitHub API resource not found: " + url);
            }

            if (response.statusCode() != 200) {
                LOGGER.error("GitHub API request failed. URL: {}, Status: {}, Response: {}", url, response.statusCode(), response.body());
                throw new GitAPIException("GitHub API request failed with status: " + response.statusCode());
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitAPIException("Error sending request to GitHub API", e);
        }
    }

    private List<String> parseChangedFiles(String responseBody) {
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray filesArray = jsonResponse.optJSONArray("files");

        if (filesArray == null || filesArray.isEmpty()) {
            LOGGER.warn("No changed files found in the GitHub API response.");
            return List.of();
        }
        List<String> changedFiles = new ArrayList<>();
        for (int i = 0; i < filesArray.length(); i++) {
            JSONObject fileObject = filesArray.getJSONObject(i);
            changedFiles.add(fileObject.getString("filename"));
        }
        return changedFiles;
    }

}
