package ecolex.search;


/**
 * Term text with document frequency.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 */
public class MultiTermFreq
{
    private String text;
    private int[] freq;

    public MultiTermFreq(String text, int[] freq)
    {
        this.text = text;
        this.freq = freq;
    }

    public MultiTermFreq(String text, int size)
    {
        this.text = text;
        this.freq = new int[size];
    }

    public int[] getFreq()
    {
        return freq;
    }

    public int getFreq(int i)
    {
        return freq[i];
    }

    public String getText()
    {
        return text;
    }

    public int getSize()
    {
        return freq.length;
    }

    public void add(MultiTermFreq freq)
    {
        int[] f = freq.getFreq();
        for (int i = 0; i < f.length; i++)
            this.freq[i] += f[i];
        this.text = freq.text; // Replace text
    }
}
