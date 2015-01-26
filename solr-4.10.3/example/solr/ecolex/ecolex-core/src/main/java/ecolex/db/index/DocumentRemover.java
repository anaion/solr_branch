//
// Copyright (c) 2009, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.index;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;

import faolex.search.EmptyIndexNameException;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexDoesNotExistException;
import faolex.search.IndexingFacility;
import faolex.search.SearchingFacility;

/**
 * Removes selected documents from an index that match the given query.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class DocumentRemover
{
    private SearchingFacility searchingFacility;

    private IndexingFacility indexingFacility;

    public DocumentRemover(SearchingFacility searchingFacility, IndexingFacility indexingFacility)
    {
        this.searchingFacility = searchingFacility;
        this.indexingFacility = indexingFacility;
    }

    /**
     * Removes documents from an index that match the given query.
     * @param index  target index
     * @param query  query for documents to be removed
     */
    public void removeDocuments(String index, Query query) throws IOException, EmptyIndexNameException, FileSeparatorInIndexNameException, IndexDoesNotExistException, UnsupportedCharactersInFilePathException
    {
        //System.out.println("Removing documents");
        Searcher searcher = searchingFacility.getSearcher(Arrays.asList(index));
        IndexReader reader = IndexReader.open(indexingFacility.getIndexDirectory(index));
        HitCollector deleteCollector = new DeleteCollector(reader);
        searcher.search(query, deleteCollector);
        reader.close();
        indexingFacility.optimize(index);
    }

    private static class DeleteCollector extends HitCollector
    {
        private IndexReader reader;

        public DeleteCollector(IndexReader reader)
        {
            this.reader = reader;
        }

        @Override
        public void collect(int doc, float score)
        {
            try
            {
                //System.out.println("Removing: " + doc);
                reader.deleteDocument(doc);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
