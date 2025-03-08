package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitHubLocalService {

    private static final Logger LOGGER = Logger.getLogger(GitHubLocalService.class.getName());
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
            return executeGitCommand("git", "diff", "--name-only", mergeBase, branchB);
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Error fetching changed files from local repository", e);
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
