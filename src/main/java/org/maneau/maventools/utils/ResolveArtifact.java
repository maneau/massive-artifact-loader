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

    public void setFoundedArtifacts(Set<String> foundedArtifacts) {
        this.foundedArtifacts = foundedArtifacts;
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

    public ArtifactResult resolveArtifact(String groupId, String artifactId, String version, boolean withDependencies) {
        return resolveArtifactWithArtifact(new DefaultArtifact(groupId, artifactId, null, version), withDependencies);
    }

    public ArtifactResult resolveArtifactWithCoordinates(String groupId, String artifactId, String version, boolean withDependencies) {
        return resolveArtifactWithArtifact(new DefaultArtifact(groupId + ":" + artifactId + ":" + version), withDependencies);
    }

    public ArtifactResult resolveArtifactWithkey(String key, boolean withDependencies) {
        return resolveArtifactWithArtifact(new DefaultArtifact(key), withDependencies);
    }

    public ArtifactResult resolvePomArtifact(Artifact artifact, boolean withDependencies) {
        if (artifact.getExtension().equals("pom")) {
            return null;
        }
        return resolveArtifactWithArtifact(getPomArtifact(artifact), withDependencies);
    }

    public Artifact getPomArtifact(Artifact artifact) {
        Artifact pomArtifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "pom", artifact.getVersion());

        return pomArtifact;
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
            LOGGER.info("Artifact déjà chargé (" + key + ") : ");
            return null;
        } else {
            LOGGER.info("Chargement de : " + key);
        }

        getArtifactRequest().setArtifact(artifact);

        ArtifactResult artifactResult = null;
        try {
            artifactResult = getRepositorySystem().resolveArtifact(getRepositorySystemSession(), getArtifactRequest());
            foundedArtifacts.add(getKey(artifact));
        } catch (ArtifactResolutionException e) {
            LOGGER.error("ArtifactResolutionException:", e);
        }

        //On récupère le pom
        if (!artifact.getExtension().equals("pom")) {
            artifactResult = resolvePomArtifact(artifact, withDependencies);
        } else {
            if (withDependencies) {
                //si c'est un pom on récupère les dépendances
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
                LOGGER.debug("Dépendence trouvées :" + artifactResult.getArtifact().getArtifactId());
                artifacts.add(artifactResult.getArtifact());
            }
        } catch (DependencyCollectionException e) {
            LOGGER.error("resolveDependencies", e);
        } catch (DependencyResolutionException e) {
            LOGGER.error("resolveDependencies", e);
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
        sb.append("Liste des artifacts téléchargés (" + foundedArtifacts.size() + ") :\n");
        for (String artifact : foundedArtifacts) {
            sb.append("\t+ " + artifact + "\n");
        }
        return sb.toString();
    }


}
