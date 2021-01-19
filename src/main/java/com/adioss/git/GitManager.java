package com.adioss.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.adioss.git.FileUtils.removeOnShutdown;

public class GitManager {
    private final Git git;
    private final CredentialsProvider credentialsProvider;

    public GitManager(String uri, String token) throws Exception {
        Path tempDirectory = Files.createTempDirectory(null);
        removeOnShutdown(tempDirectory);
        git = Git.cloneRepository().setURI(uri).setDirectory(tempDirectory.toFile()).call();
        credentialsProvider = new UsernamePasswordCredentialsProvider(token, "");
    }


    private void addContent(File content) {
        Path repoPath = Paths.get(git.getRepository().getDirectory().getParent()).toAbsolutePath();
        Path outputGitFilePath = Paths.get(repoPath.toString(), content.getName());
        System.out.println(outputGitFilePath);
        try {
            Files.deleteIfExists(outputGitFilePath);
            Files.copy(content.toPath(), outputGitFilePath);
            git.add().addFilepattern(".").call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<PushResult> push(String branchName) throws Exception {
        PushCommand push = git.push();
        push.setCredentialsProvider(credentialsProvider);
        Iterable<PushResult> origin = push.setRemote("origin").setRefSpecs(new RefSpec(branchName)).call();
        return StreamSupport.stream(origin.spliterator(), false).collect(Collectors.toList());
    }

    private void checkout(String branchName) throws GitAPIException {
        git.checkout().setCreateBranch(true).setName(branchName).call();
    }

    private void commit(String message) throws GitAPIException {
        git.commit().setMessage(message).call();

    }

    private void pushFilesToNewBranch(List<File> files, String branchName) throws Exception {
        this.checkout(branchName);
        files.forEach(this::addContent);
        this.commit("Put files to new branch");
        this.push(branchName);
    }

    public static void main(String[] args) throws Exception {
        String repo = args[0];
        String token = args[1];
        String path = args[2];

        String branchName = "test";

        List<File> files = Arrays.asList(Paths.get(path).toFile().listFiles());

        GitManager gitManager = new GitManager(repo, token);
        gitManager.pushFilesToNewBranch(files, branchName);
    }
}
