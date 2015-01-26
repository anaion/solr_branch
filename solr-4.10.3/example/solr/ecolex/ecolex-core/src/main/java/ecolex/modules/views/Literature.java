package ecolex.modules.views;

import org.objectledge.context.Context;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.builders.DefaultBuilder;

import ecolex.config.ViewsConfiguration;

/**
 * Chart selection.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class Literature
    extends DefaultBuilder
{
    private ViewsConfiguration viewsConfig;

    public Literature(Context context, ViewsConfiguration viewsConfig)
    {
        super(context);
        this.viewsConfig = viewsConfig;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        templatingContext.put("criteria", viewsConfig.getOptions("literatureCriterion"));
        templatingContext.put("allAny", viewsConfig.getOptions("allWords"));

        templatingContext.put("sortOptions", viewsConfig.getOptions("literatureSort"));
    }
}
