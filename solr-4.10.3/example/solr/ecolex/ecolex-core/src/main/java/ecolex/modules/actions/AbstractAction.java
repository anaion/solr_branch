package ecolex.modules.actions;

import org.objectledge.web.mvc.builders.PolicyProtectedAction;
import org.objectledge.web.mvc.security.PolicySystem;

import ecolex.db.index.TaskRunner;

/**
 * The base action class.
 *
 * @author <a href="mailto:gajda@ii.pw.edu.pl">Damian Gajda</a>
 * @version $Id$
 */
public abstract class AbstractAction
    extends PolicyProtectedAction
{
    protected TaskRunner taskRunner;

    public AbstractAction(PolicySystem policySystem, TaskRunner taskRunner)
    {
        super(policySystem);
        this.taskRunner = taskRunner;
    }
}
