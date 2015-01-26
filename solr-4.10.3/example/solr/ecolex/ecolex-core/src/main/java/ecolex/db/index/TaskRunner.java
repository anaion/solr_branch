package ecolex.db.index;

import java.util.HashMap;
import java.util.Map;

import pl.edu.pw.ii.taskmanager.LongRunningTaskManager;
import pl.edu.pw.ii.taskmanager.ManagedTask;
import ecolex.search.TermListing;

/**
 * Running and getting information about indexing tasks.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class TaskRunner
{
    private IndexManagerRepository indexManagerRepository;

    private LongRunningTaskManager taskManager;

    private TermListing listing;

    /**
     * Tasks for rebuilding all indexes.
     */
    private ManagedTask rebuildAllTask = null;

    /**
     * Tasks for updating all indexes.
     */
    private ManagedTask updateAllTask = null;

    /**
     * Tasks for rebuilding whole index for each index separately.
     */
    private Map<String, ManagedTask> rebuildTasks = new HashMap<String, ManagedTask>();

    /**
     * Tasks for updating index for each index separately.
     */
    private Map<String, ManagedTask> updateTasks = new HashMap<String, ManagedTask>();

    private static final String TASK_KEY = "EcolexTaskKey";


    public TaskRunner(IndexManagerRepository indexManagerRepository, LongRunningTaskManager taskManager, TermListing listing)
    {
        this.indexManagerRepository = indexManagerRepository;
        this.taskManager = taskManager;
        this.listing = listing;
    }

    /**
     * Runs the IndexAll task.
     * @return true if task has been run
     */
    public synchronized boolean runRebuildAll()
    {
        ManagedTask task = taskManager.runSingletonTask("",
            new RebuildIndexTask(indexManagerRepository.getIndexManagers(), listing), TASK_KEY);
        if (task == null)
            return false;
        rebuildAllTask = task;
        return true;
    }

    /**
     * Runs the UpdateIndex task.
     * @return true if task has been run
     */
    public synchronized boolean runUpdateAll()
    {
        ManagedTask task = taskManager.runSingletonTask("",
            new UpdateIndexTask(indexManagerRepository.getIndexManagers(), listing), TASK_KEY);
        if (task == null)
            return false;
        updateAllTask = task;
        return true;
    }

    public synchronized boolean runRebuildIndex(String index)
    {
        ManagedTask task = taskManager.runSingletonTask("",
                new RebuildIndexTask(new DocumentIndexManager[] { indexManagerRepository.getIndexManager(index) }, listing), TASK_KEY);
        if (task == null)
            return false;
        rebuildTasks.put(index, task);
        return true;
    }

    public synchronized boolean runUpdateIndex(String index)
    {
        ManagedTask task = taskManager.runSingletonTask("",
                new UpdateIndexTask(new DocumentIndexManager[] { indexManagerRepository.getIndexManager(index) }, listing), TASK_KEY);
        if (task == null)
            return false;
        updateTasks.put(index, task);
        return true;
    }

    /**
     * Gets information about the IndexAll task.
     */
    public synchronized ManagedTask getRebuildAllTask()
    {
        return rebuildAllTask;
    }

    /**
     * Gets information about the UpdateIndex task.
     */
    public synchronized ManagedTask getUpdateAllTask()
    {
        return updateAllTask;
    }

    public synchronized ManagedTask getUpdateTask(String index)
    {
        return updateTasks.get(index);
    }

    public synchronized ManagedTask getRebuildTask(String index)
    {
        return rebuildTasks.get(index);
    }
}
