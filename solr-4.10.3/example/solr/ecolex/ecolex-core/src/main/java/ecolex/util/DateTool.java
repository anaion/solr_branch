//
// Copyright (c) 2009, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import faolex.search.DateConverter;
import faolex.search.PatternDateConverter;

/**
 * Includes several operations on dates.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class DateTool
{
    private Map<Locale, DateConverter> fullConverter = new HashMap<Locale, DateConverter>();
    private Map<Locale, DateConverter> monthConverter = new HashMap<Locale, DateConverter>();
    private Map<Locale, DateConverter> yearConverter = new HashMap<Locale, DateConverter>();

    private String fullPattern;
    private String monthPattern;
    private String yearPattern;

    private static DateConverter FULL_TEMPLATE = new PatternDateConverter("yyyyMMdd");
    private static DateConverter MONTH_TEMPLATE = new PatternDateConverter("yyyyMM");
    private static DateConverter YEAR_TEMPLATE = new PatternDateConverter("yyyy");

    private static Pattern FULL_PATTERN = Pattern.compile("(\\d\\d\\d\\d)[^\\d]*(\\d\\d)[^\\d]*(\\d\\d).*");
    private static Pattern MONTH_PATTERN = Pattern.compile("(\\d\\d\\d\\d)[^\\d]*(\\d\\d)[^\\d]*(00)?");
    private static Pattern YEAR_PATTERN = Pattern.compile("(\\d\\d\\d\\d)[^\\d]*(00[^\\d]*(\\d\\d)?)?");

    public DateTool(String fullPattern, String monthPattern, String yearPattern)
    {
        this.fullPattern = fullPattern;
        this.monthPattern = monthPattern;
        this.yearPattern = yearPattern;
    }

    public void setFullPattern(String fullPattern)
    {
        this.fullPattern = fullPattern;
        fullConverter.clear();
    }

    public void setMonthPattern(String monthPattern)
    {
        this.monthPattern = monthPattern;
        monthConverter.clear();
    }

    public void setYearPattern(String yearPattern)
    {
        this.yearPattern = yearPattern;
        yearConverter.clear();
    }

    /**
     * Format a date in one of three formats.
     * The formats are:
     *   - year only
     *   - year and month
     *   - year, month and day
     */
    public String format(String date, Locale locale)
    {
        if (date.length() == 4)
            return format(YEAR_TEMPLATE.toDate(date), locale, yearConverter, yearPattern);
        if (date.length() == 6)
            return format(MONTH_TEMPLATE.toDate(date), locale, monthConverter, monthPattern);
        return format(FULL_TEMPLATE.toDate(date), locale, fullConverter, fullPattern);
    }

    /**
     * Normalizes a date string. Removes formatting characters eg '-' and
     * removes '00' values of month and/or day.
     * Returns null if the date could not be extracted.
     */
    public String normalize(String date)
    {
        Matcher m = YEAR_PATTERN.matcher(date);
        if (m.matches())
            return m.group(1);
        m = MONTH_PATTERN.matcher(date);
        if (m.matches())
            return m.group(1) + m.group(2);
        m = FULL_PATTERN.matcher(date);
        if (m.matches())
            return m.group(1) + m.group(2) + m.group(3);
        return null;
    }

    private String format(Date date, Locale locale, Map<Locale, DateConverter> converter, String pattern)
    {
        DateConverter conv = converter.get(locale);
        if (conv == null)
        {
            conv = new PatternDateConverter(pattern, locale);
            converter.put(locale, conv);
        }
        return conv.toString(date);
    }

    /**
     * Returns the date which is a given number of days before today.
     * @param days
     * @return
     */
    public Date daysBack(int days)
    {
    	Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.DATE, -days);
    	return calendar.getTime();
    }
}
