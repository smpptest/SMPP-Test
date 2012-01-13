/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.batch;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.smpp.*;
import org.smpp.pdu.DeliverSMResp;
import org.smpp.pdu.EnquireLinkResp;
import org.smpp.pdu.PDU;
import uk.org.youngman.smpp.test.SMPPEvent;


import static uk.org.youngman.smpp.test.SMPPEvent.EventType.SENT_PDU;


public class BatchRunner
{
    private Batch batch;
    private BatchEventLog eventLog;
    BatchServerPDUEventListener listener;

    BatchRunner( Batch batch, File logFile )
    {
        this.batch = batch;
        this.eventLog = new BatchEventLog( logFile );
    }

    void run()
    {
        Connection connection = null;
        int seq = 1;
        for( Batch.Event event: batch.getEvents() )
        {
            if( event instanceof ConnectionEvent )
            {
                ConnectionEvent connectionEvent = (ConnectionEvent)event;
                if( connection != null && connection.isOpened() )
                {
                    eventLog.logEvent( new Date(),
                                       SMPPEvent.EventType.CONNECT_TO_SMSC,
                                       null,
                                       "Connect failed (already connected) to "
                                           + connectionEvent.getAddress() +
                                           ":" + connectionEvent.getPort() );
                    continue;
                }
                connection = new TCPIPConnection(
                    connectionEvent.getAddress(), connectionEvent.getPort() );
                Receiver receiver = new Receiver( connection );
                listener = new BatchServerPDUEventListener();
                receiver.setServerPDUEventListener( listener );
                try
                {
                    connection.open();
                    eventLog.logEvent( new Date(),
                                       SMPPEvent.EventType.CONNECT_TO_SMSC,
                                       null,
                                       "Connected to " + connectionEvent.getAddress() +
                                           ":" + connectionEvent.getPort() );
                }
                catch( IOException e )
                {
                    System.err.println( "Exception opening connection" );
                    e.printStackTrace();
                }
                receiver.start();
            }
            else if( event instanceof Batch.DisconnectEvent )
            {
                if( connection != null && connection.isOpened() )
                {
                    try
                    {
                        connection.close();
                        eventLog.logEvent( new Date(),
                                           SMPPEvent.EventType.DISCONNECT_FROM_SMSC,
                                           null,
                                           "Disconnected" );
                    }
                    catch( IOException e )
                    {
                        System.err.println( "Exception closing connection" );
                        e.printStackTrace();
                    }
                    connection = null;
                }
                else
                {
                    eventLog.logEvent( new Date(),
                                       SMPPEvent.EventType.CONNECT_TO_SMSC,
                                       null,
                                       "Disconnect failed (not connected)" );
                    connection = null;
                }
            }
            else if( event instanceof Batch.PDUEvent )
            {
                final PDU pdu = ((Batch.PDUEvent)event).getPdu();
                if( connection == null )
                {
                    eventLog.logEvent(
                        new Date(),
                        SMPPEvent.EventType.SENT_PDU,
                        pdu,
                        "PDU not sent (not connected), seq = " + pdu.getSequenceNumber() +
                            ", type=" + Batch.CommandID.find( pdu.getCommandId() ) +
                            ", status=" +
                            Batch.CommandStatus.find( pdu.getCommandStatus() ) +
                            pdu.debugString() );
                    continue;
                }

                if( pdu.getSequenceNumber() < seq )
                {
                    pdu.setSequenceNumber( seq++ );
                }

                try
                {
                    connection.send( pdu.getData() );
                    eventLog.logEvent(
                        new Date(),
                        SMPPEvent.EventType.SENT_PDU,
                        pdu,
                        "Sent PDU, seq = " + pdu.getSequenceNumber() +
                            ", type=" + Batch.CommandID.find( pdu.getCommandId() ) +
                            ", status=" +
                            Batch.CommandStatus.find( pdu.getCommandStatus() ) +
                            pdu.debugString() );
                }
                catch( Exception e )
                {
                    System.err.println(
                            "exception caught trying to send PDU " + pdu.debugString() );
                    e.printStackTrace();
                    return;
                }
            }
            else if( event instanceof Batch.PauseEvent )
            {
                try
                {
                    final long millis = ((Batch.PauseEvent) event).getMillis();
//                    System.out.println( "Pausing for " + millis/1000.0 + " secs" );
                    Thread.sleep( millis );
                }
                catch( InterruptedException e )
                {
                    System.err.println( "Pause interrupted" );
                }
            }
            else if( event instanceof Batch.AutoResponseSettingEvent )
            {
                    Batch.AutoResponseSettingEvent settingEvent =
                        (Batch.AutoResponseSettingEvent) event;
                    listener.setAutoResponses( settingEvent.getPduType().getId(),
                                               settingEvent.isEnable() );
            }
            else
            {
                System.err.println( "Unknown event type: " + event.getClass() );
            }
        }

        if( connection != null )
        {
            try
            {
                connection.close();
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * This event listener logs all incoming PDUs and sends responses to
     * ENQUIRE_LINK PDUs.
     */
    private class BatchServerPDUEventListener
            implements ServerPDUEventListener
    {
        /**
         * Set of PDU types for which we may automatically generate responses
         */
        Set<Integer> autoResponses = new HashSet<Integer>(  );


        /**
         * Default constructor
         */
        BatchServerPDUEventListener()
        {
            // Auto-respond to ENQUIRE_LINK and DELIVER_SM by default
            autoResponses.add( Data.ENQUIRE_LINK );
            autoResponses.add( Data.DELIVER_SM );
        }


        synchronized public void handleEvent( ServerPDUEvent event )
        {
            final PDU pdu = event.getPDU();
            eventLog.logEvent(
                new Date(),
                SMPPEvent.EventType.RECEIVED_PDU,
                pdu,
                "Received PDU, seq=" + pdu.getSequenceNumber() +
                    ", type=" +
                    Batch.CommandID.find( pdu.getCommandId() ) +
                    ", status=" +
                    Batch.CommandStatus.find( pdu.getCommandStatus() ) );
            if( pdu.getCommandId()== Data.ENQUIRE_LINK &&
                autoResponses.contains( Data.ENQUIRE_LINK ) )
            {
                EnquireLinkResp response = new EnquireLinkResp();
                response.setSequenceNumber(
                    pdu.getSequenceNumber() );
                response.setCommandStatus( Data.ESME_ROK );
                try
                {
                    event.getConnection().send(
                        response.getData() );
                }
                catch( Exception e )
                {
                    System.err.println(
                        "Exception sending Enquire Link Response" );
                    e.printStackTrace();
                }
                eventLog.logEvent(
                        new Date(),
                        SMPPEvent.EventType.SENT_PDU,
                        response,
                        "Sent PDU, seq= " + pdu.getSequenceNumber() +
                                ", type=" + Batch.CommandID.find(
                                response.getCommandId() ) +
                                ", status=" + Batch.CommandStatus.find(
                                response.getCommandStatus() ) );
            }
            if( pdu.getCommandId()== Data.DELIVER_SM &&
                autoResponses.contains( Data.DELIVER_SM ) )
            {
                DeliverSMResp response = new DeliverSMResp();
                response.setSequenceNumber(
                    pdu.getSequenceNumber() );
                response.setCommandStatus( Data.ESME_ROK );
                try
                {
                    event.getConnection().send(
                        response.getData() );
                }
                catch( Exception e )
                {
                    System.err.println(
                        "Exception sending DeliverSM Response" );
                    e.printStackTrace();
                }
                eventLog.logEvent(
                    new Date(),
                    SENT_PDU,
                    response,
                    "Sent PDU, seq= " + pdu.getSequenceNumber() +
                        ", type=" + Batch.CommandID.find(
                                    response.getCommandId() ) +
                        ", status=" + Batch.CommandStatus.find(
                                    response.getCommandStatus() ) );
            }
        }

        /**
         * Enable or disable automatic responses to some request PDUs
         * @param pduType PDU type to enable/disable
         * @param enable  Enable or disable
         */
        synchronized void setAutoResponses( int pduType, boolean enable )
        {
            if( enable )
            {
                if( autoResponses.add( pduType ) )
                {
                    // System.out.println( "Enabled auto responses for " + pduType );
                }
                else
                {
                    // System.out.println( "auto responses for " + pduType +
                    //                     "not enabled or already enabled" );
                }
            }
            else
            {
                if( autoResponses.remove( pduType ) )
                {
                    // System.out.println( "Disabled auto responses for " +
                    //                             pduType );
                }
                else
                {
                    // System.out.println( "auto responses for " + pduType +
                    //                     "not disabled or already disabled" );
                }
            }
        }
    }
}
