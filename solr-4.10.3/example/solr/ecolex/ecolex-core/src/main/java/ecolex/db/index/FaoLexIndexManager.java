//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.httpclient.URI;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.jcontainer.dna.Logger;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;

import pl.edu.pw.ii.download.ResourceDownloadingException;
import ecolex.config.URLConfigurator;
import ecolex.db.download.DocumentDownloader;
import ecolex.util.ProxyProgressReporter;
import faolex.db.FaoLexDictionary;
import faolex.iterator.CollectionIterator;
import faolex.iterator.ProgressReporter;
import faolex.iterator.ProgressReportingIterator;
import faolex.search.DateConverter;
import faolex.search.EmptyIndexNameException;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexDoesNotExistException;
import faolex.search.IndexingFacility;
import faolex.search.PatternDateConverter;

/**
 * Proxy class for the faolex index manager
 */
public class FaoLexIndexManager implements DocumentIndexManager
{
    /**
     * Name of the documents index.
     */
    public static final String DOCUMENT_INDEX = faolex.db.search.DocumentIndexManager.DOCUMENT_INDEX;
    public static final String DICTIONARY_INDEX = FaoLexDictionary.DICTIONARY_INDEX;

    private static final DateConverter DATE_CONVERTER = new PatternDateConverter("yyyyMMdd");

    private faolex.db.search.DocumentIndexManager indexManager;
    private FaoLexDictionary dictionary;
    private IndexDownload downloader;
    private URLConfigurator urlConfig;
    private Logger log;
    private IndexingFacility indexingFacility;
    private DocumentRemover documentRemover;
    private faolex.db.download.URLGenerator faolexUrlGenerator;
    private DocumentDownloader docDownloader;
    private ProgressReporter reporter = null;
    private Object reporterMutex = new Object();

    public FaoLexIndexManager(faolex.db.search.DocumentIndexManager indexManager, FaoLexDictionary dictionary,
            IndexDownload downloader, URLConfigurator urlConfig, Logger log, IndexingFacility indexingFacility,
            DocumentRemover documentRemover, faolex.db.download.URLGenerator faolexUrlGenerator, DocumentDownloader docDownloader)
    {
        this.indexManager = indexManager;
        this.dictionary = dictionary;
        this.downloader = downloader;
        this.urlConfig = urlConfig;
        this.log = log;
        this.indexingFacility = indexingFacility;
        this.documentRemover = documentRemover;
        this.faolexUrlGenerator = faolexUrlGenerator;
        this.docDownloader = docDownloader;
    }

    public Date getLastUpdateDate()
    {
        return indexManager.getLastUpdateDate();
    }

    public float getProgress()
    {
        synchronized (reporterMutex)
        {
            if (reporter != null)
                return reporter.getProgress();
        }
        return indexManager.getProgress();
    }

    synchronized public void reloadIndex() throws IOException, UnsupportedCharactersInFilePathException
    {
        Date lastUpdateDate = new Date();

        // Download whole index from Faolex home.
        log.info("Downloading FaoLex dictionary started");
        ProxyProgressReporter proxyReporter = new ProxyProgressReporter(0, 0.01f);
        setReporter(proxyReporter);
        try
        {
            downloader.downloadIndex(urlConfig.getFaolexIndexAddress(), DICTIONARY_INDEX, false, proxyReporter);
            dictionary.loadFromIndex();
            log.info("Downloading FaoLex dictionary finished");
        }
        catch (IndexDoesNotExistException e)
        {
            log.error("Downloading dictionary failed", e);
            setReporter(null);
            throw new IOException("Downloading dictionary failed", e);
        }
        catch (IOException e)
        {
            log.error("Downloading dictionary failed", e);
            setReporter(null);
            throw e;
        }

        log.info("Downloading FaoLex index started");
        proxyReporter = new ProxyProgressReporter(0.01f, 1.0f);
        setReporter(proxyReporter);
        try
        {
            downloader.downloadIndex(urlConfig.getFaolexIndexAddress(), DOCUMENT_INDEX, false, proxyReporter);
        }
        catch (IOException e)
        {
            log.error("Downloading faolex index failed", e);
            setReporter(null);
            throw e;
        }

        log.info("Downloading FaoLex index finished");


        try
        {
            // Remove unneeded records.
            log.info("Removing unneeded records");
            removeUnneeded();
            log.info("Removed unneeded records");

            // Find most recent record date
            Date mostRecentDate = findMostRecent();
            indexManager.saveMostRecentRecordDate(mostRecentDate);
        }
        catch (EmptyIndexNameException e)
        {
            throw new IOException(e);
        }
        catch (FileSeparatorInIndexNameException e)
        {
            throw new IOException(e);
        }
        catch (IndexDoesNotExistException e)
        {
            throw new IOException(e);
        }

        indexManager.saveUpdateDate(lastUpdateDate);
        setReporter(null);
    }

