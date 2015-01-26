package ecolex.modules.views;

import java.util.Locale;

import org.objectledge.context.Context;
import org.objectledge.i18n.I18nContext;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.builders.DefaultBuilder;

import ecolex.config.ViewsConfiguration;
import ecolex.util.ArraysTool;

/**
 * Chart selection.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class Common
    extends DefaultBuilder
{
    private ViewsConfiguration viewsConfig;

    public Common(Context context, ViewsConfiguration viewsConfig)
    {
        super(context);
        this.viewsConfig = viewsConfig;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        Locale locale = I18nContext.getI18nContext(context).getLocale();

        templatingContext.put("criteria", viewsConfig.getOptions("commonCriterion"));
        templatingContext.put("allAny", viewsConfig.getOptions("allWords"));

        templatingContext.put("commonDatabases", viewsConfig.getOptions("commonDatabases"));

        templatingContext.put("sortOptions", viewsConfig.getOptions("commonSort", locale));

        templatingContext.put("arraysTool", new ArraysTool());
    }
}


