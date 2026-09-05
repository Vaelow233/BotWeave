package org.vaelow233.botweave.api.message.resource;

import java.net.URI;
import java.nio.file.Path;

public interface MediaSource {
    static MediaSource url(String value) {
        return new Url(URI.create(value));
    }
    static MediaSource file(Path path) {
        return new File(path);
    }
    static MediaSource bytes(byte[] value) {
        return new Bytes(value);
    }

    final class Url implements MediaSource {
        private final URI uri;
        private Url(URI uri) {
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme)
                    && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Media URL must use HTTP or HTTPS");
            }
            this.uri = uri;
        }

        public URI uri() {
            return uri;
        }
    }

    final class File implements MediaSource {
        private final Path path;
        private File(Path path) {
            this.path = path;
        }

        public URI uri() {
            return path.toUri();
        }

        public Path path() {
            return path;
        }
    }

    final class Bytes implements MediaSource {
        private final byte[] value;
        private Bytes(byte[] value) {
            this.value = value.clone();
        }

        public byte[] value() {
            return value.clone();
        }
    }
}