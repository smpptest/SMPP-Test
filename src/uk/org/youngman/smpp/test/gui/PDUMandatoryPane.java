/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import org.smpp.Data;
import uk.org.youngman.smpp.test.gui.TestConfig;


/**
 * A panel for displaying/editing the mandatory fields of PDUs.
 */
public class PDUMandatoryPane extends JPanel
{
    public enum Field {
            ADDRESS_RANGE,
            ADDR_NPI,
            ADDR_TON,
            DATA_CODING,
            DESTINATION_ADDR,
            DEST_ADDRESSES,
            DEST_ADDR_NPI,
            DEST_ADDR_TON,
            ERROR_CODE,
            ESME_ADDR,
            ESME_ADDR_NPI,
            ESME_ADDR_TON,
            ESM_CLASS,
            FINAL_DATE,
            INTERFACE_VERSION,
            MESSAGE_ID,
            MESSAGE_STATE,
            NO_UNSUCCESS,
            NUMBER_OF_DESTS,
            PASSWORD,
            PRIORITY_FLAG,
            PROTOCOL_ID,
            REGISTERED_DELIVERY,
            REPLACE_IF_PRESENT_FLAG,
            SCHEDULE_DELIVERY_TIME,
            SERVICE_TYPE,
            UDH_SEQ_REF,
            UDH_SEQ_INDEX,
            UDH_SEQ_TOTAL,
            SHORT_MESSAGE,
            SM_DEFAULT_MSG_ID,
//            SM_LENGTH,
            SOURCE_ADDR,
            SOURCE_ADDR_NPI,
            SOURCE_ADDR_TON,
            SYSTEM_ID,
            SYSTEM_TYPE,
            UNSUCCESS_SMES,
            VALIDITY_PERIOD;
    };

