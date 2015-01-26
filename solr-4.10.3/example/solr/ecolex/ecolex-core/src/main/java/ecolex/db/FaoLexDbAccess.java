//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.io.IOException;
import java.util.Date;

import ecolex.db.download.DocumentDownloader;
import faolex.db.download.URLGenerator;
import faolex.iterator.SizedIterator;

/**
 * Provides access to the FaoLex database records.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class FaoLexDbAccess implements DbAccess
{
    private DocumentDownloader downloader;
    private URLGenerator urlGenerator;

    public FaoLexDbAccess(DocumentDownloader downloader, URLGenerator urlGenerator)
    {
        this.downloader = downloader;
        this.urlGenerator = urlGenerator;
    }

    public SizedIterator<EcoLexDocument> getAll() throws IOException
    {
        return downloader.getDocuments(urlGenerator.getDownloadAllURLs());
    }

    public SizedIterator<EcoLexDocument> getModified(Date date) throws IOException
    {
        return downloader.getDocuments(urlGenerator.getDownloadModifiedURLs(date));
    }

    public SizedIterator<EcoLexDocument> getNew(Date date) throws IOException
    {
        return downloader.getDocuments(urlGenerator.getDownloadNewURLs(date));
    }

    public EcoLexDocument getDocument(String id) throws IOException
    {
        return downloader.getDocument(urlGenerator.getDownloadDocument(id));
    }
}
