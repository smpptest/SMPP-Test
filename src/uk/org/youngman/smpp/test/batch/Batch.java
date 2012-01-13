/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.batch;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.smpp.Data;
import org.smpp.pdu.PDU;


public class Batch
{
    /**
     * Marker interface. No functions required
     */
    protected interface Event
    {
    }

    /**
     * Event representing the sending of an SMPP PDU
     */
    protected static class PDUEvent implements Event
    {
        private PDU pdu;

        /**
         * Constructor
         * @param pdu SMPP PDU to send.
         */
        public PDUEvent( PDU pdu )
        {
            this.pdu = pdu;
        }

        /**
         * @return SMPP PDU to send.
         */
        public PDU getPdu()
        {
            return pdu;
        }
    }

    /**
     * Event representing a pause before the next event is triggered
     */
    protected static class PauseEvent implements Event
    {
        private long millis;

        /**
         * Constructor
         * @param millis Time to wait in milliseconds
         */
        public PauseEvent( long millis )
        {
            this.millis = millis;
        }

        /**
         * @return Time to wait in milliseconds
         */
        public long getMillis()
        {
            return millis;
        }
    }

    /**
     * Event representing disconnection from the SMSC
     */
    protected static class DisconnectEvent implements Event
    {
    }

    /**
     * Event representing a change of auto-reponse settings
     */
    protected static class AutoResponseSettingEvent
            implements Event
    {
        private CommandID pduType;
        private boolean enable;

        /**
         * Constructor
         * @param pduType  Type of PDU affected by the settings
         * @param enable   Flag indicating whether to enable or disable auto-responses
         */
        public AutoResponseSettingEvent( CommandID pduType, boolean enable )
        {
            this.pduType = pduType;
            this.enable = enable;
        }

        /**
         * @return Type of PDU affected by the settings
         */
        public CommandID getPduType()
        {
            return pduType;
        }

        /**
         * @return Flag indicating whether to enable or disable auto-responses
         */
        public boolean isEnable()
        {
            return enable;
        }
    }

    /**
     * Enumeration representing PDU types
     */
    public enum CommandID
    {
        GENERIC_NACK( Data.GENERIC_NACK ),
        BIND_RECEIVER( Data.BIND_RECEIVER ),
        BIND_RECEIVER_RESP( Data.BIND_RECEIVER_RESP ),
        BIND_TRANSMITTER( Data.BIND_TRANSMITTER ),
        BIND_TRANSMITTER_RESP( Data.BIND_TRANSMITTER_RESP ),
        QUERY_SM( Data.QUERY_SM ),
        QUERY_SM_RESP( Data.QUERY_SM_RESP ),
        SUBMIT_SM( Data.SUBMIT_SM ),
        SUBMIT_SM_RESP( Data.SUBMIT_SM_RESP ),
        DELIVER_SM( Data.DELIVER_SM ),
        DELIVER_SM_RESP( Data.DELIVER_SM_RESP ),
        UNBIND( Data.UNBIND ),
        UNBIND_RESP( Data.UNBIND_RESP ),
        REPLACE_SM( Data.REPLACE_SM ),
        REPLACE_SM_RESP( Data.REPLACE_SM_RESP ),
        CANCEL_SM( Data.CANCEL_SM ),
        CANCEL_SM_RESP( Data.CANCEL_SM_RESP ),
        BIND_TRANSCEIVER( Data.BIND_TRANSCEIVER ),
        BIND_TRANSCEIVER_RESP( Data.BIND_TRANSCEIVER_RESP ),
        OUTBIND( Data.OUTBIND ),
        ENQUIRE_LINK( Data.ENQUIRE_LINK ),
        ENQUIRE_LINK_RESP( Data.ENQUIRE_LINK_RESP ),
        SUBMIT_MULTI( Data.SUBMIT_MULTI ),
        SUBMIT_MULTI_RESP( Data.SUBMIT_MULTI_RESP ),
        ALERT_NOTIFICATION( Data.ALERT_NOTIFICATION ),
        DATA_SM( Data.DATA_SM ),
        DATA_SM_RESP( Data.DATA_SM_RESP );

        /**
         * Value of CommandId field in SMPP PDU for this PDU type.
         */
        private int id;

        /**
         * Mapping from value of CommandId field to corresponding enumeration value
         */
        private static Hashtable<Integer,CommandID> reverse =
                new Hashtable<Integer, CommandID>();

        /**
         * Constructor
         * @param id Value of CommandId field in SMPP PDU for this PDU type
         */
        private CommandID( int id )
        {
            this.id = id;
        }

        /**
         * @return Value of CommandId field in SMPP PDU for this PDU type.
         */
        public int getId()
        {
            return id;
        }

