package org.maneau.maventools.batch;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by maneau on 05/07/2014.
 */
public class ArtifactImporterTest extends TestCase {

    @Test
    public void testSimpleResolveArtifact() throws Exception {
        String[] args = {"org.codehaus.mojo:xdoclet-maven-plugin:1.0"};

        ArtifactImporter.main(args);
        assertEquals(1,ArtifactExporter.getArtifacts().size());
        assertEquals(2,ArtifactExporter.getResults().size());
    }

}
