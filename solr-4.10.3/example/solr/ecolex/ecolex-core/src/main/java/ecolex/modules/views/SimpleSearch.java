package ecolex.modules.views;

import org.objectledge.context.Context;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.builders.DefaultBuilder;

import ecolex.config.ViewsConfiguration;
import ecolex.util.ArraysTool;
import ecolex.util.DateTool;

/**
 * Chart selection.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class SimpleSearch
    extends DefaultBuilder
{
    private ViewsConfiguration viewsConfig;
    private DateTool dateTool;

    public SimpleSearch(Context context, ViewsConfiguration viewsConfig, DateTool dateTool)
    {
        super(context);
        this.viewsConfig = viewsConfig;
        this.dateTool = dateTool;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        templatingContext.put("commonDatabases", viewsConfig.getOptions("commonDatabases"));
        templatingContext.put("dateTool", dateTool);
        templatingContext.put("arraysTool", new ArraysTool());
    }
}


