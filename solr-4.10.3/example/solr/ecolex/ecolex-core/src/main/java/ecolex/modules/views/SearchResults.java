package ecolex.modules.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableException;
import org.objectledge.table.TableFilter;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.builders.DefaultBuilder;

import pl.edu.pw.ii.result.ResultTool;
import pl.edu.pw.ii.result.ResultToolFactory;
import ecolex.config.ViewsConfiguration;
import ecolex.db.download.URLGenerator;
import ecolex.db.index.IndexManagerRepository;
import ecolex.search.IndexIdentifier;
import ecolex.search.MultiSearchExecutor;
import ecolex.search.MultiSearchExecutor.MultiIndexTableTool;
import ecolex.search.QueryCreatorFactory;
import ecolex.util.ArraysTool;
import ecolex.util.DateTool;
import faolex.config.SearchResultsConfigurator;
import faolex.db.FaoLexDictionary;
import faolex.search.searching.QueryCreationException;
import faolex.search.searching.QueryCreator;

/**
 * A view to present list of search results (execute the search itself).
 *
 * @author <a href="mailto:gajda@ii.pw.edu.pl">Damian Gajda</a>
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class SearchResults
    extends DefaultBuilder
{
    protected MultiSearchExecutor searchExecutor;
    protected QueryCreatorFactory queryCreatorFactory;
    protected TableStateManager tableStateManager;
    protected ResultTool result;
    protected URLGenerator ecolexUrlGenerator;
    protected faolex.db.download.URLGenerator faolexUrlGenerator;
    protected SearchResultsConfigurator configurator;
    protected IndexManagerRepository indexManagerRepository;
    protected IndexIdentifier indexIdentifier;
    protected ViewsConfiguration viewsConfig;
    protected FaoLexDictionary dictionary;
    protected Logger log;
    protected UrlGen urlGen = new UrlGen();
    protected DateTool dateTool;
    protected Set<String> queryTokens;

    public SearchResults(Context context, MultiSearchExecutor searchExecutor,
        QueryCreatorFactory queryCreatorFactory, TableStateManager tableStateManager,
        ResultToolFactory resultToolFactory, URLGenerator ecolexUrlGenerator, faolex.db.download.URLGenerator faolexUrlGenerator,
        SearchResultsConfigurator configurator, IndexManagerRepository indexManagerRepository,
        IndexIdentifier indexIdentifier, ViewsConfiguration viewsConfig, FaoLexDictionary dictionary, Logger log, DateTool dateTool)
    {
        super(context);
        this.searchExecutor = searchExecutor;
        this.queryCreatorFactory = queryCreatorFactory;
        this.tableStateManager = tableStateManager;
        this.result = resultToolFactory.getResultTool("ecolex.SearchResults.result.");
        this.ecolexUrlGenerator = ecolexUrlGenerator;
        this.faolexUrlGenerator = faolexUrlGenerator;
        this.configurator = configurator;
        this.indexManagerRepository = indexManagerRepository;
        this.indexIdentifier = indexIdentifier;
        this.viewsConfig = viewsConfig;
        this.dictionary = dictionary;
        this.log = log;
        this.dateTool = dateTool;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        RequestParameters parameters = RequestParameters.getRequestParameters(context);
        Locale locale = I18nContext.getI18nContext(context).getLocale();

        Collection<String> indexNames = getIndexNames(parameters);
        queryTokens = getQueryTokens(parameters);
        String page = parameters.get("page", null);

        try
        {
            MultiIndexTableTool table = createHitsTable(templatingContext,
					parameters, locale, indexNames, page);

            Map<String, Integer> indexHits = new LinkedHashMap<String, Integer>();

            // Parse indexHitsParam
            String[] indexHitsParam = parameters.getStrings("indexHitsParam");
            for (String param : indexHitsParam)
            {
                String[] split = param.split(":");
                indexHits.put(split[0], Integer.valueOf(split[1]));
            }

            if (indexHits.size() == 0)
                indexHits = table.getIndexHits();

            // Generate indexHitsParam (can be different from the one parsed)
            List<String> indexHitsList = new ArrayList<String>(indexHits.size());
            for (Map.Entry<String, Integer> entry : indexHits.entrySet())
                indexHitsList.add(entry.getKey() + ":" + entry.getValue());
            templatingContext.put("indexHitsParam", indexHitsList);
            templatingContext.put("params", parameters);
            templatingContext.put("hitsTable", table);
            templatingContext.put("indexHits", indexHits);
            templatingContext.put("urlGenerator", urlGen);
            templatingContext.put("indexIdentifier", indexIdentifier);
            templatingContext.put("dictionary", dictionary);
            templatingContext.put("arraysTool", new ArraysTool());
            templatingContext.put("dateTool", dateTool);
            templatingContext.put("highlighter", this);

            if (indexHits.size() == 1)
                templatingContext.put("sortOptions", viewsConfig.getOptions(indexNames.iterator().next() + "Sort", locale));
            else
                templatingContext.put("sortOptions", viewsConfig.getOptions("commonSort", locale));
       }
        catch(QueryCreationException e)
        {
            // let users fix the query
            result.errorResult("query.create", e);
        }
        catch(TableException e)
        {
            result.seriousErrorResult("search.execute", e);
        }
     }

	protected MultiIndexTableTool createHitsTable(
			TemplatingContext templatingContext, RequestParameters parameters,
			Locale locale, Collection<String> indexNames, String page)
			throws QueryCreationException, TableException {
		QueryCreator queryCreator = queryCreatorFactory.getQueryCreator(parameters, locale);
		templatingContext.put("queryCreator", queryCreator);
		TableState state = tableStateManager.getState(context, this.getClass().getCanonicalName());
		state.setPageSize(configurator.getPageSize());
		if (page == null)
		    state.setCurrentPage(1);
		else
		    state.setCurrentPage(new Integer(page));
		
		log.debug("Query: " + queryCreator.getQuery());

		List<TableFilter> filters = new ArrayList<TableFilter>();
		MultiIndexTableTool table = (MultiIndexTableTool)searchExecutor.search(indexNames,
		        queryCreator.getQuery(), queryCreator.getSortFields(),
		        state, filters);
		return table;
	}

    private Collection<String> getIndexNames(RequestParameters parameters)
    {
        String [] indexNames = parameters.getStrings("index");
        if(indexNames.length == 0)
            return indexManagerRepository.getIndexNames();
        return Arrays.asList(indexNames);
    }

    public class UrlGen
    {
        public String getEcolexLink(String url)
        {
            try
            {
                return ecolexUrlGenerator.getLinkToFullText(url).toString();
            }
            catch (URIException e)
            {
                return null;
            }
        }

        public String getFaolexLink(String url)
        {
            try
            {
                return faolexUrlGenerator.getLinkToFullText(url).toString();
            }
            catch (URIException e)
            {
                return null;
            }
        }
    }
    
    private Set<String> getQueryTokens(RequestParameters parameters) {
    	Set<String> result = new HashSet<String>();
    	for (String name : parameters.getParameterNames()) {
    		Set<String> legalFields = new TreeSet<String>();
    		legalFields.addAll(viewsConfig.getOptions("treatyCriterion").keySet());
    		legalFields.addAll(viewsConfig.getOptions("literatureCriterion").keySet());
    		legalFields.addAll(viewsConfig.getOptions("courtDecisionsCriterion").keySet());
    		legalFields.addAll(viewsConfig.getOptions("legislationCriterion").keySet());
    		legalFields.addAll(viewsConfig.getOptions("commonCriterion").keySet());
    		legalFields.addAll(viewsConfig.getOptions("otherSearchFields").keySet());
    		legalFields.add("query");
    		if (!legalFields.contains(name)) continue;

    		String[] tokens = parameters.getStrings(name);
    		for (String token : tokens) {
    			token = token.replaceAll("\\+", " ").trim();
	    		if (token!=null && token.length()>2) {
	    			if (name.equals("query") && token.startsWith("date")) continue;
	    			if (token.startsWith("\""))
	    				result.add(token.toLowerCase().trim().replaceAll("\"", ""));
	    			else {
	    				for (String t : token.split("[\\s,\\[,\\]]")) {
	    					if (t!=null && t.length()>2) {
	    						result.add(t);
	    					}
	    				}
	    			}
	    		}
    		}
    	}		
    	return result;
    }
    
    public String showTokens() {
    	String text = "tokens: ";
    	for (String token : queryTokens) {
    		text = text + " " + token;
    	}
    	return text;
    }
    public String highlight(String text) {
    	for (String token : queryTokens) {
    		Pattern pattern = Pattern.compile("(?i)([\\s]"+token+"|^"+token+")");
    		Matcher matcher = pattern.matcher(text);
    		text = matcher.replaceAll("<span style='color:red'>$1</span>");
    	}
    	return text;
    }
    
    public boolean contains(String[] values, String key) {
    	for (String v : values) {
    		if (v.equals(key)) return true;
    	}
    	return false;
    }
    
    public boolean empty(String[] values) {
    	return values==null || values.length==0;
    }
    
    public int min(int a, String b) {
    	return Math.min(a, Integer.parseInt(b));
    }
    
    public int max(int a, String b) {
    	return Math.max(a, Integer.parseInt(b));
    }
    
    public boolean inRange(String key, int min, int max) {
    	try {
    		int k = Integer.parseInt(key);
    		return k>=min && k<=max;
    	}
    	catch (Exception e) {
    		return false;
    	}
    }
}