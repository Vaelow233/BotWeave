package org.vaelow233.botweave.core.connector;

public interface ConnectorFactory<C> {
    String type();
    Connector create(C configuration, ConnectorContext context);
}