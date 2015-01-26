//
// Copyright (c) 2009, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.download;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.jcontainer.dna.Configuration;
import org.jcontainer.dna.ConfigurationException;

import faolex.config.URLConfigurator;
import faolex.db.download.AlphaIsisURLGenerator;

/**
 * Overrides the default Faolex URL generator.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class FaoLexUrlGenerator extends AlphaIsisURLGenerator
{
    public FaoLexUrlGenerator(Configuration config, URLConfigurator urlConfig)
            throws ConfigurationException
    {
        super(config, urlConfig);
    }

    @Override
    public Collection<URI> getDownloadAllURLs() throws URIException
    {
        return super.getDownloadAllURLs();
    }

    @Override
    public Collection<URI> getDownloadModifiedURLs(Date date) throws URIException
    {
    	// getDateURLs(date, "T=all*(Z=L%2BZ=M%2BZ=R)*DM=");
        return getDateURLs(date, "T:all%%20AND%%20DM:%%5B%s%%20TO%%20Z%%5D%%20AND(Z:L%%20OR%%20Z:M%%20ORZ:R)"); 
    }

    @Override
    public Collection<URI> getDownloadNewURLs(Date date) throws URIException
    {
    	// return getDateURLs(date, "T=all*(Z=L%2BZ=M%2BZ=R)*DE=");
        return getDateURLs(date, "T:all%%20AND%%20DE:%%5B%s%%20TO%%20Z%%5D%%20AND(Z:L%%20OR%%20Z:M%%20ORZ:R)"); 
    }
}
