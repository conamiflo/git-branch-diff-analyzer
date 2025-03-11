package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import exceptions.GitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitHubLocalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubLocalService.class.getName());
    private final String localRepoPath;

    public GitHubLocalService(String localRepoPath) {
        if (localRepoPath == null || localRepoPath.isBlank()) {
            throw new IllegalArgumentException("Local repository path must not be null or empty.");
        }
        Path gitDir = Path.of(localRepoPath, ".git");
        if (!Files.isDirectory(gitDir)) {
            throw new IllegalArgumentException("Invalid repository path: " + localRepoPath);
        }
        this.localRepoPath = localRepoPath;
    }

    public String getMergeBase(String branchA, String branchB) throws IOException, InterruptedException {
        try {
            List<String> output = executeGitCommand("git", "merge-base", branchA, branchB);
            if (output.isEmpty()) {
                throw new GitException("Failed to determine merge base.");
            }
            return output.getFirst();
        } catch (GitException e) {
            LOGGER.error("Error executing Git command to get merge base: {}", e.getMessage());
            throw e;
        }
    }

    public List<String> getChangedFiles(String branchB, String mergeBase) {
        try {
            return executeGitCommand("git", "diff", "--diff-filter=AM", "--name-only", mergeBase, branchB);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error fetching changed files from local repository", e);
            return new ArrayList<>();
        }
    }

    private List<String> executeGitCommand(String... commands) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder()
                .command(commands)
                .directory(new java.io.File(localRepoPath))
                .redirectErrorStream(true);

        Process process = processBuilder.start();
        List<String> output = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new GitException("Git command failed: " + String.join(" ", commands));
        }
        return output;
    }
}
