package ecolex.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;

import ecolex.config.ViewsConfiguration;
import ecolex.db.EcoLexDocument;
import ecolex.db.index.IndexManagerRepository;
import ecolex.util.CollectionMap;
import faolex.search.SearchingFacility;

/**
 * Retrieves references and back-links for records.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class ReferenceRetriever
{
    private static final Sort REFERENCES_SORT = new Sort(new SortField("searchDate", SortField.STRING, true));

    private Map<String, String> referenceFields = new LinkedHashMap<String, String>();

    /**
     * References not to be displayed but linked to search which would display them.
     */
    private Set<String> linkReferences;

    private Set<String> idFields;

    private SearchingFacility searchingFacility;

    private IndexManagerRepository indexManagerRepository;

    private IndexIdentifier indexIdentifier;

    public ReferenceRetriever(SearchingFacility searchingFacility, IndexManagerRepository indexManagerRepository, IndexIdentifier indexIdentifier,
            ViewsConfiguration viewsConfig)
    {
        this.searchingFacility = searchingFacility;
        this.indexManagerRepository = indexManagerRepository;
        this.indexIdentifier = indexIdentifier;

        for(Map.Entry<String, ViewsConfiguration.Option> option : viewsConfig.getOptions("referenceFields").entrySet())
            referenceFields.put(option.getKey(), option.getValue().getText());
        linkReferences = viewsConfig.getOptions("linkReferences").keySet();
        idFields = viewsConfig.getOptions("referenceIds").keySet();
    }

    public References getReferences(EcoLexDocument document, String indexName) throws IOException
    {
        Collection<String> indexNames = new LinkedList<String>(indexManagerRepository.getIndexNames());

        indexNames.remove(indexName);

        References references = new References();
        references.references.put(indexName, new CollectionMap<String, Document>());
        for (String index : indexNames)
            references.references.put(index, new CollectionMap<String, Document>());

        Searcher searcherOthers = searchingFacility.getSearcher(indexNames);
        Searcher searcherThis = searchingFacility.getSearcher(Arrays.asList(indexName));

        try
        {
        	// Back-links for enables
            Set<String> ids = getDocumentIds(document);
            for (String field : new String[] {"enabledByTreaty"})
            {
                for (String id : ids)
                {
                    Term term = new Term(field, id);
                    Hits hits = searcherThis.search(new TermQuery(term), REFERENCES_SORT);
                    for (int i = 0; i < hits.length(); i++)
                        addReference(references, referenceFields.get(field), hits.doc(i));

                    hits = searcherOthers.search(new TermQuery(term), REFERENCES_SORT);
                    references.referenceLinksCount += hits.length();
                    if (hits.length() > 0)
                        references.referenceLinks.add(term);
                }
            }
            
            // References
            for (String field : referenceFields.keySet())
            {
                Set<String> values = document.getFieldValues(field);
                if (values != null)
                {
                    getReferences(searcherThis, references, field, values);
                    getReferences(searcherOthers, references, field, values);
                }
            }

            // Back-links
            ids = getDocumentIds(document);
            for (String field : referenceFields.keySet())
            {
                for (String id : ids)
                {
                    Term term = new Term(field, id);
                    Hits hits = searcherThis.search(new TermQuery(term), REFERENCES_SORT);
                    for (int i = 0; i < hits.length(); i++)
                        addReference(references, referenceFields.get(field), hits.doc(i));

                    hits = searcherOthers.search(new TermQuery(term), REFERENCES_SORT);
                    references.referenceLinksCount += hits.length();
                    if (hits.length() > 0)
                        references.referenceLinks.add(term);
                }
            }
        }
        finally
        {
            searchingFacility.returnSearcher(searcherThis);
            searchingFacility.returnSearcher(searcherOthers);
        }

        return references;
    }

    private void getReferences(Searcher searcher, References references, String field, Set<String> values) throws IOException
    {
        /*
        BooleanQuery query = new BooleanQuery();
        for (String value : values)
            query.add(getIdQuery(value), BooleanClause.Occur.SHOULD);

        Hits hits = searcher.search(query, REFERENCES_SORT);
        for (int i = 0; i < hits.length(); i++)
            addReference(references, field, hits.doc(i));
        */
         for (String value : values)
        {
            Hits hits = searcher.search(getIdQuery(value));
            if (hits.length() > 0)
                addReference(references, field, hits.doc(0));
        }
    }

    private void addReference(References references, String field, Document doc)
    {
        String index = indexIdentifier.identify(doc);
        references.references.get(index).add(field, doc);
    }

    private Query getIdQuery(String id)
    {
        BooleanQuery query = new BooleanQuery();
        for (String field : idFields)
            query.add(new TermQuery(new Term(field, id)), BooleanClause.Occur.SHOULD);
        return query;
    }

    private Set<String> getDocumentIds(EcoLexDocument document)
    {
    	Set<String> ids = new HashSet<String>();
        for (String field : idFields)
        {
            String id = document.getFieldValue(field);
            if (id != null)
                ids.add(id);
        }
        return ids;
    }

    public static class References
    {
        public Map<String, CollectionMap<String, Document>> references = new LinkedHashMap<String, CollectionMap<String, Document>>();
        public List<Term> referenceLinks = new ArrayList<Term>();
        public int referenceLinksCount = 0;

        public Map<String, CollectionMap<String, Document>> getReferences()
        {
            return references;
        }
        public List<Term> getReferenceLinks()
        {
            return referenceLinks;
        }
        public int getReferenceLinksCount()
        {
            return referenceLinksCount;
        }
        public boolean isEmpty()
        {
            for (CollectionMap<String, Document> map : references.values())
                if (!map.isEmpty())
                    return false;
            return referenceLinksCount == 0;
        }
    }
}
