package services;

import exceptions.GitCommandException;
import exceptions.GitException;
import services.interfaces.IGitHubLocalService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GitHubLocalService implements IGitHubLocalService {

    private final String localRepoPath;

    public GitHubLocalService(String localRepoPath) {
        if (localRepoPath == null || localRepoPath.isBlank()) {
            throw new IllegalArgumentException("Local repository path must not be null or empty.");
        }
        if (!Files.exists(Path.of(localRepoPath, ".git"))) {
            throw new IllegalArgumentException("Invalid repository path: " + localRepoPath);
        }
        this.localRepoPath = localRepoPath;
    }

    @Override
    public boolean branchExists(String branch) {
        try {
            executeGitCommand("git", "rev-parse", "--verify", branch);
            return true;
        } catch (GitException e) {
            return false;
        }
    }

    @Override
    public String getMergeBase(String branchA, String branchB) {
        validateBranchesExist(branchA, branchB);
        List<String> output = executeGitCommand("git", "merge-base", branchA, branchB);
        if (output.isEmpty()) {
            throw new GitCommandException("Failed to determine merge base between branches: " + branchA + " and " + branchB);
        }
        return output.getFirst();
    }

    @Override
    public List<String> getChangedFiles(String branchB, String mergeBase) {
        return executeGitCommand("git", "diff", "--diff-filter=AM", "--name-only", mergeBase, branchB);
    }

    public void validateBranchesExist(String branchA, String branchB) {
        if (!branchExists(branchA) || !branchExists(branchB)) {
            throw new GitCommandException("One of the branches does not exist: " + branchA + ", " + branchB);
        }
    }

    public List<String> executeGitCommand(String... commands) {
        List<String> output = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder(commands)
                .directory(Path.of(localRepoPath).toFile())
                .redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(output::add);
            }
            if (process.waitFor() != 0) {
                throw new GitCommandException("Git command failed: " + String.join(" ", commands) + "\nError output: " + output);
            }
            return output;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitCommandException("Git command execution failed", e);
        }
    }
}
