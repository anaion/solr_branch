//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.index;

import java.util.LinkedHashMap;
import java.util.Map;

import ecolex.db.DbAccess;

/**
 * Manages a repository of DbAccess objects.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class DbAccessRepository
{
    Map<String, DbAccess> dbAccess = new LinkedHashMap<String, DbAccess>();

    public DbAccessRepository(String[] indexNames, DbAccess[] dbAccess)
    {
        if (indexNames.length != dbAccess.length)
            throw new IllegalArgumentException("Wrong number of parameters");
        for (int i = 0; i < indexNames.length; i++)
            this.dbAccess.put(indexNames[i], dbAccess[i]);
    }

    public DbAccess getDbAccess(String indexName)
    {
        return dbAccess.get(indexName);
    }
}
