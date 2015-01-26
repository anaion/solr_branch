//
// Copyright (c) 2008, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import faolex.iterator.SizedIterator;

/**
 * Creates processors which process a stream (iterator) of records.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public interface DocumentProcessorFactory
{
    /**
     * Creates a record processor.
     * @param iterator  input iterator
     * @return  output iterator
     */
    SizedIterator<EcoLexDocument> createProcessor(SizedIterator<EcoLexDocument> iterator);
}
