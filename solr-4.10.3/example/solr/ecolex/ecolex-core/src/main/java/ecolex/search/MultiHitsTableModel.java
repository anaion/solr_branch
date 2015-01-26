//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.search.Hits;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.TableFilter;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableRowSet;
import org.objectledge.table.TableState;

import faolex.search.DateConverter;
import faolex.search.searching.LuceneSearchHit;
import faolex.search.searching.SearchFieldsInfo;

/**
 * A <code>TableModel</code> implementation which wraps up lucene's search results.
 *
 * @author <a href="mailto:gajda@ii.pw.edu.pl">Damian Gajda</a>
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class MultiHitsTableModel implements TableModel<LuceneSearchHit>
{
    protected Hits[] hits;
    protected MultiHitsRowSet rowSet;
    protected TableColumn<LuceneSearchHit>[] columns;
    private DateConverter dateConverter;

    public MultiHitsTableModel(Hits hits[], SearchFieldsInfo fieldsInfo, DateConverter dateConverter)
        throws TableException
    {
        this.hits = hits;
        this.dateConverter = dateConverter;
        String[] fields = fieldsInfo.getStoredFields();
        List<TableColumn<LuceneSearchHit>> tmpColumns = new ArrayList<TableColumn<LuceneSearchHit>>(fields.length);
        for(String fieldName : fields)
        {
            Comparator<LuceneSearchHit> cmp = null;
            if(fieldsInfo.isSortable(fieldName))
            {
                // just a dummy comparator
                cmp = new Comparator<LuceneSearchHit>()
                {
                    public int compare(LuceneSearchHit o1, LuceneSearchHit o2)
                    {
                        return 0;
                    }
                };
            }
            tmpColumns.add(new TableColumn<LuceneSearchHit>(fieldName, cmp));
        }
        columns = tmpColumns.toArray(new TableColumn[tmpColumns.size()]);
    }

    public TableColumn<LuceneSearchHit>[] getColumns()
    {
        return columns;
    }

    public TableRowSet<LuceneSearchHit> getRowSet(TableState state, TableFilter<LuceneSearchHit>[] filters)
    {
        if(rowSet == null)
        {
            rowSet = new MultiHitsRowSet(hits, dateConverter, state, filters);
            hits = null; // make GC happy
        }
        return rowSet;
    }
}
