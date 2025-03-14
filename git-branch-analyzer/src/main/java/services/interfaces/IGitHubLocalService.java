package services.interfaces;

import exceptions.GitException;
import java.util.List;

public interface IGitHubLocalService {
    boolean branchExists(String branch);
    String getMergeBase(String branchA, String branchB);
    List<String> getChangedFiles(String branchB, String mergeBase);
}
