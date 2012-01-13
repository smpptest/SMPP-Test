/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import org.smpp.Data;
import org.smpp.Session;
import org.smpp.pdu.PDU;
import org.smpp.pdu.Response;
import org.smpp.pdu.ValueNotSetException;
import uk.org.youngman.smpp.test.SMPPEvent;


import static uk.org.youngman.smpp.test.SMPPEvent.EventType;
import static uk.org.youngman.smpp.test.SMPPEvent.TIMESTAMP_FORMAT;


public class SMPPEventLog
{
    private Vector<SMPPEvent> events = new Vector<SMPPEvent>();
    private JTextArea displayArea = new JTextArea( 8, 50 );
    private JFrame parent;
    private Session session = null;

    SMPPEventLog( JFrame parent )
    {
        this.parent = parent;

        displayArea.setEditable( false );
        displayArea.addMouseListener( new MouseAdapter()
        {
            @Override public void mouseClicked( MouseEvent mouseEvent )
            {
                if( mouseEvent.getClickCount() == 2 ||
                        mouseEvent.isPopupTrigger() )
                {
                    Point p = mouseEvent.getPoint();
                    int line = 0;
                    try
                    {
                        line = displayArea.getLineOfOffset(
                                displayArea.viewToModel( p ) );
                    }
                    catch( BadLocationException e )
                    {
                        // Ignore clicks outside log area
                        return;
                    }

                    if( mouseEvent.getClickCount() == 2 )
                    {
                        displayEvent( line );
                    }
                    else if( mouseEvent.isPopupTrigger() )
                    {
                        // popup menu potentially offering display event/send ack
                        displayPopup( line, mouseEvent );
                    }
                }
            }

            @Override public void mousePressed( MouseEvent mouseEvent )
            {
                if( mouseEvent.isPopupTrigger() )
                {
                    // popup menu potentially offering display event/send ack
                    displayPopup( mouseEvent );
                }
            }

            @Override public void mouseReleased( MouseEvent mouseEvent )
            {
                if( mouseEvent.isPopupTrigger() )
                {
                    // popup menu potentially offering display event/send ack
                    displayPopup( mouseEvent );
                }
            }
        } );
    }


    /**
     * Add an event to the Event Log
     * @param time Time of event
     * @param type Type of event
     * @param pdu  PDU associated with event (may be null)
     * @param message Message describing event details
     */
    void logEvent(
            Date time, SMPPEvent.EventType type, PDU pdu, String message )
    {
        displayArea.append( TIMESTAMP_FORMAT.format( time ) + " " +
                                    message + "\n" );
        events.add( new SMPPEvent( time, type, pdu, message ) );
    }


    /**
     * Creates a dialog displaying the event at a given line in the log
     * @param line Line number of event to display
     */
    private void displayEvent( int line )
    {
        SMPPEventDialog dialog = new SMPPEventDialog(
            parent, events.elementAt( line ) );
        dialog.setVisible( true );
    }


    /**
     * Creates a popup menu offering options appropriate to the event
     * at a given line in the log
     * @param mouseEvent mouseEvent triggering popup
     */
    private void displayPopup( MouseEvent mouseEvent )
    {
        Point p = mouseEvent.getPoint();
        int line = 0;
        try
        {
            line = displayArea.getLineOfOffset(
                    displayArea.viewToModel( p ) );
        }
        catch( BadLocationException e )
        {
            // Ignore clicks outside log area
            return;
        }

        displayPopup( line, mouseEvent );
    }


