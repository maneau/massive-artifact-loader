package org.maneau.maventools.utils;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by maneau on 05/07/2014.
 * This class Provides feature for deploying artifacts
 */
public class DeployArtifact {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployArtifact.class);

    private RepositorySystemSession repositorySystemSession;
    private RepositorySystem repositorySystem;
    private final String user = ConfigUtils.getProperty("enterprise.repository.user");
    private final String password = ConfigUtils.getProperty("enterprise.repository.password");
    private final String id = ConfigUtils.getProperty("enterprise.repository.id");
    private final String type = ConfigUtils.getProperty("enterprise.repository.type");
    private final String url = ConfigUtils.getProperty("enterprise.repository.url");

    private final boolean isManager = Boolean.valueOf(ConfigUtils.getProperty("enterprise.repository.isManager"));

    private final Set<String> failedArtifacts = new HashSet<String>();

    private final Set<String> deployedArtifacts = new HashSet<String>();

    public Set<String> getDeployedArtifacts() {
        return deployedArtifacts;
    }

    public DeployArtifact() {
        setRepositorySystem(Booter.newRepositorySystem());
        setRepositorySystemSession(Booter.newRepositorySystemSession(getRepositorySystem()));

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

    public String getDeployed() {
        StringBuilder sb = new StringBuilder();
        sb.append("Number of deployed artifact (").append(deployedArtifacts.size()).append(") :\n");
        for (String artifact : deployedArtifacts) {
            sb.append("\t+ ").append(artifact).append("\n");
        }
        return sb.toString();
    }

    void deploy(Artifact... artifacts) throws DeploymentException {
        RemoteRepository repoObj = new RemoteRepository(id, type, url);
        repoObj.setRepositoryManager(isManager);

        if (user != null && !"".equals(user)) {
            LOGGER.debug("Settings authentication : for [" + user + "/"
                    + StringUtils.repeat("*", password.length()) + "]");
            repoObj.setAuthentication(new Authentication(user, password));
        }

        DeployRequest deployRequest = new DeployRequest();

        for (Artifact artifact : artifacts) {
            deployRequest.addArtifact(artifact);
        }

        deployRequest.setRepository(repoObj);

        repositorySystem.deploy(getRepositorySystemSession(), deployRequest);
    }

    public void deployArtifactByList(List<String> keys) {
        String localPath = ConfigUtils.getProperty("local.repository.path");

        for (String key : keys) {
            try {
                Artifact artifact = new DefaultArtifact(key);
                String filePath = localPath + File.separator + generatePathForArtifact(artifact);
                File file = new File(filePath);
                if (!file.exists()) {
                    LOGGER.error("Artifact file not founded = " + filePath);
                } else {
                    //This method's not Working : artifact.setFile(file);
                    //( groupId, artifactId, classifier, extension, version, properties, file);
                    artifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                            artifact.getClassifier(), artifact.getExtension(), artifact.getVersion(),
                            null, file);

                    if (artifact.getFile() == null) {
                        LOGGER.error(artifact.getArtifactId() + " no file attached");
                    } else {
                        LOGGER.debug(artifact.getArtifactId() + " file : " + artifact.getFile().getPath());
                        deploy(artifact);
                        deployedArtifacts.add(getKey(artifact));
                    }
                }
            } catch (DeploymentException e) {
                if (e.getMessage().contains("Return code is: 400")) {
                    failedArtifacts.add(key + " (Already exists '400')");
                } else {
                    LOGGER.error("Error while deploying : " + key, e);
                    int beginIndex = e.getMessage().indexOf("Return code is: ");
                    if (beginIndex > 0) {
                        String code = e.getMessage().substring(beginIndex);
                        failedArtifacts.add(key + " (" + code + ")");
                    } else {
                        failedArtifacts.add(key);
                    }
                }
            }
        }
    }

    String generatePathForArtifact(Artifact artifact) {
        StringBuilder sbName = new StringBuilder();
        sbName.append(artifact.getArtifactId()).append("-");
        sbName.append(artifact.getVersion());
        if (!"".equals(artifact.getClassifier())) {
            sbName.append("-").append(artifact.getClassifier());
        }
        sbName.append(".").append(artifact.getExtension());

        @SuppressWarnings("StringBufferReplaceableByString") StringBuilder sbPath = new StringBuilder();
        sbPath.append(artifact.getGroupId().replace(".", File.separator)).append(File.separator);
        sbPath.append(artifact.getArtifactId()).append(File.separator);
        sbPath.append(artifact.getVersion()).append(File.separator);
        sbPath.append(sbName);

        return sbPath.toString();
    }

    public String getSummaryResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("List of successfully deployed artifacts (").append(deployedArtifacts.size()).append(") :\n");
        for (String artifact : deployedArtifacts) {
            sb.append("\t+ ").append(artifact).append("\tOK\n");
        }

        sb.append("\nList of failure deployed artifacts (").append(failedArtifacts.size()).append(") :\n");
        for (String artifact : failedArtifacts) {
            sb.append("\t+ ").append(artifact).append("\tFAILED\n");
        }
        return sb.toString();
    }

}
