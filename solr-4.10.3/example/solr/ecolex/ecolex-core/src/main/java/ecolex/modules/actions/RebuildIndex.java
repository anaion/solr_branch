//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//
package ecolex.modules.actions;

import org.objectledge.context.Context;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.web.mvc.security.PolicySystem;

import ecolex.db.index.TaskRunner;


/**
 * Creates the document index from scratch.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class RebuildIndex
    extends AbstractAction
{
    public RebuildIndex(PolicySystem policySystem, TaskRunner taskRunner)
    {
        super(policySystem, taskRunner);
    }

    public void process(Context context)
        throws ProcessingException
    {
        RequestParameters parameters = RequestParameters.getRequestParameters(context);
        String indexName = parameters.get("index");

        taskRunner.runRebuildIndex(indexName);
        // TODO: How to report failing to run the task?
    }
}
