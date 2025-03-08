package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubLocalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubLocalService.class.getName());
    private final String localRepoPath;

    public GitHubLocalService(String localRepoPath) {
        this.localRepoPath = localRepoPath;
    }

    public String getMergeBase(String branchA, String branchB) throws IOException, InterruptedException {
        List<String> output = executeGitCommand("git", "merge-base", branchA, branchB);
        if (output.isEmpty()) {
            throw new IOException("Failed to determine merge base.");
        }
        return output.getFirst();
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
                .directory(new java.io.File(localRepoPath));

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
            throw new IOException("Git command failed: " + String.join(" ", commands));
        }
        return output;
    }
}
