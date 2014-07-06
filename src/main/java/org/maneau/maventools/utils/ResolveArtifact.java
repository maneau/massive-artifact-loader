package org.maneau.maventools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.*;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by maneau on 05/07/2014.
 * Class providing feature for downloading artifacts
 */
public class ResolveArtifact {
    private static Logger LOGGER = LoggerFactory.getLogger(ResolveArtifact.class);

    private RepositorySystemSession repositorySystemSession;
    private RepositorySystem repositorySystem;
    private ArtifactRequest artifactRequest;
    private CollectRequest collectRequest;

    public Set<String> getFoundedArtifacts() {
        return foundedArtifacts;
    }

    private Set<String> foundedArtifacts = new HashSet<String>();

    public ResolveArtifact() {
        setRepositorySystem(Booter.newRepositorySystem());

        setRepositorySystemSession(Booter.newRepositorySystemSession(getRepositorySystem()));
        setArtifactRequest(new ArtifactRequest());
        setCollectRequest(new CollectRequest());

        for (RemoteRepository repository : Repositories.STANDARD) {
            getArtifactRequest().addRepository(repository);
            getCollectRequest().addRepository(repository);
        }
    }

    public ArtifactResult resolveArtifactWithKey(String key, boolean withDependencies) {
        return resolveArtifactWithArtifact(new DefaultArtifact(key), withDependencies);
    }

    public ArtifactResult resolvePomArtifact(Artifact artifact, boolean withDependencies) {
        if (artifact.getExtension().equals("pom")) {
            return null;
        }
        return resolveArtifactWithArtifact(getPomArtifact(artifact), withDependencies);
    }

    public Artifact getPomArtifact(Artifact artifact) {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "pom", artifact.getVersion());
    }

    private String getKey(Artifact a) {
        StringBuilder sb = new StringBuilder();
        sb.append(a.getGroupId());
        sb.append(":").append(a.getArtifactId());
        sb.append(":").append(a.getExtension());
        sb.append(":").append(a.getVersion());
        if (a.getClassifier().length() > 0) {
            sb.append(":").append(a.getClassifier());
        }
        return sb.toString();
    }

    public ArtifactResult resolveArtifactWithArtifact(Artifact artifact, boolean withDependencies) {
        String key = getKey(artifact);
        if (foundedArtifacts.contains(key)) {
            LOGGER.debug("Artifact already downloaded (" + key + ") : ");
            return null;
        } else {
            LOGGER.debug("Downloading artifact : (" + key + ")");
        }

        getArtifactRequest().setArtifact(artifact);

        ArtifactResult artifactResult = null;
        try {
            artifactResult = getRepositorySystem().resolveArtifact(getRepositorySystemSession(), getArtifactRequest());
            foundedArtifacts.add(getKey(artifact));
        } catch (ArtifactResolutionException e) {
            LOGGER.error("ArtifactResolutionException:", e);
        }

        //Don't forget to get the pom
        if (!artifact.getExtension().equals("pom")) {
            artifactResult = resolvePomArtifact(artifact, withDependencies);
        } else {
            if (withDependencies) {
                //Getting dependencies
                for (Artifact subArtifact : resolveDependencies(artifact)) {
                    resolveArtifactWithArtifact(subArtifact, withDependencies);
                }
            }
        }
        return artifactResult;
    }

    public List<Artifact> resolveDependencies(Artifact artifact) {
        List<Artifact> artifacts = new ArrayList<Artifact>();
        try {
            LOGGER.info("ResolveDependencies recursively");
            getCollectRequest().setRoot(new Dependency(artifact, "test"));

            CollectResult collectDependencies = repositorySystem.collectDependencies(getRepositorySystemSession(), getCollectRequest());

            DependencyNode node = collectDependencies.getRoot();
            DependencyResult dependencyResult = repositorySystem.resolveDependencies(getRepositorySystemSession(), new DependencyRequest(node, null));

            for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
                LOGGER.debug("Dependencies founded :" + artifactResult.getArtifact().getArtifactId());
                artifacts.add(artifactResult.getArtifact());
            }
        } catch (DependencyCollectionException e) {
            LOGGER.error("Error while resolving dependencies", e);
        } catch (DependencyResolutionException e) {
            LOGGER.error("Error while resolving dependencies", e);
        }
        return artifacts;
    }

    public void setArtifactRequest(ArtifactRequest artifactRequest) {
        this.artifactRequest = artifactRequest;
    }

    public ArtifactRequest getArtifactRequest() {
        return artifactRequest;
    }

    public void setRepositorySystem(RepositorySystem repositorySystem) {
        this.repositorySystem = repositorySystem;
    }

    public RepositorySystem getRepositorySystem() {
        return repositorySystem;
    }

    public void setRepositorySystemSession(RepositorySystemSession repositorySystemSession) {
        this.repositorySystemSession = repositorySystemSession;
    }

    public RepositorySystemSession getRepositorySystemSession() {
        return repositorySystemSession;
    }


    public void setCollectRequest(CollectRequest collectRequest) {
        this.collectRequest = collectRequest;
    }

    public CollectRequest getCollectRequest() {
        return collectRequest;
    }

    public String getResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("List of downloaded artifacts (").append(foundedArtifacts.size()).append(") :\n");
        for (String artifact : foundedArtifacts) {
            sb.append("\t+ ").append(artifact).append("\n");
        }
        return sb.toString();
    }


}
