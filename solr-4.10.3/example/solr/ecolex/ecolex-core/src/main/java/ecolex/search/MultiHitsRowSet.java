//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.search;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.objectledge.table.TableFilter;
import org.objectledge.table.TableRow;
import org.objectledge.table.TableState;
import org.objectledge.table.generic.BaseRowSet;

import faolex.search.DateConverter;
import faolex.search.searching.LuceneSearchHit;

/**
 * A <code>TableRowSet</code> implementation which wraps up lucene's search results.
 * This is very important to create a <code>TableTool</code> before closing a <code>Searcher</code>
 * which produced <code>Hits</code> used by this row set, otherwise no field values will be
 * drawn from lucene's index.
 *
 * @author <a href="mailto:gajda@ii.pw.edu.pl">Damian Gajda</a>
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class MultiHitsRowSet extends BaseRowSet<LuceneSearchHit>
{
    protected TableRow<LuceneSearchHit>[] rows;
    protected int totalRowCount;

    public MultiHitsRowSet(Hits[] hits, DateConverter dateConverter, TableState state,
        TableFilter<LuceneSearchHit>[] filters)
    {
        super(state, filters);

        int[] starts = new int[hits.length + 1];
        totalRowCount = 0;
        for (int i = 0; i < hits.length; i++)
        {
            starts[i] = totalRowCount;
            totalRowCount += hits[i].length();
        }
        starts[hits.length] = totalRowCount;

        // get rows together with documents contents

        int page = state.getCurrentPage();
        int perPage = state.getPageSize();

        int listSize = totalRowCount;
        int start = 0;
        int end = listSize;

        if(page > 0 && perPage > 0)
        {
            start = (page-1)*perPage;
            end = page*perPage;

            end = ( end<listSize )? end: listSize;
        }
        rows = new TableRow[end-start];

        if (totalRowCount == 0)
            return; // Nothing more to do

        int currentHits = 0;
        while (starts[currentHits + 1] <= start)
            currentHits++;
        int currentNum = start - starts[currentHits];

        for(int i=start, j=0; i<end; i++, j++)
        {
            try
            {
                Document doc = hits[currentHits].doc(currentNum);
                float score = hits[currentHits].score(currentNum);
                currentNum++;
                if (currentNum == hits[currentHits].length())
                {
                    do currentHits++;
                    while (currentHits < hits.length && hits[currentHits].length() == 0);
                    currentNum = 0;
                }
                LuceneSearchHit hit = new LuceneSearchHit(doc, score, dateConverter);
                rows[j] = new TableRow<LuceneSearchHit>(Integer.toString(i), hit, 0, 0, 0);
            }
            catch(IOException e)
            {
                throw new RuntimeException("problem retrieving lucene document fields", e);
            }
        }
    }

    public int getPageRowCount()
    {
        return rows.length;
    }

    public TableRow<LuceneSearchHit> getParentRow(TableRow<LuceneSearchHit> childRow)
    {
        return null;
    }

    public TableRow<LuceneSearchHit> getRootRow()
    {
        return null;
    }

    public TableRow<LuceneSearchHit>[] getRows()
    {
        return rows;
    }

    @Override
    public TableState getState()
    {
        return state;
    }

    public int getTotalRowCount()
    {
        return totalRowCount;
    }

    public boolean hasMoreChildren(TableRow<LuceneSearchHit> ancestorRow, TableRow<LuceneSearchHit> descendantRow)
    {
        return false;
    }
}
