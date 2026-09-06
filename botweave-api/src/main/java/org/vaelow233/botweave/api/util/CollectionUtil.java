package org.vaelow233.botweave.api.util;

import java.util.*;

public class CollectionUtil {
    private CollectionUtil() {

    }

    @SafeVarargs
    public static <T> Set<T> ofSet(T... items) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(items)));
    }
}
