package org.maneau.maventools.utils;

import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;

import java.util.Arrays;
import java.util.List;

/**
 * Created by maneau on 05/07/2014.
 * Class for generating Repositories
 */
public class Repositories {
    public static RemoteRepository create(String id, String url, String snapshotUpdates, String releaseUpdates) {
        RemoteRepository repository;

        repository = new RemoteRepository(id, "default", url);
        repository.setPolicy(true, new RepositoryPolicy(snapshotUpdates != null, snapshotUpdates, RepositoryPolicy.CHECKSUM_POLICY_WARN));
        repository.setPolicy(false, new RepositoryPolicy(releaseUpdates != null, releaseUpdates, RepositoryPolicy.CHECKSUM_POLICY_WARN));
        return repository;
    }

    public static final RemoteRepository MAVEN_CENTRAL = create(
            "central",
            ConfigUtils.getProperty("central.repository.url"),
            null,
            RepositoryPolicy.UPDATE_POLICY_DAILY
    );

    public static final List<RemoteRepository> STANDARD = Arrays.asList(MAVEN_CENTRAL);

}