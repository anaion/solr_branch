//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.io.IOException;
import java.util.Date;

import ecolex.db.download.DocumentDownloader;
import ecolex.db.download.URLGenerator;
import faolex.iterator.SizedIterator;

/**
 * Provides access to literature record entries.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class LiteratureDbAccess implements DbAccess
{
    private DocumentDownloader downloader;
    private URLGenerator urlGenerator;

    public LiteratureDbAccess(DocumentDownloader downloader, URLGenerator urlGenerator)
    {
        this.downloader = downloader;
        this.urlGenerator = urlGenerator;
    }

    public SizedIterator<EcoLexDocument> getAll() throws IOException
    {
        return downloader.getDocuments(urlGenerator.getDownloadLiterature());
    }

    public SizedIterator<EcoLexDocument> getModified(Date date) throws IOException
    {
        return downloader.getDocuments(urlGenerator.getModifiedLiterature(date));
    }

    public SizedIterator<EcoLexDocument> getNew(Date date) throws IOException
    {
        return downloader.getDocuments(urlGenerator.getNewLiterature(date));
    }

    public EcoLexDocument getDocument(String id) throws IOException
    {
        return downloader.getDocument(urlGenerator.getDownloadLiterature(id));
    }
}
