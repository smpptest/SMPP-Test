/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */


package uk.org.youngman.smpp.test.gui;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.smpp.*;
import org.smpp.pdu.DeliverSMResp;
import org.smpp.pdu.EnquireLinkResp;
import org.smpp.pdu.PDU;

import static javax.swing.JOptionPane.showMessageDialog;
import static uk.org.youngman.smpp.test.SMPPEvent.EventType.*;


public class TestMainDialog
        extends JFrame
{
    private Session session = null;
    private Receiver receiver = null;
    private TestServerPDUEventListener eventListener = null;

    private JButton m_ConnectButton;
    private JButton m_DisconnectButton;
    private JButton m_SendButton;
    private JButton m_ExitButton;
    private JCheckBox enquireLinkResponses;
    private JCheckBox deliverSMResponses;

    private JScrollPane m_ScrollPane;

    private SMPPEventLog m_EventLog = new SMPPEventLog( this );

    SMPPSendDialog sendDialog = null;

    public TestMainDialog( String title ) throws HeadlessException
    {
        super( title );

        this.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );

        JPanel mainPanel = new JPanel( new BorderLayout() );

        // The button panel will contain buttons to connect, disconnect, send a PDU and quit
        JPanel buttonPanel = new JPanel( new FlowLayout() );
        m_ConnectButton = new JButton( "Connect" );
        buttonPanel.add( m_ConnectButton );
        m_ConnectButton.setEnabled( true );
        m_ConnectButton.addActionListener( new ConnectActionListener() );

        m_DisconnectButton = new JButton( "Disconnect" );
        buttonPanel.add( m_DisconnectButton );
        m_DisconnectButton.setEnabled( false );
        m_DisconnectButton.addActionListener( new DisconnectActionListener() );

        m_SendButton = new JButton( "Send PDU" );
        buttonPanel.add( m_SendButton );
        m_SendButton.setEnabled( false );
        m_SendButton.addActionListener( new SendActionListener() );

        m_ExitButton = new JButton( "Exit" );
        buttonPanel.add( m_ExitButton );
        m_ExitButton.setEnabled( true );
        m_ExitButton.addActionListener( new ExitActionListener() );

        mainPanel.add( buttonPanel, BorderLayout.NORTH );

        JPanel logPanel = new JPanel();
        m_ScrollPane = new JScrollPane( m_EventLog.getDisplayArea() );
        logPanel.add( m_ScrollPane );
        mainPanel.add( logPanel, BorderLayout.CENTER );

        getRootPane().setDefaultButton( m_ConnectButton );

        // Settings panel initially for setting which PDU types get automatic
        // responses, but other settings may be added later.
        JPanel settingsPanel = new JPanel(  );

        settingsPanel.setBorder( new TitledBorder( "Automatic responses" ) );
        enquireLinkResponses = new JCheckBox( "Enquire Link" );
        enquireLinkResponses.setSelected( true );
        settingsPanel.add( enquireLinkResponses );
        enquireLinkResponses.addChangeListener( new ChangeListener()
        {
            public void stateChanged( ChangeEvent changeEvent )
            {
                if( eventListener != null )
                {
                    eventListener.setAutoResponses(
                        Data.ENQUIRE_LINK, enquireLinkResponses.isSelected() );
                }
            }
        } );

        deliverSMResponses = new JCheckBox( "DeliverSM" );
        deliverSMResponses.setSelected( true );
        settingsPanel.add( deliverSMResponses );
        deliverSMResponses.addChangeListener( new ChangeListener()
        {
            public void stateChanged( ChangeEvent changeEvent )
            {
                if( eventListener != null )
                {
                    eventListener.setAutoResponses(
                        Data.DELIVER_SM, deliverSMResponses.isSelected() );
                }
            }
        } );

        mainPanel.add( settingsPanel, BorderLayout.SOUTH );

        // Set the main panel and lay it out
        super.setContentPane( mainPanel );
        pack();
    }


    /**
     * This ActionListener creates a connection when the Connect button is
     * clicked.
     */
    private class ConnectActionListener
            implements ActionListener
    {
        public void actionPerformed( ActionEvent actionEvent )
        {
            m_ConnectButton.setEnabled( false );
            SMPPConnectDialog dialog = new SMPPConnectDialog(
                    TestMainDialog.this, "Connect" );
            dialog.setVisible( true );
            if( !dialog.isCancelled() )
            {
                TCPIPConnection connection = null;
                try
                {
                    connection = new TCPIPConnection(
                            dialog.getConnectAddress(), dialog.getConnectPort() );
                    connection.open();
                    if( connection.isOpened() )
                    {
                        session = new Session( connection );
                        m_EventLog.setSession( session );
                        m_EventLog.logEvent( new Date(), CONNECT_TO_SMSC, null,
                                             "Connected to " + dialog
                                                     .getConnectAddress() + ":" + dialog
                                                     .getConnectPort() );

                        receiver = new Receiver( connection );
                        eventListener = new TestServerPDUEventListener();
                        eventListener.setAutoResponses(
                            Data.DELIVER_SM, deliverSMResponses.isEnabled() );
                        eventListener.setAutoResponses(
                            Data.ENQUIRE_LINK, enquireLinkResponses.isEnabled() );
                        receiver.setServerPDUEventListener( eventListener );
                        receiver.start();
                        sendDialog = new SMPPSendDialog(
                            TestMainDialog.this, session, m_EventLog );
                        m_DisconnectButton.setEnabled( true );
                        getRootPane().setDefaultButton( m_SendButton );
                        m_SendButton.setEnabled( true );
                    }
                    else
                    {
                        m_EventLog.logEvent(
                            new Date(),
                            CONNECT_TO_SMSC,
                            null,
                            "Connection failed to " + dialog.getConnectAddress()
                                      + ":" + dialog.getConnectPort() );
                        showMessageDialog(
                                TestMainDialog.this, "Connection failed" );
                        m_ConnectButton.setEnabled( true );
                    }
                }
                catch( Exception e )
                {
                    m_EventLog.logEvent( new Date(),
                                  CONNECT_TO_SMSC,
                                  null,
                                  "Exception connecting to " +
                                          dialog.getConnectAddress() + ":" +
                                          dialog.getConnectPort() + " (" +
                                          e.getMessage() + ")" );
                    showMessageDialog(
                            TestMainDialog.this,
                            "Exception caught while connecting:\n" + e );
                    System.err.println( e );
                    e.printStackTrace();
                    m_ConnectButton.setEnabled( true );
                    if( null != connection && connection.isOpened() )
                    {
                        // Just to be sure
                        try
                        {
                            connection.close();
                        }
                        catch( IOException ie )
                        {
                            System.err.println(
                                    "Exception closing connection" );
                            ie.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    /**
     * This ActionListener closes th connection when the Disconnect button is
     * clicked.
     */
    private class DisconnectActionListener
            implements ActionListener
    {
        public void actionPerformed( ActionEvent actionEvent )
        {
            receiver.stop();
            try
            {
                session.getConnection().close();
                m_EventLog.setSession( null );
                m_EventLog.logEvent( new Date(),
                          DISCONNECT_FROM_SMSC,
                          null,
                          "Session Disconnected" );
                eventListener = null;
            }
            catch( Exception e )
            {
                m_EventLog.logEvent( new Date(),
                          DISCONNECT_FROM_SMSC,
                          null,
                          "Exception caught while disconnecting:\n" + e.getMessage() );
                showMessageDialog(
                        TestMainDialog.this,
                        "Exception caught while disconnecting:\n" + e.getMessage() );
                System.err.println( e );
                e.printStackTrace();
            }
            m_ConnectButton.setEnabled( true );
            m_DisconnectButton.setEnabled( false );
            m_SendButton.setEnabled( false );

            getRootPane().setDefaultButton( m_ConnectButton );
        }
    }

    /**
     * This ActionListener creates a Send Dialog when the Send button is
     * clicked.
     */
    private class SendActionListener
            implements ActionListener
    {

        public void actionPerformed( ActionEvent actionEvent )
        {
            sendDialog.setVisible( true );
        }
    }


    /**
     * This ActionListener disposes of the main dialog when the Exit button
     * is clicked.
     */
    private class ExitActionListener
            implements ActionListener
    {
        public void actionPerformed( ActionEvent actionEvent )

        {
            if( receiver != null )
            {
                receiver.stop();
            }
            TestMainDialog.this.setVisible( false );
            TestMainDialog.this.dispose();
        }
    }


    private class TestServerPDUEventListener
            implements ServerPDUEventListener
    {
        Set<Integer> autoResponses = new HashSet<Integer>(  );

        synchronized public void handleEvent( ServerPDUEvent event )
        {
            final PDU pdu = event.getPDU();
            m_EventLog.logEvent(
                new Date(),
                RECEIVED_PDU,
                pdu,
                "Received PDU, seq= " + pdu.getSequenceNumber() +
                    ", type=" + PDUHeaderPane.CommandID.find(
                            pdu.getCommandId() ) +
                    ", status=" + PDUHeaderPane.CommandStatus.find(
                            pdu.getCommandStatus() ) );
            if( pdu.getCommandId()== Data.ENQUIRE_LINK &&
                autoResponses.contains( Data.ENQUIRE_LINK ) )
            {
                EnquireLinkResp response = new EnquireLinkResp();
                response.setSequenceNumber(
                    pdu.getSequenceNumber() );
                response.setCommandStatus( Data.ESME_ROK );
                try
                {
                    event.getConnection().send( response.getData() );
                }
                catch( Exception e )
                {
                    showMessageDialog(
                        TestMainDialog.this,
                        "Exception caught while sending Enquire Link Response\n" +
                        e.getMessage() );
                    System.err.println(
                        "Exception sending Enquire Link Response" );
                    e.printStackTrace();
                }
                m_EventLog.logEvent(
                    new Date(),
                    SENT_PDU,
                    response,
                    "Sent PDU, seq= " + pdu.getSequenceNumber() +
                        ", type=" + PDUHeaderPane.CommandID.find(
                                    response.getCommandId() ) +
                        ", status=" + PDUHeaderPane.CommandStatus.find(
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
                    event.getConnection().send( response.getData() );
                }
                catch( Exception e )
                {
                    showMessageDialog(
                        TestMainDialog.this,
                        "Exception caught while sending DeliverSM Response\n" +
                        e.getMessage() );
                    System.err.println(
                        "Exception sending DeliverSM Response" );
                    e.printStackTrace();
                }
                m_EventLog.logEvent(
                    new Date(),
                    SENT_PDU,
                    response,
                    "Sent PDU, seq= " + pdu.getSequenceNumber() +
                        ", type=" + PDUHeaderPane.CommandID.find(
                                    response.getCommandId() ) +
                        ", status=" + PDUHeaderPane.CommandStatus.find(
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
                autoResponses.add( pduType );
            }
            else
            {
                autoResponses.remove( pduType );
            }
        }
    }
}
