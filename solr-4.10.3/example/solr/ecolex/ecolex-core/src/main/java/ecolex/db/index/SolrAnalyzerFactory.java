package ecolex.db.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.solr.schema.IndexSchema;

import faolex.search.AnalyzerFactory;

/**
 * Provides the analyzer defined in a Solr schema.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class SolrAnalyzerFactory implements AnalyzerFactory
{
    private IndexSchema schema;

    public SolrAnalyzerFactory(IndexSchema schema)
    {
        this.schema = schema;
    }

    public Analyzer getAnalyzer()
    {
        return schema.getAnalyzer();
    }
}
