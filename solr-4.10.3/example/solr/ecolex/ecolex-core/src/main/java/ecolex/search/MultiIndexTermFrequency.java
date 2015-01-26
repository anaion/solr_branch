package ecolex.search;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;

import faolex.iterator.CloseableIterator;
import faolex.search.EmptyIndexNameException;
import faolex.search.FieldTerm;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexDoesNotExistException;
import faolex.search.IndexingFacility;

/**
 *
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class MultiIndexTermFrequency
{
    private IndexingFacility indexingFacility;

    public MultiIndexTermFrequency(IndexingFacility indexingFacility)
    {
        this.indexingFacility = indexingFacility;
    }

    /**
     * @return Iterator over arrays of FieldTerm. Each array element corresponds to one source index.
     */
    public CloseableIterator<MultiTermFreq> getTerms(Collection<String> indexes, String field, String start)
        throws EmptyIndexNameException, FileSeparatorInIndexNameException, IndexDoesNotExistException,
            IOException, UnsupportedCharactersInFilePathException
    {
        List<CloseableIterator<FieldTerm>> iterators = new ArrayList<CloseableIterator<FieldTerm>>();
        for (String index : indexes)
            iterators.add(indexingFacility.getTerms(index, field, start));
        return new MultiIndexTermIterator(iterators);
    }

    /**
     * @return List of arrays of FieldTerm. Each array element corresponds to one source index.
     */
    public List<MultiTermFreq> getTerms(Collection<String> indexes, String field, String start, int count)
        throws EmptyIndexNameException, FileSeparatorInIndexNameException, IndexDoesNotExistException,
            IOException, UnsupportedCharactersInFilePathException
    {
        List<MultiTermFreq> result = new ArrayList<MultiTermFreq>();
        CloseableIterator<MultiTermFreq> terms = getTerms(indexes, field, start);
        for (int i = 0; i < count; i++)
        {
            if (terms.hasNext())
                result.add(terms.next());
            else
                break;
        }
        terms.close();
        return result;
    }

    private static class MultiIndexTermIterator implements CloseableIterator<MultiTermFreq>
    {
        CloseableIterator<FieldTerm>[] iterators;
        FieldTerm[] next;

        @SuppressWarnings("unchecked")
        public MultiIndexTermIterator(Collection<CloseableIterator<FieldTerm>> iterators)
        {
            this.iterators = iterators.toArray(new CloseableIterator[0]);
            next = new FieldTerm[iterators.size()];
            for (int i = 0; i < iterators.size(); i++)
            {
                if (this.iterators[i].hasNext())
                    next[i] = this.iterators[i].next();
                else
                    next[i] = null;
            }
        }

        public boolean hasNext()
        {
            for (FieldTerm term : next)
                if (term != null)
                    return true;
            return false;
        }

        public MultiTermFreq next()
        {
            String text = null;
            int freq = 0;

            for (FieldTerm term : next)
            {
                if (term != null)
                {
                    if (term.getText().equals(text))
                        freq += term.getFreq();
                    else if (text == null || term.getText().compareTo(text) < 0)
                    {
                        text = term.getText();
                        freq = term.getFreq();
                    }
                }
            }

            MultiTermFreq terms = new MultiTermFreq(text, iterators.length);

            // progress iterators with found next value
            for (int i = 0; i < iterators.length; i++)
                if (next[i] != null && next[i].getText().equals(text))
                {
                    terms.getFreq()[i] = next[i].getFreq();
                    if (iterators[i].hasNext())
                        next[i] = iterators[i].next();
                    else
                        next[i] = null;
                }
                else
                    terms.getFreq()[i] = 0;

            return terms;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public void close() throws IOException
        {
            for (Closeable c : iterators)
                c.close();
        }
    }
}
