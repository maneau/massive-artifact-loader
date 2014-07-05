package org.maneau.maventools.batch;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by maneau on 05/07/2014.
 */
public class ArtifactExporterTest extends TestCase {

    @Test
    public void testSimpleResolveArtifact() throws Exception {
        String[] args = {"org.codehaus.mojo:xdoclet-maven-plugin:1.0"};

        ArtifactExporter.main(args);
        assertEquals(1,ArtifactExporter.getArtifacts().size());
        assertEquals(2,ArtifactExporter.getResults().size());
    }

    @Test
    public void testMultipleResolveArtifact() throws Exception {
        String[] args = {"org.codehaus.mojo:xdoclet-maven-plugin:1.0" , "org.sonatype.aether:aether-api:1.11"};

        ArtifactExporter.main(args);
        assertEquals(2,ArtifactExporter.getArtifacts().size());
        assertEquals(4,ArtifactExporter.getResults().size());
    }

    @Test
    public void testSingleRecursiveResolving() throws Exception {
        String[] args = {"-r","org.codehaus.mojo:xdoclet-maven-plugin:1.0" };

        ArtifactExporter.main(args);
        assertEquals(1,ArtifactExporter.getArtifacts().size());
        assertEquals(132,ArtifactExporter.getResults().size());
    }

}
