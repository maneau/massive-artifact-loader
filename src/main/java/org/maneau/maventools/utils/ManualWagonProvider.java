package org.maneau.maventools.utils;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagonAuthenticator;
import org.apache.maven.wagon.providers.http.LightweightHttpsWagon;
import org.sonatype.aether.connector.wagon.WagonProvider;

/**
 * Created by maneau on 05/07/2014.
 * A simplistic provider for wagon instances when no Plexus-compatible IoC container is used.
 */
class ManualWagonProvider implements WagonProvider {

    public Wagon lookup(String roleHint) throws Exception {
        LightweightHttpWagon wagon;
        if ("http".equals(roleHint)) {
            wagon = new LightweightHttpWagon();
        } else if ("https".equals(roleHint)) {
            wagon = new LightweightHttpsWagon();
        } else {
            return null;
        }
        wagon.setAuthenticator(new LightweightHttpWagonAuthenticator());
        return wagon;
    }

    public void release(Wagon wagon) {

    }

}