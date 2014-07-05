package org.maneau.maventools.utils;


import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.LocalRepository;

/**
 * Created by maneau on 05/07/2014.
 */
public class Booter {

    public static RepositorySystem newRepositorySystem() {
        return ManualRepositorySystemFactory.newRepositorySystem();
    }

    public static RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        String localRepoPath = ConfigUtils.getProperty("local.repository.path");
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository(localRepoPath);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

        return session;
    }

}
