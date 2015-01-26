package ecolex.modules.actions;

import org.objectledge.context.Context;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.web.mvc.security.PolicySystem;

import ecolex.db.index.TaskRunner;


/**
 * Updates a selected index.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class UpdateIndex
    extends AbstractAction
{
    public UpdateIndex(PolicySystem policySystem, TaskRunner taskRunner)
    {
        super(policySystem, taskRunner);
    }

    public void process(Context context)
        throws ProcessingException
    {
        RequestParameters parameters = RequestParameters.getRequestParameters(context);
        String indexName = parameters.get("index");

        taskRunner.runUpdateIndex(indexName);
        // TODO: How to report failing to run the task?
    }
}
