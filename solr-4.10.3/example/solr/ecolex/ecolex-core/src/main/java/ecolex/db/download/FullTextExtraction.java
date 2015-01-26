//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.download;

import java.io.IOException;
import java.net.FileNameMap;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.jcontainer.dna.Configuration;
import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.Logger;

import pl.edu.pw.ii.download.HttpResourceDownloader;
import pl.edu.pw.ii.download.HttpResponse;
import pl.edu.pw.ii.download.ResourceDownloadingException;
import pl.edu.pw.ii.extraction.DocumentParser;
import pl.edu.pw.ii.extraction.ParseException;
import ecolex.db.DocumentProcessorFactory;
import ecolex.db.EcoLexDocument;
import ecolex.db.ProcessingIterator;
import faolex.iterator.SizedIterator;

/**
 * Extracts text from full text documents attached to a EcoLex record.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class FullTextExtraction implements DocumentProcessorFactory
{
    private Logger log;

    /**
     * Downloader component.
     */
    private HttpResourceDownloader downloader;

    /**
     * Generates URLs to full documents
     */
    private URLGenerator urlGenerator;

    /**
     * Maps file names to mime types.
     */
    private FileNameMap fileNameMap;

    /**
     * Maps link field to target field.
     * The first is the field where the link is stored.
     * The second is the field that will be filled with the document contents.
     */
    private Map<String, String> fields = new TreeMap<String, String>();

    /**
     * Parses documents in various formats.
     */
    private DocumentParser parser;

    public FullTextExtraction(Configuration config, Logger logger, HttpResourceDownloader downloader,
        URLGenerator urlGenerator, FileNameMap fileNameMap, DocumentParser parser) throws ConfigurationException
    {
        this.log = logger;
        this.downloader = downloader;
        this.urlGenerator = urlGenerator;
        this.fileNameMap = fileNameMap;
        this.parser = parser;

        for (Configuration fieldConfig : config.getChildren("field"))
            fields.put(fieldConfig.getChild("linkField").getValue(), fieldConfig.getChild("targetField").getValue());
    }

    /**
     * Extracts text for documents in the given iterator.
     */
    public SizedIterator<EcoLexDocument> createProcessor(SizedIterator<EcoLexDocument> iterator)
    {
        return new Iterator(iterator);
    }

    public String getText(URI uri, String contentType)
    {
        HttpResponse response = null;
        try
        {
            response = downloader.downloadAll(uri);
            if (response.getStatusCode() != HttpStatus.SC_OK)
            {
                log.error("Wrong HTTP response: " + response.getStatusCode() + " for file:" + uri);
            }
            else
            {
                String plainText = parser.parse(response.getBodyStream(), contentType);
                response.close();
                log.info("Downloaded file: " + uri );
                return plainText;
            }
        }
        catch (OutOfMemoryError e)
        {
            log.error("Out of memory while parsing file: "+uri +". Continuing anyway.", e);
        }
        catch (IOException e)
        {
            log.error("Problem getting/parsing the file: "+uri, e);
        }
        catch(ParseException e)
        {
            log.error("Problem parsing the file: "+uri, e);
        }
        catch(ResourceDownloadingException e)
        {
            log.error("Problem getting the file: "+uri, e);
        }
        try
        {
        	if (response != null)
        		response.close();
        }
        catch (IOException e)
        {
            log.error("Problem closing response object", e);
        }
        return null;
    }

    /**
     * Extracts text from full text documents attached to this document.
     */
    public void extractText(EcoLexDocument document)
    {
        for (Map.Entry<String, String> fieldInfo : fields.entrySet())
        {
            Collection<String> fileNames = document.getFieldValues(fieldInfo.getKey());
            if (fileNames == null)
                continue;
            for (String fileName : fileNames)
            {
                String type = fileNameMap.getContentTypeFor(fileName);
                if (parser.supports(type))
                {
                    try
                    {
                        URI uri = urlGenerator.getLinkToFullText(fileName, fieldInfo.getKey());
                        String fullText = getText(uri, type);
                        if (fullText != null)
                            document.addField(fieldInfo.getValue(), fullText);
                    }
                    catch (URIException e)
                    {
                        log.error("Bad URL for file: "+fileName, e);
                    }
                }
                else
                {
                    log.error("File type not supported: " + type + " for file: " + fileName);
                }
            }
        }
    }

    /**
     * Iterator calling extractText() for all documents.
     */
    protected class Iterator extends ProcessingIterator<EcoLexDocument>
    {
        public Iterator(SizedIterator<EcoLexDocument> iterator)
        {
            super(iterator);
        }

        @Override
        protected void process(EcoLexDocument document)
        {
            extractText(document);
        }
    }
}
