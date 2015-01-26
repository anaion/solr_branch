//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.jcontainer.dna.Logger;
import org.objectledge.filesystem.FileSystem;

import pl.edu.pw.ii.download.HttpResourceDownloader;
import pl.edu.pw.ii.download.HttpResponse;
import pl.edu.pw.ii.download.ResourceDownloadingException;
import ecolex.db.EcoLexDocument;
import ecolex.db.RemoveDuplicatesIterator;
import faolex.db.download.CharFixerInputStream;
import faolex.iterator.CollectionIterator;
import faolex.iterator.SizedIterator;

/**
 * Downloads single or multiple documents in XML format.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class DocumentDownloader
{
    private HttpResourceDownloader downloader;

    private Logger log;

    private FileSystem fileSystem;

    public DocumentDownloader(HttpResourceDownloader downloader, Logger logger, FileSystem fileSystem)
        {
            this.downloader = downloader;
            this.log = logger;
            this.fileSystem = fileSystem;
        }

    public SizedIterator<EcoLexDocument> getDocuments(Collection<URI> urls)
    {
        SizedIterator<EcoLexDocument> iterator =
            new MultipleURIDocumentRetriever(new CollectionIterator<URI>(urls), downloader, log, fileSystem);

        return new RemoveDuplicatesIterator(iterator, log);
    }

    public SizedIterator<EcoLexDocument> getDocuments(URI url)
    {
        return getDocuments(Arrays.asList(url));
    }

    public EcoLexDocument getDocument(URI uri)
    {
        Iterator<EcoLexDocument> iterator = getDocuments(Arrays.asList(uri));
        if (iterator.hasNext())
            return iterator.next();
        return null;
    }
    
    synchronized public String getPlainDocument(URI url) throws IOException, ResourceDownloadingException {
    	HttpResponse response = downloader.downloadAll(url);

        if (response.getStatusCode() != HttpStatus.SC_OK)
            throw new IOException("Http response: " + response.getStatusCode() + " for URL: " + url);
              
        InputStream is = new EmptyLineRemoverInputStream(response.getBodyStream());
     // Write xml to disk, then parse from disk: 
        String tempFilename = getFileName("data/eco", "lex.tmp");
        fileSystem.write(tempFilename, is);
        is = fileSystem.getInputStream(tempFilename);
        BufferedReader r = new BufferedReader(new InputStreamReader(is, "utf-8"));
		StringBuilder sb = new StringBuilder();
		while (r.ready()) {
			String s = r.readLine();
			sb.append(s);
		}
		is.close();
		fileSystem.delete(tempFilename);
		return sb.toString();
    }
    
    protected String getFileName(String prefix, String suffix)
    {
        return prefix + "_plain" + suffix;
    }
}
