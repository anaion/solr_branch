package ecolex.modules.views;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.objectledge.context.Context;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.builders.PolicyProtectedBuilder;
import org.objectledge.web.mvc.security.PolicySystem;

import ecolex.db.index.IndexManagerRepository;
import ecolex.db.index.TaskRunner;
import faolex.search.BasicTimeConverter;
import faolex.search.TimeConverter;

/**
 * A view to manage the ECOLEX full text search activities.
 *
 * @author <a href="mailto:gajda@ii.pw.edu.pl">Damian Gajda</a>
 * @version $Id$
 */
public class Manager
    extends PolicyProtectedBuilder
{
    /**
     * Runs indexing and updating tasks.
     */
    TaskRunner taskRunner;

    /**
     * Index manager repository.
     */
    private IndexManagerRepository managerRepository;

    TimeConverter timeConverter = new BasicTimeConverter();

    public Manager(Context context, PolicySystem policySystem, TaskRunner taskRunner,
        IndexManagerRepository managerRepository)
    {
        super(context, policySystem);
        this.taskRunner = taskRunner;
        this.managerRepository = managerRepository;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        //templatingContext.put("rebuildAllTask", taskRunner.getRebuildAllTask());
        //templatingContext.put("updateAllTask", taskRunner.getUpdateAllTask());
        templatingContext.put("taskRunner", taskRunner);

        Map<String, Date> lastUpdateDates = new LinkedHashMap<String, Date>();
        Map<String, Date> mostRecentDates = new LinkedHashMap<String, Date>();
        for (String index : managerRepository.getIndexNames())
        {
            lastUpdateDates.put(index, managerRepository.getIndexManager(index).getLastUpdateDate());
            mostRecentDates.put(index, managerRepository.getIndexManager(index).getMostRecentRecordDate());
        }
        templatingContext.put("lastUpdateDates", lastUpdateDates);
        templatingContext.put("mostRecentDates", mostRecentDates);

        templatingContext.put("timeConverter", timeConverter);
    }
}
