package ecolex.modules.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class Listing
    extends DefaultBuilder
{
    private IndexManagerRepository indexManagerRepository;

    private TermListing listing;

    private static final int PAGE_SIZE = 20;

    public Listing(Context context, IndexManagerRepository indexManagerRepository, TermListing listing)
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

        // save original text for the "cancel" button
        String originalText = parameters.get("original", null);
        if (originalText == null)
            originalText = fieldText;
        templatingContext.put("original", originalText);

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

        int page = Integer.valueOf(parameters.get("page", "0"));
        int offset = Integer.valueOf(parameters.get("offset", "0"));

        String startFrom = parameters.get("startFrom", null);
        if (startFrom != null)
        {
            startFrom = startFrom.trim();
            if (!startFrom.equals(""))
            {
                int pos = listing.findPosition(startFrom, list);
                page = pos / PAGE_SIZE;
                offset = pos % PAGE_SIZE;
            }
            else
            {
                page = 0;
                offset = 0;
            }
        }
        int startingIndex = page * PAGE_SIZE + offset;

        if (startingIndex < 0)
        {
            page = 0;
            offset = 0;
            startingIndex = 0;
        }

        if (startingIndex + PAGE_SIZE < list.size())
            templatingContext.put("nextPage", String.valueOf(page + 1));
        if (startingIndex > 0)
            templatingContext.put("prevPage", String.valueOf(page - 1));

        List<MultiTermFreq> result = new ArrayList<MultiTermFreq>();
        for (int i = startingIndex; i < list.size() && i < startingIndex + PAGE_SIZE; i++)
            result.add(list.get(i));

        templatingContext.put("result", result);
        templatingContext.put("page", page);
        templatingContext.put("offset", offset);
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
