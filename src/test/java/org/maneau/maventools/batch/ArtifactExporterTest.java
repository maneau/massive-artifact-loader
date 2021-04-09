package org.maneau.maventools.batch;

import junit.framework.TestCase;

/**
 * Created by maneau on 05/07/2014.
 * Test class for ArtifactExporter
 */
public class ArtifactExporterTest extends TestCase {

    public void testSimpleResolveArtifact() throws Exception {
        String[] args = {"org.codehaus.mojo:xdoclet-maven-plugin:1.0"};

        ArtifactExporter.main(args);
        assertEquals(1, ArtifactExporter.getArtifacts().size());
        assertEquals(2, ArtifactExporter.getResults().size());
    }

    public void testMultipleResolveArtifact() throws Exception {
        String[] args = {"org.codehaus.mojo:xdoclet-maven-plugin:1.0", "org.sonatype.aether:aether-api:1.11"};

        ArtifactExporter.main(args);
        assertEquals(2, ArtifactExporter.getArtifacts().size());
        assertEquals(4, ArtifactExporter.getResults().size());
    }

    public void testSingleRecursiveResolving() throws Exception {
        String[] args = {"-r", "org.codehaus.mojo:xdoclet-maven-plugin:1.0"};

        ArtifactExporter.main(args);
        assertEquals(1, ArtifactExporter.getArtifacts().size());
        assertEquals(132, ArtifactExporter.getResults().size());
    }

    public void testParentRecursiveResolving() throws Exception {
        String[] args = {"org.apache.maven:maven-model:jar:3.0.1"};

        ArtifactExporter.main(args);
        assertTrue(ArtifactExporter.getResults().contains("org.apache.maven:maven:pom:3.0.1"));
    }

}
