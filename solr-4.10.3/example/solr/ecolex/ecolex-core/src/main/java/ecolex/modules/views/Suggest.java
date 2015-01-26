package ecolex.modules.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.objectledge.context.Context;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.builders.DefaultBuilder;

import ecolex.db.index.IndexManagerRepository;
import ecolex.search.MultiTermFreq;
import ecolex.search.TermListing;
import ecolex.util.ArraysTool;
import faolex.search.EmptyIndexNameException;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexDoesNotExistException;

/**
 * Listing selection.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class Suggest
    extends DefaultBuilder
{
    private IndexManagerRepository indexManagerRepository;

    private TermListing listing;

    private static final int PAGE_SIZE = 20;

    public Suggest(Context context, IndexManagerRepository indexManagerRepository, TermListing listing)
    {
        super(context);
        this.indexManagerRepository = indexManagerRepository;
        this.listing = listing;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        RequestParameters parameters = RequestParameters.getRequestParameters(context);
        Locale locale = I18nContext.getI18nContext(context).getLocale();

        String listingField = parameters.get("listingField");
        String fieldText = parameters.get(listingField, "");


        String[] indexNames = parameters.getStrings("index");
        if (indexNames.length == 0)
            indexNames = indexManagerRepository.getIndexNames().toArray(new String[0]);

        List<MultiTermFreq> list = null;
        try
        {
            list = listing.getListing(indexNames, listingField, locale);
        }
        catch (EmptyIndexNameException e)
        {
            throw new ProcessingException(e);
        }
        catch (FileSeparatorInIndexNameException e)
        {
            throw new ProcessingException(e);
        }
        catch (IndexDoesNotExistException e)
        {
            throw new ProcessingException(e);
        }
        catch (IOException e)
        {
            throw new ProcessingException(e);
        }
        catch (UnsupportedCharactersInFilePathException e)
        {
            throw new ProcessingException(e);
        }


        String startFrom = parameters.get("startFrom", null);
       


        List<MultiTermFreq> result = new ArrayList<MultiTermFreq>();
        for (int i = 0; i < list.size() ; i++) {
        	MultiTermFreq term = (MultiTermFreq)list.get(i);
        	if (term.getText().toLowerCase().startsWith(startFrom.toLowerCase())) {
        		result.add(list.get(i));
        	}
        }
        Collections.sort(result, new Comparator<MultiTermFreq>() {

			@Override
			public int compare(MultiTermFreq t1, MultiTermFreq t2) {
				int f1=0, f2=0;
				for (int i : t1.getFreq()) {f1+=i;}
				for (int i : t2.getFreq()) {f2+=i;}
				return f2-f1;
			}
        	
        });

        result = result.subList(0, Math.min(result.size(),10));
        templatingContext.put("result", result);
        templatingContext.put("indexNames", Arrays.asList(indexNames));

        fieldText = fieldText.replaceAll("(\\s\"\\s\")+"," ");
        fieldText = fieldText.replaceAll("\"\""," ");
        fieldText = fieldText.replaceAll("\\s\\s+"," ");

        templatingContext.put("fieldText", fieldText);

        ArrayList<String> selectedOptions = new ArrayList<String>();

        String[] selected = fieldText.split("\"");

        if(selected.length == 1)
        {
            selected = fieldText.split("\\s");
        }

        for(int i = 0; i < selected.length; i++)
        {
            int index = fieldText.indexOf(selected[i]);

            if(index == 0 || index + selected[i].length() == fieldText.length() ||
                    fieldText.charAt(index - 1 ) != '"' || fieldText.charAt(index + selected[i].length()) != '"')
            {
                String [] termsSplitted = selected[i].split("\\s");

                for(int j = 0; j < termsSplitted.length; j++)
                    selectedOptions.add(termsSplitted[j].trim());
            }
            else
                   selectedOptions.add(selected[i].trim());
        }

        templatingContext.put("selectedListingOptions", selectedOptions);
        templatingContext.put("intArraysTool", new IntArraysTool());
        templatingContext.put("arraysTool", new ArraysTool());
    }

    public static class IntArraysTool
    {
        public static List<Integer> asList(int[] a) {
            List<Integer> list = new ArrayList<Integer>(a.length);
            for (int i = 0; i < a.length; i++)
                list.add(a[i]);
            return list;
        }
    }
}
