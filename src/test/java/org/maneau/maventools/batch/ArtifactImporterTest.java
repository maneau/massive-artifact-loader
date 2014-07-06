package org.maneau.maventools.batch;

import junit.framework.TestCase;

/**
 * Created by maneau on 05/07/2014.
 * Test class for ArtifactImporter
 */
public class ArtifactImporterTest extends TestCase {

    public void testSimpleResolveArtifact() throws Exception {
        String[] args = {"org.codehaus.mojo:xdoclet-maven-plugin:1.0"};

        ArtifactImporter.main(args);
        assertEquals(1, ArtifactImporter.getArtifacts().size());
        assertEquals(1, ArtifactImporter.getResults().size());
    }

    public void testMultipleResolveArtifact() throws Exception {
        String[] args = {"-f", "src/test/resources/local_repo.list"};

        ArtifactImporter.main(args);
        assertEquals(132, ArtifactImporter.getArtifacts().size());
        assertEquals(132, ArtifactImporter.getResults().size());
    }

}
