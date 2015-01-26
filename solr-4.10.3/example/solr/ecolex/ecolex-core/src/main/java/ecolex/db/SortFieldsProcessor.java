//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.util.HashSet;
import java.util.Set;

import ecolex.util.RemoveAccents;
import faolex.iterator.SizedIterator;

/**
 * Converts sort fields to lowercase and removes accents.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class SortFieldsProcessor implements DocumentProcessorFactory
{
    private static final String FIELD_PREFIX = "sort";

    public SizedIterator<EcoLexDocument> createProcessor(SizedIterator<EcoLexDocument> iterator)
    {
        return new Iterator(iterator);
    }

    public static class Iterator extends ProcessingIterator<EcoLexDocument>
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
                 if (!fieldName.toLowerCase().startsWith(FIELD_PREFIX))
                     continue;

                 Set<String> values = document.getFieldValues(fieldName);
                 if (values.isEmpty())
                     continue;

                 document.removeField(fieldName);
                 StringBuilder sb = new StringBuilder();
                 for (String value : values)
                     sb.append(RemoveAccents.removeAccents(value.toLowerCase())).append(" ");
                 document.addField(fieldName, sb.toString().trim());
             }
        }
    }
}
