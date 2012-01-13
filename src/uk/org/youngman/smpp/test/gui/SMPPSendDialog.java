/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import org.smpp.Session;
import org.smpp.pdu.PDU;
import uk.org.youngman.smpp.test.SMPPEvent;


public class SMPPSendDialog
    extends JDialog
{
    public SMPPSendDialog( final Frame parent,
                           final Session session,
                           final SMPPEventLog eventLog  )
    {
        super( parent, "Send PDU", true );

        setDefaultCloseOperation( HIDE_ON_CLOSE );

        JPanel mainPanel = new JPanel();
        BoxLayout layout = new BoxLayout( mainPanel, BoxLayout.PAGE_AXIS );
        mainPanel.setLayout( layout );
        setContentPane( mainPanel );

        final PDUPane pduPane = new PDUPane( null, true );
        mainPanel.add( pduPane );

        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton( "Close" );
        buttonPanel.add( closeButton );
        closeButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                SMPPSendDialog.this.setVisible( false );
                SMPPSendDialog.this.dispose();
            }
        } );

        JButton sendButton = new JButton( "Send" );
        sendButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                try
                {
                    final PDU pdu = pduPane.getPDU();
                    if( pdu != null )
                    {
                        session.getConnection().send( pdu.getData() );
                        eventLog.logEvent(
                            new Date(),
                            SMPPEvent.EventType.SENT_PDU,
                            pdu,
                            "Sent PDU, seq=" + pdu.getSequenceNumber() +
                                    ", type=" + pduPane.getPDUType() );
                        pduPane.incrementSeqNumber();
                        dispose();
                    }
                }
                catch( Exception ex )
                {
                    JOptionPane.showMessageDialog(
                            SMPPSendDialog.this,
                            "Caught Exception: " + ex,
                            "Exception",
                            JOptionPane.ERROR_MESSAGE );
                    System.err.println( ex );
                    ex.printStackTrace();
                }
            }
        } );
        buttonPanel.add( sendButton );

        mainPanel.add( buttonPanel );
        getRootPane().setDefaultButton( sendButton );

        pack();
    }
}
