/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 *
 * The Batch test client reads a batch script in XML and writes a log
 * of all PDUs sent and received. The batch script should contain
 * instructions to connect to an SMSC and send PDUs. Pauses between
 * PDus can be scripted.  PDUs received from the SMSC are logged, but
 * no actions can be scripted for received PDUs, except automated
 * responses to ENQUIRE_LINK and DELIVER_SM PDUs.
 */

package uk.org.youngman.smpp.test.batch;

import java.io.File;


public class BatchClient
{
    private static BatchClient ourInstance = new BatchClient();

    public static BatchClient getInstance() {
        return ourInstance;
    }

    /**
     * The expected arguments are a batch file and a log file
     * @param args calling arguments
     */
    public static void main(String args[])
    {
        if( args.length != 2 )
        {
            System.err.println( "Usage: java BatchClient batch_file log_file" );
            return;
        }

        final File batchFile = new File( args[0] );
        final File logFile = new File( args[1] );

//        SmppObject.getDebug().activate();

        final BatchReader reader = new BatchReader( batchFile );
        final Batch batch = reader.read();
        if( batch == null )
        {
            System.err.println( "Error reading batch. Batch not run." );
            return;
        }

        final BatchRunner runner = new BatchRunner( batch, logFile );
        runner.run();
    }
}
