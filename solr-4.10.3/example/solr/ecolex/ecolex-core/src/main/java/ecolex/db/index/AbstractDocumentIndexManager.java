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
import org.jcontainer.dna.Logger;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;

import pl.edu.pw.ii.download.ResourceDownloadingException;
import ecolex.db.download.DocumentDownloader;
import ecolex.db.download.URLGenerator;
import faolex.iterator.CollectionIterator;
import faolex.iterator.ProgressReporter;
import faolex.iterator.ProgressReportingIterator;
import faolex.search.DateConverter;
import faolex.search.EmptyIndexNameException;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexAlreadyExistsException;
import faolex.search.IndexDoesNotExistException;
import faolex.search.IndexingFacility;
import faolex.search.SearchingFacility;

/**
 * Operations on an index of document records.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemyslaw Wiech</a>
 * @version $Id$
 */
public abstract class AbstractDocumentIndexManager implements DocumentIndexManager
{
    /**
     * Name of the documents index.
     */
    private String documentIndex;

    /**
     * Prefix for the name of the file where the last update date is stored.
     */
    public static final String LAST_UPDATE_DATE_FILENAME_PREFIX = "data/lastUpdateDate-";

    /**
     * Prefix for the name of the file where the most recent record date is stored.
     */
    public static final String MOST_RECENT_RECORD_DATE_FILENAME_PREFIX = "data/mostRecentDate-";

    /**
     * Indexing facility.
     */
    protected IndexingFacility indexingFacility;

    private SearchingFacility searchingFacility;

    /**
     * Access to the file system.
     */
    private FileSystem fileSystem;

    /**
     * Converts Date to String and vice-versa.
     */
    protected DateConverter dateConverter;

    protected Logger log;
    
    protected URLGenerator urlGenerator;

    private DocumentDownloader docDownloader;
    
    /**
     * Object reporting the progress of the current task.
     * Only one task can be active at once.
     */
    private ProgressReporter progressReporter = null;

    /**
     * Collects the most recent date from a stream of records.
     */
    private MostRecentDateExtractor mostRecentDate = null;

    /**
     * Constructs the object
     * @throws IOException
     */
    public AbstractDocumentIndexManager(IndexingFacility indexingFacility, SearchingFacility searchingFacility,
        DateConverter dateConverter, FileSystem fileSystem, String documentIndex, Logger log, URLGenerator urlGenerator, DocumentDownloader docDownloader)
        throws IOException
    {
        this.indexingFacility = indexingFacility;
        this.searchingFacility = searchingFacility;
        this.dateConverter = dateConverter;
        this.fileSystem = fileSystem;
        this.documentIndex = documentIndex;
        this.log = log;
        this.urlGenerator = urlGenerator;
        this.docDownloader = docDownloader;

        try
        {
            // Unlock the index in case it was left locked
            indexingFacility.unlock(documentIndex);
        }
        catch(EmptyIndexNameException e)
        {
            // Shouldn't happen
            log.error("", e);
        }
        catch(FileSeparatorInIndexNameException e)
        {
            // Shouldn't happen
            log.error("", e);
        }
        catch(IndexDoesNotExistException e)
        {
            // If it didn't exist, it couldn't have been locked
            //log.error("", e);
        }
        catch(UnsupportedCharactersInFilePathException e)
        {
            // Shouldn't happen
            log.error("", e);
        }
    }

    /* (non-Javadoc)
     * @see ecolex.db.index.DocumentIndexManager#reloadIndex()
     */
    synchronized public void reloadIndex()
        throws IOException, UnsupportedCharactersInFilePathException
    {
        try
        {
            log.info("Index reloading started (" + documentIndex + ")");
            progressReporter = null;
            mostRecentDate = null;
            Date lastUpdateDate = new Date();
            searchingFacility.clearSearcher(documentIndex); // Close searcher in order to be able to delete the index in Windows
            indexingFacility.deleteIndex(documentIndex);
            indexingFacility.createIndex(documentIndex);

            Iterator<Document> docs = getAllDocuments();

            indexingFacility.add(documentIndex, docs);
            saveUpdateDate(lastUpdateDate);
            log.info("Index reloading finished (" + documentIndex + ")");
            if (mostRecentDate != null)
            {
                saveMostRecentRecordDate(mostRecentDate.getMostRecentDate());
                log.info("Most recent record date: " + mostRecentDate.getMostRecentDate());
            }
        }
        catch (EmptyIndexNameException e)
        {
            // shouldn't happen
            log.error("", e);
        }
        catch (FileSeparatorInIndexNameException e)
        {
            // shouldn't happen
            log.error("", e);
        }
        catch (IndexDoesNotExistException e)
        {
            // shouldn't happen
            log.error("", e);
        }
        catch (IndexAlreadyExistsException e)
        {
            // shouldn't happen
            log.error("", e);
        }
    }