        /**
         * Find the enumeration value corresponding to value of CommandId field
         * @param id value of CommandId field
         * @return  enumeration value
         */
        static public CommandID find( int id )
        {
            return reverse.get( id );
        }

        /**
         * Populate the mapping of CommandId values to enumeration values
         * @param id value of CommandId field
         */
        static private void register( CommandID id )
        {
            reverse.put( id.getId(), id );
        }
    }

    /**
     * Java won't let me populate CommandID.reverse from the constructors
     */
    static
    {
        for( CommandID id: CommandID.values() )
        {
            CommandID.register( id );
        }
    }

    /**
     * Enumeration representing defined values of the CommandStatus field defined in the SMPP Spec (v3.4)
     */
    public enum CommandStatus {
        ESME_ROK( 0 ),
        ESME_RINVMSGLEN( 1 ),
        ESME_RINVCMDLEN( 2 ),
        ESME_RINVCMDID( 3 ),
        ESME_RINVBNDSTS( 4 ),
        ESME_RALYBND( 5 ),
        ESME_RINVPRTFLG( 6 ),
        ESME_RINVREGDLVFLG( 7 ),
        ESME_RSYSERR( 8 ),
        ESME_RINVSRCADR( 10 ),
        ESME_RINVDSTADR( 11 ),
        ESME_RINVMSGID( 12 ),
        ESME_RBINDFAIL( 13 ),
        ESME_RINVPASWD( 14 ),
        ESME_RINVSYSID( 15 ),
        ESME_RCANCELFAIL( 17 ),
        ESME_RREPLACEFAIL( 19 ),
        ESME_RMSGQFUL( 20 ),
        ESME_RINVSERTYP( 21 ),
        ESME_RADDCUSTFAIL( 25 ),
        ESME_RDELCUSTFAIL( 26 ),
        ESME_RMODCUSTFAIL( 27 ),
        ESME_RENQCUSTFAIL( 28 ),
        ESME_RINVCUSTID( 29 ),
        ESME_RINVCUSTNAME( 31 ),
        ESME_RINVCUSTADR( 33 ),
        ESME_RINVADR( 34 ),
        ESME_RCUSTEXIST( 35 ),
        ESME_RCUSTNOTEXIST( 36 ),
        ESME_RADDDLFAIL( 38 ),
        ESME_RMODDLFAIL( 39 ),
        ESME_RDELDLFAIL( 40 ),
        ESME_RVIEWDLFAIL( 41 ),
        ESME_RLISTDLSFAIL( 48 ),
        ESME_RPARAMRETFAIL( 49 ),
        ESME_RINVPARAM( 50 ),
        ESME_RINVNUMDESTS( 51 ),
        ESME_RINVDLNAME( 52 ),
        ESME_RINVDLMEMBDESC( 53 ),
        ESME_RINVDLMEMBTYP( 56 ),
        ESME_RINVDLMODOPT( 57 ),
        ESME_RINVDESTFLAG( 64 ),
        ESME_RINVSUBREP( 66 ),
        ESME_RINVESMCLASS( 67 ),
        ESME_RCNTSUBDL( 68 ),
        ESME_RSUBMITFAIL( 69 ),
        ESME_RINVSRCTON( 72 ),
        ESME_RINVSRCNPI( 73 ),
        ESME_RINVDSTTON( 80 ),
        ESME_RINVDSTNPI( 81 ),
        ESME_RINVSYSTYP( 83 ),
        ESME_RINVREPFLAG( 84 ),
        ESME_RINVNUMMSGS( 85 ),
        ESME_RTHROTTLED( 88 ),
        ESME_RPROVNOTALLWD( 89 ),
        ESME_RINVSCHED( 97 ),
        ESME_RINVEXPIRY( 98 ),
        ESME_RINVDFTMSGID( 99 ),
        ESME_RX_T_APPN( 100 ),
        ESME_RX_P_APPN( 101 ),
        ESME_RX_R_APPN( 102 ),
        ESME_RQUERYFAIL( 103 ),
        ESME_RINVPGCUSTID( 128 ),
        ESME_RINVPGCUSTIDLEN( 129 ),
        ESME_RINVCITYLEN( 130 ),
        ESME_RINVSTATELEN( 131 ),
        ESME_RINVZIPPREFIXLEN( 132 ),
        ESME_RINVZIPPOSTFIXLEN( 133 ),
        ESME_RINVMINLEN( 134 ),
        ESME_RINVMIN( 135 ),
        ESME_RINVPINLEN( 136 ),
        ESME_RINVTERMCODELEN( 137 ),
        ESME_RINVCHANNELLEN( 138 ),
        ESME_RINVCOVREGIONLEN( 139 ),
        ESME_RINVCAPCODELEN( 140 ),
        ESME_RINVMDTLEN( 141 ),
        ESME_RINVPRIORMSGLEN( 142 ),
        ESME_RINVPERMSGLEN( 143 ),
        ESME_RINVPGALERTLEN( 144 ),
        ESME_RINVSMUSERLEN( 145 ),
        ESME_RINVRTDBLEN( 146 ),
        ESME_RINVREGDELLEN( 147 ),
        ESME_RINVMSGDISTLEN( 148 ),
        ESME_RINVPRIORMSG( 149 ),
        ESME_RINVMDT( 150 ),
        ESME_RINVPERMSG( 151 ),
        ESME_RINVMSGDIST( 152 ),
        ESME_RINVPGALERT( 153 ),
        ESME_RINVSMUSER( 154 ),
        ESME_RINVRTDB( 155 ),
        ESME_RINVREGDEL( 156 ),
        ESME_RINVOPTPARSTREAM( 157 ),
        ESME_ROPTPARNOTALLWD( 158 ),
        ESME_RINVOPTPARLEN( 159 ),
        ESME_RMISSINGOPTPARAM( 195 ),
        ESME_RINVOPTPARAMVAL( 196 ),
        ESME_RDELIVERYFAILURE( 254 ),
        ESME_RUNKNOWNERR( 255 ),
        ESME_LAST_ERROR( 300 );

