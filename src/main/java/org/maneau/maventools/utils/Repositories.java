package org.maneau.maventools.utils;

import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;

import java.util.Arrays;
import java.util.List;

/**
 * Created by maneau on 05/07/2014.
 * Class for generating Repositories
 */
class Repositories {
    private static RemoteRepository create(String id, @SuppressWarnings("SameParameterValue") String url, String snapshotUpdates, String releaseUpdates) {
        RemoteRepository repository;

        repository = new RemoteRepository(id, "default", url);
        repository.setPolicy(true, new RepositoryPolicy(
                snapshotUpdates != null, snapshotUpdates,
                RepositoryPolicy.CHECKSUM_POLICY_WARN));
        repository.setPolicy(false, new RepositoryPolicy(
                RepositoryPolicy.UPDATE_POLICY_DAILY != null,
                RepositoryPolicy.UPDATE_POLICY_DAILY,
                RepositoryPolicy.CHECKSUM_POLICY_WARN));

        String login = ConfigUtils.getProperty("central.repository.login");
        String password = ConfigUtils.getProperty("central.repository.password");

        if (login != null && login.length() > 0 && password != null && password.length() > 0) {
            repository.setAuthentication(new Authentication(login, password));
        }
        return repository;
    }

    private static final RemoteRepository MAVEN_CENTRAL = create(
            "central",
            ConfigUtils.getProperty("central.repository.url"),
            null,
            RepositoryPolicy.UPDATE_POLICY_DAILY
    );

    public static final List<RemoteRepository> STANDARD = Arrays.asList(MAVEN_CENTRAL);

}