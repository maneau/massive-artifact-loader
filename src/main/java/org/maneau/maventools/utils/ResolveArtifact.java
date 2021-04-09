package org.maneau.maventools.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by maneau on 05/07/2014.
 * Class providing feature for downloading artifacts
 */
public class ResolveArtifact {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolveArtifact.class);

    private RepositorySystemSession repositorySystemSession;
    private RepositorySystem repositorySystem;
    private ArtifactRequest artifactRequest;
    private CollectRequest collectRequest;

    public Set<String> getFoundedArtifacts() {
        return foundedArtifacts;
    }

    private final Set<String> foundedArtifacts = new HashSet<String>();

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

    private ArtifactResult resolvePomArtifact(Artifact artifact, boolean withDependencies) {
        resolveParentArtifact(artifact, withDependencies);

        if (artifact.getExtension().equals("pom")) {
            return null;
        }
        return resolveArtifactWithArtifact(getPomArtifact(artifact), withDependencies);
    }

    private void resolveParentArtifact(Artifact artifact, boolean withDependencies) {
        if (withDependencies) {
            String key = getKey(artifact);
            try {
                ArtifactRequest artifactPomRequest = new ArtifactRequest();
                artifactPomRequest.setArtifact(getPomArtifact(artifact));

                ArtifactResult artifactPomResult = getRepositorySystem().resolveArtifact(getRepositorySystemSession(), artifactPomRequest);
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model pomModel = reader.read(new FileReader(artifactPomResult.getArtifact().getFile()));
                if (pomModel.getParent() != null) {
                    Artifact parentArtifact = new DefaultArtifact(pomModel.getParent().getGroupId(), pomModel.getParent().getArtifactId(), "pom", pomModel.getParent().getVersion());
                    String parentKey = getKey(parentArtifact);
                    foundedArtifacts.add(parentKey);
                    LOGGER.debug("Parents founded for " + key);

                    //recursive
                    resolveParentArtifact(parentArtifact, withDependencies);
                } else {
                    LOGGER.debug("No parents for " + key);
                }
            } catch (Exception e) {
                LOGGER.error("ArtifactResolutionException:", e);
            }
        }
    }

    Artifact getPomArtifact(Artifact artifact) {
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

    ArtifactResult resolveArtifactWithArtifact(Artifact artifact, boolean withDependencies) {
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
        resolvePomArtifact(artifact, withDependencies);

        if (withDependencies) {
            //Getting dependencies
            for (Artifact subArtifact : resolveDependencies(artifact)) {
                //noinspection ConstantConditions
                resolveArtifactWithArtifact(subArtifact, withDependencies);
            }
        }
        return artifactResult;
    }

    List<Artifact> resolveDependencies(Artifact artifact) {
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

    void setArtifactRequest(ArtifactRequest artifactRequest) {
        this.artifactRequest = artifactRequest;
    }

    ArtifactRequest getArtifactRequest() {
        return artifactRequest;
    }

    void setRepositorySystem(RepositorySystem repositorySystem) {
        this.repositorySystem = repositorySystem;
    }

    RepositorySystem getRepositorySystem() {
        return repositorySystem;
    }

    void setRepositorySystemSession(RepositorySystemSession repositorySystemSession) {
        this.repositorySystemSession = repositorySystemSession;
    }

    RepositorySystemSession getRepositorySystemSession() {
        return repositorySystemSession;
    }


    void setCollectRequest(CollectRequest collectRequest) {
        this.collectRequest = collectRequest;
    }

    CollectRequest getCollectRequest() {
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