        /**
         * Value of CommandStatus field in SMPP PDU for this status
         */
        private int status;

        /**
         * Mapping from value of CommandStatus field to corresponding enumeration value
         */
        private static Hashtable<Integer,CommandStatus> reverse =
                new Hashtable<Integer, CommandStatus>();

        /**
         * Constructor
         * @param status Value of CommandStatus field corresponding to this enumeration value
         */
        private CommandStatus( int status )
        {
            this.status = status;
        }

        /**
         * @return Value of CommandStatus field corresponding to this enumeration value
         */
        private int getStatus()
        {
            return status;
        }

        /**
         * Find enumeration value corresponding to value of CommandStatus field
         * @param status Value of CommandStatus field to find
         * @return Value of CommandStatus field corresponding to this enumeration value
         */
        public static CommandStatus find( int status )
        {
            return reverse.get( status );
        }

        /**
         * Populate the mapping of CommandStatus values to enumeration values
         * @param status value of CommandStatus field
         */
        private static void register( CommandStatus status )
        {
            reverse.put( status.getStatus(), status );
        }
    }

    /**
     * Java won't let me populate CommandStatus.reverse from the constructors
     */
    static
    {
        for( CommandStatus status: CommandStatus.values() )
        {
            CommandStatus.register( status );
        }
    }

    /**
     * List of events for this batch run
     */
    private List<Event> events = new ArrayList<Event>();

    /**
     * Add a PDU event
     * @param pdu PDU to be sent
     */
    public void addPDU( PDU pdu )
    {
        if( pdu != null )
        {
            events.add( new PDUEvent( pdu ) );
        }
    }

    /**
     * Add a connection event
     * @param connectionEvent Event details
     */
    public void addConnection( ConnectionEvent connectionEvent )
    {
        if( connectionEvent != null )
        {
            events.add( connectionEvent );
        }
    }

    /**
     * Add a disconnection event
     * @param event Disconnection event
     */
    public void addDisconnection( DisconnectEvent event )
    {
        if( event != null )
        {
            events.add( event );
        }
    }

    /**
     * Add a pause event
     * @param millis Time to pause in milliseconds
     */
    public void addPause( long millis )
    {
        if( millis < 1 )
        {
            throw new IllegalArgumentException( "Pause given a negative value" );
        }

        events.add( new PauseEvent( millis ) );
    }

    /**
     * Create events to enable or disable auto-responses
     * @param pduTypes Type names
     * @param enable   Flag to enable or disable responses for listed PDU types
     */
    public void addAutoResponseSettings( String[] pduTypes, boolean enable )
    {
        if( pduTypes == null )
        {
            return;
        }

        for( String typeName : pduTypes )
        {
            CommandID type;
            try
            {
                type = CommandID.valueOf( typeName );
            }
            catch( IllegalArgumentException e )
            {
                throw new IllegalArgumentException(
                    "Did not recognise PDU type\"" + typeName + "\"", e );
            }
            events.add( new AutoResponseSettingEvent( type, enable ) );
        }
    }

    /**
     * List the events in this batch
     * @return List of events
     */
    public List<Event> getEvents()
    {
        return events;
    }
}
