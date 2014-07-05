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
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository("local-repo");
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

        //session.setTransferListener(new ConsoleTransferListener());
        //session.setRepositoryListener(new ConsoleRepositoryListener());

        return session;
    }

}
