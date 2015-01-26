//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.download;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import ecolex.config.URLConfigurator;

/**
 * Generates URLs for retreiving XML data from the Elis database.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class URLGenerator
{
    private static final String[] LITERATURE_SUBJECTS = {"G", "K", "D", "W", "J", "L", "E", "Z", "Y", "V", "X", "Q", "A", "C", "U"};
    //private static final String[] LITERATURE_PERIODS = {"b94", "b96", "b98", "b02", "b05", "a05"};
    private static final String[] LITERATURE_TYPES = {"ana", "mon"};

    /**
     * URL configuration.
     */
    private URLConfigurator urlConfig;

    /**
     * Constructs the object.
     */
    public URLGenerator(URLConfigurator urlConfig)
    {
        this.urlConfig = urlConfig;
    }

    public Collection<URI> getDownloadAllTreaties() throws URIException
    {
        return Arrays.asList(getQueryXmlUrl("tre", "ES:I%20AND%20STAT:c", "@xmltrenop"));
    }

    public Collection<URI> getDownloadCourtDecisions() throws URIException
    {
        return Arrays.asList(getQueryXmlUrl("cou", "ES:I%20AND%20STAT:c", "@xmlcou"));
    }

    public Collection<URI> getDownloadLiterature() throws URIException
    {
        Collection<URI> list = new ArrayList<URI>();
        //for (String period : LITERATURE_PERIODS)
        for (String subject : LITERATURE_SUBJECTS)
            for (String type : LITERATURE_TYPES)
                //list.add(getQueryXmlUrl("libcat", "ES:I%20AND%20STAT:c%20AND%20T:" + type + "%20AND%20B12:" + subject + "%20AND%20PRD:" + period, "@xmllit"));
            	list.add(getQueryXmlUrl("libcat", "ES:I%20AND%20STAT:c%20AND%20T:" + type + "%20AND%20B12:" + subject, "@xmllit"));
        return list;
    }


    public Collection<URI> getNewTreaties(Date date) throws URIException
    {
        return getDateURLs("tre", "ES:I%20AND%20STAT:c%20AND%20DE:", date, "@xmltrenop");
    }

    public Collection<URI> getNewCourtDecisions(Date date) throws URIException
    {
        return getDateURLs("cou", "ES:I%20AND%20STAT:c%20AND%20DE:", date, "@xmlcou");
    }

    public Collection<URI> getNewLiterature(Date date) throws URIException
    {
        return getDateURLs("libcat", "ES:I%20AND%20STAT:c%20AND%20DE:", date, "@xmllit");
    }


    public Collection<URI> getModifiedTreaties(Date date) throws URIException
    {
        return getDateURLs("tre", "ES:I%20AND%20STAT:c%20AND%20DM:", date, "@xmltrenop");
    }

    public Collection<URI> getModifiedCourtDecisions(Date date) throws URIException
    {
        return getDateURLs("cou", "ES:I%20AND%20STAT:c%20AND%20DM:", date, "@xmlcou");
    }

    public Collection<URI> getModifiedLiterature(Date date) throws URIException
    {
        return getDateURLs("libcat", "ES:I%20AND%20STAT:c%20AND%20DM:", date, "@xmllit");
    }

    /**
     * Returns new or modified parties.
     */
    public Collection<URI> getModifiedParties(Date date) throws URIException
    {
        Collection<URI> uris = getDateURLs("tre", "de:", date, "@xmlpardoc");
        uris.addAll(getDateURLs("tre", "DM:", date, "@xmlpardoc"));
        return uris;
    }


    public URI getDownloadTreaty(String treatyId) throws URIException
    {
        return getQueryXmlUrl("tre", "ES:I%20AND%20STAT:c%20AND%20ID:" + treatyId, "@xmltrenop");
    }
    
    public URI getDownloadTreatyWithParties(String treatyId) throws URIException
    {
        return getQueryXmlUrl("tre", "ES:I%20AND%20STAT:c%20AND%20ID:" + treatyId, "@xmltre");
    }

    public URI getDownloadCourtDecision(String courtDecisionId) throws URIException
    {
        return getQueryXmlUrl("cou", "ES:I%20AND%20STAT:c%20AND%20ID:" + courtDecisionId, "@xmlcou");
    }

    public URI getDownloadLiterature(String literatureId) throws URIException
    {
        return getQueryXmlUrl("libcat", "ES:I%20AND%20STAT:c%20AND%20ID:" + literatureId, "@xmllit");
    }

    public Collection<URI> getDownloadTreatyParties(String treaty) throws URIException
    {
        return Arrays.asList(getQueryXmlUrl("tre", "ES:I%20AND%20STAT:c%20AND%20ID:" + treaty, "@xmlpardoc"));
    }

    public URI getQueryXmlUrl(String database, String query, String format) throws URIException
    {
        return getXmlURL(database, query, format, "query");
    }

    public URI getLinkXmlUrl(String database, String query, String format) throws URIException
    {
        return getXmlURL(database, query, format, "link");
    }

    public Collection<URI> getDateURLs(String database, String query, Date date, String format)
        throws URIException
    {
        Collection<URI> list = new ArrayList<URI>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Calendar now = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyyMM");

        while (calendar.compareTo(now) <= 0)
        {
            list.add(getQueryXmlUrl(database, query + df.format(calendar.getTime()) + "*", format));
            calendar.add(Calendar.MONTH, 1);
        }
        return list;
    }

    public URI getLinkToFullText(String fileName, String fieldName) throws URIException
    {
        String url = urlConfig.getDocumentBaseAddress() + urlConfig.getLinkPath(fieldName) + fileName;
        return new URI(url, true);
    }

    public URI getLinkToFullText(String fileName) throws URIException
    {
        return getLinkToFullText(fileName, "");
    }


    private URI getXmlURL(String database, String query, String format, String searchType)
        throws URIException
    {
        return getXmlURL(database, query, format, "xmlf", "@xmlh", searchType);
    }

    private URI getXmlURL(String database, String query, String format, String lang, String pageHeader, String searchType)
        throws URIException
    {
    	String table = "allt";
    	if (database.equals("cou")) table = "allc";
    	if (database.equals("libcat")) table = "alll";
    	if (format.equals("@xmlpardoc")) pageHeader = "@xmlhdoc";
        return getCgiURL(urlConfig.getLocalBaseAddress(), database, query, format, lang, table,
            "&page_header="+ pageHeader+"&de_query_id=1", searchType);
    }

    private URI getCgiURL(String baseAddress, String database, String query,
        String format, String lang, String table, String other, String searchType)
        throws URIException
    {
        String url = null;
        if(searchType.equals("query"))
            url = baseAddress + "?database=" + database + "&search_type=" + searchType
            + "&table=" + table + "&query=" + query
            + "&format_name=" + format + "&lang=" + lang + other;
        else
            url = baseAddress + "?database=" + database + "&search_type=" + searchType
            + "&table=" + table + "&rec_id=" + query
            + "&format_name=" + format + "&lang=" + lang + other;

        return new URI(url, true);
    }
}
