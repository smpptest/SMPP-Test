/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */


package uk.org.youngman.smpp.test.batch;


public class ConnectionEvent implements Batch.Event
{
    private int port;
    private String address;

    public void setAddress( String address )
    {
        this.address = address;
    }

    public void setPort( int port )
    {

        this.port = port;
    }

    public String getAddress()
    {
        return address;
    }

    public int getPort()
    {
        return port;
    }
}
