package org.maneau.maventools.batch;

import org.maneau.maventools.utils.FileUtils;
import org.maneau.maventools.utils.ResolveArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by maneau on 05/07/2014.
 */
public class ArtifactExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactExporter.class);

    private static Set<String> results;
    private static boolean isRecursive = false;

    public static Set<String> getResults() {
        return results;
    }

    public static void setResults(Set<String> results) {
        ArtifactExporter.results = results;
    }

    public static List<String> getArtifacts() {
        return artifacts;
    }

    public static void setArtifacts(List<String> artifacts) {
        ArtifactExporter.artifacts = artifacts;
    }

    private static List<String> artifacts;

    private static void usage() {
        print("Usage : ArtifactExporter (-f|--file file) (\"groupId:ArtifactId(:type):version(:classifier)\")");
        print(" -f|--file : file containing artifacts");
        print(" \"groupId:ArtifactId(:type):version(:classifier)\" : package can be multiples");
    }

    private static void print(String txt) {
        System.out.println(txt);
    }

    public static void main(String[] args) {
        LOGGER.info("Staring");
        if (args.length == 0) {
            usage();
            return;
        } else {
            LOGGER.info("Starting ArtifactDependencies with : " + args);
        }

        artifacts = new ArrayList<String>();
        for (String arg : args) {
            if ("-r".equalsIgnoreCase(arg) || "--recurse".equalsIgnoreCase(arg)) {
                isRecursive = true;
            } else {
                artifacts.add(arg);
            }
        }

        ResolveArtifact resolver = new ResolveArtifact();
        for (String artifact : artifacts) {
            print("Searching for artifact : " + artifacts);
            resolver.resolveArtifactWithkey(artifact, isRecursive);
        }

        results = resolver.getFoundedArtifacts();
        FileUtils.saveExportedListToFile(results);
        print(resolver.getResults());
    }
}
