package ecolex.db.index;

import java.io.IOException;

import org.objectledge.context.Context;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;
import org.objectledge.pipeline.ProcessingException;

import pl.edu.pw.ii.taskmanager.LongRunningTask;
import ecolex.search.TermListing;
import faolex.search.IndexDoesNotExistException;

/**
 *
 * Type description.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class UpdateIndexTask
    implements LongRunningTask
{
    private DocumentIndexManager[] indexManagers;
    private TermListing listing;

    private String processingMessage = null;

    private int currentManager = -1;

    public UpdateIndexTask(DocumentIndexManager[] indexManagers, TermListing listing)
    {
        this.indexManagers = indexManagers;
        this.listing = listing;
    }

    public void cancel()
    {
    }

    public String getName()
    {
        return "UpdateIndex";
    }

    public float getProgress()
    {
        if (currentManager == -1)
            return 0;
        if (currentManager == -2)
            return 1;
        float part = 1f / indexManagers.length;
        int cur = currentManager;
        return part * cur +part * indexManagers[cur].getProgress();
    }

    public String getProcessingMessage()
    {
        return processingMessage;
    }

    public String getProcessingLog()
    {
        return null;
    }

    public void execute(Context context) throws ProcessingException
    {
        try
        {
            for (int i = 0; i < indexManagers.length; i++)
            {
                currentManager = i;
                processingMessage = "Updating index: " + indexManagers[i].getIndexName();
                indexManagers[i].updateIndex();
                listing.clearCache(indexManagers[i].getIndexName());
            }
            currentManager = -2;
            processingMessage = "Successful";
        }
        catch(IOException e)
        {
            throw new ProcessingException(e);
        }
        catch(UnsupportedCharactersInFilePathException e)
        {
            throw new ProcessingException(e);
        }
        catch(IndexDoesNotExistException e)
        {
            throw new ProcessingException(e);
        }
    }
}
