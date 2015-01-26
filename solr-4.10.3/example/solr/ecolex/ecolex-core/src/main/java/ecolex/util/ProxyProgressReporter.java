package ecolex.util;

import faolex.iterator.ProgressReporter;

public class ProxyProgressReporter implements ProgressReporter
{
    ProgressReporter reporter = null;

    /**
     * Bottom end of progress range.
     */
    private float rangeMin;

    /**
     * Range of the progress (rangeMax-rangeMin).
     */
    private float range;

    public ProxyProgressReporter(ProgressReporter reporter, float rangeMin, float rangeMax)
    {
        this.reporter = reporter;
        this.rangeMin = rangeMin;
        this.range = rangeMax - rangeMin;
    }

    public ProxyProgressReporter(float rangeMin, float rangeMax)
    {
        this(null, rangeMin, rangeMax);
    }

    public ProxyProgressReporter(ProgressReporter reporter)
    {
        this(reporter, 0, 1);
    }

    public ProxyProgressReporter()
    {
        this(null);
    }

    public float getProgress()
    {
        if (reporter == null)
            return rangeMin;
        return rangeMin + reporter.getProgress() * range;
    }

    public void setReporter(ProgressReporter reporter)
    {
        this.reporter = reporter;
    }
}
