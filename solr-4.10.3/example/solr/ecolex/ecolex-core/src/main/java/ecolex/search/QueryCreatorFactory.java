package ecolex.search;

import java.util.Locale;

import org.objectledge.parameters.Parameters;

import faolex.search.searching.QueryCreationException;
import faolex.search.searching.QueryCreator;

public interface QueryCreatorFactory
{
    public  QueryCreator getQueryCreator(Parameters parameters, Locale locale) throws QueryCreationException;
}