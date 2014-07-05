package org.maneau.maventools.utils;

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
 */
public class DeployArtifact {
    private static Logger LOGGER = LoggerFactory.getLogger(DeployArtifact.class);

    private RepositorySystemSession repositorySystemSession;
    private RepositorySystem repositorySystem;
    private String user = ConfigUtils.getProperty("enterprise.repository.user");
    private String password = ConfigUtils.getProperty("enterprise.repository.password");
    private String id = ConfigUtils.getProperty("enterprise.repository.id");
    private String type = ConfigUtils.getProperty("enterprise.repository.type");
    private String url = ConfigUtils.getProperty("enterprise.repository.url");

    boolean isManager = Boolean.valueOf(ConfigUtils.getProperty("repository.isManager"));

    public Set<String> getDeployedArtifacts() {
        return deployedArtifacts;
    }

    public void setDeployedArtifacts(Set<String> deployedArtifacts) {
        this.deployedArtifacts = deployedArtifacts;
    }

    private Set<String> deployedArtifacts = new HashSet<String>();

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

    public String getDeployed() {
        StringBuilder sb = new StringBuilder();
        sb.append("Number of deployed artifact (" + deployedArtifacts.size() + ") :\n");
        for (String artifact : deployedArtifacts) {
            sb.append("\t+ " + artifact + "\n");
        }
        return sb.toString();
    }

    public void deploy(Artifact artifact, Artifact pom) throws DeploymentException {
        RemoteRepository repoObj = new RemoteRepository(id, type, url);
        repoObj.setRepositoryManager(isManager);
        repoObj.setAuthentication(new Authentication(user, password));

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact(artifact);
        deployRequest.addArtifact(pom);
        deployRequest.setRepository(repoObj);

        repositorySystem.deploy(getRepositorySystemSession(), deployRequest);
    }

    public void deploy(Artifact artifact) throws DeploymentException {
        RemoteRepository repoObj = new RemoteRepository(id, type, url);
        repoObj.setRepositoryManager(isManager);
        //repoObj.setAuthentication(new Authentication(user, password));

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact(artifact);
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
                    artifact.setFile(file);
                    System.out.println(artifact.getArtifactId());
                    deploy(artifact);
                }
            } catch (DeploymentException e) {
                LOGGER.error("Error while deploying : " + key, e);
            }
        }
    }

    public String generatePathForArtifact(Artifact artifact) {
        StringBuilder sbName = new StringBuilder();
        sbName.append(artifact.getArtifactId()).append("-");
        sbName.append(artifact.getVersion());
        if (!"".equals(artifact.getClassifier())) {
            sbName.append("-").append(artifact.getClassifier());
        }
        sbName.append(".").append(artifact.getExtension());

        StringBuilder sbPath = new StringBuilder();
        sbPath.append(artifact.getGroupId().replace(".", File.separator)).append(File.separator);
        sbPath.append(artifact.getArtifactId()).append(File.separator);
        sbPath.append(artifact.getVersion()).append(File.separator);
        sbPath.append(sbName);

        return sbPath.toString();
    }

}
