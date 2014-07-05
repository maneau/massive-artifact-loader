package org.maneau.maventools.batch;

import org.maneau.maventools.utils.ConfigUtils;
import org.maneau.maventools.utils.DeployArtifact;
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
public class ArtifactImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactImporter.class);

    private static Set<String> results;
    private static boolean isRecursive = false;

    public static Set<String> getResults() {
        return results;
    }

    public static void setResults(Set<String> results) {
        ArtifactImporter.results = results;
    }

    public static List<String> getArtifacts() {
        return artifacts;
    }

    public static void setArtifacts(List<String> artifacts) {
        ArtifactImporter.artifacts = artifacts;
    }

    private static List<String> artifacts;

    private static void usage() {
        print("Usage : ArtifactImporter (-r|--recurse) \"groupId:ArtifactId(:type):version(:classifier)\"");
        print(" -e|--recurse : recusif");
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
        } else {
            LOGGER.info("Starting ArtifactDependencies with : " + args);
        }

        artifacts = new ArrayList<String>();
        for (int i=0; i < args.length ; i++) {
            String arg = args[i];
            if ("-f".equalsIgnoreCase(arg) || "--file".equalsIgnoreCase(arg)) {
                if(i<args.length-1) {
                    artifacts.addAll(FileUtils.loadExportedListFromFile(args[i + 1]));
                } else {
                    print("Missing fileName after -f ");
                    usage();
                }
            } else {
                artifacts.add(arg);
            }
        }

        //TODO Importer les artifacts
        DeployArtifact deployer = new DeployArtifact();
        deployer.deployArtifactByList(artifacts);

        /*results = resolver.getFoundedArtifacts();
        FileUtils.saveExportedListToFile(results);
        print(resolver.getResults());
        */
    }
}
