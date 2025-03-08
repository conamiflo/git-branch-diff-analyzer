import analyzer.BranchDiffAnalyzer;
import service.GitHubAPIService;
import service.GitHubLocalService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {

        final Logger LOGGER = Logger.getLogger(Main.class.getName());

        String owner = "conamiflo";
        String repo = "chess-vision";
        String branchA = "feature/fastapi";
        String branchB = "feature/fastapi-rest";
        String localRepoPath = "C:\\Users\\Nemanja\\Desktop\\chess-bot";

        String accessToken = System.getenv("GITHUB_ACCESS_TOKEN");

        if (accessToken == null || accessToken.isEmpty()) {
            LOGGER.log(Level.SEVERE, "GitHub access token is not set.");
            return;
        }

        GitHubAPIService gitHubAPIService = new GitHubAPIService(accessToken);
        GitHubLocalService gitHubLocalService = new GitHubLocalService(localRepoPath);
        BranchDiffAnalyzer branchAnalyzer = new BranchDiffAnalyzer(gitHubLocalService, gitHubAPIService);

        try {
            String mergeBase = gitHubLocalService.getMergeBase(branchA, branchB);
            List<String> commonChangedFiles = branchAnalyzer.findCommonFiles(owner, repo, branchA, branchB, mergeBase);
            System.out.println("Common changed files: ");
            commonChangedFiles.forEach(System.out::println);
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }

    }
}
