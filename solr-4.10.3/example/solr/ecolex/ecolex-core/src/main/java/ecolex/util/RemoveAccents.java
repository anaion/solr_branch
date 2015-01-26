package ecolex.util;

import java.util.regex.Pattern;

import com.ibm.icu.text.Normalizer;

public class RemoveAccents
{
    private static final Pattern PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    public static String removeAccents(String s)
    {
        s = Normalizer.decompose(s, false);
        s = PATTERN.matcher(s).replaceAll("");
        return s;
    }
}
