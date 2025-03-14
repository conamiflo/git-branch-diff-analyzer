package services;

import exceptions.GitAPIException;
import exceptions.GitException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import services.interfaces.IGitHubAPIService;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubAPIServiceTest {

    @Mock
    private HttpClient mockClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private IGitHubAPIService gitHubAPIService;

    private static final String ACCESS_TOKEN = "test_token";
    private static final String OWNER = "test_owner";
    private static final String REPO = "test_repo";
    private static final String BRANCH_A = "branchA";
    private static final String MERGE_BASE = "mergeBase";

    @BeforeEach
    void setUp() {
        gitHubAPIService = new GitHubAPIService(ACCESS_TOKEN, mockClient);
    }

    @Test
    void getChangedFiles_WithValidResponse_ShouldReturnFileList() throws Exception {
        String jsonResponse = "{'files':[{ 'filename':'file1.txt'},{'filename':'file2.txt'}]}";
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);

        List<String> changedFiles = gitHubAPIService.getChangedFiles(OWNER, REPO, BRANCH_A, MERGE_BASE);

        assertThat(changedFiles).containsExactly("file1.txt", "file2.txt");
    }

    @Test
    void getChangedFiles_WithNoChanges_ShouldReturnEmptyList() throws Exception {
        String jsonResponse = new JSONObject().put("files", List.of()).toString();
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);

        List<String> changedFiles = gitHubAPIService.getChangedFiles(OWNER, REPO, BRANCH_A, MERGE_BASE);

        assertThat(changedFiles).isEmpty();
    }

    @Test
    void getChangedFiles_WithNonExistentRepo_ShouldThrowGitAPIException() throws Exception {
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockResponse.body()).thenReturn("Repository not found");

        assertThatThrownBy(() -> gitHubAPIService.getChangedFiles(OWNER, REPO, BRANCH_A, MERGE_BASE))
                .isInstanceOf(GitAPIException.class)
                .hasMessageContaining("GitHub API resource not found");
    }

    @Test
    void getChangedFiles_WithServerError_ShouldThrowGitAPIException() throws Exception {
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");

        assertThatThrownBy(() -> gitHubAPIService.getChangedFiles(OWNER, REPO, BRANCH_A, MERGE_BASE))
                .isInstanceOf(GitAPIException.class)
                .hasMessageContaining("GitHub API request failed with status: 500");
    }

    @Test
    void getChangedFiles_WithIOException_ShouldThrowGitAPIException() throws Exception {
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Network error"));

        assertThatThrownBy(() -> gitHubAPIService.getChangedFiles(OWNER, REPO, BRANCH_A, MERGE_BASE))
                .isInstanceOf(GitAPIException.class)
                .hasMessageContaining("Error sending request to GitHub API");
    }
}
