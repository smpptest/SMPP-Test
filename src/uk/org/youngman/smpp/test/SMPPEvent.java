/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test;


import java.text.SimpleDateFormat;
import java.util.Date;
import org.smpp.pdu.PDU;


public class SMPPEvent
{
    public static final SimpleDateFormat TIMESTAMP_FORMAT =
            new SimpleDateFormat( "HH:mm:ss.SSS" );

    public enum EventType
    { CONNECT_TO_SMSC, DISCONNECT_FROM_SMSC, SENT_PDU,
        RECEIVED_PDU, LOST_CONNECTION, CONNECT_FAILED, UNKNOWN }

    /**
     * Time of event
     */
    private Date timestamp;

    /**
     * Type of event recorded
     */
    private EventType type;

    /**
     * PDU associated with this event or NULL.
     */
    private PDU pdu;

    /**
     * Message associated with event, e.g. Connection failed.
     */
    private String message;

    /**
     * Default constructor
     */
    protected SMPPEvent()
    {
        timestamp = null;
        type = EventType.UNKNOWN;
        pdu = null;
        message = null;
    }

    /**
     * Main constructor
     * @param type Type of event
     * @param pdu  PDU associated with this event, or null.
     * @param message Message text associated with this event.
     * @param timestamp
     */
    public SMPPEvent(
            Date timestamp, EventType type, PDU pdu, String message )
    {
        this.timestamp = timestamp;
        this.type = type;
        this.pdu = pdu;
        this.message = message;
    }

    public EventType getType()
    {
        return type;
    }

    public Date getTime()
    {
        return timestamp;
    }

    public PDU getPdu()
    {
        return pdu;
    }

    public String getMessage()
    {
        return message;
    }
}
