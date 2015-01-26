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
 * Access methods to court decisions records.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class CourtDecisionsDbAccess implements DbAccess
{
    private DocumentDownloader downloader;
    private URLGenerator urlGenerator;

    public CourtDecisionsDbAccess(DocumentDownloader downloader, URLGenerator urlGenerator)
    {
        this.downloader = downloader;
        this.urlGenerator = urlGenerator;
    }

    public SizedIterator<EcoLexDocument> getAll() throws IOException
    {
        return downloader.getDocuments(urlGenerator.getDownloadCourtDecisions());
    }

    public SizedIterator<EcoLexDocument> getModified(Date date) throws IOException
    {
        return downloader.getDocuments(urlGenerator.getModifiedCourtDecisions(date));
    }

    public SizedIterator<EcoLexDocument> getNew(Date date) throws IOException
    {
        return downloader.getDocuments(urlGenerator.getNewCourtDecisions(date));
    }

    public EcoLexDocument getDocument(String id) throws IOException
    {
        return downloader.getDocument(urlGenerator.getDownloadCourtDecision(id));
    }
}
