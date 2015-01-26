//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jcontainer.dna.Logger;

import ecolex.util.DateTool;
import faolex.iterator.SizedIterator;

/**
 * Converts dates in the document to a format specified by the dateConverter.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class DatesProcessor implements DocumentProcessorFactory
{
    private static final String FIELD_KEYWORD = "date";

    private DateTool dateTool;

    private Logger log;

    public DatesProcessor(DateTool dateTool, Logger log)
    {
        this.dateTool = dateTool;
        this.log = log;
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
                 if (!fieldName.toLowerCase().contains(FIELD_KEYWORD))
                     continue;
                 Set<String> dates = document.getFieldValues(fieldName);
                 if (dates.isEmpty())
                     continue;

                 Set<String> newDates = new LinkedHashSet<String>();

                 for (String date : dates)
                 {
                     String normalized = dateTool.normalize(date);
                     if (normalized != null)
                         newDates.add(normalized);
                     else
                         log.warn("Problem parsing date: " + date);
                 }

                 document.removeField(fieldName);
                 for (String date : newDates)
                     document.addField(fieldName, date);
             }
        }
    }
}
