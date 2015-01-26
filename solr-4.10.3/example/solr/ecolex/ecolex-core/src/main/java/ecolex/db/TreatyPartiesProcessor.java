//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import ecolex.config.ViewsConfiguration;
import faolex.iterator.SizedIterator;
import faolex.search.DateConverter;

/**
 * Adds a country field to the document for each party and updates document's date of modification
 * based on parties' dates fo entry and modification.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class TreatyPartiesProcessor implements DocumentProcessorFactory
{
    private static final String[] PARTY_COUNTRY_FIELDS = { "country", "country_fr_FR", "country_es_ES" };
    //private static final String SORT_COUNTRY_FIELD = "sortCountry";
    private static final String DATE_MODIFIED_FIELD = "dateOfModification";
    private static final String DATE_ENTERED_FIELD = "dateOfEntry";
    private static final String DATE_LAST_ACTION_FIELD = "dateOfLastLegalAction";

    private TreatiesDbAccess dbAccess;
    private DateConverter dateConverter;
    private ViewsConfiguration viewsConfiguration;

    public TreatyPartiesProcessor(TreatiesDbAccess dbAccess, DateConverter dateConverter, ViewsConfiguration viewsConfiguration)
    {
        this.dbAccess = dbAccess;
        this.dateConverter = dateConverter;
        this.viewsConfiguration = viewsConfiguration;
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
            String treaty = document.getId();

            Date dateOfModification = document.getDate(DATE_MODIFIED_FIELD);
            Date originalDate = dateOfModification;
            Date dateOfLastLegalAction = null;

            Set<String> partyDetails = viewsConfiguration.getOptions("RecordDetails.parties").keySet();

            try
            {
//                StringBuilder sortCountry = new StringBuilder();
                java.util.Iterator<EcoLexDocument> partiesIterator = dbAccess.getParties(treaty);
                while (partiesIterator.hasNext())
                {
                    EcoLexDocument partyDocument = partiesIterator.next();

                    // Add country
                    for (String field : PARTY_COUNTRY_FIELDS)
                    {
                        String party = partyDocument.getFieldValue(field);
                        if (party != null)
                            document.addField(field, party);
                    }

                    // Update date of modification
                    Date partyEntryDate = partyDocument.getDate(DATE_ENTERED_FIELD);
                    Date partyModificationDate = partyDocument.getDate(DATE_MODIFIED_FIELD);

                    if (dateOfModification == null || (partyEntryDate != null && partyEntryDate.after(dateOfModification)))
                        dateOfModification = partyEntryDate;
                    if (dateOfModification == null || (partyModificationDate != null && partyModificationDate.after(dateOfModification)))
                        dateOfModification = partyModificationDate;

                    // Update date of last legal action
                    for (String field : partyDetails)
                    {
                    	Date date = partyDocument.getDate(field);
                    	//System.out.println(partyDocument.getFieldValue(field) + "-->" + date);
                    	if (dateOfLastLegalAction == null || (date != null && date.after(dateOfLastLegalAction)))
                    			dateOfLastLegalAction = date;
                    }
                }
//                if (sortCountry.length() > 0);
//                    document.addField(SORT_COUNTRY_FIELD, sortCountry.toString());
                if (dateOfModification != null && !dateOfModification.equals(originalDate))
                {
                    document.removeField(DATE_MODIFIED_FIELD);
                    document.addField(DATE_MODIFIED_FIELD, dateConverter.toString(dateOfModification));
                    //System.out.println("Updated date from " + originalDate + " to " + dateOfModification);
                }
                if (dateOfLastLegalAction != null)
                	document.addField(DATE_LAST_ACTION_FIELD, dateConverter.toString(dateOfLastLegalAction));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
