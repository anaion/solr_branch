//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.httpclient.URI;

import ecolex.db.download.DocumentDownloader;
import ecolex.db.download.URLGenerator;
import faolex.iterator.SizedIterator;

/**
 * Provides access to the treaties database entries.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class TreatiesDbAccess implements DbAccess
{
    private DocumentDownloader downloader;
    private URLGenerator urlGenerator;

    public TreatiesDbAccess(DocumentDownloader downloader, URLGenerator urlGenerator)
    {
        this.downloader = downloader;
        this.urlGenerator = urlGenerator;
    }

    public SizedIterator<EcoLexDocument> getAll() throws IOException
    {
        return downloader.getDocuments(urlGenerator.getDownloadAllTreaties());
    }

    public SizedIterator<EcoLexDocument> getModified(Date date) throws IOException
    {
        Collection<URI> urls = urlGenerator.getModifiedTreaties(date);

        // Modified treaties are also those that have modified or new parties
        Collection<URI> parties = urlGenerator.getModifiedParties(date);
        Iterator<EcoLexDocument> partiesIterator = downloader.getDocuments(parties);
        Set<String> treatyIds = new HashSet<String>();
        while (partiesIterator.hasNext())
        {
            EcoLexDocument party = partiesIterator.next();
            String treatyId = party.getFieldValue("treatyId");
            treatyIds.add(treatyId);
        }
        for (String treatyId : treatyIds)
        {
            //System.out.println("Adding treaty for party: " + treatyId);
            urls.add(urlGenerator.getDownloadTreaty(treatyId));
        }

        return downloader.getDocuments(urls);
    }

    public SizedIterator<EcoLexDocument> getNew(Date date) throws IOException
    {
        return downloader.getDocuments(urlGenerator.getNewTreaties(date));
    }

    public EcoLexDocument getDocument(String id) throws IOException
    {
        return downloader.getDocument(urlGenerator.getDownloadTreaty(id));
    }

    /**
     * Get all parties for the given treaty.
     */
    public SizedIterator<EcoLexDocument> getParties(String treatyId) throws IOException
    {
        return downloader.getDocuments(urlGenerator.getDownloadTreatyParties(treatyId));
    }
}
