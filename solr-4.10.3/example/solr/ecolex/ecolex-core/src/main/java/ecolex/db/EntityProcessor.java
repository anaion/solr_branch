//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.util.HashSet;
import java.util.Set;

import org.objectledge.encodings.HTMLEntityDecoder;

import faolex.iterator.SizedIterator;

/**
 * Converts dates in the document to a format specified by the dateConverter.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class EntityProcessor implements DocumentProcessorFactory
{
    private HTMLEntityDecoder htmlDecoder;

    public EntityProcessor(HTMLEntityDecoder htmlDecoder)
    {
        this.htmlDecoder = htmlDecoder;
    }

    public SizedIterator<EcoLexDocument> createProcessor(SizedIterator<EcoLexDocument> iterator)
    {
        return new Iterator(iterator);
    }

    public class Iterator extends ProcessingIterator<EcoLexDocument>
    {
        public Iterator(SizedIterator<EcoLexDocument> iterator)
        {
            super(iterator);
        }

     @Override
        public void process(EcoLexDocument document)
        {
             Set<String> fields = new HashSet<String>(document.getFields());
             for (String fieldName : fields)
             {
                 Set<String> values = document.getFieldValues(fieldName);
                 if (values.isEmpty())
                     continue;

                 document.removeField(fieldName);
                 for (String value : values)
                     document.addField(fieldName, htmlDecoder.decode(value));
             }
        }
    }
}
