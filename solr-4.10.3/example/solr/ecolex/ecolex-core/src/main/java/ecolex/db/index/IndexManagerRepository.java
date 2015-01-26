//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.index;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contains index managers for used indexes..
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class IndexManagerRepository
{
    private DocumentIndexManager[] managers;

    private Map<String, DocumentIndexManager> managersByName = new LinkedHashMap<String, DocumentIndexManager>();

    public IndexManagerRepository(DocumentIndexManager[] managers)
    {
        this.managers = managers;
        for (DocumentIndexManager manager : managers)
            managersByName.put(manager.getIndexName(), manager);
    }

    public Collection<String> getIndexNames()
    {
        return managersByName.keySet();
    }

    public DocumentIndexManager getIndexManager(String indexName)
    {
        return managersByName.get(indexName);
    }

    public DocumentIndexManager[] getIndexManagers()
    {
        return managers;
    }
}
