//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db.index;

import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.analysis.BaseTokenFilterFactory;

import faolex.search.internal.AccentRemovingFilter;

/**
 * Creates a filter for removing accents from characters.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class AccentRemovingFilterFactory extends BaseTokenFilterFactory
{
    public TokenStream create(TokenStream input)
    {
        return new AccentRemovingFilter(input);
    }
}
