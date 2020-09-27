package com.qlservices;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class QuantLibLoader {
    private static final Logger LOG = Logger.getLogger(QuantLibLoader.class);
    void onStart(@Observes StartupEvent ev) {
        LOG.info("in OnStart event, loading JNI library");
        System.loadLibrary("QuantLibJNI");
    }
}