    private static Hashtable<Integer, List<Field>> fieldsTable =
            new Hashtable<Integer, List<Field>>();
    static
    {
        final ArrayList<Field> EMPTY_FIELD_LIST = new ArrayList<Field>();
        final Field[] BIND_FIELDS = { Field.SYSTEM_ID,
                                      Field.PASSWORD,
                                      Field.SYSTEM_TYPE,
                                      Field.INTERFACE_VERSION,
                                      Field.ADDR_TON,
                                      Field.ADDR_NPI,
                                      Field.ADDRESS_RANGE };
        final Field[] BIND_RESP_FIELDS = { Field.SYSTEM_ID };
        fieldsTable.put( Data.BIND_TRANSMITTER,
                         Arrays.asList( BIND_FIELDS ) );
        fieldsTable.put( Data.BIND_TRANSMITTER_RESP,
                         Arrays.asList( BIND_RESP_FIELDS ) );
        fieldsTable.put( Data.BIND_RECEIVER,
                         Arrays.asList( BIND_FIELDS ) );
        fieldsTable.put( Data.BIND_RECEIVER_RESP,
                         Arrays.asList( BIND_RESP_FIELDS ) );
        fieldsTable.put( Data.BIND_TRANSCEIVER,
                         Arrays.asList( BIND_FIELDS ) );
        fieldsTable.put( Data.BIND_TRANSCEIVER_RESP,
                         Arrays.asList( BIND_RESP_FIELDS ) );
        final Field[] OUTBIND_FIELDS = { Field.SYSTEM_ID,
                                         Field.PASSWORD };
        fieldsTable.put( Data.OUTBIND,
                         Arrays.asList( OUTBIND_FIELDS ) );
        fieldsTable.put( Data.UNBIND, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.UNBIND_RESP, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.GENERIC_NACK, EMPTY_FIELD_LIST );
        final Field[] SUBMIT_FIELDS = { Field.SERVICE_TYPE,
                                        Field.SOURCE_ADDR_TON,
                                        Field.SOURCE_ADDR_NPI,
                                        Field.SOURCE_ADDR,
                                        Field.DEST_ADDR_TON,
                                        Field.DEST_ADDR_NPI,
                                        Field.DESTINATION_ADDR,
                                        Field.ESM_CLASS,
                                        Field.PROTOCOL_ID,
                                        Field.PRIORITY_FLAG,
                                        Field.SCHEDULE_DELIVERY_TIME,
                                        Field.VALIDITY_PERIOD,
                                        Field.REGISTERED_DELIVERY,
                                        Field.REPLACE_IF_PRESENT_FLAG,
                                        Field.DATA_CODING,
                                        Field.SM_DEFAULT_MSG_ID,
//                                        Field.SM_LENGTH,
                                        Field.UDH_SEQ_REF,
                                        Field.UDH_SEQ_INDEX,
                                        Field.UDH_SEQ_TOTAL,
                                        Field.SHORT_MESSAGE };
        final Field[] SUBMIT_RESP_FIELDS = { Field.MESSAGE_ID };
        fieldsTable.put( Data.SUBMIT_SM,
                         Arrays.asList( SUBMIT_FIELDS ) );
        fieldsTable.put( Data.SUBMIT_SM_RESP,
                         Arrays.asList( SUBMIT_RESP_FIELDS ) );
        final Field[] SUBMIT_MULTI_FIELDS = { Field.SERVICE_TYPE,
                                              Field.SOURCE_ADDR_TON,
                                              Field.SOURCE_ADDR_NPI,
                                              Field.SOURCE_ADDR,
                                              Field.DEST_ADDR_TON,
                                              Field.DEST_ADDR_NPI,
                                              Field.NUMBER_OF_DESTS,
                                              Field.DEST_ADDRESSES,
                                              Field.ESM_CLASS,
                                              Field.PROTOCOL_ID,
                                              Field.PRIORITY_FLAG,
                                              Field.SCHEDULE_DELIVERY_TIME,
                                              Field.VALIDITY_PERIOD,
                                              Field.REGISTERED_DELIVERY,
                                              Field.REPLACE_IF_PRESENT_FLAG,
                                              Field.DATA_CODING,
                                              Field.SM_DEFAULT_MSG_ID,
//                                              Field.SM_LENGTH,
                                              Field.SHORT_MESSAGE };
        final Field[] SUBMIT_MULTI_RESP_FIELDS = { Field.MESSAGE_ID,
                                                   Field.NO_UNSUCCESS,
                                                   Field.UNSUCCESS_SMES };
        fieldsTable.put( Data.SUBMIT_MULTI,
                         Arrays.asList( SUBMIT_MULTI_FIELDS ) );
        fieldsTable.put( Data.SUBMIT_MULTI_RESP,
                         Arrays.asList( SUBMIT_MULTI_RESP_FIELDS ) );
        fieldsTable.put( Data.DELIVER_SM,
                         Arrays.asList( SUBMIT_FIELDS ) );
        fieldsTable.put( Data.DELIVER_SM_RESP,
                         Arrays.asList( SUBMIT_RESP_FIELDS ) );
        final Field[] DATA_FIELDS = { Field.SERVICE_TYPE,
                                      Field.SOURCE_ADDR_TON,
                                      Field.SOURCE_ADDR_NPI,
                                      Field.SOURCE_ADDR,
                                      Field.DEST_ADDR_TON,
                                      Field.DEST_ADDR_NPI,
                                      Field.DESTINATION_ADDR,
                                      Field.ESM_CLASS,
                                      Field.REGISTERED_DELIVERY,
                                      Field.DATA_CODING };
        final Field[] DATA_RESP_FIELDS = { Field.MESSAGE_ID };
        fieldsTable.put( Data.DATA_SM,
                         Arrays.asList( DATA_FIELDS ) );
        fieldsTable.put( Data.DATA_SM_RESP,
                         Arrays.asList( DATA_RESP_FIELDS ) );
        final Field[] QUERY_FIELDS = { Field.MESSAGE_ID,
                                       Field.SOURCE_ADDR_TON,
                                       Field.SOURCE_ADDR_NPI,
                                       Field.SOURCE_ADDR };
        final Field[] QUERY_RESP_FIELDS = { Field.MESSAGE_ID,
                                            Field.FINAL_DATE,
                                            Field.MESSAGE_STATE,
                                            Field.ERROR_CODE };
        fieldsTable.put( Data.QUERY_SM,
                         Arrays.asList( QUERY_FIELDS ) );
        fieldsTable.put( Data.QUERY_SM_RESP,
                         Arrays.asList( QUERY_RESP_FIELDS ) );
        final Field[] CANCEL_FIELDS = { Field.SERVICE_TYPE,
                                        Field.MESSAGE_ID,
                                        Field.SOURCE_ADDR_TON,
                                        Field.SOURCE_ADDR_NPI,
                                        Field.SOURCE_ADDR,
                                        Field.DEST_ADDR_TON,
                                        Field.DEST_ADDR_NPI,
                                        Field.DESTINATION_ADDR };
        fieldsTable.put( Data.CANCEL_SM,
                         Arrays.asList( CANCEL_FIELDS ) );
        fieldsTable.put( Data.CANCEL_SM_RESP, EMPTY_FIELD_LIST );
        final Field[] REPLACE_FIELDS = { Field.MESSAGE_ID,
                                         Field.SOURCE_ADDR_TON,
                                         Field.SOURCE_ADDR_NPI,
                                         Field.SOURCE_ADDR,
                                         Field.SCHEDULE_DELIVERY_TIME,
                                         Field.VALIDITY_PERIOD,
                                         Field.REGISTERED_DELIVERY,
                                         Field.SM_DEFAULT_MSG_ID,
//                                         Field.SM_LENGTH,
                                         Field.SHORT_MESSAGE };
        fieldsTable.put( Data.REPLACE_SM,
                         Arrays.asList( SUBMIT_FIELDS ) );
        fieldsTable.put( Data.REPLACE_SM_RESP, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.ENQUIRE_LINK, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.ENQUIRE_LINK_RESP, EMPTY_FIELD_LIST );
        final Field[] ALERT_FIELDS = { Field.SOURCE_ADDR_TON,
                                       Field.SOURCE_ADDR_NPI,
                                       Field.SOURCE_ADDR,
                                       Field.ESME_ADDR_TON,
                                       Field.ESME_ADDR_NPI,
                                       Field.ESME_ADDR };
        fieldsTable.put( Data.ALERT_NOTIFICATION,
                         Arrays.asList( ALERT_FIELDS ) );
    }

