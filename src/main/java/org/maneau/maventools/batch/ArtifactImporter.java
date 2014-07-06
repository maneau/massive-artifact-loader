package org.maneau.maventools.batch;

import org.maneau.maventools.utils.ConfigUtils;
import org.maneau.maventools.utils.DeployArtifact;
import org.maneau.maventools.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by maneau on 05/07/2014.
 * Main class for Import Artifact in the enterprise repository
 */
public class ArtifactImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactImporter.class);

    private static Set<String> results;

    public static Set<String> getResults() {
        return results;
    }

    public static List<String> getArtifacts() {
        return artifacts;
    }

    public static void setArtifacts(List<String> artifacts) {
        ArtifactImporter.artifacts = artifacts;
    }

    private static List<String> artifacts;

    private static void usage() {

        print("Usage : ArtifactImporter (-f|--file file) (\"groupId:ArtifactId(:type):version(:classifier)\")");
        print(" -f|--file : file containing artifacts");
        print(" \"groupId:ArtifactId(:type):version(:classifier)\" : package can be multiples");
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

        artifacts = new ArrayList<String>();
        for (int i=0; i < args.length ; i++) {
            String arg = args[i];
            if ("-f".equalsIgnoreCase(arg) || "--file".equalsIgnoreCase(arg)) {
                if(i<args.length-1) {
                    artifacts.addAll(FileUtils.loadExportedListFromFile(args[i + 1]));
                    i++;
                } else {
                    print("Missing fileName after -f ");
                    usage();
                }
            } else {
                artifacts.add(arg);
            }
        }

        DeployArtifact deployer = new DeployArtifact();
        deployer.deployArtifactByList(artifacts);

        results = deployer.getDeployedArtifacts();
        FileUtils.saveExportedListToFile(results);
        print(deployer.getSummaryResults());
    }
}
