//
// Copyright (c) 2008, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.config;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.jcontainer.dna.Configuration;
import org.jcontainer.dna.ConfigurationException;

/**
 * Configuration for various aspects of displayed views.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class ViewsConfiguration
{
    private MapMap<String, String, Option> options = new MapMap<String, String, Option>();

    /**
     * Creates the object and reads configuration.
     */
    public ViewsConfiguration(Configuration config) throws ConfigurationException
    {
        for(Configuration typeOfDoc : config.getChildren("options"))
        {
            String name = typeOfDoc.getAttribute("name");
            for(Configuration option : typeOfDoc.getChildren("option"))
            {
                String valueAttr = option.getAttribute("value");
                String listing = option.getAttribute("listing", "yes");
                String type = option.getAttribute("type", null);
                String text = option.getValue(valueAttr);

                options.add(name, valueAttr, new Option(text, listing.equals("yes"), type));
            }
        }
    }

    /**
     * Returns options for a given name.
     * @return Options map
     */
    public Map<String, Option> getOptions(String name)
    {
        return options.get(name);
    }

    /**
     * Returns options for a given name.
     * Appends locale identifier (e.g. "en_US") to all options ending with underscore.
     * @return Options map
     */
    public Map<String, Option> getOptions(String whichTypes, Locale locale)
    {
        Map<String, Option> map = new LinkedHashMap<String, Option>();
        String localeStr = locale.toString();

        for (Map.Entry<String, Option> entry : options.get(whichTypes).entrySet())
        {
            if (entry.getKey().endsWith("_"))
                map.put(entry.getKey() + localeStr, entry.getValue());
            else
                map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    /**
     * Represents a single option value.
     */
    public static class Option
    {
        /**
         * Option text.
         */
        private String text;

        /**
         * Whether to add a listing button.
         */
        private boolean listing;

        /**
         * Option type.
         * Used for record details display.
         */
        private String type;

        Option(String text, boolean listing, String type)
        {
            this.text = text;
            this.listing = listing;
            this.type = type;
        }

        public String getText()
        {
            return text;
        }

        public boolean isListing()
        {
            return listing;
        }

        public String getType()
        {
            return type;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }
}
