//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.config;

import java.util.Map;
import java.util.TreeMap;

import org.jcontainer.dna.Configuration;
import org.jcontainer.dna.ConfigurationException;

/**
 *
 * Holds configuration of base addresses.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemyslaw Wiech</a>
 * @version $Id$
 */
public class URLConfigurator
{
    /**
     * Base address for local calls to the database.
     */
    private String localBaseAddress;

    /**
     * Base address of full documents.
     */
    private String documentBaseAddress;

    /**
     * Path modifiers for selected fields.
     */
    private Map<String, String> linkPaths = new TreeMap<String, String>();

    /**
     * Base address for downloading faolex index.
     * E.g. "http://host:8080/faolex/"
     */
    private String faolexIndexAddress;

    public URLConfigurator(Configuration config) throws ConfigurationException
    {
        localBaseAddress = config.getChild("localBaseAddress").getValue();
        documentBaseAddress = config.getChild("documentBaseAddress").getValue();
        faolexIndexAddress = config.getChild("faolexIndexAddress").getValue();

        for(Configuration linkPath : config.getChildren("linkPath"))
            linkPaths.put(linkPath.getAttribute("field"), linkPath.getValue());
    }

    public String getDocumentBaseAddress()
    {
        return documentBaseAddress;
    }

    public String getLocalBaseAddress()
    {
        return localBaseAddress;
    }

    public String getFaolexIndexAddress()
    {
        return faolexIndexAddress;
    }

    public String getLinkPath(String field)
    {
        String path = linkPaths.get(field);
        if (path == null)
            return "";
        return path;
    }
}