    /**
     * Creates a popup menu offering options appropriate to the event
     * at a given line in the log
     * @param line Line number of event to display
     * @param mouseEvent mouseEvent triggering popup
     */
    private void displayPopup( final int line, MouseEvent mouseEvent )
    {
        // Create a popup menu
        JPopupMenu menu = new JPopupMenu();

        // Add a "display" option
        JMenuItem item = new JMenuItem( "Display" );
        item.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                displayEvent( line );
            }
        } );
        menu.add( item );

        // If this is a request PDU add a "respond" option
        SMPPEvent smppEvent = events.elementAt( line );
        final PDU pdu = smppEvent.getPdu();
        if( smppEvent.getType() == EventType.RECEIVED_PDU && pdu.isRequest() )
        {
            item = new JMenuItem( "Respond" );
            item.addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent actionEvent )
                {
                    sendResponse( pdu );
                }
            } );
        }
        menu.add( item );

        menu.show( getDisplayArea(), mouseEvent.getX(), mouseEvent.getY() );
    }

    
    /**
     * Send a response to a request PDU and add response to this EventLog. If
     * the session is not bound or the pdu is not a request then no response
     * is sent.
     * @param request request for which a response is sent
     */
    private void sendResponse( final PDU request )
    {
        if( session == null )
        {
            System.out.println( "session null" );
            return;
        }

        final Response response = createResponse( request );
        if( response == null )
        {
            System.out.println( "Null response" );
            return;
        }

        try
        {
            session.getConnection().send( response.getData() );
            System.out.println( "Sent response " + response.debugString() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        catch( ValueNotSetException e )
        {
            e.printStackTrace();
        }

        logEvent( new Date(),
                  EventType.SENT_PDU,
                  response,
                  "Sent PDU, seq=" + response.getSequenceNumber() +
                  ", type=" + PDUHeaderPane.CommandID.find(
                          response.getCommandId() ) +
                  ", status=" + PDUHeaderPane.CommandStatus.find(
                          response.getCommandStatus() ) );
    }


    /**
     * Creates a response PDU matching the request PDU passed as a
     * parameter. The sequence number of the request is copied into
     * the created response. In the event that the PDU passed in is
     * null or is not a request then null may be returned.
     *
     * Does this belong in a utils class rather than in SMPPEventLog?
     *
     * @param request request to which a response is created
     * @return response pdu or null
     */
    private Response createResponse( PDU request )
    {
        if( null == request || !request.isRequest() )
        {
            // Should be a request, if not return null
            return null;
        }

        PDU response = null;
        switch( request.getCommandId() )
        {
        case Data.BIND_RECEIVER:
            response = PDU.createPDU( Data.BIND_RECEIVER_RESP );
            break;
        case Data.BIND_TRANSMITTER:
            response = PDU.createPDU( Data.BIND_TRANSMITTER_RESP );
            break;
        case Data.QUERY_SM:
            response = PDU.createPDU( Data.QUERY_SM_RESP );
            break;
        case Data.SUBMIT_SM:
            response = PDU.createPDU( Data.SUBMIT_SM_RESP );
            break;
        case Data.DELIVER_SM:
            response = PDU.createPDU( Data.DELIVER_SM_RESP );
            break;
        case Data.UNBIND:
            response = PDU.createPDU( Data.UNBIND_RESP );
            break;
        case Data.REPLACE_SM:
            response = PDU.createPDU( Data.REPLACE_SM_RESP );
            break;
        case Data.CANCEL_SM:
            response = PDU.createPDU( Data.CANCEL_SM_RESP );
            break;
        case Data.BIND_TRANSCEIVER:
            response = PDU.createPDU( Data.BIND_TRANSCEIVER_RESP );
            break;
        case Data.ENQUIRE_LINK:
            response = PDU.createPDU( Data.ENQUIRE_LINK_RESP );
            break;
        case Data.DATA_SM:
            response = PDU.createPDU( Data.DATA_SM_RESP );
            break;
        }
        if( null != response )
        {
            response.setSequenceNumber( request.getSequenceNumber() );
        }

        return (Response)response;
    }


    /**
     * Set the current session
     * @param session session to set as current session (may be null)
     */
    public void setSession( Session session )
    {
        this.session = session;
    }


    /**
     * Get a component that displays the log, so it can be included in
     * relevant dialogs.
     * @return JComponent
     */
    JComponent getDisplayArea()
    {
        return displayArea;
    }
}
