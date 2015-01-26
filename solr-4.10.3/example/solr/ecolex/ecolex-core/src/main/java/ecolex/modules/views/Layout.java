package ecolex.modules.views;

import java.util.Arrays;

import org.objectledge.context.Context;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.builders.DefaultBuilder;

import ecolex.config.ViewsConfiguration;
import ecolex.util.DateTool;

/**
 * Chart selection.
 *
 * @author <a href="mailto:pwiech@ii.pw.edu.pl">Przemysław Więch</a>
 * @version $Id$
 */
public class Layout
    extends DefaultBuilder
{
    private DateTool dateTool;

    public Layout(Context context, DateTool dateTool)
    {
        super(context);
        this.dateTool = dateTool;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        templatingContext.put("dateTool", dateTool);
    }
}
