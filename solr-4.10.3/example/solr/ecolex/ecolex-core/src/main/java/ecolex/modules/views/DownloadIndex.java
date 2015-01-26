package ecolex.modules.views;

import org.objectledge.context.Context;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.upload.FileDownload;

import faolex.search.IndexingFacility;

/**
 * Allows for downloading of the whole index in one zip file.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class DownloadIndex
    extends faolex.modules.views.DownloadIndex
{

    public DownloadIndex(Context context, IndexingFacility indexingFacility, FileDownload fileDownload, FileSystem fileSystem)
    {
        super(context, indexingFacility, fileDownload, fileSystem);
    }
}
