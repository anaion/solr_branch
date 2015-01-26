//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//

package ecolex.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import faolex.iterator.ProgressReporter;

/**
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */

public class ProgressReporterInputStream extends FilterInputStream implements ProgressReporter
{
    private long size;
    private long progress = 0;

    public ProgressReporterInputStream(InputStream in, long size)
    {
        super(in);
        this.size = size;
    }

    public float getProgress()
    {
        if (size <= 0)
            return 0;
        return (float)progress / size;
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    @Override
    public int read() throws IOException
    {
        int ret = super.read();
        if (ret != -1)
            progress++;
        return ret;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int ret = super.read(b, off, len);
        if (ret != -1)
            progress += ret;
        return ret;
    }

    @Override
    public long skip(long n) throws IOException
    {
        long ret = super.skip(n);
        progress += ret;
        return ret;
    }
}
