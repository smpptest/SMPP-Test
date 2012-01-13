/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


public class TestClient
{
    // New PDU button launches a PDU editing screen with send/cancel buttons
    // Log shows connect/disconnect events and PDUS in and out.
    // Double clicking on a PDU displays full details (reply button available).
    // Also right click menu for reply/display.
    private static TestClient ourInstance = new TestClient();

    public static TestClient getInstance() {
        return ourInstance;
    }

    public static void main(String args[])
    {
        TestMainDialog dialog = new TestMainDialog( "SMPP Test Client" );
        dialog.setVisible( true );
    }
}
