//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.index;

import java.io.IOException;

import org.jcontainer.dna.Logger;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;

import ecolex.db.EcoLexDocument;
import faolex.iterator.FilterIterator;
import faolex.iterator.SizedIterator;
import faolex.search.EmptyIndexNameException;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexDoesNotExistException;
import faolex.search.IndexingFacility;

public class NewDocumentsIterator
    extends FilterIterator<EcoLexDocument>
{
    private IndexingFacility indexingFacility;
    private Logger log;
    private String documentIndex;
    private int counter = 0;

    public NewDocumentsIterator(SizedIterator<EcoLexDocument> iterator,
        String documentIndex, IndexingFacility indexingFacility, Logger log)
    {
        super(iterator);
        this.indexingFacility = indexingFacility;
        this.documentIndex = documentIndex;
        this.log = log;
    }

    @Override
    protected boolean check(EcoLexDocument document)
    {
        counter++;
        try
        {
            String id = document.getId();
            boolean exists = indexingFacility.containsDocument(documentIndex, id);

            if (exists)
                log.debug("NOT adding " + id);
            else
                log.debug("Adding " + id);
            return !exists;
        }
        catch(IOException e)
        {
            log.error("Could not check existence of record in index", e);
            throw new RuntimeException(e);
        }
        catch (EmptyIndexNameException e)
        {
            log.error("Could not check existence of record in index", e);
            throw new RuntimeException(e);
        }
        catch (FileSeparatorInIndexNameException e)
        {
            log.error("Could not check existence of record in index", e);
            throw new RuntimeException(e);
        }
        catch (IndexDoesNotExistException e)
        {
            log.error("Could not check existence of record in index", e);
            throw new RuntimeException(e);
        }
        catch (UnsupportedCharactersInFilePathException e)
        {
            log.error("Could not check existence of record in index", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext()
    {
        boolean hasNext = super.hasNext();
        if (!hasNext)
            log.info("Added " + (counter + size() - iterator.size()) + " out of " + counter + " received documents");
        return hasNext;
    }
}
