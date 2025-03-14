package services;

import exceptions.GitCommandException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import services.interfaces.IGitHubLocalService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GitHubLocalServiceTest {

    private IGitHubLocalService gitHubLocalService;

    @TempDir
    Path tempRepoPath;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        new ProcessBuilder("git", "init")
                .directory(tempRepoPath.toFile())
                .start().waitFor();

        Files.createFile(tempRepoPath.resolve("file1.java"));
        executeGitCommand("git", "add", "file1.java");
        executeGitCommand("git", "commit", "-m", "Initial commit");

        gitHubLocalService = new GitHubLocalService(tempRepoPath.toString());
    }

    @Test
    void branchExists_WithExistingBranch_ShouldReturnTrue() throws IOException, InterruptedException {
        executeGitCommand("git", "checkout", "-b", "feature/branch");
        executeGitCommand("git", "commit", "--allow-empty", "-m", "Add empty commit to feature/branch");

        boolean result = gitHubLocalService.branchExists("feature/branch");
        assertThat(result).isTrue();
    }

    @Test
    void branchExists_WithNonExistingBranch_ShouldReturnFalse() {
        boolean result = gitHubLocalService.branchExists("non-existent-branch");
        assertThat(result).isFalse();
    }

    @Test
    void getMergeBase_WithValidBranches_ShouldReturnMergeBaseCommit() throws IOException, InterruptedException {
        executeGitCommand("git", "checkout", "-b", "feature/branch");
        executeGitCommand("git", "commit", "--allow-empty", "-m", "Add empty commit to feature/branch");
        executeGitCommand("git", "checkout", "main");

        String mergeBase = gitHubLocalService.getMergeBase("main", "feature/branch");

        assertThat(mergeBase).isNotNull();
    }

    @Test
    void getMergeBase_WithInvalidBranches_ShouldThrowGitCommandException() {
        assertThatThrownBy(() -> gitHubLocalService.getMergeBase("non-existent-branch", "main"))
                .isInstanceOf(GitCommandException.class)
                .hasMessageContaining("One of the branches does not exist");
    }

    @Test
    void getChangedFiles_WithValidMergeBase_ShouldReturnModifiedFilesList() throws IOException, InterruptedException {
        executeGitCommand("git", "checkout", "-b", "feature/branch");
        Files.write(tempRepoPath.resolve("file1.java"), "modification".getBytes());
        executeGitCommand("git", "add", "file1.java");
        executeGitCommand("git", "commit", "-m", "Modify file1.java");
        executeGitCommand("git", "checkout", "main");

        String mergeBaseCommit = gitHubLocalService.getMergeBase("main", "feature/branch");

        List<String> changedFiles = gitHubLocalService.getChangedFiles("feature/branch", mergeBaseCommit);
        assertThat(changedFiles).isNotEmpty().containsExactlyInAnyOrder("file1.java");
    }

    @Test
    void getChangedFiles_WithInvalidMergeBase_ShouldThrowGitCommandException() {
        assertThatThrownBy(() -> gitHubLocalService.getChangedFiles("non-existent-branch", "HEAD~1"))
                .isInstanceOf(GitCommandException.class)
                .hasMessageContaining("Git command failed");
    }

    private void executeGitCommand(String... command) throws IOException, InterruptedException {
        new ProcessBuilder(command)
                .directory(tempRepoPath.toFile())
                .start().waitFor();
    }
}
