package org.maneau.maventools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by maneau on 05/07/2014.
 * Class for getting properties
 */
public class ConfigUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployArtifact.class);

    private static final Properties properties = new Properties();

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }

    public static void init() {
        InputStream in = ConfigUtils.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            LOGGER.error("Error while loading config.properties", e);
        }
        LOGGER.debug("Number of properties loaded : " + properties.size());
    }
}
