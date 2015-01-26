package ecolex.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;

import ecolex.util.RemoveAccents;
import faolex.search.EmptyIndexNameException;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexDoesNotExistException;

public class TermListing
{
    private MultiIndexTermFrequency multiIndexTermFrequency;

    /**
     * Cached listings.
     */
    private Map<String, List<MultiTermFreq>> listings = Collections.synchronizedMap(new HashMap<String, List<MultiTermFreq>>());

    private static final MultiTermComparator FIELD_TERM_ARRAY_COMPARATOR = new MultiTermComparator();

    public TermListing(MultiIndexTermFrequency multiIndexTermFrequency)
    {
        this.multiIndexTermFrequency = multiIndexTermFrequency;
    }

    public List<MultiTermFreq> getListing(String[] indexNames, String listingField, Locale locale)
        throws EmptyIndexNameException, FileSeparatorInIndexNameException, IndexDoesNotExistException, IOException, UnsupportedCharactersInFilePathException
    {
        String[] sortedIndexNames = indexNames.clone(); // create a copy for sorting and generating a listing name
        Arrays.sort(sortedIndexNames);
        String listingName = listingField + ":" + locale + ":";
        for (String index : sortedIndexNames)
            listingName += index;

        List<MultiTermFreq> result = listings.get(listingName);

        if(result == null)
        {
            for (String field : getPossibleListingFields(listingField, locale))
            {
                result = multiIndexTermFrequency.getTerms(Arrays.asList(indexNames), field, "", Integer.MAX_VALUE);
                if (result != null && result.size() > 0)
                    break; // Found listing field
            }

            Collections.sort(result, FIELD_TERM_ARRAY_COMPARATOR);
            result = mergeDifferentCase(result);
            listings.put(listingName, result);
        }

        return result;
    }

    public int findPosition(String startFrom, List<MultiTermFreq> result)
    {
        MultiTermFreq fieldTerm =  new MultiTermFreq(startFrom, 0);
        int pos = Collections.binarySearch(result, fieldTerm,  FIELD_TERM_ARRAY_COMPARATOR);
        if (pos < 0)
            pos++;
        return Math.abs(pos);
    }

    public void clearCache(String index)
    {
        // TODO Clear cache only for listings from selected index
        listings.clear();
    }

    /**
     * Merges items with the same text but different case. Sums their frequency.
     */
    private List<MultiTermFreq> mergeDifferentCase(List<MultiTermFreq> list)
    {
        List<MultiTermFreq> result = new ArrayList<MultiTermFreq>(list.size());
        MultiTermFreq prevItem = null;
        for (MultiTermFreq item : list)
        {
            if (prevItem != null && prevItem.getText().toLowerCase().equals(item.getText().toLowerCase()))
                prevItem.add(item);
            else
            {
                if (prevItem != null)
                    result.add(prevItem);
                prevItem = item;
            }
        }
        if (prevItem != null)
            result.add(prevItem);
        return result;
    }

    private static class MultiTermComparator implements Comparator<MultiTermFreq>
    {
        public final int compare (MultiTermFreq pFirst, MultiTermFreq pSecond)
        {
            String firstString = RemoveAccents.removeAccents(pFirst.getText());
            String secondString = RemoveAccents.removeAccents(pSecond.getText());

            return firstString.compareToIgnoreCase(secondString);
        } // end compare
    }

    private List<String> getPossibleListingFields(String fieldName, Locale locale)
    {
        List<String> list = new ArrayList<String>();
        list.add(fieldName + "Whole_" + locale);
        list.add(fieldName + "Whole");
        list.add(fieldName);
        return list;
    }
}

