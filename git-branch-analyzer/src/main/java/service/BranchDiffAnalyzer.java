package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchDiffAnalyzer {

    private final GitHubLocalService gitLocalService;
    private final GitHubAPIService gitAPIService;

    public BranchDiffAnalyzer(GitHubLocalService gitLocalService, GitHubAPIService gitAPIService) {
        this.gitLocalService = gitLocalService;
        this.gitAPIService = gitAPIService;
    }

    public List<String> findCommonFiles(String owner, String repository, String branchA, String branchB, String mergeBase) throws IOException, InterruptedException {
        try {

            Set<String> remoteChanges = new HashSet<>(gitAPIService.getChangedFiles(owner, repository, branchA, mergeBase));
            Set<String> localChanges = new HashSet<>(gitLocalService.getChangedFiles(branchB, mergeBase));

            remoteChanges.retainAll(localChanges);
            return new ArrayList<>(remoteChanges);

        } catch (IOException | InterruptedException e) {
            System.err.println("Error comparing changed files: " + e.getMessage());
            return List.of();
        }
    }
}