    /* (non-Javadoc)
     * @see ecolex.db.index.DocumentIndexManager#updateIndex()
     */
    synchronized public void updateIndex()
        throws IndexDoesNotExistException, IOException,
        UnsupportedCharactersInFilePathException
    {
        if (getMostRecentRecordDate() == null)
        {
            reloadIndex();
            return;
        }
        log.info("Index update started (" + documentIndex + ")");
        Date lastUpdateDate = new Date();
        Date mostRecent = null;

        retreiveNew();
        if (mostRecentDate != null)
            mostRecent = mostRecentDate.getMostRecentDate();

        retreiveModified();
        if (mostRecentDate != null)
        {
            Date d = mostRecentDate.getMostRecentDate();
            if (mostRecent == null || (d != null && d.after(mostRecent)))
                mostRecent = d;
        }

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
            new ProgressReportingIterator<String>(new CollectionIterator(ids),0.5f,0.9f); 
	        setProgressReporter(reporter);
	        while (reporter.hasNext()) {
	        	String id = reporter.next();
	        	if (i==0) beg = id.replaceAll("[^0-9]", "");
	        	if (i==10000 || n==ids.size()-1) {
	        		end = id.replaceAll("[^0-9]", "");;
	        		i=-1;
	        		String uri = "";
	        		if (getIndexName().equals("treaties")) {
	        			uri = urlGenerator.getDownloadTreaty(id).toString();
	        		} else if (getIndexName().equals("literature")) {
	        			uri = urlGenerator.getDownloadLiterature(id).toString();
	        		} else if (getIndexName().equals("courtdecisions")) {
	        			uri = urlGenerator.getDownloadCourtDecision(id).toString();
	        		}
	        		uri = uri.substring(0,uri.indexOf("query="));
	        		uri = uri+"query=ES:I AND IA:[" + beg + " TO " + end + "]&format_name=@recid&lang=xmlf&page_header=@xmlh";
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
		
        saveUpdateDate(lastUpdateDate);

        log.info("Index update finished (" + documentIndex + ")");
        if (mostRecent != null)
        {
            saveMostRecentRecordDate(mostRecent);
            log.info("Most recent record date: " + mostRecentDate.getMostRecentDate());
        }
    }

    /* (non-Javadoc)
     * @see ecolex.db.index.DocumentIndexManager#getProgress()
     */
    public float getProgress()
    {
        ProgressReporter reporter = progressReporter;
        if (reporter == null)
            return 0;
        return reporter.getProgress();
    }

    public String getIndexName()
    {
        return documentIndex;
    }

    /**
     * Updates the index with modified records.
     */
    private void retreiveModified()
        throws IndexDoesNotExistException, IOException,
        UnsupportedCharactersInFilePathException
    {
        try
        {
            log.info("Retrieving new documents (" + documentIndex + ")");
            Iterator<Document> docs = getModifiedDocuments(getMostRecentRecordDate());
            indexingFacility.replace(documentIndex, docs);
        }
        catch(EmptyIndexNameException e)
        {
            // shouldn't happen
            log.error("", e);
        }
        catch(FileSeparatorInIndexNameException e)
        {
            // shouldn't happen
            log.error("", e);
        }
    }

    /**
     * Updates the index with new records.
     */
    private void retreiveNew()
        throws IndexDoesNotExistException, IOException,
        UnsupportedCharactersInFilePathException
    {
        try
        {
            log.info("Retrieving new documents (" + documentIndex + ")");
            Iterator<Document> docs = getNewDocuments(getMostRecentRecordDate());
            indexingFacility.add(documentIndex, docs);
        }
        catch(EmptyIndexNameException e)
        {
            // shouldn't happen
            log.error("", e);
        }
        catch(FileSeparatorInIndexNameException e)
        {
            // shouldn't happen
            log.error("", e);
        }
    }

    protected abstract Iterator<Document> getAllDocuments() throws IOException;

    protected abstract Iterator<Document> getModifiedDocuments(Date lastUpdateDate) throws IOException;

    protected abstract Iterator<Document> getNewDocuments(Date lastUpdateDate) throws IOException;

    /* (non-Javadoc)
     * @see ecolex.db.index.DocumentIndexManager#getLastUpdateDate()
     */
    public Date getLastUpdateDate()
    {
        try
        {
            String date = fileSystem.read(LAST_UPDATE_DATE_FILENAME_PREFIX + getIndexName(), "UTF8");
            return dateConverter.toDate(date);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    /**
     * Saves the last update date.
     * @throws
     * @throws IOException
     */
    private void saveUpdateDate(Date date) throws IOException
    {
        fileSystem.write(LAST_UPDATE_DATE_FILENAME_PREFIX + getIndexName(), dateConverter.toString(date),
            "UTF8");
    }

    /* (non-Javadoc)
     * @see ecolex.db.index.DocumentIndexManager#getMostRecentRecordDate()
     */
    public Date getMostRecentRecordDate()
    {
        try
        {
            String date = fileSystem.read(MOST_RECENT_RECORD_DATE_FILENAME_PREFIX + getIndexName(), "UTF8");
            return dateConverter.toDate(date);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    /**
     * Saves the last update date.
     * @throws
     * @throws IOException
     */
    protected void saveMostRecentRecordDate(Date date) throws IOException
    {
        fileSystem.write(MOST_RECENT_RECORD_DATE_FILENAME_PREFIX + getIndexName(), dateConverter.toString(date),
            "UTF8");
    }

    protected void setProgressReporter(ProgressReporter progressReporter)
    {
        this.progressReporter = progressReporter;
    }

    protected void setMostRecentDate(MostRecentDateExtractor mostRecentDate)
    {
        this.mostRecentDate = mostRecentDate;
    }

}
