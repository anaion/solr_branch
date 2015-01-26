//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.index;

import java.util.Date;

import org.jcontainer.dna.Logger;

import ecolex.db.EcoLexDocument;
import faolex.iterator.FilterIterator;
import faolex.iterator.SizedIterator;

/**
 * Filters out documents that were not modified on or after the last update date.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class ModifiedDocumentsIterator
    extends FilterIterator<EcoLexDocument>
{
    private static final String DATE_MODIFIED_FIELD = "dateOfModification";

    private Date lastUpdateDate;

    private Logger log;

    private int counter = 0;

    public ModifiedDocumentsIterator(SizedIterator<EcoLexDocument> iterator, Date lastUpdateDate, Logger log)
    {
        super(iterator);
        this.lastUpdateDate = lastUpdateDate;
        this.log = log;
    }

    @Override
    protected boolean check(EcoLexDocument document)
    {
        counter++;
        Date modDate = document.getDate(DATE_MODIFIED_FIELD);
        if (!modDate.before(lastUpdateDate))
        {
            log.debug("Inserting or replacing " + document.getId() + " (" + modDate + " NOT BEFORE " + lastUpdateDate + ")");
            return true;
        }

        log.debug("NOT replacing " + document.getId() + " (" + modDate + " BEFORE " + lastUpdateDate + ")");
        return false;
    }

    @Override
    public boolean hasNext()
    {
        boolean hasNext = super.hasNext();
        if (!hasNext)
            log.info("Inserted or replaced " + (counter + size() - iterator.size()) + " out of " + counter + " received documents");
        return hasNext;
    }
}
