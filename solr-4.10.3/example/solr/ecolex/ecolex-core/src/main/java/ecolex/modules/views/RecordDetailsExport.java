package ecolex.modules.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.httpclient.URIException;
import org.objectledge.context.Context;
import org.objectledge.encodings.HTMLEntityDecoder;
import org.objectledge.encodings.HTMLEntityEncoder;
import org.objectledge.i18n.I18n;
import org.objectledge.i18n.I18nContext;
import org.objectledge.pipeline.ProcessingException;

import pl.edu.pw.ii.download.ResourceDownloadingException;
import ecolex.config.ViewsConfiguration;
import ecolex.config.ViewsConfiguration.Option;
import ecolex.db.EcoLexDocument;
import ecolex.db.TreatiesDbAccess;
import ecolex.db.download.DocumentDownloader;
import ecolex.db.download.URLGenerator;
import ecolex.db.index.DbAccessRepository;
import ecolex.search.IndexIdentifier;
import ecolex.search.ReferenceRetriever;
import ecolex.search.ReferenceRetriever.References;
import ecolex.util.DateTool;
import ecolex.util.RemoveAccents;
import faolex.db.FaoLexDictionary;
import faolex.iterator.SizedIterator;
import faolex.search.searching.LuceneSearchHit;

/**
 * Chart selection.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class RecordDetailsExport
{
    private static final StringComparator STRING_COMPARATOR = new StringComparator();
    private static final MapComparator<String, String> PARTY_COMPARATOR = new MapComparator<String, String>("country", STRING_COMPARATOR);

    private ViewsConfiguration viewsConfig;
    private DbAccessRepository dbAccess;
    private ReferenceRetriever referenceRetriever;
    private IndexIdentifier indexIdentifier;
    private FaoLexDictionary dictionary;
    private URLGenerator ecolexUrlGenerator;
    private faolex.db.download.URLGenerator faolexUrlGenerator;
    private HTMLEntityEncoder htmlEncoder;
    private HTMLEntityDecoder htmlDecoder;
    private I18n i18n;
    private DateTool dateTool;
    private Context context;
    private DocumentDownloader downloader;

    public RecordDetailsExport(Context context, ViewsConfiguration viewsConfig, DbAccessRepository dbAccess,
            ReferenceRetriever referenceRetriever, IndexIdentifier indexIdentifier, FaoLexDictionary dictionary,
            URLGenerator ecolexUrlGenerator, faolex.db.download.URLGenerator faolexUrlGenerator,
            HTMLEntityEncoder htmlEncoder, HTMLEntityDecoder htmlDecoder, I18n i18n, DateTool dateTool, DocumentDownloader downloader)
    {
        this.viewsConfig = viewsConfig;
        this.dbAccess = dbAccess;
        this.referenceRetriever = referenceRetriever;
        this.indexIdentifier = indexIdentifier;
        this.dictionary = dictionary;
        this.ecolexUrlGenerator = ecolexUrlGenerator;
        this.faolexUrlGenerator = faolexUrlGenerator;
        this.htmlEncoder = htmlEncoder;
        this.htmlDecoder = htmlDecoder;
        this.i18n = i18n;
        this.dateTool = dateTool;
        this.context = context;
        this.downloader = downloader;
    }

    public Map<String, String> getFields(LuceneSearchHit hit) throws ProcessingException {
    	String index;
    	Locale locale = I18nContext.getI18nContext(context).getLocale(); 	

		index = indexIdentifier.identify(hit.getDocument());
    	Map<String, Option> recordDetailMap = null;
        Map<String, Option> partyDetailMap = null;

        recordDetailMap = viewsConfig.getOptions("RecordDetails." + index);
        if (recordDetailMap == null)
            throw new ProcessingException("Unknown record type");
        
        EcoLexDocument document = null;
        try
        {
            document = dbAccess.getDbAccess(index).getDocument(hit.getDocument().get("id"));

            if(index.equals("treaties"))
            {
                //SizedIterator<EcoLexDocument> partiesIterator = ((TreatiesDbAccess)dbAccess.getDbAccess(index)).getParties(hit.getDocument().get("id"));
                partyDetailMap = viewsConfig.getOptions("RecordDetails.parties");
            }
        }
        catch (IOException e)
        {
        	throw new ProcessingException(e);
        }

        if (document == null) return null;
        process(document, recordDetailMap, locale);

        Map<String, String> fieldMap = new LinkedHashMap<String, String>();

        for (Map.Entry<String, Option> entry : recordDetailMap.entrySet())
        {
            String valueString = "";
            Set<String> values =  getValues(document, entry.getKey(), locale);

            if(values != null)
            {
                for(String value : values)
                    valueString += value + "; ";

                valueString = valueString.substring(0,valueString.lastIndexOf(";"));

                fieldMap.put(entry.getValue().getText(), valueString);
            }
        }
        
    	return fieldMap;
    }
    
    public Collection getPartyTableHeaders() {
    	return viewsConfig.getOptions("RecordDetails.parties").values();
    }
    
    public Set<Map<String, String>> getParties(LuceneSearchHit hit) throws ProcessingException {
    	Locale locale = I18nContext.getI18nContext(context).getLocale();
    	
    	Map<String, Option> partyDetailMap = viewsConfig.getOptions("RecordDetails.parties");
    	Set<Map<String, String>> partiesValues = new TreeSet<Map<String, String>>(PARTY_COMPARATOR);

    	String index = indexIdentifier.identify(hit.getDocument());
    	String id = hit.getDocument().get("id");
    	
    	SizedIterator<EcoLexDocument> partiesIterator;
		try {
			partiesIterator = ((TreatiesDbAccess)dbAccess.getDbAccess(index)).getParties(id);
		} catch (IOException e) {
			throw new ProcessingException(e);
		}
        while(partiesIterator.hasNext())
        {
            EcoLexDocument party = partiesIterator.next();
            Map<String, String> partyValues = new LinkedHashMap<String, String>();

            for(String field : partyDetailMap.keySet())
            {
                String valueString = getValue(party, field, locale);
                if ("0000-00-00".equals(valueString))
                    valueString = null;
                partyValues.put(field, valueString);
            }

            partiesValues.add(partyValues);
        }
    	return partiesValues;
    }
    
    public References getReferences(LuceneSearchHit hit) throws ProcessingException {
    	 try
         {
    		 String index = indexIdentifier.identify(hit.getDocument());
    		 String id = hit.getDocument().get("id");
    		 EcoLexDocument document = dbAccess.getDbAccess(index).getDocument(id);
    		 if (document==null) return null;
             return referenceRetriever.getReferences(document, index);
         }
         catch (IOException e)
         {
             throw new ProcessingException(e);
         }
    }
   
    public String getXML(LuceneSearchHit hit) throws ProcessingException {
    	String index = indexIdentifier.identify(hit.getDocument());
		String id = hit.getDocument().get("id");
		org.apache.commons.httpclient.URI uri = null;
		try {
			if (index.equals("treaties")) {	
				uri = ecolexUrlGenerator.getQueryXmlUrl("tre","ES:I%20AND%20STAT:c%20AND%20ID:" + id, "@xmlexp");	
			}
			if (index.equals("literature")) {
				uri = ecolexUrlGenerator.getQueryXmlUrl("libcat","ES:I%20AND%20STAT:c%20AND%20ID:" + id, "@xmlexp");
			}
			if (index.equals("courtdecisions")) {
				uri = ecolexUrlGenerator.getQueryXmlUrl("cou","ES:I%20AND%20STAT:c%20AND%20ID:" + id, "@xmlexp");
			}
			if (index.equals("documents")) {
				uri = faolexUrlGenerator.getDownloadDocument(id);
			}
			//uri = new org.apache.commons.httpclient.URI(uri.getURI().replace("&page_header=xmlh",""));
			String xml = downloader.getPlainDocument(uri);
			if (xml.contains("<document>")) {
				return  xml.substring(xml.indexOf("<document>"), xml.lastIndexOf("</document>")+"</document>".length());
			} else return "";
			
		} catch (URIException e) {
			throw new ProcessingException(e);
		} catch (IOException e) {
			throw new ProcessingException(e);
		} catch (ResourceDownloadingException e) {
			throw new ProcessingException(e);
		}
		
    }
    
    private Set<String> getValues(EcoLexDocument document, String key, Locale locale)
    {
    	if (document == null) return new TreeSet<String>();
        Set<String> result = document.getFieldValues(key + "_" + locale);
        if (result == null)
            result = document.getFieldValues(key);
        return result;
    }

    private String getValue(EcoLexDocument document, String key, Locale locale)
    {
    	if (document==null) return null;
        Set<String> set = getValues(document, key, locale);
        if (set == null)
            return null;
        return set.iterator().next();
    }



    private static final Map<String, String> LINK_LANGUAGES = new TreeMap<String, String>();
    static
    {
        LINK_LANGUAGES.put("linkToFullText", "English");
        LINK_LANGUAGES.put("linkToFullTextFr", "French");
        LINK_LANGUAGES.put("linkToFullTextSp", "Spanish");
        LINK_LANGUAGES.put("linkToFullTextOther", "Other");
    }
    private static final String LINKS_TO_FULL_TEXT_FIELD = "linksToFullText";

    private void process(EcoLexDocument document, Map<String, Option> detailsMap, Locale locale)
    {
    	if (document == null) return;
        EcoLexDocument newDoc = new EcoLexDocument();
        for (String field : document.getFields())
        {
            if (LINK_LANGUAGES.containsKey(field))
            {
                for (String value : document.getFieldValues(field))
                {
                    try
                    {
                        String link = ecolexUrlGenerator.getLinkToFullText(value, field).toString();
                        if(value.lastIndexOf('/')>=0)
                    		value = value.substring(value.lastIndexOf('/')).substring(1);
                        newDoc.addField(LINKS_TO_FULL_TEXT_FIELD, link(link, value + " (" + i18n.get(locale, "ecolex.views.strings." + LINK_LANGUAGES.get(field)) + ")"));
                    }
                    catch (URIException e)
                    {
                        // ignore this
                        e.printStackTrace();
                    }
                }
            }

            if (!detailsMap.containsKey(field))
                continue;

            if ("link".equals(detailsMap.get(field).getType()))
            {
                for (String value : document.getFieldValues(field))
                {
                    String link = value;
                    if (!link.startsWith("http://"))
                        link = "http://" + link;
                    if (value.length() > 80)
                        value = value.substring(0, 78) + "...";
                    newDoc.addField(field, link(link, value));
                }
            }
            else if ("faolexLink".equals(detailsMap.get(field).getType()))
            {
                for (String value : document.getFieldValues(field))
                {
                    try
                    {
                        String link = faolexUrlGenerator.getLinkToFullText(value).toString();
                        newDoc.addField(field, link(link, value));
                    }
                    catch (URIException e)
                    {
                        // Ignore this
                        e.printStackTrace();
                    }
                }
            }
            else if ("ecolexLink".equals(detailsMap.get(field).getType()))
            {
                for (String value : document.getFieldValues(field))
                {
                    try
                    {
                        String link = ecolexUrlGenerator.getLinkToFullText(value, field).toString();
                        newDoc.addField(field, link(link, value));
                    }
                    catch (URIException e)
                    {
                        // Ignore this
                        e.printStackTrace();
                    }
                }
            }
            else if ("dictionary".equals(detailsMap.get(field).getType()))
            {
                for (String value : document.getFieldValues(field))
                    newDoc.addField(field, encode(dictionary.getTerm(value, locale)));
            }
            else if ("date".equals(detailsMap.get(field).getType()))
            {
                newDoc.addField(field, null); // This will remove any illegal date values from the output
                for (String value : document.getFieldValues(field))
                {

                    String normalized = dateTool.normalize(value);
                    if (normalized != null)
                        value = dateTool.format(normalized, locale);
                    newDoc.addField(field, value);
                }
            }
            else if ("year".equals(detailsMap.get(field).getType()))
            {
                for (String value : document.getFieldValues(field))
                    newDoc.addField(field, value.substring(0, 4));
            }
            else if("breaks".equals(detailsMap.get(field).getType()))
            {
                StringBuilder sb = new StringBuilder();
                for (String value : document.getFieldValues(field))
                    sb.append(value).append("<br/><br/>");
//                    sb.append("<p>").append(value).append("</p>");
                newDoc.addField(field, sb.substring(0, sb.length() - "<br/><br/>".length()));
//                newDoc.addField(field, sb.toString());
            }
            else
            {
                for (String value : document.getFieldValues(field))
                    newDoc.addField(field, encode(htmlDecoder.decode(value)));
            }
        }

        // Merge new fields to document
        for (String field : newDoc.getFields())
        {
            document.removeField(field);
            for (String value : newDoc.getFieldValues(field))
                document.addField(field, value);
        }
    }

    private String encode(String text)
    {
        return htmlEncoder.encodeAttribute(text, "UTF-8");
    }

    private String link(String link, String text)
    {
        return "<a href=\"" + encode(link) + "\" target=\"_blank\">" + encode(text) + "</a>";
    }

    private static class MapComparator<K, V> implements Comparator<Map<K, V>>
    {
        private K key;
        private Comparator<V> comparator;

        public MapComparator(K key, Comparator<V> comparator)
        {
            this.key = key;
            this.comparator = comparator;
        }

        public int compare(Map<K, V> map1, Map<K, V> map2)
        {
            V value1 = map1.get(key);
            V value2 = map2.get(key);
            return comparator.compare(value1, value2);
        }
    }

    private static class StringComparator implements Comparator<String>
    {
        public int compare(String s1, String s2)
        {
            s1 = RemoveAccents.removeAccents(s1).toLowerCase();
            s2 = RemoveAccents.removeAccents(s2).toLowerCase();
            return s1.compareTo(s2);
        }
    }
}
