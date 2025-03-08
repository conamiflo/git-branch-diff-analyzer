package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GitHubLocalService {

    private final String localRepoPath;

    public GitHubLocalService(String localRepoPath) {
        this.localRepoPath = localRepoPath;
    }

    public String getMergeBase(String branchA, String branchB) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder()
                .command("git", "merge-base", branchA, branchB)
                .directory(new java.io.File(localRepoPath));
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String mergeBase = reader.readLine();
            if (mergeBase == null || mergeBase.isEmpty()) {
                throw new IOException("Failed to determine merge base.");
            }
            return mergeBase;
        }
    }

    public List<String> getChangedFiles(String branchB, String mergeBase) throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder()
                .command("git", "diff", "--name-only", mergeBase, branchB)
                .directory(new java.io.File(localRepoPath));

        Process process = processBuilder.start();
        List<String> changedFiles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                changedFiles.add(line);
            }
        }
        int exitCode = process.waitFor();
        if (process.waitFor() != 0) {
            throw new IOException("Git command failed with exit code: " + exitCode);
        }
        return changedFiles;
    }



}
