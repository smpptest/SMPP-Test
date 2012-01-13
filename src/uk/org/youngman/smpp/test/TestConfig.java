/*
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;


/**
 * Configuration for the tester. Written as a singleton.
 */
public class TestConfig
{
    /**
     * Set up a logger
     */
    static Logger logger = Logger.getLogger( TestConfig.class.getName() );

    /**
     * Singleton instance.
     */
    static TestConfig INSTANCE = new TestConfig();

    /**
     * Configuration read from file "Test.properties"
     */
    static PropertyResourceBundle props = null;
    static
    {
        try
        {
            props = (PropertyResourceBundle)ResourceBundle.getBundle( "Test" );
        }
        catch( MissingResourceException ex )
        {
            // Test.props not found. Function getString checks for props
            // to be null, which makes this safe to ignore,
            // as long as all getters use getString().
        }
    }

    /**
     * Default path for kill file
     */
    private static final String DEFAULT_SERVER = "localhost";

    /**
     * Default limit on connections.
     */
    private static final int DEFAULT_PORT = 2775;


    /**
     * Constructor is private to ensure singleton.
     */
    private TestConfig()
    {
    }


    /**
     * Provides access to the singleton instance.
     */
    public static TestConfig getConfig()
    {
        return INSTANCE;
    }


    /**
     * Get a string from the config and trim spaces.
     * Catches MissingResourceException when thrown from the standard getString.
     * @throws NullPointerException, ClassCastException
     * @return String (can be empty) or null
     */
    private String getString( String key )
    {
        if( props == null )
        {
            return "";
        }

        try
        {
            return props.getString( key ).trim();
        }
        catch( MissingResourceException ex )
        {
            logger.warning( "Failed to find key \"" + key + "\" in configuration" );
            return "";
        }
    }


    /**
     * Get the configured default server
     */
    public String getServer()
    {
        String server = getString( "Server" );
        if( server==null || server.equals( "" ) )
        {
            return DEFAULT_SERVER;
        }
        return server;
    }


    /**
     * Get the configured default port number
     */
    public int getPort()
    {
        String num = getString( "Port" );
        if( num == null || num.equals( "" ) )
        {
            return DEFAULT_PORT;
        }

        try
        {
            return Integer.parseInt( num, 10 );
        }
        catch( NumberFormatException ex )
        {
            logger.warning( "Bad value for listening port (" + num + ")" );
            throw new IllegalStateException(
                "Bad value for listening port (" + num + ")", ex );
        }
    }


    /**
     * Get the configured default system_id
     * @throws NullPointerException, ClassCastException
     */
    public String getSystemId()
    {
        String systemId = getString( "SystemId" );
        if( systemId == null )
        {
            return "";
        }
        return systemId;
    }

    /**
     * Get the configured default system_type
     * @throws NullPointerException, ClassCastException
     */
    public String getSystemType()
    {
        String systemType = getString( "SystemType" );
        if( systemType == null )
        {
            return "";
        }
        return systemType;
    }

    /**
     * Get the configured default password
     * @throws NullPointerException, ClassCastException
     */
    public String getPassword()
    {
        String password = getString( "Password" );
        if( password == null )
        {
            return "";
        }
        return password;
    }
}
