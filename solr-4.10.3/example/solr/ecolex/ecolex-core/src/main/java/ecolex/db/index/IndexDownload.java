//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.index;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexOutput;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;

import pl.edu.pw.ii.download.HttpResourceDownloader;
import pl.edu.pw.ii.download.HttpResponse;
import pl.edu.pw.ii.download.ResourceDownloadingException;
import ecolex.util.ProgressReporterInputStream;
import ecolex.util.ProxyProgressReporter;
import faolex.iterator.ProgressReporter;
import faolex.search.EmptyIndexNameException;
import faolex.search.FileSeparatorInIndexNameException;
import faolex.search.IndexAlreadyExistsException;
import faolex.search.IndexDoesNotExistException;
import faolex.search.IndexingFacility;
import faolex.search.SearchingFacility;

public class IndexDownload
{
    private static final String DOWNLOAD_DIR = "data/download/";

    private FileSystem fileSystem;
    private IndexingFacility indexingFacility;
    private SearchingFacility searchingFacility;
    private HttpResourceDownloader downloader;

    public IndexDownload(FileSystem fileSystem, IndexingFacility indexingFacility, SearchingFacility searchingFacility, HttpResourceDownloader downloader)
    {
        this.fileSystem = fileSystem;
        this.indexingFacility = indexingFacility;
        this.searchingFacility = searchingFacility;
        this.downloader = downloader;
    }

    public void downloadIndex(String baseUrl, String indexName, boolean checkVersion, ProxyProgressReporter proxyReporter)
        throws IOException
    {
        downloadIndex(baseUrl, indexName, indexName, checkVersion, proxyReporter);
    }

    public void downloadIndex(String baseUrl, String remoteIndexName, String localIndexName, boolean checkVersion, ProxyProgressReporter proxyReporter)
        throws IOException
    {
        // TODO synchronize by index name - if already downloading, do not start again
        // TODO if (checkVersion)   check if remote version is different before donwloading

        // download remote index to local filesystem
        try
        {
            URI uri = new URI(baseUrl + "ledge/view/DownloadIndex?index=" + remoteIndexName, false);
            HttpResponse response = downloader.downloadAll(uri);
            if (response.getStatusCode() != HttpStatus.SC_OK)
                throw new IOException("Http response: " + response.getStatusCode() + " for URI: " + uri);
            InputStream is = response.getBodyStream();
            long size = response.getContentLength();
            is = new ProgressReporterInputStream(is, size);
            if (proxyReporter != null)
                proxyReporter.setReporter(new ProxyProgressReporter((ProgressReporter)is, 0, 0.7f));

            fileSystem.mkdirs(DOWNLOAD_DIR);

            String tmpFilename = DOWNLOAD_DIR + remoteIndexName + ".tmp";
            fileSystem.write(tmpFilename, is);

            // unzip remote index to indexName.tmp
            String tmpIndexName = localIndexName + ".tmp";

            indexingFacility.deleteIndex(tmpIndexName);
            indexingFacility.createIndex(tmpIndexName);

            Directory tmpIndexDirectory = getIndexDirectory(tmpIndexName);
            clearDirectory(tmpIndexDirectory);

            is = fileSystem.getInputStream(tmpFilename);
            is = new ProgressReporterInputStream(is, size);
            if (proxyReporter != null)
                proxyReporter.setReporter(new ProxyProgressReporter((ProgressReporter)is, 0.7f, 1));

            ZipInputStream zip = new ZipInputStream(is);
            ZipEntry entry = zip.getNextEntry();
            while (entry != null)
            {
                String name = entry.getName();
                IndexOutput output = tmpIndexDirectory.createOutput(name);
                int b = zip.read();
                while (b != -1)
                {
                    output.writeByte((byte)b);
                    b = zip.read();
                }
                output.close();
                entry = zip.getNextEntry();
            }
            zip.close();
            fileSystem.delete(tmpFilename);

            // check index consistency
            checkConsistency(tmpIndexName);

            // delete local index
            searchingFacility.clearSearcher(localIndexName); // Close searcher in order to be able to delete the index in Windows
            indexingFacility.deleteIndex(localIndexName);

            // rename downloaded index dir
            indexingFacility.renameIndex(tmpIndexName, localIndexName);
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
        catch (UnsupportedCharactersInFilePathException e)
        {
            throw new IOException(e);
        }
        catch (IndexAlreadyExistsException e)
        {
            throw new IOException(e);
        }
        catch (ResourceDownloadingException e)
        {
            if (e.getCause() instanceof SocketTimeoutException)
                throw new IOException("Transfer timed out. Please try again.");
            throw new IOException(e);
        }
    }

    private void checkConsistency(String indexName) throws EmptyIndexNameException,
        FileSeparatorInIndexNameException, IndexDoesNotExistException, IOException, UnsupportedCharactersInFilePathException
    {
        Iterator<Document> docs = indexingFacility.getDocumentIterator(indexName);
        while (docs.hasNext())
            docs.next();
    }

    /**
     * Deletes all files in the given directory.
     */
    private void clearDirectory(Directory directory) throws IOException
    {
        String[] files = directory.list();
        if (files == null)
            return;
        for (String file : files)
            directory.deleteFile(file);
    }

    private Directory getIndexDirectory(String indexName) throws IOException
    {
        try
        {
            return indexingFacility.getIndexDirectory(indexName);
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
        catch (UnsupportedCharactersInFilePathException e)
        {
            throw new IOException(e);
        }
    }
}