    private boolean editFlag;
    int type;

    private JTextField addressRange = new JTextField( 20 );
    private JTextField addrNPI = new JTextField( 20 );
    private JTextField addrTON = new JTextField( 20 );
    private JTextField dataCoding = new JTextField( 20 );
    private JTextField destinationAddr = new JTextField( 20 );
    private JTextField destAddresses = new JTextField( 20 );
    private JTextField destAddrNPI = new JTextField( 20 );
    private JTextField destAddrTON = new JTextField( 20 );
    private JTextField errorCode = new JTextField( 20 );
    private JTextField esmeAddr = new JTextField( 20 );
    private JTextField esmeAddrNPI = new JTextField( 20 );
    private JTextField esmeAddrTON = new JTextField( 20 );
    private JTextField esmClass = new JTextField( 20 );
    private JTextField finalDate = new JTextField( 20 );
    private JTextField interfaceVersion = new JTextField( 20 );
    private JTextField messageID = new JTextField( 20 );
    private JTextField messageState = new JTextField( 20 );
    private JTextField noUnsuccess = new JTextField( 20 );
    private JTextField numberOfDests = new JTextField( 20 );
    private JTextField password = new JTextField( 20 );
    private JTextField priorityFlag = new JTextField( 20 );
    private JTextField protocolID = new JTextField( 20 );
    private JTextField registeredDelivery = new JTextField( 20 );
    private JTextField replaceIfPresentFlag = new JTextField( 20 );
    private JTextField scheduleDeliveryTime = new JTextField( 20 );
    private JTextField serviceType = new JTextField( 20 );
    private JTextField udhSeqRef = new JTextField( 20 );
    private JTextField udhSeqIndex = new JTextField( 20 );
    private JTextField udhSeqTotal = new JTextField( 20 );
    private JTextArea shortMessage = new JTextArea( 5, 20 );
    private JTextField smDefaultMsgID = new JTextField( 20 );
    private JTextField sourceAddr = new JTextField( 20 );
    private JTextField sourceAddrNPI = new JTextField( 20 );
    private JTextField sourceAddrTON = new JTextField( 20 );
    private JTextField systemID = new JTextField( 20 );
    private JTextField systemType = new JTextField( 20 );
    private JTextField unsuccessSMEs = new JTextField( 20 );
    private JTextField validityPeriod = new JTextField( 20 );

