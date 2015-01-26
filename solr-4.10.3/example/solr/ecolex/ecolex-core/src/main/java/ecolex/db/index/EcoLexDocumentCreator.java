package ecolex.db.index;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.update.DocumentBuilder;

import ecolex.db.EcoLexDocument;
import faolex.search.DocumentCreator;

/**
 * Creates a Lucene document from an EcoLex record.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class EcoLexDocumentCreator implements DocumentCreator
{
    private IndexSchema schema;

    private Set<String> fields = new HashSet<String>();
    private Set<String> storedFields = new HashSet<String>();
    private Set<String> indexedFields = new HashSet<String>();
    private Set<String> tokenizedFields = new HashSet<String>();

    public EcoLexDocumentCreator(IndexSchema schema)
    {
        this.schema = schema;

        for (Map.Entry<String, SchemaField> field : schema.getFields().entrySet())
        {
            fields.add(field.getKey());
            if (field.getValue().indexed())
                indexedFields.add(field.getKey());
            if (field.getValue().stored())
                storedFields.add(field.getKey());
            if (field.getValue().getType().isTokenized())
                tokenizedFields.add(field.getKey());
        }
    }

    public Document create(Object o)
    {
        EcoLexDocument ecodoc = (EcoLexDocument)o;
        //System.out.println("Creating document: " + ecodoc.getFieldValues("Id"));

        DocumentBuilder builder = new DocumentBuilder(schema);
        builder.startDoc();

        for (String field : ecodoc.getFields())
            for (String value : ecodoc.getFieldValues(field))
            {
                if (schema.hasExplicitField(field))
                       builder.addField(field, value);
            }

        return builder.getDoc();
    }

    public Set<String> getFields()
    {
        return fields;
    }

    public Index getIndexed(String fieldName)
    {
        if (tokenizedFields.contains(fieldName))
            return Index.TOKENIZED;
        if (indexedFields.contains(fieldName))
            return Index.UN_TOKENIZED;
        return Index.NO;
    }

    public Store getStored(String fieldName)
    {
        return storedFields.contains(fieldName) ? Store.YES : Store.NO;
    }
}
