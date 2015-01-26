package ecolex.search;

import org.apache.lucene.document.Document;

/**
 * Identifies the source index by looking at document fields.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class IndexIdentifier
{
    public String identify(Document document)
    {
        String id = document.get("id");
        return identify(id);
    }
    
    public String identify(String id)
    {
        if (id.contains("TRE"))
            return "treaties";
        if (id.contains("COU"))
            return "courtdecisions";
        if (id.contains("ANA") || id.contains("MON") || id.contains("SER"))
            return "literature";
        if (id.contains("LEX"))
            return "documents";
        return null;
    }
}
