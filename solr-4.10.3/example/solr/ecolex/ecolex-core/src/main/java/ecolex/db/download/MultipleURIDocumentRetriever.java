//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.jcontainer.dna.Logger;
import org.objectledge.filesystem.FileSystem;
import org.xmlpull.v1.XmlPullParserException;

import pl.edu.pw.ii.download.HttpResourceDownloader;
import pl.edu.pw.ii.download.HttpResponse;
import pl.edu.pw.ii.download.ResourceDownloadingException;
import ecolex.db.EcoLexDocument;
import faolex.iterator.CollectionIterator;
import faolex.iterator.MultipleSizedIterator;
import faolex.iterator.SizedIterator;

/**
 *
 * Downloads documents from multiple URIs and allows iterating
 * over all of them.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemyslaw Wiech</a>
 * @version $Id$
 */
public class MultipleURIDocumentRetriever
    extends MultipleSizedIterator<EcoLexDocument, URI>
{
    private static int fileCounter = -1;
    /**
     * Downloader component.
     */
    private HttpResourceDownloader downloader;

    private Logger logger;

    private FileSystem fileSystem;

    private String tempFilename = null;
    private Reader tempFileReader = null;

    public MultipleURIDocumentRetriever(SizedIterator<URI> uris,
        HttpResourceDownloader downloader, Logger logger, FileSystem fileSystem)
    {
        super(uris);
        this.downloader = downloader;
        this.logger = logger;
        this.fileSystem = fileSystem;
    }

    /**
     * Try to delete temporary file when iteration is finished.
     */
    @Override
    public boolean hasNext()
    {
        boolean has = super.hasNext();
        if (!has && tempFilename != null)
            deleteTempFile();
        return has;
    }

    private void deleteTempFile()
    {
        try
        {
            if (tempFileReader != null)
                tempFileReader.close();
            fileSystem.delete(tempFilename);
        }
        catch (IOException e)
        {
            // Ignore this
        }
    }

    // Reads ahead the header of the stream (4096 chars should be ok) and gets the encoding from there.
    // Leaves the stream in the original state.
    // The header typically looks like that: <?xml version="1.0" encoding="utf-8"?>    
    private String readEncodingFromXMLHeader(InputStream is) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String header = r.readLine();
        if (header == null) return "utf-8";
        Pattern headerPattern = Pattern.compile("encoding *= *\"([a-zA-Z0-9-]+)\"");
        Matcher matcher = headerPattern.matcher(header);
        String encoding = "utf-8";
        if (matcher.find()) {            	
        	encoding = matcher.group(1).trim();
        	logger.info("Got encoding from the XML header: " + encoding);
        } else 
        	logger.info("Encoding not found in the XML header. Using UTF-8.");
        return encoding;    	
    }
    
    @Override
    protected SizedIterator<EcoLexDocument> getSource(URI uri)
    {
        try
        {
            if (tempFilename != null)
                deleteTempFile();

            HttpResponse response = downloader.downloadAll(uri);

            if (response.getStatusCode() != HttpStatus.SC_OK)
                throw new IOException("Http response: " + response.getStatusCode() + " for URL: " + uri);
            
            logger.info("Got response statusCode: " + response.getStatusCode());            
            InputStream is = new EmptyLineRemoverInputStream(response.getBodyStream());           
            
            // Write xml to disk, then parse from disk: 
            tempFilename = getFileName("data/eco", "lex.tmp");
            fileSystem.write(tempFilename, is);
            
            logger.info("Successfully written response body to file data/eco/lex.tmp");
            
            is = fileSystem.getInputStream(tempFilename);
            String encoding = readEncodingFromXMLHeader(is);
            is.close();

            is = fileSystem.getInputStream(tempFilename);

            tempFileReader = new InputStreamReader(is, encoding);
            SizedIterator<EcoLexDocument> iterator = new XmlDocumentParser(tempFileReader);
            if (!iterator.hasNext())
                logger.warn("File contains no records: " + uri);
            else if (iterator.size() > 1) // do not log one-record documents (used for displaying record details)
                logger.info("Retrieving " + iterator.size() + " records from: " + uri);
            else
                logger.info("Retrieving 1 record from: " + uri);
            return iterator;
        }
        catch(IOException e)
        {
            logger.error("Failed downloading file from uri: " + uri, e);
            return new CollectionIterator<EcoLexDocument>(new LinkedList());
        }
        catch(ResourceDownloadingException e)
        {
            logger.error("Failed downloading file from uri: " + uri, e);
            return new CollectionIterator<EcoLexDocument>(new LinkedList());
        }
        catch(XmlPullParserException e)
        {
            logger.error("Failed parsing file from uri: " + uri, e);
            return new CollectionIterator<EcoLexDocument>(new LinkedList());
        }
        catch(Throwable t)
        {
            logger.error("Error processing file from uri: " + uri, t);
            return new CollectionIterator<EcoLexDocument>(new LinkedList());
        }
    }

    protected String getFileName(String prefix, String suffix)
    {
        if (fileCounter == -1)
        {
            fileCounter = new Random().nextInt() & 0xffff;
        }

        fileCounter++;

        return prefix + Integer.toString(fileCounter) + suffix;
    }
}
