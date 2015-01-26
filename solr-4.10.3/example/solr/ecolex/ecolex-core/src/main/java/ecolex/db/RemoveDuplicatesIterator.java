//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.util.HashSet;
import java.util.Set;

import org.jcontainer.dna.Logger;

import faolex.iterator.FilterIterator;
import faolex.iterator.SizedIterator;

/**
 * Removes duplicate documents from the stream based on their IDs.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class RemoveDuplicatesIterator
    extends FilterIterator<EcoLexDocument>
{
    private Set<String> ids;
    private Logger log;

    public RemoveDuplicatesIterator(SizedIterator<EcoLexDocument> iterator, Logger log)
    {
        super(iterator);
        this.log = log;
        ids = new HashSet<String>();
    }

    @Override
    protected boolean check(EcoLexDocument document)
    {
        String id = document.getId();
        if (ids.contains(id))
            return false;
        ids.add(id);
        return true;
    }

    @Override
    public boolean hasNext()
    {
        boolean hasNext = super.hasNext();
        if (!hasNext)
            log.info("Received " + ids.size() + " unique records. Discarded " + (iterator.size() - size()) + " duplicates");
        return hasNext;
    }

}