    Hashtable<Field, JComponent> editTable =
        new Hashtable<Field, JComponent>(  );
    {
        editTable.put( Field.ADDRESS_RANGE, addressRange );
        editTable.put( Field.ADDR_NPI, addrNPI );
        editTable.put( Field.ADDR_TON, addrTON );
        editTable.put( Field.DATA_CODING, dataCoding );
        editTable.put( Field.DESTINATION_ADDR, destinationAddr );
        editTable.put( Field.DEST_ADDRESSES, destAddresses );
        editTable.put( Field.DEST_ADDR_NPI, destAddrNPI );
        editTable.put( Field.DEST_ADDR_TON, destAddrTON );
        editTable.put( Field.ERROR_CODE, errorCode );
        editTable.put( Field.ESME_ADDR, esmeAddr );
        editTable.put( Field.ESME_ADDR_NPI, esmeAddrNPI );
        editTable.put( Field.ESME_ADDR_TON, esmeAddrTON );
        editTable.put( Field.ESM_CLASS, esmClass );
        editTable.put( Field.FINAL_DATE, finalDate );
        editTable.put( Field.INTERFACE_VERSION, interfaceVersion );
        editTable.put( Field.MESSAGE_ID, messageID );
        editTable.put( Field.MESSAGE_STATE, messageState );
        editTable.put( Field.NO_UNSUCCESS, noUnsuccess );
        editTable.put( Field.NUMBER_OF_DESTS, numberOfDests );
        editTable.put( Field.PASSWORD, password );
        editTable.put( Field.PRIORITY_FLAG, priorityFlag );
        editTable.put( Field.PROTOCOL_ID, protocolID );
        editTable.put( Field.REGISTERED_DELIVERY, registeredDelivery );
        editTable.put( Field.REPLACE_IF_PRESENT_FLAG, replaceIfPresentFlag );
        editTable.put( Field.SCHEDULE_DELIVERY_TIME, scheduleDeliveryTime );
        editTable.put( Field.SERVICE_TYPE, serviceType );
        editTable.put( Field.UDH_SEQ_REF, udhSeqRef );
        editTable.put( Field.UDH_SEQ_INDEX, udhSeqIndex );
        editTable.put( Field.UDH_SEQ_TOTAL, udhSeqTotal );
        editTable.put( Field.SHORT_MESSAGE, shortMessage );
        editTable.put( Field.SM_DEFAULT_MSG_ID, smDefaultMsgID );
        editTable.put( Field.SOURCE_ADDR, sourceAddr );
        editTable.put( Field.SOURCE_ADDR_NPI, sourceAddrNPI );
        editTable.put( Field.SOURCE_ADDR_TON, sourceAddrTON );
        editTable.put( Field.SYSTEM_ID, systemID );
        editTable.put( Field.SYSTEM_TYPE, systemType );
        editTable.put( Field.UNSUCCESS_SMES, unsuccessSMEs );
        editTable.put( Field.VALIDITY_PERIOD, validityPeriod );
    }

    /**
     * Constructor
     * @param editFlag Allow this PDU to be edited?
     */
    public PDUMandatoryPane( boolean editFlag )
    {
        this.editFlag = editFlag;
        setBorder( new TitledBorder( "Mandatory Fields" ) );

        for( JComponent component: editTable.values() )
        {
            ((JTextComponent)component).setEditable( editFlag );
        }

        setLayout( new GridBagLayout() );

        TestConfig cfg = TestConfig.getConfig();
        systemID.setText( cfg.getSystemId() );
        systemType.setText( cfg.getSystemType() );
        password.setText( cfg.getPassword() );
    }


    protected void setType( Integer newType )
    {
        if( type == newType )
        {
            // type is unchanged
            return;
        }

        type = newType;

        // Clear the existing fields (if any)
        removeAll();

        // Set up the new fields
        List<Field> fields = fieldsTable.get( type );
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        for( Field field : fields )
        {
            constraints.gridx = 0;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            add( new JLabel( field.toString() ), constraints );
            constraints.gridx = 1;
            constraints.weightx = 0.5;
            add( editTable.get( field ), constraints );

            ++constraints.gridy;
        }

        invalidate();
        revalidate();
    }

    public String getAddressRange()
    {
        return addressRange.getText();
    }

    public byte getAddrNPI()
    {
        return Byte.parseByte( "0" + addrNPI.getText(), 10 );
    }

    public byte getAddrTON()
    {
        return Byte.parseByte( "0" + addrTON.getText(), 10 );
    }

    public byte getDataCoding()
    {
        return Byte.parseByte( "0" + dataCoding.getText(), 10 );
    }

