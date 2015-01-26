package ecolex.util;
import junit.framework.TestCase;


public class DateToolTest extends TestCase
{

    private DateTool dateTool;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        dateTool = new DateTool("yyyyMMdd", "yyyyMM", "yyyy");
    }

    public void testNormalize()
    {
        assertEquals("2006", dateTool.normalize("2006"));
        assertEquals("200601", dateTool.normalize("200601"));
        assertEquals("20060101", dateTool.normalize("20060101"));

        assertEquals("20060101", dateTool.normalize("2006-01-01"));
        assertEquals("20060101", dateTool.normalize("2006.01.01"));

        assertEquals("2006", dateTool.normalize("200600"));
        assertEquals("2006", dateTool.normalize("2006."));
        assertEquals("2006", dateTool.normalize("2006--"));
        assertEquals("2006", dateTool.normalize("2006.00"));
        assertEquals("2006", dateTool.normalize("20060001"));
        assertEquals("2006", dateTool.normalize("2006.00.01"));

        assertEquals("200601", dateTool.normalize("200601"));
        assertEquals("200601", dateTool.normalize("2006.01"));
        assertEquals("200601", dateTool.normalize("2006.01."));

        assertEquals("200601", dateTool.normalize("20060100"));
        assertEquals("200601", dateTool.normalize("2006.01.00"));
    }

}
