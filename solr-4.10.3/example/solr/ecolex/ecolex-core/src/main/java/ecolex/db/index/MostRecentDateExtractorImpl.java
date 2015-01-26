package ecolex.db.index;

import java.util.Date;
import java.util.Set;

import ecolex.db.EcoLexDocument;
import faolex.iterator.FilterIterator;
import faolex.iterator.SizedIterator;

/**
 * Saves the most recent date of modification or entry from incoming documents.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemyslaw Wiech</a>
 * @version $Id$
 */
public class MostRecentDateExtractorImpl
    extends FilterIterator<EcoLexDocument> implements MostRecentDateExtractor
{
    private static String[] DATE_FIELDS = { "dateOfEntry", "dateOfModification"};

    private Date mostRecent = null;

    public MostRecentDateExtractorImpl(SizedIterator<EcoLexDocument> iterator)
    {
        super(iterator);
    }

    @Override
    protected boolean check(EcoLexDocument document)
    {
        for (String fieldName : DATE_FIELDS)
        {
            Set<Date> dates = document.getDates(fieldName);
            if (dates != null)
                for (Date date : dates)
                    check(date);
        }
        return true;
    }

    private void check(Date date)
    {
        if (date == null || date.after(new Date()))
            return;
        if (mostRecent == null || date.after(mostRecent))
            mostRecent = date;
    }

    /* (non-Javadoc)
     * @see ecolex.db.index.MostRecentDateExtractor#getMostRecentDate()
     */
    public Date getMostRecentDate()
    {
        return mostRecent;
    }
}
