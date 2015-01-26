package ecolex.modules.views;

import java.util.Arrays;

import org.objectledge.context.Context;
import org.objectledge.parameters.RequestParameters;
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
public class CourtDecisions
    extends DefaultBuilder
{
    private ViewsConfiguration viewsConfig;

    public CourtDecisions(Context context, ViewsConfiguration viewsConfig)
    {
        super(context);
        this.viewsConfig = viewsConfig;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        RequestParameters parameters = RequestParameters.getRequestParameters(context);

        templatingContext.put("criteria", viewsConfig.getOptions("courtDecisionsCriterion"));
        templatingContext.put("allAny", viewsConfig.getOptions("allWords"));

        templatingContext.put("typesOfText", viewsConfig.getOptions("courtDecisionsTypesOfText"));
        templatingContext.put("selectedTypeOfText", Arrays.asList(parameters.getStrings("typeOfText")));

        templatingContext.put("sortOptions", viewsConfig.getOptions("courtdecisionsSort"));
    }
}