    public String getDestinationAddr()
    {
        return destinationAddr.getText();
    }

    public String getDestAddresses()
    {
        return destAddresses.getText();
    }

    public byte getDestAddrNPI()
    {
        return Byte.parseByte( "0" + destAddrNPI.getText(), 10 );
    }

    public byte getDestAddrTON()
    {
        return Byte.parseByte( "0" + destAddrTON.getText(), 10 );
    }

    public byte getErrorCode()
    {
        return Byte.parseByte( "0" + errorCode.getText(), 10 );
    }

    public String getEsmeAddr()
    {
        return esmeAddr.getText();
    }

    public byte getEsmeAddrNPI()
    {
        return Byte.parseByte( "0" + esmeAddrNPI.getText(), 10 );
    }

    public byte getEsmeAddrTON()
    {
        return Byte.parseByte( "0" + esmeAddrTON.getText(), 10 );
    }

    public byte getEsmClass()
    {
        return Byte.parseByte( "0" + esmClass.getText(), 10 );
    }

    public String getFinalDate()
    {
        return finalDate.getText();
    }

    public byte getInterfaceVersion()
    {
        return Byte.parseByte( "0" + interfaceVersion.getText(), 10 );
    }

    public String getMessageID()
    {
        return messageID.getText();
    }

    public byte getMessageState()
    {
        return Byte.parseByte( "0" + messageState.getText(), 10 );
    }

    public String getNoUnsuccess()
    {
        return noUnsuccess.getText();
    }

    public String getNumberOfDests()
    {
        return numberOfDests.getText();
    }

    public String getPassword()
    {
        return password.getText();
    }

    public byte getPriorityFlag()
    {
        return Byte.parseByte( "0" + priorityFlag.getText(), 10 );
    }

    public byte getProtocolID()
    {
        return Byte.parseByte( "0" + protocolID.getText(), 10 );
    }

    public byte getRegisteredDelivery()
    {
        return Byte.parseByte( "0" + registeredDelivery.getText(), 10 );
    }

    public byte getReplaceIfPresentFlag()
    {
        return Byte.parseByte( "0" + replaceIfPresentFlag.getText(), 10 );
    }

    public String getScheduleDeliveryTime()
    {
        return scheduleDeliveryTime.getText();
    }

    public String getServiceType()
    {
        return serviceType.getText();
    }

