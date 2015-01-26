//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Maps a key to a collection of values.
 * By default LinkedLists are used. Override the createCollection method to create other collections.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 */
public class CollectionMap<K, V> extends LinkedHashMap<K, Collection<V>>
{
    public void add(K key, V value)
    {
        Collection<V> list = get(key);
        if (list == null)
        {
            list = createCollection();
            put(key, list);
        }
        list.add(value);
    }

    public Collection<V> createCollection()
    {
        return new LinkedList<V>();
    }

    public static <K, V> CollectionMap<K, V> createSetMap()
    {
        return new CollectionMap<K, V>() {
                @Override
                public Collection<V> createCollection()
                {
                    return new LinkedHashSet<V>();
                }
            };
    }
}
