//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.db;

import faolex.iterator.SizedIterator;

/**
 * Abstract implementstion of the SizedIterator interface with a process()
 * method for processing elements.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 * @param <E>
 */
public abstract class ProcessingIterator<E> implements SizedIterator<E>
{

    private SizedIterator<E> iterator;

    /**
     * Processes subsequent elements
     */
    protected abstract void process(E element);

    public ProcessingIterator(SizedIterator<E> iterator)
    {
        this.iterator = iterator;
    }

    public int size()
    {
        return iterator.size();
    }

    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    public E next()
    {
        E element =  iterator.next();
        process(element);
        return element;
    }

    public void remove()
    {
        iterator.remove();
    }
}
