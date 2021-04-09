package org.maneau.maventools.batch;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.maneau.maventools.utils.ConfigUtils;
import org.maneau.maventools.utils.FileUtils;
import org.maneau.maventools.utils.ResolveArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by maneau on 05/07/2014.
 * Main Class for exporting or downloading artifact from central repository
 */
class PomExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PomExporter.class);

    private static Set<String> results;

    public static Set<String> getResults() {
        return results;
    }

    public static List<String> getArtifacts() {
        return artifacts;
    }

    public static void setArtifacts(List<String> artifacts) {
        PomExporter.artifacts = artifacts;
    }

    private static List<String> artifacts = new ArrayList<String>();

    private static void usage() {
        print("Usage : PomExporter (-r|--recursive) \"pom.xml\"");
        print(" -r|--recursive : recursive mode on dependencies");
    }

    private static void print(String txt) {
        System.out.println(txt);
    }

    public static void main(String[] args) throws IOException, XmlPullParserException {
        ConfigUtils.init();

        LOGGER.info("Staring");
        if (args.length == 0) {
            usage();
            return;
        }

        String filename = "pom.xml";

        boolean isRecursive = false;
        for (String arg : args) {
            if ("-r".equalsIgnoreCase(arg) || "--recursive".equalsIgnoreCase(arg)) {
                isRecursive = true;
            } else {
                filename = arg;
            }
        }

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model pomModel = reader.read(new FileReader(filename));
        if (pomModel.getParent() != null) {
            artifacts.add(getKey(pomModel.getParent().getGroupId(),
                    pomModel.getParent().getArtifactId(),
                    pomModel.getParent().getVersion(),
                    "pom",
                    null));
        }

        for (Dependency dep : pomModel.getDependencies()) {
            artifacts.add(getKey(dep.getGroupId(),
                    getValuesFromProperties(pomModel.getProperties(), dep.getArtifactId()),
                    getValuesFromProperties(pomModel.getProperties(), dep.getVersion()),
                    getValuesFromProperties(pomModel.getProperties(), dep.getType()),
                    getValuesFromProperties(pomModel.getProperties(), dep.getClassifier())));
        }

        if(pomModel.getDependencyManagement() != null && pomModel.getDependencyManagement().getDependencies() != null) {
            for (Dependency dep : pomModel.getDependencyManagement().getDependencies()) {
                artifacts.add(getKey(dep.getGroupId(),
                        getValuesFromProperties(pomModel.getProperties(), dep.getArtifactId()),
                        getValuesFromProperties(pomModel.getProperties(), dep.getVersion()),
                        getValuesFromProperties(pomModel.getProperties(), dep.getType()),
                        getValuesFromProperties(pomModel.getProperties(), dep.getClassifier())));
            }
        }

        ResolveArtifact resolver = new ResolveArtifact();
        for (String artifact : artifacts) {
            print("Searching for artifact : " + artifacts);
            resolver.resolveArtifactWithKey(artifact, isRecursive);
        }

        results = resolver.getFoundedArtifacts();
        FileUtils.saveExportedListToFile(results);
        print(resolver.getResults());
    }

    private static String getValuesFromProperties(Properties properties, String artifactId) {
        if (artifactId != null && artifactId.startsWith("${")) {
            return properties.getProperty(
                    artifactId.replace("${", "")
                            .replace("}", ""));
        } else {
            return artifactId;
        }
    }

    private static String getKey(String groupId, String artifactId, String version, String extension, String classifier) {
        if (extension == null) {
            extension = "jar";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(groupId);
        sb.append(":").append(artifactId);
        sb.append(":").append(extension);
        sb.append(":").append(version);
        if (classifier != null) {
            sb.append(":").append(classifier);
        }
        return sb.toString();
    }
        /*
        StringBuilder sb = new StringBuilder();
            sb.append(a.getGroupId());
            sb.append(":").append(a.getArtifactId());
            sb.append(":").append(a.getExtension());
            sb.append(":").append(a.getVersion());
            if (a.getClassifier().length() > 0) {
            sb.append(":").append(a.getClassifier());
        }
        return sb.toString();*/
}
