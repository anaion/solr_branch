package ecolex.search;

import java.util.Locale;

import org.objectledge.parameters.Parameters;

import ecolex.config.ViewsConfiguration;
import faolex.search.AnalyzerFactory;
import faolex.search.searching.IllegalSortFieldName;
import faolex.search.searching.QueryCreationException;
import faolex.search.searching.QueryCreator;
import faolex.search.searching.QueryCreatorConfig;
import faolex.search.searching.SearchFieldsInfo;

/**
 * Query creator factory impl.
 *
 * @author <a href="mailto:gajda@ii.pw.edu.pl">Damian Gajda</a>
 * @version $Id$
 */
public class QueryCreatorFactoryImpl implements QueryCreatorFactory
{
    private QueryCreatorConfig config;
    private AnalyzerFactory analyzerFactory;
    private SearchFieldsInfo fieldsInfo;
    private ViewsConfiguration viewsConfiguration;

    public QueryCreatorFactoryImpl(QueryCreatorConfig config, AnalyzerFactory analyzerFactory,
                SearchFieldsInfo fieldsInfo, ViewsConfiguration viewsConfiguration)
    {
        this.config = config;
        this.analyzerFactory = analyzerFactory;
        this.fieldsInfo = fieldsInfo;
        this.viewsConfiguration = viewsConfiguration;
    }

    /**
     * {@inheritDoc}
     * @throws QueryCreationException
     */
    public QueryCreator getQueryCreator(Parameters parameters, Locale locale) throws QueryCreationException
    {
        try
        {
            return new AdvancedQueryCreator(config, parameters,
                    analyzerFactory.getAnalyzer(), fieldsInfo, viewsConfiguration, locale);
        }
        catch(IllegalSortFieldName e)
        {
            throw new QueryCreationException(e);
        }
    }
}