    public short getUdhSeqRef()
    {
        if( udhSeqRef.getText().length() > 0 )
        {
            try
            {
                return Short.parseShort( udhSeqRef.getText() );
            }
            catch( NumberFormatException ex )
            {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public short getUdhSeqIndex()
    {
        if( udhSeqIndex.getText().length() > 0 )
        {
            try
            {
                return Short.parseShort( udhSeqIndex.getText() );
            }
            catch( NumberFormatException ex )
            {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public short getUdhSeqTotal()
    {
        if( udhSeqTotal.getText().length() > 0 )
        {
            try
            {
                return Short.parseShort( udhSeqTotal.getText() );
            }
            catch( NumberFormatException ex )
            {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public String getShortMessage()
    {
        return shortMessage.getText();
    }

    public byte getSmDefaultMsgID()
    {
        return Byte.parseByte( "0" + smDefaultMsgID.getText(), 10 );
    }

    public String getSourceAddr()
    {
        return sourceAddr.getText();
    }

    public byte getSourceAddrNPI()
    {
        return Byte.parseByte( "0" + sourceAddrNPI.getText(), 10 );
    }

    public byte getSourceAddrTON()
    {
        return Byte.parseByte( "0" + sourceAddrTON.getText(), 10 );
    }

    public String getSystemID()
    {
        return systemID.getText();
    }

    public String getSystemType()
    {
        return systemType.getText();
    }

    public String getUnsuccessSMEs()
    {
        return unsuccessSMEs.getText();
    }

    public String getValidityPeriod()
    {
        return validityPeriod.getText();
    }

    public void setAddressRange( String addressRange )
    {
        this.addressRange.setText( addressRange );
    }

    public void setAddrNPI( Byte addrNPI )
    {
        this.addrNPI.setText( addrNPI.toString() );
    }

    public void setAddrTON( Byte addrTON )
    {
        this.addrTON.setText( addrTON.toString() );
    }

    public void setDataCoding( Byte dataCoding )
    {
        this.dataCoding.setText( dataCoding.toString() );
    }

    public void setDestinationAddr( String destinationAddr )
    {
        this.destinationAddr.setText( destinationAddr );
    }

    public void setDestAddresses( String destAddresses )
    {
        this.destAddresses.setText( destAddresses );
    }

    public void setDestAddrNPI( Byte destAddrNPI )
    {
        this.destAddrNPI.setText( destAddrNPI.toString() );
    }

    public void setDestAddrTON( Byte destAddrTON )
    {
        this.destAddrTON.setText( destAddrTON.toString() );
    }

    public void setErrorCode( Byte errorCode )
    {
        this.errorCode.setText( errorCode.toString() );
    }

    public void setEsmeAddr( String esmeAddr )
    {
        this.esmeAddr.setText( esmeAddr );
    }

    public void setEsmeAddrNPI( Byte esmeAddrNPI )
    {
        this.esmeAddrNPI.setText( esmeAddrNPI.toString() );
    }

    public void setEsmeAddrTON( Byte esmeAddrTON )
    {
        this.esmeAddrTON.setText( esmeAddrTON.toString() );
    }

    public void setEsmClass( Byte esmClass )
    {
        this.esmClass.setText( esmClass.toString() );
    }

    public void setFinalDate( String finalDate )
    {
        this.finalDate.setText( finalDate );
    }

    public void setInterfaceVersion( Byte interfaceVersion )
    {
        this.interfaceVersion.setText( interfaceVersion.toString() );
    }

    public void setMessageID( String messageID )
    {
        this.messageID.setText( messageID );
    }

    public void setMessageState( Byte messageState )
    {
        this.messageState.setText( messageState.toString() );
    }

    public void setNoUnsuccess( String noUnsuccess )
    {
        this.noUnsuccess.setText( noUnsuccess );
    }

    public void setNumberOfDests( String numberOfDests )
    {
        this.numberOfDests.setText( numberOfDests );
    }

    public void setPassword( String password )
    {
        this.password.setText( password );
    }

    public void setPriorityFlag( Byte priorityFlag )
    {
        this.priorityFlag.setText( priorityFlag.toString() );
    }

    public void setProtocolID( Byte protocolID )
    {
        this.protocolID.setText( protocolID.toString() );
    }

    public void setRegisteredDelivery( Byte registeredDelivery )
    {
        this.registeredDelivery.setText( registeredDelivery.toString() );
    }

    public void setReplaceIfPresentFlag( Byte replaceIfPresentFlag )
    {
        this.replaceIfPresentFlag.setText( replaceIfPresentFlag.toString() );
    }

    public void setScheduleDeliveryTime( String scheduleDeliveryTime )
    {
        this.scheduleDeliveryTime.setText( scheduleDeliveryTime );
    }

    public void setServiceType( String serviceType )
    {
        this.serviceType.setText( serviceType );
    }

    public void setUdhSeqRef( short ref )
    {
        udhSeqRef.setText( Short.toString( ref ) );
    }

    public void setUdhSeqIndex( short index )
    {
        udhSeqIndex.setText( Short.toString( index ) );
    }

    public void setUdhSeqTotal( short total )
    {
        udhSeqTotal.setText( Short.toString( total ) );
    }

    public void setShortMessage( String shortMessage )
    {
        this.shortMessage.setText( shortMessage );
    }

    public void setSmDefaultMsgID( Byte smDefaultMsgID )
    {
        this.smDefaultMsgID.setText( smDefaultMsgID.toString() );
    }

    public void setSourceAddr( String sourceAddr )
    {
        this.sourceAddr.setText( sourceAddr );
    }

    public void setSourceAddrNPI( Byte sourceAddrNPI )
    {
        this.sourceAddrNPI.setText( sourceAddrNPI.toString() );
    }

    public void setSourceAddrTON( Byte sourceAddrTON )
    {
        this.sourceAddrTON.setText( sourceAddrTON.toString() );
    }

    public void setSystemID( String systemID )
    {
        this.systemID.setText( systemID );
    }

    public void setSystemType( String systemType )
    {
        this.systemType.setText( systemType );
    }

    public void setUnsuccessSMEs( String unsuccessSMEs )
    {
        this.unsuccessSMEs.setText( unsuccessSMEs );
    }

    public void setValidityPeriod( String validityPeriod )
    {
        this.validityPeriod.setText( validityPeriod );
    }
}
