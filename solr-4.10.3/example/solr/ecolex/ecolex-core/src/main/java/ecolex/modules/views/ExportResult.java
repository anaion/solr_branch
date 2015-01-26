package ecolex.modules.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.encodings.HTMLEntityDecoder;
import org.objectledge.encodings.HTMLEntityEncoder;
import org.objectledge.i18n.I18n;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableException;
import org.objectledge.table.TableFilter;
import org.objectledge.table.TableRow;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.HttpContext;

import pl.edu.pw.ii.result.ResultToolFactory;
import ecolex.config.ViewsConfiguration;
import ecolex.db.download.DocumentDownloader;
import ecolex.db.download.URLGenerator;
import ecolex.db.index.DbAccessRepository;
import ecolex.db.index.IndexManagerRepository;
import ecolex.search.IndexIdentifier;
import ecolex.search.MultiSearchExecutor;
import ecolex.search.MultiSearchExecutor.MultiIndexTableTool;
import ecolex.search.QueryCreatorFactory;
import ecolex.search.ReferenceRetriever;
import ecolex.util.DateTool;
import faolex.config.SearchResultsConfigurator;
import faolex.db.FaoLexDictionary;
import faolex.search.PatternDateConverter;
import faolex.search.searching.LuceneSearchHit;
import faolex.search.searching.QueryCreationException;
import faolex.search.searching.QueryCreator;

/**
 * A view to show exported items of selected search results (execute the search itself).
 *
 * @version $Id$
 */
public class ExportResult
    extends SearchResults
{
   
	private DbAccessRepository dbAccess;
	private ReferenceRetriever referenceRetriever;
	private HTMLEntityEncoder htmlEncoder;
    private HTMLEntityDecoder htmlDecoder;
    private I18n i18n;
    private RecordDetailsExport recordDetailsExport;
    private DocumentDownloader downloader;
    
    public ExportResult(Context context, MultiSearchExecutor searchExecutor,
        QueryCreatorFactory queryCreatorFactory, TableStateManager tableStateManager,
        ResultToolFactory resultToolFactory, URLGenerator ecolexUrlGenerator, faolex.db.download.URLGenerator faolexUrlGenerator,
        SearchResultsConfigurator configurator, IndexManagerRepository indexManagerRepository,
        IndexIdentifier indexIdentifier, ViewsConfiguration viewsConfig, FaoLexDictionary dictionary, Logger log, DateTool dateTool,
        DbAccessRepository dbAccess, ReferenceRetriever referenceRetriever, HTMLEntityEncoder htmlEncoder, HTMLEntityDecoder htmlDecoder, I18n i18n, DocumentDownloader downloader)
    {
        super(context, searchExecutor, queryCreatorFactory, tableStateManager, resultToolFactory, ecolexUrlGenerator, faolexUrlGenerator,
        		configurator, indexManagerRepository, indexIdentifier, viewsConfig, dictionary, log, dateTool);
        this.dbAccess = dbAccess;
        this.referenceRetriever = referenceRetriever;
        this.htmlEncoder = htmlEncoder;
        this.htmlDecoder = htmlDecoder;
        this.i18n = i18n;
        this.recordDetailsExport = new RecordDetailsExport(context, viewsConfig, dbAccess, referenceRetriever, indexIdentifier, dictionary, ecolexUrlGenerator, faolexUrlGenerator, htmlEncoder, htmlDecoder, i18n, dateTool, downloader);
    }
    
    
	@Override
	public void process(TemplatingContext templatingContext)
			throws ProcessingException {
		RequestParameters parameters = RequestParameters.getRequestParameters(context);
		super.process(templatingContext);
		if (parameters.get("exportType").equals("full") || parameters.get("exportType").equals("xml")) {
			templatingContext.put("recordDetails", recordDetailsExport);
			templatingContext.put("dateConverter", new PatternDateConverter("yyyyMMdd"));
		}
		if (parameters.get("exportType").equals("xml")) {
	        HttpContext httpContext = HttpContext.getHttpContext(context);
	        httpContext.setContentType("application/xml");
		}
	}


	private void processFull(TemplatingContext templatingContext) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String highlight(String text) {
		//no highligting on this screen
		return text;
	}
	
	@Override
	protected MultiIndexTableTool createHitsTable(
			TemplatingContext templatingContext, RequestParameters parameters,
			Locale locale, Collection<String> indexNames, String page)
			throws QueryCreationException, TableException {
		if (!parameters.isDefined("selected")) {
			QueryCreator queryCreator = queryCreatorFactory.getQueryCreator(parameters, locale);
			templatingContext.put("queryCreator", queryCreator);
			TableState state = tableStateManager.getState(context, this.getClass().getCanonicalName());
			state.setPageSize(configurator.getPageSize());
			if (page == null)
			    state.setCurrentPage(1);
			else
			    state.setCurrentPage(new Integer(page));
			
			state.setOld();
			log.debug("Query: " + queryCreator.getQuery());

			List<TableFilter> filters = new ArrayList<TableFilter>();
			MultiIndexTableTool hitsTable = (MultiIndexTableTool)searchExecutor.search(indexNames,
			        queryCreator.getQuery(), queryCreator.getSortFields(),
			        state, filters);
			
			templatingContext.put("page",page);
			List<Integer> selected = new LinkedList<Integer>();
			for (int i=0; i<hitsTable.getPageRowCount(); i++) {
				selected.add(i);
				templatingContext.put("selected", selected);
			}
			return hitsTable;
			
		}
		QueryCreator queryCreator = queryCreatorFactory.getQueryCreator(parameters, locale);
		templatingContext.put("queryCreator", queryCreator);
		TableState state = tableStateManager.getState(context, this.getClass().getCanonicalName());
		
        SortedSet<String> selectedItems = new TreeSet<String>();
        SortedSet<String> index = new TreeSet<String>();
	    for (String selected : parameters.getStrings("selected")) {
	    	String id = selected.replaceAll("\\.", "");
	    	selectedItems.add(id);
	    	index.add(indexIdentifier.identify(id));
	    }
	    state.setPageSize(0);
	    List<String> indx = new LinkedList<String>();
	    indx.addAll(index);
	    Collections.sort(indx, new Comparator<String> () {
			@Override
			public int compare(String a, String b) {
				if ("treaties".equals(a)) return -1;
				if ("treaties".equals(b)) return 1;
				if ("documents".equals(a)) return -1;
				if ("documents".equals(b)) return 1;
				if ("literature".equals(a)) return -1;
				if ("literature".equals(b)) return 1;
				if ("courtdecisions".equals(a)) return -1;
				if ("courtdecisions".equals(b)) return 1;

				return 1;
			}
	    	
	    });
		
		log.debug("Query: " + queryCreator.getQuery());

		List<TableFilter> filters = new ArrayList<TableFilter>();
		MultiIndexTableTool table = (MultiIndexTableTool)searchExecutor.search(indx,
		        queryCreator.getQuery(), queryCreator.getSortFields(),
		        state, filters);
		
		List<Integer> selected = new LinkedList<Integer>();
		for (int i=0; i<table.getRows().size(); i++) {
			String id = (String)((LuceneSearchHit)((TableRow)table.getRows().get(i)).getObject()).get("id");
			if (selectedItems.contains(id)) {
				selected.add(i);
			}	
		}
		templatingContext.put("selected", selected);
		return table;
	}
}
