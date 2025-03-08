import service.BranchDiffAnalyzer;
import service.GitHubAPIService;
import service.GitHubLocalService;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        String owner = "conamiflo";
        String repo = "chess-vision";
        String branchA = "feature/fastapi";
        String branchB = "feature/fastapi-rest";
        String localRepoPath = "C:\\Users\\Nemanja\\Desktop\\chess-bot";

        String accessToken = System.getenv("GITHUB_ACCESS_TOKEN");
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("GitHub access token is not set.");
        }

        GitHubAPIService gitHubAPIService = new GitHubAPIService(accessToken);
        GitHubLocalService gitHubLocalService = new GitHubLocalService(localRepoPath);
        BranchDiffAnalyzer branchAnalyzer = new BranchDiffAnalyzer(gitHubLocalService, gitHubAPIService);

        String mergeBase = gitHubLocalService.getMergeBase(branchA, branchB);

        List<String> commonChangedFiles = branchAnalyzer.findCommonFiles(owner, repo, branchA, branchB, mergeBase);

        System.out.println("Common changed files: ");
        commonChangedFiles.forEach(System.out::println);

    }
}
