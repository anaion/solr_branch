//
// Copyright (c) 2008, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map of maps. Extends LinkedHashMap adding a utility method for adding values.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class MapMap<K1, K2,  V> extends LinkedHashMap<K1, Map<K2, V>>
{
    /**
     * Adds value for given keys.
     */
    public void add(K1 key1, K2 key2, V value)
    {
        Map<K2, V> map = get(key1);
        if (map == null)
        {
            map = createMap();
            put(key1, map);
        }
        map.put(key2, value);
    }

    /**
     * Creates a second-level map.
     * This method can be overridden to create different maps.
     */
    public Map<K2, V> createMap()
    {
        return new LinkedHashMap<K2, V>();
    }
}
