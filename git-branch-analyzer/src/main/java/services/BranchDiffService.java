package services;

import services.interfaces.IGitHubAPIService;
import services.interfaces.IGitHubLocalService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchDiffService {
    private final IGitHubAPIService gitHubAPIService;
    private final IGitHubLocalService gitHubLocalService;

    public BranchDiffService(GitHubAPIService gitHubAPIService, GitHubLocalService gitHubLocalService) {
        this.gitHubAPIService = gitHubAPIService;
        this.gitHubLocalService = gitHubLocalService;
    }

    public List<String> getCommonChangedFiles(String owner, String repository, String branchA, String branchB) {

        String mergeBase = gitHubLocalService.getMergeBase(branchA, branchB);
        List<String> remoteChanges = gitHubAPIService.getChangedFiles(owner, repository, branchA, mergeBase);
        List<String> localChanges = gitHubLocalService.getChangedFiles(branchB, mergeBase);
        return findCommonFiles(remoteChanges, localChanges);
    }

    public List<String> findCommonFiles(List<String> remoteChanges, List<String> localChanges) {
        Set<String> remoteSet = new HashSet<>(remoteChanges);
        Set<String> localSet = new HashSet<>(localChanges);
        remoteSet.retainAll(localSet);
        return List.copyOf(remoteSet);
    }

}
