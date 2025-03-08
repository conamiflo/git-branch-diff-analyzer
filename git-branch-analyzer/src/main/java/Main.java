import analyzer.BranchDiffAnalyzer;
import service.GitHubAPIService;
import service.GitHubLocalService;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {

        String owner = "conamiflo";
        String repo = "chess-vision";
        String branchA = "feature/fastapi";
        String branchB = "feature/fastapi-rest";
        String localRepoPath = "C:\\Users\\Nemanja\\Desktop\\chess-bot";

        String accessToken = System.getenv("GITHUB_ACCESS_TOKEN");

        if (accessToken == null || accessToken.isEmpty()) {
            LOGGER.error("GitHub access token is not set.");
            return;
        }

        GitHubAPIService gitHubAPIService = new GitHubAPIService(accessToken);
        GitHubLocalService gitHubLocalService = new GitHubLocalService(localRepoPath);
        BranchDiffAnalyzer branchAnalyzer = new BranchDiffAnalyzer(gitHubLocalService, gitHubAPIService);

        try {
            String mergeBase = gitHubLocalService.getMergeBase(branchA, branchB);
            List<String> commonChangedFiles = branchAnalyzer.findCommonFiles(owner, repo, branchA, branchB, mergeBase);
            if (commonChangedFiles.isEmpty()) {
                LOGGER.info("No common changed files found between {} and {}", branchA, branchB);
            } else {
                LOGGER.info("Common changed files: ");
                commonChangedFiles.forEach(LOGGER::info);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Unexpected error", e);
        }

    }
}