    /**
     * Removes documents that are not needed.
     * We should only have those that have typeOfText:
     *   - legislation
     *   - regulation
     *   - miscellaneous
     */
    private void removeUnneeded() throws EmptyIndexNameException, FileSeparatorInIndexNameException, IndexDoesNotExistException, IOException, UnsupportedCharactersInFilePathException
    {
        BooleanQuery query = new BooleanQuery();
        query.add(new MatchAllDocsQuery(), Occur.MUST);
        query.add(new TermQuery(new Term("typeOfText", "legislation")), Occur.MUST_NOT);
        query.add(new TermQuery(new Term("typeOfText", "regulation")), Occur.MUST_NOT);
        query.add(new TermQuery(new Term("typeOfText", "miscellaneous")), Occur.MUST_NOT);

        log.info(query.toString());
        documentRemover.removeDocuments(DOCUMENT_INDEX, query);
    }

    synchronized public void updateIndex() throws IndexDoesNotExistException, IOException, UnsupportedCharactersInFilePathException
    {
        if (getLastUpdateDate() == null)
        {
            reloadIndex();
            return;
        }
        ProxyProgressReporter proxyReporter = new ProxyProgressReporter(0, 0.1f);
        setReporter(proxyReporter);
        downloader.downloadIndex(urlConfig.getFaolexIndexAddress(), DICTIONARY_INDEX, true, proxyReporter);
        indexManager.updateIndex();

        Iterator<Document> it;
        log.info("Deleting outdated documents");
        List<String> delete = new LinkedList<String>();
        SortedSet<String> ids = new TreeSet<String>(new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				return a.replaceAll("[^0-9]", "").compareTo(b.replaceAll("[^0-9]", ""));
			}
        	
        }));
        SortedSet<String> remoteIds = new TreeSet<String>();
		try {
	        it = indexingFacility.getDocumentIterator(getIndexName());
	        while (it.hasNext()) {
	        	Document d = it.next();
	        	String id = d.get("id");
	        	ids.add(id);
	        }
	        int i=0,n=0;
	        String beg=null;
	        String end=null;
	        ProgressReportingIterator<String> reporter =
            new ProgressReportingIterator<String>(new CollectionIterator(ids),0.1f,0.9f); 
	        setReporter(reporter);
	        while (reporter.hasNext()) {
	        	String id = reporter.next();
	        	if (i==0) beg = id.replaceAll("[^0-9]", "");
	        	if (i==10000 || n==ids.size()-1) {
	        		end = id.replaceAll("[^0-9]", "");;
	        		i=-1;
	        		String uri = faolexUrlGenerator.getDownloadDocument(id).toString();
	        		uri = uri.substring(0,uri.indexOf("query="));
	        		uri = uri+"query=(Z:L OR Z:M OR Z:R) AND IA:[" + beg + " TO " + end + "]&format_name=@recid&lang=xmlf&page_header=xmlh";
	        		log.info("uri: "+uri);
	        		String xml = docDownloader.getPlainDocument(new URI(uri,false));
	        		int first = xml.indexOf("<recid>");
	        		int last = xml.lastIndexOf("</recid>");
	        		if (first>0 && last>0) {
		        		xml = xml.substring(first,last);
		        		xml = xml.replaceAll("</recid>", "");
		        		xml = xml.replaceAll("\\s", "");
		        		remoteIds.addAll(Arrays.asList(xml.split("<recid>")));
	        		}
	        	}
	        	i++;
	        	n++;
	        }
	        for (String id : ids) {
	        	if (!remoteIds.contains(id)) {
	        		delete.add(id);
	        	}
	        }
			indexingFacility.remove(getIndexName(), delete.iterator());
		} catch (EmptyIndexNameException e) {
			log.error(e.getMessage());
		} catch (FileSeparatorInIndexNameException e) {
			log.error(e.getMessage());
		} catch (NullPointerException e) {
			log.error(e.getMessage());
		} catch (ResourceDownloadingException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("Deleted " + delete.size() + " outdated records");
    }

    public String getIndexName()
    {
        return DOCUMENT_INDEX;
    }

    private void setReporter(ProgressReporter reporter)
    {
        synchronized (reporterMutex)
        {
            this.reporter = reporter;
        }
    }

    public Date getMostRecentRecordDate()
    {
        return indexManager.getMostRecentRecordDate();
    }

    private Date findMostRecent() throws EmptyIndexNameException, FileSeparatorInIndexNameException, IndexDoesNotExistException, IOException, UnsupportedCharactersInFilePathException
    {
        Iterator<Document> docs = indexingFacility.getDocumentIterator(DOCUMENT_INDEX);
        Date mostRecent = null;
        while (docs.hasNext())
        {
            Document doc = docs.next();
            mostRecent = newer(mostRecent, doc.get("dateOfModification"));
            mostRecent = newer(mostRecent, doc.get("dateOfEntry"));
        }
       return mostRecent;
    }

    private Date newer(Date d1, String d2)
    {
        return newer(d1, DATE_CONVERTER.toDate(d2));
    }

    private Date newer(Date d1, Date d2)
    {
        if (d1 == null)
            return d2;
        if (d2 == null)
            return d1;
        if (d1.after(d2))
            return d1;
        return d2;
    }

}
