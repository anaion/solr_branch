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
public class Treaties
    extends DefaultBuilder
{
    private ViewsConfiguration viewsConfig;

    public Treaties(Context context, ViewsConfiguration viewsConfig)
    {
        super(context);
        this.viewsConfig = viewsConfig;
    }

    @Override
    public void process(TemplatingContext templatingContext)
        throws ProcessingException
    {
        RequestParameters parameters = RequestParameters.getRequestParameters(context);

        templatingContext.put("criteria", viewsConfig.getOptions("treatyCriterion"));
        templatingContext.put("allAny", viewsConfig.getOptions("allWords"));

        templatingContext.put("typesOfText", viewsConfig.getOptions("treatyTypesOfText"));
        templatingContext.put("selectedTypeOfText", Arrays.asList(parameters.getStrings("typeOfText")));
        templatingContext.put("fieldsOfApplication", viewsConfig.getOptions("treatyFieldsOfApplication"));
        templatingContext.put("selectedFieldsOfApplication", Arrays.asList(parameters.getStrings("fieldOfApplication")));

        templatingContext.put("sortOptions", viewsConfig.getOptions("treatiesSort"));
    }
}
