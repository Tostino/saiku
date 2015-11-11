package gosimple.olap;

import mondrian.olap.Util;
import mondrian.spi.DynamicSchemaProcessor;
import mondrian.spi.impl.FilterDynamicSchemaProcessor;
import org.apache.commons.io.IOUtils;

import java.sql.*;

public final class GoSimpleDSP extends FilterDynamicSchemaProcessor implements DynamicSchemaProcessor
{
    private final static String cubeTemplateFile = "cube.xml";
    private final static String roleTemplateFile = "role.xml";
    private final static String cubes_tag = "~CUBES~";
    private final static String roles_tag = "~ROLES~";
    private final static String reporting_role_tag = "~REPORTING_ROLE~";
    private final static String client_description_tag = "~CLIENT_DESCRIPTION~";
    private final static String client_name_tag = "~CLIENT_NAME~";

    /**
     * {@inheritDoc}
     * <p/>
     * <p>FilterDynamicSchemaProcessor's implementation of this method reads
     * from the URL supplied (that is, it does not perform URL translation)
     * and passes it through the {@link #filter} method.
     *
     * @param schemaUrl
     * @param connectInfo
     */
    @Override
    public String processSchema(final String schemaUrl, final Util.PropertyList connectInfo) throws Exception
    {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final String schema = super.processSchema(schemaUrl, connectInfo);
        final String url = connectInfo.get("Jdbc");
        final String user = connectInfo.get("JdbcUser");
        final String password = connectInfo.get("JdbcPassword");
        final String driver = connectInfo.get("JdbcDrivers");
        final String cubeTemplate = IOUtils.toString(classLoader.getResourceAsStream(cubeTemplateFile));
        final String roleTemplate = IOUtils.toString(classLoader.getResourceAsStream(roleTemplateFile));
        final StringBuilder cubeBuilder = new StringBuilder();
        final StringBuilder roleBuilder = new StringBuilder();

        String final_schema = new String(schema);

        Class.forName(driver);

        try(final Connection connection = DriverManager.getConnection(url, user, password))
        {
            // Get all clients, and loop through to set the template values for each client.
            try(final PreparedStatement client_statement = connection.prepareStatement("SELECT mc.client_sk, mc.reporting_role, dc.client_description, dc.client_name FROM meta_client mc INNER JOIN dim_client dc USING (client_sk);"))
            {
                try (final ResultSet client_result = client_statement.executeQuery())
                {
                    while (client_result.next())
                    {
                        // Set the mutable template variables.
                        String cubeTemplateTemp = cubeTemplate;
                        String roleTemplateTemp = roleTemplate;

                        final int client_sk = client_result.getInt("client_sk");
                        final String reporting_role = client_result.getString("reporting_role");
                        final String client_description = client_result.getString("client_description");
                        final String client_name = client_result.getString("client_name");

                        cubeTemplateTemp = cubeTemplateTemp.replace(reporting_role_tag, reporting_role);
                        cubeTemplateTemp = cubeTemplateTemp.replace(client_description_tag, client_description);
                        cubeTemplateTemp = cubeTemplateTemp.replace(client_name_tag, client_name);

                        roleTemplateTemp = roleTemplateTemp.replace(reporting_role_tag, reporting_role);
                        roleTemplateTemp = roleTemplateTemp.replace(client_description_tag, client_description);
                        roleTemplateTemp = roleTemplateTemp.replace(client_name_tag, client_name);

                        // Set enabled template tags
                        try (final PreparedStatement enabled_statement = connection.prepareStatement("SELECT met.enabled_tag, COALESCE(me.enabled, false) as enabled FROM meta_enabled_tag met LEFT JOIN meta_enabled me ON me.enabled_tag = met.enabled_tag AND me.client_sk = ?;"))
                        {
                            enabled_statement.setInt(1, client_sk);

                            try (final ResultSet enabled_result = enabled_statement.executeQuery())
                            {
                                while(enabled_result.next())
                                {
                                    final String enabled_tag = enabled_result.getString("enabled_tag");
                                    final Boolean enabled = enabled_result.getBoolean("enabled");

                                    cubeTemplateTemp = cubeTemplateTemp.replace(enabled_tag, enabled.toString());
                                }
                            }
                        }

                        // Set caption template tags
                        try (final PreparedStatement caption_statement = connection.prepareStatement("SELECT mct.caption_tag, mc.caption FROM meta_caption_tag mct INNER JOIN meta_caption mc ON mc.caption_tag = mct.caption_tag AND mc.client_sk = ?;"))
                        {
                            caption_statement.setInt(1, client_sk);

                            try (final ResultSet caption_result = caption_statement.executeQuery())
                            {
                                while(caption_result.next())
                                {
                                    final String caption_tag = caption_result.getString("caption_tag");
                                    final String caption = caption_result.getString("caption");

                                    cubeTemplateTemp = cubeTemplateTemp.replace(caption_tag, caption);
                                }
                            }
                        }

                        cubeBuilder.append(cubeTemplateTemp);
                        roleBuilder.append(roleTemplateTemp);
                    }
                }
            }
        }

        final_schema = final_schema.replace(cubes_tag, cubeBuilder.toString());
        final_schema = final_schema.replace(roles_tag, roleBuilder.toString());

        return final_schema;
    }
}
