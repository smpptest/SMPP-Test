/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SMPPConnectDialog
        extends JDialog
{
    private JButton confirmButton;
    private JButton cancelButton;
    private JTextField connectAddress;
    private JTextField connectPort;

    private boolean cancelled;

    public SMPPConnectDialog( JFrame parent, String title )
    {
        // Lay out sub panels vertically
        super( parent, title, true );
        JPanel mainPanel = new JPanel();
        BoxLayout mainLayout = new BoxLayout( mainPanel, BoxLayout.PAGE_AXIS );
        mainPanel.setLayout( mainLayout );
        setContentPane( mainPanel );

        TestConfig cfg = TestConfig.getConfig();

        // Subpanel for connection address
        connectAddress = new JTextField( 15 );
        connectPort = new JTextField( 15 );
        JPanel topPanel = new JPanel( new FlowLayout() );
        topPanel.add( new JLabel( "Server Address" ) );
        connectAddress.setText( cfg.getServer() );
        topPanel.add( connectAddress );
        topPanel.add( new JLabel( "Server Port" ) );
        topPanel.add( connectPort );
        connectPort.setText( String.valueOf( cfg.getPort() ) );
        mainPanel.add( topPanel );

        // Subpanel for buttons
        JPanel buttonPanel = new JPanel( new FlowLayout() );
        cancelButton = new JButton( "Cancel" );
        cancelButton.addActionListener( new CancelActionListener() );
        confirmButton = new JButton( "Confirm" );
        confirmButton.addActionListener( new ConfirmActionListener() );
        buttonPanel.add( cancelButton );
        buttonPanel.add( confirmButton );
        mainPanel.add( buttonPanel );

        getRootPane().setDefaultButton( confirmButton );

        cancelled = false;

        pack();
    }

    public String getConnectAddress()
    {
        return connectAddress.getText();
    }

    public int getConnectPort()
    {
        return Integer.parseInt( connectPort.getText(), 10 );
    }


    public  boolean isCancelled()
    {
        return cancelled;
    }


    private class CancelActionListener
            implements ActionListener
    {
        public void actionPerformed( ActionEvent actionEvent )
        {
            SMPPConnectDialog.this.dispose();
            cancelled = true;
        }
    }

    private class ConfirmActionListener
            implements ActionListener
    {
        public void actionPerformed( ActionEvent actionEvent )
        {
            SMPPConnectDialog.this.dispose();
            cancelled = false;
        }
    }
}