//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;
import org.objectledge.table.TableException;
import org.objectledge.table.TableFilter;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableTool;

import faolex.search.DateConverter;
import faolex.search.EmptyIndexNameException;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexingFacility;
import faolex.search.SearchingFacility;
import faolex.search.searching.LuceneSearchHit;
import faolex.search.searching.SearchExecutor;
import faolex.search.searching.SearchFieldsInfo;

/**
 * SearchExecutor implementation for searching lucene indexes.
 * Search results are sorted by source index first.
 *
 * @author <a href="mailto:gajda@ii.pw.edu.pl">Damian Gajda</a>
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class MultiSearchExecutor implements SearchExecutor
{
    private SearchingFacility searchingFacility;
    private IndexingFacility indexingFacility;
    private SearchFieldsInfo fieldsInfo;
    private DateConverter dateConverter;

    public MultiSearchExecutor(SearchingFacility searchingFacility, IndexingFacility indexingFacility,
        SearchFieldsInfo fieldsInfo, DateConverter dateConverter)
    {
        this.searchingFacility = searchingFacility;
        this.indexingFacility = indexingFacility;
        this.fieldsInfo = fieldsInfo;
        this.dateConverter = dateConverter;
    }

    protected void setupTableState(TableState state)
    {
        state.setRootId(null);
        state.setTreeView(false);
        if(state.isNew())
        {
            state.setCurrentPage(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public TableTool search(Collection<String> indexNames, Query query, SortField[] sortFields,
        TableState state, List<TableFilter> tableFilters) throws TableException
    {
        if(query == null)
        {
            return null;
        }

        // setup sorting
        Sort sort = new Sort(sortFields);

        // search
        Searcher searcher = null;
        TableTool<LuceneSearchHit> tool = null;
        try
        {
            Map<String, Integer> indexHits = new LinkedHashMap<String, Integer>();
            List<Hits> hits = new ArrayList<Hits>(indexNames.size());
            int i = 0;
            for (String indexName : indexNames)
            {
                try
                {
                    if (indexingFacility.indexExists(indexName))
                    {
                        searcher = searchingFacility.getSearcher(Arrays.asList(indexName));
                        Hits h = searcher.search(query, sort);
                        hits.add(h);
                        indexHits.put(indexName, h.length());
                    }
                }
                catch (EmptyIndexNameException e)
                {
                    e.printStackTrace();
                }
                catch (FileSeparatorInIndexNameException e)
                {
                    e.printStackTrace();
                }
                catch (UnsupportedCharactersInFilePathException e)
                {
                    e.printStackTrace();
                }
            }
            if (hits.size() == 0)
                throw new TableException("Error while searching: no searchable indexes found");

            setupTableState(state);
            TableModel<LuceneSearchHit> model = new MultiHitsTableModel(hits.toArray(new Hits[0]), fieldsInfo, dateConverter);

            tool = new MultiIndexTableTool(state, tableFilters, model, indexHits);
        }
        catch(IOException e)
        {
            throw new TableException("error while searching", e);
        }
        finally
        {
            searchingFacility.returnSearcher(searcher);
        }
        return tool;
    }

    public static class MultiIndexTableTool extends TableTool
    {

        Map<String, Integer> indexHits;

        public MultiIndexTableTool(TableState state, List<TableFilter> filters, TableModel model, Map<String, Integer> indexHits)
                throws TableException
        {
            super(state, filters, model);
            this.indexHits = indexHits;
        }

        public Map<String, Integer> getIndexHits()
        {
            return indexHits;
        }
    }
}
