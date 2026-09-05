package org.vaelow233.botweave.connector.qq.ob11.config;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OneBotConfig {
    private final String schema;
    private final String address;
    private final int port;
    private final String token;

    public OneBotConfig(String schema, String address, int port, String token) {
        this.schema = schema;
        this.address = address;
        this.port = port;
        this.token = token;
    }

    public String schema() {
        return schema;
    }

    public String address() {
        return address;
    }

    public int port() {
        return port;
    }

    public String token() {
        return token;
    }

    public URI toURI() {
        return URI.create(schema + "://" + address + ":" + port + "/");
    }

    public Map<String, String> getHeaders() {
        if (token != null && !token.isEmpty()) {
            return Collections.singletonMap("Authorization", "Bearer " + token);
        } else return new HashMap<>();
    }
}
