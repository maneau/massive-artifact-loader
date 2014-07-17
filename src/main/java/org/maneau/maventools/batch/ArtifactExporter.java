package org.maneau.maventools.batch;

import org.maneau.maventools.utils.ConfigUtils;
import org.maneau.maventools.utils.FileUtils;
import org.maneau.maventools.utils.ResolveArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by maneau on 05/07/2014.
 * Main Class for exporting or downloading artifact from central repository
 */
class ArtifactExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactExporter.class);

    private static Set<String> results;

    public static Set<String> getResults() {
        return results;
    }

    public static List<String> getArtifacts() {
        return artifacts;
    }

    public static void setArtifacts(List<String> artifacts) {
        ArtifactExporter.artifacts = artifacts;
    }

    private static List<String> artifacts;

    private static void usage() {
        print("Usage : ArtifactExporter (-r|--recursive) \"groupId:ArtifactId(:type):version(:classifier)\"");
        print(" -r|--recursive : recursive mode on dependencies");
        print(" \"groupId:ArtifactId(:type):version(:classifier)\" : package");
    }

    private static void print(String txt) {
        System.out.println(txt);
    }

    public static void main(String[] args) {
        ConfigUtils.init();

        LOGGER.info("Staring");
        if (args.length == 0) {
            usage();
            return;
        }

        boolean isRecursive = false;
        artifacts = new ArrayList<String>();
        for (String arg : args) {
            if ("-r".equalsIgnoreCase(arg) || "--recursive".equalsIgnoreCase(arg)) {
                isRecursive = true;
            } else {
                artifacts.add(arg);
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
}
