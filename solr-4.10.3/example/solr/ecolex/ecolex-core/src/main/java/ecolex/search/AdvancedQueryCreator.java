package ecolex.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.solr.core.SolrException;
import org.objectledge.parameters.Parameters;

import ecolex.config.ViewsConfiguration;
import faolex.search.searching.IllegalSortFieldName;
import faolex.search.searching.QueryCreationException;
import faolex.search.searching.QueryCreator;
import faolex.search.searching.QueryCreatorConfig;
import faolex.search.searching.SearchFieldsInfo;

/**
 * Query creator for advanced queries.
 *
 * @author Jakub Gawryjołek
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class AdvancedQueryCreator implements QueryCreator
{
    private static final String ALL_FIELDS_PARAMETER = "allFields";
    private static final String ASC_ORDER_PREFIX = "asc_";
    private static final String DESC_ORDER_PREFIX = "desc_";
    private static final String TRANSLATION_SUFFIX = "_";

    private static final String EXCLUDE_PREFIX = "exclude_";

    protected String query;
    protected BooleanQuery queryObject;
    protected SortField[] sortFields;

    private Parameters parameters;
    private Analyzer analyzer;
    private SearchFieldsInfo fieldsInfo;

    private boolean operatorOr;

    public AdvancedQueryCreator(QueryCreatorConfig config, Parameters parameters, Analyzer analyzer,
        SearchFieldsInfo fieldsInfo, ViewsConfiguration viewsConfiguration, Locale locale)
        throws QueryCreationException, IllegalSortFieldName
    {
        this.parameters = parameters;
        this.analyzer = analyzer;
        this.fieldsInfo = fieldsInfo;

        operatorOr = parameters.get("op", "").equals("or");

        queryObject = new BooleanQuery();

        if (parameters.isDefined(config.getQueryParamName()))
        {
            String[] excludeFields = null;
            String includeFields = parameters.get("includeFields", "");
            if (!includeFields.equals("all"))
                excludeFields = new String[] { "fullText" };

            query = parameters.get(config.getQueryParamName(), "");
            add(parseAllFieldsQuery(query, excludeFields));
        }
        else
        {
            Collection<String> textFields = getTextFields(viewsConfiguration);
            for (String field : textFields)
                if (!field.equals(ALL_FIELDS_PARAMETER))
                    add(getQuery(field));

            add(getAllFieldsQuery());
            add(getDateQuery("searchDate"));
            add(getDateQuery("entryIntoForceDate"));
            addExclude(getQuery("exclude_repealed"));

            for (String field : viewsConfiguration.getOptions("referenceFields").keySet())
                add(getQuery(field, false));

            query = queryObject.toString();
        }

        String sortFieldName = parameters.get(config.getSortFieldParamName(), config.getDefaultSortField());

        this.sortFields = createSortFields(sortFieldName, config, locale);
    }

    /**
     * {@inheritDoc}
     */
    public SortField[] getSortFields()
    {
        return sortFields;
    }

    /**
     * {@inheritDoc}
     */
    public Query getQuery()
    {
        return queryObject;
    }

    /**
     * {@inheritDoc}
     */
    public String getQueryString()
    {
        return query;
    }

    /**
     * {@inheritDoc}
     */
    public String getErrorQueryString()
    {
        return query;
    }

    // implementation /////////////////////////////////////////////////////////////////////////////

    protected void add(Query query)
    {
        if (query != null)
            queryObject.add(query, operatorOr ? Occur.SHOULD : Occur.MUST);
    }

    protected void addExclude(Query query)
    {
        if (query != null)
            queryObject.add(query, Occur.MUST_NOT);
    }

    protected void add(Query[] queries)
    {
        for (Query query : queries)
            add(query);
    }

    private Query getAllFieldsQuery() throws QueryCreationException
    {
        return parseAllFieldsQuery(parameters.get(ALL_FIELDS_PARAMETER, null));
    }

    private Query parseAllFieldsQuery(String queryStr) throws QueryCreationException
    {
        return parseAllFieldsQuery(queryStr, null);
    }

    private Query parseAllFieldsQuery(String queryStr, String[] excludeFields) throws QueryCreationException
    {
        if (queryStr == null || queryStr.length() == 0)
            return null;

        Collection<String> fields = new ArrayList<String>(Arrays.asList(fieldsInfo.getSearchableFields()));
        if (excludeFields != null)
            fields.removeAll(Arrays.asList(excludeFields));

        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields.toArray(new String[0]), analyzer);
        parser.setDefaultOperator(QueryParser.AND_OPERATOR);
        return parse(parser, queryStr);
    }

    private Query getQuery(String queryStr, String fieldName, Operator operator, boolean parse) throws QueryCreationException
    {
        if (queryStr == null || queryStr.length() == 0)
            return null;

        if (!parse)
            return new TermQuery(new Term(fieldName, queryStr));

        QueryParser parser = new QueryParser(fieldName, analyzer);
        parser.setDefaultOperator(operator);
        return parse(parser, queryStr);
    }

    private Query parse(QueryParser parser, String queryStr) throws QueryCreationException
    {
        try
        {
            try
            {
                return parser.parse(queryStr);
            }
            catch (SolrException e)
            {
                // Try removing all colons
                queryStr = queryStr.replace(':', ' ');
                return parser.parse(queryStr);
            }
        }
        catch (ParseException e)
        {
            throw new QueryCreationException();
        }
    }

    private void checkSortField(String sortFieldName, String scoreFieldValue) throws IllegalSortFieldName
    {
        if(sortFieldName != null && !sortFieldName.equals("") && !sortFieldName.equals(scoreFieldValue)
            && !fieldsInfo.isSortable(sortFieldName))
        {
            throw new IllegalSortFieldName(sortFieldName);
        }
    }

    private SortField[] createSortFields(String sortFieldNames, QueryCreatorConfig config, Locale locale) throws IllegalSortFieldName
    {
        String[] fields = sortFieldNames.split("\\+");
        SortField[] result = new SortField[fields.length];

        for (int i = 0; i < fields.length; i++)
        {
            String field = fields[i];
            String sortOrder =  config.getDefaultSortOrder();
            if (field.startsWith(ASC_ORDER_PREFIX))
            {
                sortOrder = config.getAscSortOrderValue();
                field = field.substring(ASC_ORDER_PREFIX.length());
            }
            else if (field.startsWith(DESC_ORDER_PREFIX))
            {
                sortOrder = config.getDescSortOrderValue();
                field = field.substring(DESC_ORDER_PREFIX.length());
            }

            if (field.endsWith(TRANSLATION_SUFFIX))
                field += locale.toString();

            checkSortField(field, config.getScoreSortFieldValue());
            result[i] = createSortField(field, sortOrder, config.getScoreSortFieldValue(),
                config.getDefaultSortOrder(), config.getAscSortOrderValue());
        }
        return result;
    }

    private SortField createSortField(String sortFieldName, String sortOrder,
        String scoreSortFieldValue, String defaultSortOrder, String ascSortOrderValue)
    {
        if(!sortFieldName.equals(scoreSortFieldValue))
        {
            return new SortField(sortFieldName, SortField.STRING, sortOrder.equals(defaultSortOrder));
        }
        else if(sortOrder.equals(ascSortOrderValue))
        {
            return new SortField((String)null, SortField.SCORE, true);
        }
        return SortField.FIELD_SCORE;
    }

    private Query getDateQuery(String fieldName)
    {
        String yearStart = parameters.get(fieldName + "_start", null);
        String yearEnd = parameters.get(fieldName + "_end", null);
        if (yearStart != null)
        {
            yearStart = yearStart.trim();
            if (yearStart.equals(""))
                yearStart = null;
        }
        if (yearEnd != null)
        {
            yearEnd = yearEnd.trim();
            if (yearEnd.equals(""))
                yearEnd = null;
        }

        if (yearEnd != null)
            yearEnd += "z"; // To include end year in the search

        if (yearStart == null && yearEnd == null)
            return null;

        return new ConstantScoreRangeQuery(fieldName, yearStart, yearEnd, true, true);
    }

    /**
     * Creates a query object for text fields and check boxes.
     */
    private Query getQuery(String fieldName) throws QueryCreationException
    {
        return getQuery(fieldName, true);
    }

    /**
     * Creates a query object for text fields and check boxes.
     */
    private Query getQuery(String fieldName, boolean parse) throws QueryCreationException
    {
        String[] values = parameters.getStrings(fieldName);
        if (values.length == 0)
            return null;

        String allWords = parameters.get(fieldName + "_allWords", "");
        Operator operator = Operator.AND; // default
        if (allWords.equals("anyWord"))
            operator = Operator.OR;

        if (fieldName.startsWith(EXCLUDE_PREFIX))
            fieldName = fieldName.substring(EXCLUDE_PREFIX.length());

        // Single value (text field or check box)
        if (values.length == 1)
            return getQuery(values[0], fieldName, operator, parse);

        // Multiple values (check boxes)
        BooleanQuery booleanQuery = new BooleanQuery();
        for (String value : values)
            if (!value.trim().equals(""))
                booleanQuery.add(getQuery(value, fieldName, operator, parse), Occur.SHOULD);

        if (booleanQuery.getClauses().length == 0)
            return null;

        return booleanQuery;
    }

    private Collection<String> getTextFields(ViewsConfiguration viewsConfiguration)
    {
        Set<String> fields = new TreeSet<String>();
        fields.addAll(viewsConfiguration.getOptions("treatyCriterion").keySet());
        fields.addAll(viewsConfiguration.getOptions("literatureCriterion").keySet());
        fields.addAll(viewsConfiguration.getOptions("courtDecisionsCriterion").keySet());
        fields.addAll(viewsConfiguration.getOptions("legislationCriterion").keySet());
        fields.addAll(viewsConfiguration.getOptions("commonCriterion").keySet());
        fields.addAll(viewsConfiguration.getOptions("otherSearchFields").keySet());
        return fields;
    }
}
