package ecolex.db.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import faolex.db.download.CharFixerInputStream;

public class EmptyLineRemoverInputStream extends BufferedInputStream /*CharFixerInputStream*/ {
	private boolean beg = true;
	
	public EmptyLineRemoverInputStream(InputStream stream) {
		super(stream);
	}

	@Override
	public int read() throws IOException {
		int c;
		do {
			c = super.read();
		}
		while (beg==true && ((char)c =='\r' || (char)c == '\n'));
		beg = false;
		return c;
	}

	@Override
	public int read(byte[] b, int off, int len)
	        throws IOException
	    {
	        if(b == null)
	        {
	            throw new NullPointerException();
	        }
	        else if((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
	            || ((off + len) < 0))
	        {
	            throw new IndexOutOfBoundsException();
	        }
	        else if(len == 0)
	        {
	            return 0;
	        }

	        int c = read();
	        if(c == -1)
	        {
	            return -1;
	        }
	        b[off] = (byte)c;

	        int i = 1;
	        try
	        {
	            for (; i < len; i++)
	            {
	                c = read();
	                if(c == -1)
	                {
	                    break;
	                }
	                if(b != null)
	                {
	                    b[off + i] = (byte)c;
	                }
	            }
	        }
	        catch(IOException ee)
	        {
	        }
	        return i;
	    }
	
	
	
}
