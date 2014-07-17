package org.maneau.maventools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by maneau on 05/07/2014.
 * This Utility class used for File Access
 */
public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolveArtifact.class);
    private static final String FILENAME = ConfigUtils.getProperty("exported.artifacts.file");

    public static void saveExportedListToFile(Set<String> artifacts) {

        FileWriter fileWriter;
        BufferedWriter bufferedWriter = null;

        try {
            fileWriter = new FileWriter(FILENAME, false);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (String line : artifacts) {
                bufferedWriter.write(line);
                bufferedWriter.write("\n");
            }

        } catch (IOException e) {
            LOGGER.error("Error while writing file : " + FILENAME, e);
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (IOException e) {
                    LOGGER.error("Error while closing file : " + FILENAME, e);
                }
            }
        }
    }

    public static Set<String> loadExportedListFromFile(String fileName) {
        Scanner scanner = null;
        Set<String> artifacts = new HashSet<String>();

        try {
            scanner = new Scanner(new File(fileName));

            // On boucle sur chaque champ detecté
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line != null && line.length() > 0) {
                    artifacts.add(line);
                }
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("Error while writing file : " + fileName, e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return artifacts;
    }
}
