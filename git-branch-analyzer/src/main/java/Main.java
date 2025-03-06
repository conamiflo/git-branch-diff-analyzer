import service.BranchDiffAnalyzer;
import service.GitHubAPIService;
import service.GitHubLocalService;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        String owner = "conamiflo";
        String repository = "chess-vision";
        String branchA = "feature/stockfish-integration";
        String branchB = "feature/stockfish-integration";
        String localRepositoryPath = "C:\\Users\\Nemanja\\Desktop\\chess-bot";

        GitHubAPIService gitHubAPIService = new GitHubAPIService("");
        GitHubLocalService gitHubLocalService = new GitHubLocalService(localRepositoryPath);
        BranchDiffAnalyzer branchAnalyzer = new BranchDiffAnalyzer(gitHubLocalService, gitHubAPIService);

        String mergeBase = gitHubLocalService.getMergeBase(branchA, branchB);

        List<String> commonChangedFiles = branchAnalyzer.findCommonFiles(owner, repository, branchA, branchB, mergeBase);

        System.out.println("Common changed files: ");
        commonChangedFiles.forEach(System.out::println);
    }
}
