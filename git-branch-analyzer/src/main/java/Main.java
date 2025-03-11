import exceptions.GitException;
import services.BranchDiffService;

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

        try {
            BranchDiffService branchDiffService = new BranchDiffService(accessToken, localRepoPath);
            List<String> commonChangedFiles = branchDiffService.getCommonChangedFiles(owner, repo, branchA, branchB);
            if (commonChangedFiles.isEmpty()) {
                LOGGER.info("No common changed files found between {} and {}", branchA, branchB);
            } else {
                LOGGER.info("Common changed files: ");
                commonChangedFiles.forEach(LOGGER::info);
            }

        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid input parameters: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred: {}", e.getMessage());
        }

    }

}
