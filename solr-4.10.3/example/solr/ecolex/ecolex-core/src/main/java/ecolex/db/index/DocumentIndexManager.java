package ecolex.db.index;

import java.io.IOException;
import java.util.Date;

import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;

import faolex.search.IndexDoesNotExistException;

/**
 * Operations on an index of records.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemyslaw Wiech</a>
 * @version $Id$
 */
public interface DocumentIndexManager
{
    /**
     * Returns the name of the managed index.
     */
    public String getIndexName();

    /**
     * Creates the index from scratch.
     */
    public void reloadIndex() throws IOException, UnsupportedCharactersInFilePathException;

    /**
     * Updates the document index with new and modified records.
     */
    public void updateIndex() throws IndexDoesNotExistException, IOException, UnsupportedCharactersInFilePathException;

    /**
     * Returns the progress of the currently running task ranging from 0 to 1.
     */
    public float getProgress();

    /**
     * Gets the date of last update of the index
     * @return the date of last update of the index or null if date is unknown (never updated)
     */
    public Date getLastUpdateDate();

    /**
     * Gets the date of most recent indexed record
     * @return the date of most recent indexed record or null if date is unknown (never updated)
     */
    public Date getMostRecentRecordDate();

}
