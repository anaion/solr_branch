//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.modules.actions;

import org.objectledge.context.Context;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.web.mvc.security.PolicySystem;

import ecolex.db.index.TaskRunner;


/**
 * Creates all document indexes from scratch.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class RebuildAll
    extends AbstractAction
{
    public RebuildAll(PolicySystem policySystem, TaskRunner taskRunner)
    {
        super(policySystem, taskRunner);
    }

    public void process(Context context)
        throws ProcessingException
    {
        taskRunner.runRebuildAll();
        // TODO: How to report failing to run the task?
    }
}
