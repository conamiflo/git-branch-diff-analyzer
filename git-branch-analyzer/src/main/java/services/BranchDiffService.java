package services;

import exceptions.GitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchDiffService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BranchDiffService.class);
    private final GitHubAPIService gitHubAPIService;
    private final GitHubLocalService gitHubLocalService;

    public BranchDiffService(String accessToken, String localRepoPath) {
        try {
            this.gitHubAPIService = new GitHubAPIService(accessToken);
            this.gitHubLocalService = new GitHubLocalService(localRepoPath);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error initializing BranchDiffService");
            throw e;
        }
    }

    public List<String> getCommonChangedFiles(String owner, String repository, String branchA, String branchB) {
        try {
            if (owner == null || owner.isBlank() ||
                repository == null || repository.isBlank() ||
                branchA == null || branchA.isBlank() ||
                branchB == null || branchB.isBlank()) {
                throw new IllegalArgumentException("Owner, repository, and branch names must not be null or empty.");
            }

            String mergeBase = gitHubLocalService.getMergeBase(branchA, branchB);

            List<String> remoteChanges = gitHubAPIService.getChangedFiles(owner, repository, branchA, mergeBase);
            List<String> localChanges = gitHubLocalService.getChangedFiles(branchB, mergeBase);

            return findCommonFiles(remoteChanges, localChanges);

        } catch (IllegalArgumentException | GitException e) {
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error: {}", e.getMessage(), e);
        }
        return List.of();
    }

    public List<String> findCommonFiles(List<String> remoteChanges, List<String> localChanges) {
        Set<String> remoteSet = new HashSet<>(remoteChanges);
        Set<String> localSet = new HashSet<>(localChanges);
        remoteSet.retainAll(localSet);
        return List.copyOf(remoteSet);
    }

}
