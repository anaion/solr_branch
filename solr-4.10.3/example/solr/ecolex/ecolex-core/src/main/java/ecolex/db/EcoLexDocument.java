package ecolex.db;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import faolex.search.DateConverter;
import faolex.search.PatternDateConverter;

/**
 * Representation of a single EcoLex record.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class EcoLexDocument
{
    public static final String ID_FIELD = "id";

    private static final DateConverter SIMPLE_DATE_CONVERTER = new PatternDateConverter("yyyyMMdd");
    private static final DateConverter DASHED_DATE_CONVERTER = new PatternDateConverter("yyyy-MM-dd");

    private Map<String, Set<String>> fields = new HashMap<String, Set<String>>();

    public String getId()
    {
        Set<String> values = getFieldValues(ID_FIELD);
        if (values == null || values.isEmpty())
            return null;
        return values.iterator().next();
    }

    /**
     * Returns all values in the field.
     */
    public Set<String> getFieldValues(String fieldName)
    {
        return fields.get(fieldName);
    }

    /**
     * Returns one of the field values.
     */
    public String getFieldValue(String fieldName)
    {
        Set<String> values = getFieldValues(fieldName);
        if (values == null || values.isEmpty())
            return null;
        return values.iterator().next();
    }

    /**
     * Adds a value to the given field name. This does not remove any previously added values.
     */
    public void addField(String fieldName, String value)
    {
        if (fieldName == null || fieldName.equals(""))
            return;

        Set<String> set = fields.get(fieldName);
        if (set == null)
        {
            set = new LinkedHashSet<String>();
            fields.put(fieldName, set);
        }
        if (value != null)
            set.add(value);
    }

    /**
     * Removes given value from a field.
     */
    public void removeField(String fieldName, String value)
    {
        if (fieldName == null || fieldName.equals(""))
            return;

        Set<String> set = fields.get(fieldName);
        if (set == null)
            return;
        set.remove(value);
    }

    /**
     * Removes all values from a field.
     */
    public void removeField(String fieldName)
    {
        if (fieldName == null || fieldName.equals(""))
            return;

        fields.remove(fieldName);
    }

    /**
     * Returns a set of all available fields.
     */
    public Set<String> getFields()
    {
        return fields.keySet();
    }


    /**
     * Get field value as a date.
     * Recognizes 2 formats: yyyyMMdd and yyyy-MM-dd
     * @return parsed date or null if field does not exist or cannot be parsed
     */
    public Date getDate(String fieldName)
    {
        return toDate(getFieldValue(fieldName));
    }

    /**
     * Returns a set of all dates within this field.
     * If a value cannot be transformed into a date, it is not returned in the set.
     */
    public Set<Date> getDates(String fieldName)
    {
        Set<Date> set = new HashSet<Date>();
        Set<String> values = getFieldValues(fieldName);
        if (values == null)
            return null;
        for (String value : values)
        {
            Date date = toDate(value);
            if (date != null)
                set.add(date);
        }
        return set;
    }

    /**
     * Converts a string value into a Date object.
     * @return Date object or null if an error occurred
     */
    private Date toDate(String value)
    {
        if (value == null)
            return null;
        if (value.equals("0000-00-00"))
            return null;
        if (value.indexOf('-') != -1)
            return DASHED_DATE_CONVERTER.toDate(value);
        return SIMPLE_DATE_CONVERTER.toDate(value);
    }
}
