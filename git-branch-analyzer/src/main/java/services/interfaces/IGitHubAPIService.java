package services.interfaces;

import exceptions.GitException;
import java.util.List;

public interface IGitHubAPIService {
    List<String> getChangedFiles(String owner, String repository, String branchA, String mergeBase) throws GitException;
}
