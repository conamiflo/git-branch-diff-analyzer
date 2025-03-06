import service.GitHubAPIService;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String owner = "conamiflo";
        String repo = "chess-vision";
        String mergeBase = "refactoring";
        String branchA = "feature/stockfish-integration";

        GitHubAPIService gitHubAPIService = new GitHubAPIService("");

        try {
            List<String> changedFiles = gitHubAPIService.getChangedFiles(owner, repo, branchA, mergeBase);
            System.out.println("Changed files remote:");
            changedFiles.forEach(System.out::println);

        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }

    }
}
