package analyzer;

import service.GitHubAPIService;
import service.GitHubLocalService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BranchDiffAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchDiffAnalyzer.class.getName());
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
            LOGGER.error("Error comparing changed files", e);
            throw e;
        }
    }
}

