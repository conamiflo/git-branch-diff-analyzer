package services;

import exceptions.GitAPIException;
import exceptions.GitCommandException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchDiffServiceTest {

    @InjectMocks
    private BranchDiffService branchDiffService;

    @Mock
    private GitHubAPIService gitHubAPIService;

    @Mock
    private GitHubLocalService gitHubLocalService;

    private final String OWNER = "conamiflo";
    private final String REPO = "chess-vision";
    private final String BRANCH_A = "feature/fastapi";
    private final String BRANCH_B = "feature/fastapi-rest";
    private final String MERGE_BASE = "abc123";

    @Test
    void getCommonChangedFiles_WithValidBranches_ShouldReturnCommonFiles() throws GitAPIException, GitCommandException {
        List<String> remoteChanges = List.of("file1.java", "file2.java", "file3.java");
        List<String> localChanges = List.of("file2.java", "file3.java", "file4.java");

        when(gitHubLocalService.getMergeBase(BRANCH_A, BRANCH_B)).thenReturn(MERGE_BASE);
        when(gitHubAPIService.getChangedFiles(OWNER, REPO, BRANCH_A, MERGE_BASE)).thenReturn(remoteChanges);
        when(gitHubLocalService.getChangedFiles(BRANCH_B, MERGE_BASE)).thenReturn(localChanges);

        List<String> commonFiles = branchDiffService.getCommonChangedFiles(OWNER, REPO, BRANCH_A, BRANCH_B);

        assertThat(commonFiles).containsExactlyInAnyOrder("file2.java", "file3.java");
    }

    @Test
    void getCommonChangedFiles_WithNoCommonFiles_ShouldReturnEmptyList() throws GitAPIException, GitCommandException {
        List<String> remoteChanges = List.of("fileA.java");
        List<String> localChanges = List.of("fileB.java");

        when(gitHubLocalService.getMergeBase(BRANCH_A, BRANCH_B)).thenReturn(MERGE_BASE);
        when(gitHubAPIService.getChangedFiles(OWNER, REPO, BRANCH_A, MERGE_BASE)).thenReturn(remoteChanges);
        when(gitHubLocalService.getChangedFiles(BRANCH_B, MERGE_BASE)).thenReturn(localChanges);

        List<String> commonFiles = branchDiffService.getCommonChangedFiles(OWNER, REPO, BRANCH_A, BRANCH_B);
        assertTrue(commonFiles.isEmpty());
    }

    @Test
    void getCommonChangedFiles_WithInvalidBranch_ShouldThrowGitCommandException() throws GitCommandException {
        when(gitHubLocalService.getMergeBase(BRANCH_A, BRANCH_B)).thenThrow(new GitCommandException("Branch not found"));
        assertThrows(GitCommandException.class, () ->
                branchDiffService.getCommonChangedFiles(OWNER, REPO, BRANCH_A, BRANCH_B)
        );
    }

    @Test
    void getCommonChangedFiles_WithAPIError_ShouldThrowGitAPIException() throws GitAPIException, GitCommandException {
        when(gitHubLocalService.getMergeBase(BRANCH_A, BRANCH_B)).thenReturn(MERGE_BASE);
        when(gitHubAPIService.getChangedFiles(OWNER, REPO, BRANCH_A, MERGE_BASE))
                .thenThrow(new GitAPIException("API error"));

        assertThrows(GitAPIException.class, () ->
                branchDiffService.getCommonChangedFiles(OWNER, REPO, BRANCH_A, BRANCH_B)
        );
    }

    @Test
    void findCommonFiles_WithValidLists_ShouldReturnCommonFiles() {
        List<String> remoteChanges = List.of("file1.java", "file2.java", "file3.java");
        List<String> localChanges = List.of("file2.java", "file3.java", "file4.java");

        List<String> commonFiles = branchDiffService.findCommonFiles(remoteChanges, localChanges);

        assertThat(commonFiles).containsExactlyInAnyOrder("file2.java", "file3.java");
    }

    @Test
    void findCommonFiles_WithNoCommonFiles_ShouldReturnEmptyList() {
        List<String> remoteChanges = List.of("file1.java");
        List<String> localChanges = List.of("file2.java");

        List<String> commonFiles = branchDiffService.findCommonFiles(remoteChanges, localChanges);

        assertTrue(commonFiles.isEmpty());
    }
}
