import exceptions.GitAPIException;
import exceptions.GitCommandException;
import services.BranchDiffService;

import java.net.http.HttpClient;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.GitHubAPIService;
import services.GitHubLocalService;
import services.interfaces.IGitHubAPIService;
import services.interfaces.IGitHubLocalService;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {

        if (args.length < 5) {
            LOGGER.error("Usage: java -jar git-branch-analyzer-1.0-SNAPSHOT.jar <owner> <repo> <branchA> <branchB> <localRepoPath>");
            return;
        }

        String owner = args[0];
        String repo = args[1];
        String branchA = args[2];
        String branchB = args[3];
        String localRepoPath = args[4];
        String accessToken = System.getenv("GITHUB_ACCESS_TOKEN");

        try {
            IGitHubAPIService gitHubAPIService = new GitHubAPIService(accessToken, HttpClient.newHttpClient());
            IGitHubLocalService gitHubLocalService = new GitHubLocalService(localRepoPath);
            BranchDiffService branchDiffService = new BranchDiffService(gitHubAPIService, gitHubLocalService);

            List<String> commonChangedFiles = branchDiffService.getCommonChangedFiles(owner, repo, branchA, branchB);

            if (commonChangedFiles.isEmpty()) {
                LOGGER.info("No common changed files found between {} and {}", branchA, branchB);
            } else {
                LOGGER.info("Common changed files: ");
                commonChangedFiles.forEach(LOGGER::info);
            }

        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid input parameters: ", e);
        } catch (GitAPIException e) {
            LOGGER.error("GitHub API error: ", e);
        } catch (GitCommandException e) {
            LOGGER.error("Git command error: ", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error: ", e);
        }
    }

}
