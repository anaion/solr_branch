//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.download;

import java.io.Reader;

import org.xmlpull.v1.XmlPullParserException;

import ecolex.db.EcoLexDocument;
import faolex.db.download.XmlIteratorParser;

/**
 *
 * Parses an XML list of FaoLex records into the class representation.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemyslaw Wiech</a>
 * @version $Id$
 */
public class XmlDocumentParser extends XmlIteratorParser<EcoLexDocument>
{
    public XmlDocumentParser(Reader reader)
        throws XmlPullParserException
    {
        super(reader, "document");
        setSizeAttribute("result", "numberResultsPresented");
    }

    @Override
    protected void processField(String name, String text)
    {
        text = text.trim();
        if (text.length() != 0)
            currentElement.addField(name, text);
    }

    @Override
    protected EcoLexDocument createElement()
    {
        return new EcoLexDocument();
    }
}
