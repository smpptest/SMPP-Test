/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import uk.org.youngman.smpp.test.SMPPEvent;


public class SMPPEventDialog
        extends JDialog
{
    public SMPPEventDialog( JFrame frame, SMPPEvent event )
    {
        super( frame, "SMPP Event" );

        setDefaultCloseOperation( DISPOSE_ON_CLOSE );

        JPanel mainPanel = new JPanel();
        BoxLayout layout = new BoxLayout( mainPanel, BoxLayout.PAGE_AXIS );
        mainPanel.setLayout( layout );
        setContentPane( mainPanel );

        JPanel messagePanel = new JPanel( new FlowLayout() );
        messagePanel.add(
            new Label( SMPPEvent.TIMESTAMP_FORMAT.format( event.getTime() ) ) );
        JTextField messageField = new JTextField( 30 );
        messagePanel.add( messageField );
        messageField.setText( event.getMessage() );
        messageField.setEditable( false );
        messageField.setEnabled( false );
        mainPanel.add( messagePanel );

        PDUPane pduPane = new PDUPane( event.getPdu(), false );
        mainPanel.add( pduPane );

        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton( "Close" );
        buttonPanel.add( closeButton );
        closeButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                SMPPEventDialog.this.setVisible( false );
                SMPPEventDialog.this.dispose();
            }
        } );
        mainPanel.add( buttonPanel );

        pack();
    }
}
