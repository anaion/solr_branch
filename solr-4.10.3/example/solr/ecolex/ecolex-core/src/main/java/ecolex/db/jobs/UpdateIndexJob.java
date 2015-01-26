package ecolex.db.jobs;

import org.objectledge.scheduler.Job;

import ecolex.db.index.TaskRunner;

/**
 * Runs the task to update all indexes.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class UpdateIndexJob
    extends Job
{
    private TaskRunner taskRunner;

    public UpdateIndexJob(TaskRunner taskRunner)
    {
        this.taskRunner = taskRunner;
    }

    @Override
    public void run(String[] arg0)
    {
        taskRunner.runUpdateAll();
        // TODO: How to report failing to run the task?
    }

}
