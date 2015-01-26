package ecolex.db.index;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.jcontainer.dna.Logger;
import org.objectledge.filesystem.FileSystem;

import ecolex.db.DbAccess;
import ecolex.db.DocumentProcessorFactory;
import ecolex.db.EcoLexDocument;
import ecolex.db.download.DocumentDownloader;
import ecolex.db.download.URLGenerator;
import faolex.iterator.ProgressReportingIterator;
import faolex.iterator.SizedIterator;
import faolex.search.DateConverter;
import faolex.search.DocumentCreator;
import faolex.search.DocumentCreatorIterator;
import faolex.search.IndexingFacility;
import faolex.search.SearchingFacility;

/**
 * Operations on an EcoLex index of records.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemyslaw Wiech</a>
 * @version $Id$
 */
public class EcoLexIndexManager extends AbstractDocumentIndexManager
{
    private DocumentCreator documentCreator;
    private DocumentProcessorFactory[] processors;
    private DbAccess dbAccess;

    public EcoLexIndexManager(IndexingFacility indexingFacility, SearchingFacility searchingFacility, DateConverter dateConverter,
            FileSystem fileSystem, Logger log, DocumentCreator documentCreator,
            DocumentProcessorFactory[] processors, String documentIndex, DbAccess dbAccess, URLGenerator urlGenerator, DocumentDownloader docDownloader) throws IOException
    {
        super(indexingFacility, searchingFacility, dateConverter, fileSystem, documentIndex, log, urlGenerator, docDownloader);
        this.documentCreator = documentCreator;
        this.processors = processors;
        this.dbAccess = dbAccess;
    }

    @Override
    protected Iterator<Document> getAllDocuments() throws IOException
    {
        SizedIterator<EcoLexDocument> docs = dbAccess.getAll();
        docs = processDocuments(docs);
        return getDocuments(docs);
    }

    @Override
    protected Iterator<Document> getModifiedDocuments(Date lastUpdateDate) throws IOException
    {
        SizedIterator<EcoLexDocument> docs = dbAccess.getModified(lastUpdateDate);
        docs = processDocuments(docs);
        docs = new ModifiedDocumentsIterator(docs, lastUpdateDate, log);
        return getDocuments(docs);
    }

    @Override
    protected Iterator<Document> getNewDocuments(Date lastUpdateDate) throws IOException
    {
        SizedIterator<EcoLexDocument> docs = dbAccess.getNew(lastUpdateDate);
        docs = processDocuments(docs);
        docs = new NewDocumentsIterator(docs, getIndexName(), indexingFacility, log);
        return getDocuments(docs);
    }

    protected SizedIterator<EcoLexDocument> processDocuments(SizedIterator<EcoLexDocument> iterator)
    {
        SizedIterator<EcoLexDocument> elDocs = iterator;

        for (DocumentProcessorFactory factory : processors)
            elDocs = factory.createProcessor(elDocs);

        return elDocs;
    }

    protected Iterator<Document> getDocuments(SizedIterator<EcoLexDocument> iterator)
    {
        ProgressReportingIterator<EcoLexDocument> reporter =
            new ProgressReportingIterator<EcoLexDocument>(iterator,0.0f,0.5f);
        setProgressReporter(reporter);

        // save most recent date
        MostRecentDateExtractorImpl mostRecentDate = new MostRecentDateExtractorImpl(reporter);
        setMostRecentDate(mostRecentDate);

        Iterator<Document> docs = new DocumentCreatorIterator(mostRecentDate, documentCreator);
        return docs;
    }
}
