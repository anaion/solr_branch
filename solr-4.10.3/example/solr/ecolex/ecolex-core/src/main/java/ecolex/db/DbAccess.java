//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.io.IOException;
import java.util.Date;

import faolex.iterator.SizedIterator;

/**
 * Provides access to data records.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public interface DbAccess
{
    /**
     * Returns all records from the data source.
     * @return Iterator over retrieved documents.
     */
    public SizedIterator<EcoLexDocument> getAll() throws IOException;

    /**
     * Returns new records from the data source.
     * @return Iterator over retrieved documents.
     */
    public SizedIterator<EcoLexDocument> getNew(Date date) throws IOException;

    /**
     * Returns modified records from the data source.
     * @return Iterator over retrieved documents.
     */
    public SizedIterator<EcoLexDocument> getModified(Date date) throws IOException;

    /**
     * Retrieves a single record for the given ID.
     */
    public EcoLexDocument getDocument(String id) throws IOException;
}
