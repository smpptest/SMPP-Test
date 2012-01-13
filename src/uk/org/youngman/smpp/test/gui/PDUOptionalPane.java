/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import org.smpp.Data;
import org.smpp.util.ByteBuffer;


/**
 * A panel for displaying/editing the optional fields of PDUs.
 */
public class PDUOptionalPane extends JPanel
{
    public enum Field {
            ADDITIONAL_STATUS_INFO_TEXT,
            ALERT_ON_MSG_DELIVERY,
            CALLBACK_NUM,
            CALLBACK_NUM_ATAG,
            CALLBACK_NUM_PRES_IND,
            DELIVERY_FAILURE_REASON,
            DESTINATION_PORT,
            DEST_ADDR_SUBUNIT,
            DEST_BEARER_TYPE,
            DEST_NETWORK_TYPE,
            DEST_SUBADDRESS,
            DEST_TELEMATICS_ID,
            DISPLAY_TIME,
            DPF_RESULT,
            ITS_REPLY_TYPE,
            ITS_SESSION_INFO,
            LANGUAGE_INDICATOR,
            MESSAGE_PAYLOAD,
            MESSAGE_STATE,
            MORE_MESSAGES_TO_SEND,
            MS_AVAILABILITY_STATUS,
            MS_MSG_WAIT_FACILITIES,
            MS_VALIDITY,
            NETWORK_ERROR_CODE,
            NUMBER_OF_MESSAGES,
            PAYLOAD_TYPE,
            PRIVACY_INDICATOR,
            QOS_TIME_TO_LIVE,
            RECEIPTED_MESSAGE_ID,
            SAR_MSG_REF_NUM,
            SAR_SEGMENT_SEQNUM,
            SAR_TOTAL_SEGMENTS,
            SC_INTERFACE_VERSION,
            SET_DPF,
            SMS_SIGNAL,
            SOURCE_ADDR_SUBUNIT,
            SOURCE_BEARER_TYPE,
            SOURCE_NETWORK_TYPE,
            SOURCE_PORT,
            SOURCE_SUBADDRESS,
            SOURCE_TELEMATICS_ID,
            USER_MESSAGE_REFERENCE,
            USER_RESPONSE_CODE,
            USSD_SERVICE_OP }

    private static Hashtable<Integer, List<Field>> fieldsTable =
        new Hashtable<Integer, List<Field>>();
    static
    {
        final ArrayList<Field> EMPTY_FIELD_LIST = new ArrayList<Field>();
        final Field[] BIND_RESP_FIELDS = { Field.SC_INTERFACE_VERSION };
        fieldsTable.put( Data.BIND_TRANSMITTER, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.BIND_TRANSMITTER_RESP,
                         Arrays.asList( BIND_RESP_FIELDS ) );
        fieldsTable.put( Data.BIND_RECEIVER, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.BIND_RECEIVER_RESP,
                         Arrays.asList( BIND_RESP_FIELDS ) );
        fieldsTable.put( Data.BIND_TRANSCEIVER, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.BIND_TRANSCEIVER_RESP,
                         Arrays.asList( BIND_RESP_FIELDS ) );
        fieldsTable.put( Data.OUTBIND, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.UNBIND, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.UNBIND_RESP, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.GENERIC_NACK, EMPTY_FIELD_LIST );
        final Field[] SUBMIT_FIELDS = { Field.USER_MESSAGE_REFERENCE,
                                        Field.SOURCE_PORT,
                                        Field.SOURCE_ADDR_SUBUNIT,
                                        Field.DESTINATION_PORT,
                                        Field.DEST_ADDR_SUBUNIT,
                                        Field.SAR_MSG_REF_NUM,
                                        Field.SAR_TOTAL_SEGMENTS,
                                        Field.SAR_SEGMENT_SEQNUM,
                                        Field.MORE_MESSAGES_TO_SEND,
                                        Field.PAYLOAD_TYPE,
                                        Field.MESSAGE_PAYLOAD,
                                        Field.PRIVACY_INDICATOR,
                                        Field.CALLBACK_NUM,
                                        Field.CALLBACK_NUM_PRES_IND,
                                        Field.CALLBACK_NUM_ATAG,
                                        Field.SOURCE_SUBADDRESS,
                                        Field.DEST_SUBADDRESS,
                                        Field.USER_RESPONSE_CODE,
                                        Field.DISPLAY_TIME,
                                        Field.SMS_SIGNAL,
                                        Field.MS_VALIDITY,
                                        Field.MS_MSG_WAIT_FACILITIES,
                                        Field.NUMBER_OF_MESSAGES,
                                        Field.ALERT_ON_MSG_DELIVERY,
                                        Field.LANGUAGE_INDICATOR,
                                        Field.ITS_REPLY_TYPE,
                                        Field.ITS_SESSION_INFO,
                                        Field.USSD_SERVICE_OP };
        fieldsTable.put( Data.SUBMIT_SM,
                         Arrays.asList( SUBMIT_FIELDS ) );
        fieldsTable.put( Data.SUBMIT_SM_RESP, EMPTY_FIELD_LIST );
        final Field[] SUBMIT_MULTI_FIELDS = { Field.USER_MESSAGE_REFERENCE,
                                              Field.SOURCE_PORT,
                                              Field.SOURCE_ADDR_SUBUNIT,
                                              Field.DESTINATION_PORT,
                                              Field.DEST_ADDR_SUBUNIT,
                                              Field.SAR_MSG_REF_NUM,
                                              Field.SAR_TOTAL_SEGMENTS,
                                              Field.SAR_SEGMENT_SEQNUM,
                                              Field.PAYLOAD_TYPE,
                                              Field.MESSAGE_PAYLOAD,
                                              Field.PRIVACY_INDICATOR,
                                              Field.CALLBACK_NUM,
                                              Field.CALLBACK_NUM_PRES_IND,
                                              Field.CALLBACK_NUM_ATAG,
                                              Field.SOURCE_SUBADDRESS,
                                              Field.DEST_SUBADDRESS,
                                              Field.DISPLAY_TIME,
                                              Field.SMS_SIGNAL,
                                              Field.MS_VALIDITY,
                                              Field.MS_MSG_WAIT_FACILITIES,
                                              Field.ALERT_ON_MSG_DELIVERY,
                                              Field.LANGUAGE_INDICATOR,
        };
        fieldsTable.put( Data.SUBMIT_MULTI,
                         Arrays.asList( SUBMIT_MULTI_FIELDS ) );
        fieldsTable.put( Data.SUBMIT_MULTI_RESP, EMPTY_FIELD_LIST );
        final Field[] DELIVER_FIELDS = { Field.USER_MESSAGE_REFERENCE,
                                         Field.SOURCE_PORT,
                                         Field.DESTINATION_PORT,
                                         Field.SAR_MSG_REF_NUM,
                                         Field.SAR_TOTAL_SEGMENTS,
                                         Field.SAR_SEGMENT_SEQNUM,
                                         Field.USER_RESPONSE_CODE,
                                         Field.PRIVACY_INDICATOR,
                                         Field.PAYLOAD_TYPE,
                                         Field.MESSAGE_PAYLOAD,
                                         Field.CALLBACK_NUM,
                                         Field.SOURCE_SUBADDRESS,
                                         Field.DEST_SUBADDRESS,
                                         Field.LANGUAGE_INDICATOR,
                                         Field.ITS_SESSION_INFO,
                                         Field.NETWORK_ERROR_CODE,
                                         Field.MESSAGE_STATE,
                                         Field.RECEIPTED_MESSAGE_ID };
        fieldsTable.put( Data.DELIVER_SM,
                         Arrays.asList( DELIVER_FIELDS ) );
        fieldsTable.put( Data.DELIVER_SM_RESP, EMPTY_FIELD_LIST );
        final Field[] DATA_FIELDS = { Field.SOURCE_PORT,
                                      Field.SOURCE_ADDR_SUBUNIT,
                                      Field.SOURCE_NETWORK_TYPE,
                                      Field.SOURCE_BEARER_TYPE,
                                      Field.SOURCE_TELEMATICS_ID,
                                      Field.DESTINATION_PORT,
                                      Field.DEST_ADDR_SUBUNIT,
                                      Field.DEST_NETWORK_TYPE,
                                      Field.DEST_BEARER_TYPE,
                                      Field.DEST_TELEMATICS_ID,
                                      Field.SAR_MSG_REF_NUM,
                                      Field.SAR_TOTAL_SEGMENTS,
                                      Field.SAR_SEGMENT_SEQNUM,
                                      Field.MORE_MESSAGES_TO_SEND,
                                      Field.QOS_TIME_TO_LIVE,
                                      Field.PAYLOAD_TYPE,
                                      Field.MESSAGE_PAYLOAD,
                                      Field.SET_DPF,
                                      Field.RECEIPTED_MESSAGE_ID,
                                      Field.MESSAGE_STATE,
                                      Field.NETWORK_ERROR_CODE,
                                      Field.USER_MESSAGE_REFERENCE,
                                      Field.PRIVACY_INDICATOR,
                                      Field.CALLBACK_NUM,
                                      Field.CALLBACK_NUM_PRES_IND,
                                      Field.CALLBACK_NUM_ATAG,
                                      Field.SOURCE_SUBADDRESS,
                                      Field.DEST_SUBADDRESS,
                                      Field.USER_RESPONSE_CODE,
                                      Field.DISPLAY_TIME,
                                      Field.SMS_SIGNAL,
                                      Field.MS_VALIDITY,
                                      Field.MS_MSG_WAIT_FACILITIES,
                                      Field.NUMBER_OF_MESSAGES,
                                      Field.ALERT_ON_MSG_DELIVERY,
                                      Field.LANGUAGE_INDICATOR,
                                      Field.ITS_REPLY_TYPE,
                                      Field.ITS_SESSION_INFO };
        final Field[] DATA_RESP_FIELDS = { Field.DELIVERY_FAILURE_REASON,
                                           Field.NETWORK_ERROR_CODE,
                                           Field.ADDITIONAL_STATUS_INFO_TEXT,
                                           Field.DPF_RESULT };
        fieldsTable.put( Data.DATA_SM,
                         Arrays.asList( DATA_FIELDS ) );
        fieldsTable.put( Data.DATA_SM_RESP,
                         Arrays.asList( DATA_RESP_FIELDS ) );
        fieldsTable.put( Data.QUERY_SM, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.QUERY_SM_RESP, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.CANCEL_SM, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.CANCEL_SM_RESP, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.REPLACE_SM, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.REPLACE_SM_RESP, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.ENQUIRE_LINK, EMPTY_FIELD_LIST );
        fieldsTable.put( Data.ENQUIRE_LINK_RESP, EMPTY_FIELD_LIST );
        final Field[] ALERT_FIELDS = { Field.MS_AVAILABILITY_STATUS };
        fieldsTable.put( Data.ALERT_NOTIFICATION,
                         Arrays.asList( ALERT_FIELDS ) );
    }

    int type;

    private JTextField additionalStatusInfoText = new JTextField( 20 );
    private JTextField alertOnMsgDelivery = new JTextField( 20 );
    private JTextField callbackNum = new JTextField( 20 );
    private JTextField callbackNumAtag = new JTextField( 20 );
    private JTextField callbackNumPresInd = new JTextField( 20 );
    private JTextField deliveryFailureReason = new JTextField( 20 );
    private JTextField destinationPort = new JTextField( 20 );
    private JTextField destAddrSubunit = new JTextField( 20 );
    private JTextField destBearerType = new JTextField( 20 );
    private JTextField destNetworkType = new JTextField( 20 );
    private JTextField destSubaddress = new JTextField( 20 );
    private JTextField destTelematicsID = new JTextField( 20 );
    private JTextField displayTime = new JTextField( 20 );
    private JTextField dpfResult = new JTextField( 20 );
    private JTextField itsReplyType = new JTextField( 20 );
    private JTextField itsSessionInfo = new JTextField( 20 );
    private JTextField languageIndicator = new JTextField( 20 );
    private JTextField messagePayload = new JTextField( 20 );
    private JTextField messageState = new JTextField( 20 );
    private JTextField moreMessagesToSend = new JTextField( 20 );
    private JTextField msAvailabilityStatus = new JTextField( 20 );
    private JTextField msMsgWaitFacilities = new JTextField( 20 );
    private JTextField msValidity = new JTextField( 20 );
    private JTextField networkErrorCode = new JTextField( 20 );
    private JTextField numberOfMessages = new JTextField( 20 );
    private JTextField payloadType = new JTextField( 20 );
    private JTextField privacyIndicator = new JTextField( 20 );
    private JTextField qosTimeToLive = new JTextField( 20 );
    private JTextField receiptedMessageID = new JTextField( 20 );
    private JTextField sarMsgRefNum = new JTextField( 20 );
    private JTextField sarSegmentSeqNum = new JTextField( 20 );
    private JTextField sarTotalSegments = new JTextField( 20 );
    private JTextField scInterfaceVersion = new JTextField( 20 );
    private JTextField setDPF = new JTextField( 20 );
    private JTextField smsSignal = new JTextField( 20 );
    private JTextField sourceAddrSubunit = new JTextField( 20 );
    private JTextField sourceBearerType = new JTextField( 20 );
    private JTextField sourceNetworkType = new JTextField( 20 );
    private JTextField sourcePort = new JTextField( 20 );
    private JTextField sourceSubaddress = new JTextField( 20 );
    private JTextField sourceTelematicsID = new JTextField( 20 );
    private JTextField userMessageReference = new JTextField( 20 );
    private JTextField userResponseCode = new JTextField( 20 );
    private JTextField ussdServiceOp = new JTextField( 20 );

    Hashtable<Field, JComponent> editTable =
        new Hashtable<Field, JComponent>(  );
    {
        editTable.put( Field.ADDITIONAL_STATUS_INFO_TEXT, additionalStatusInfoText );
        editTable.put( Field.ALERT_ON_MSG_DELIVERY, alertOnMsgDelivery );
        editTable.put( Field.CALLBACK_NUM, callbackNum );
        editTable.put( Field.CALLBACK_NUM_ATAG, callbackNumAtag );
        editTable.put( Field.CALLBACK_NUM_PRES_IND, callbackNumPresInd );
        editTable.put( Field.DELIVERY_FAILURE_REASON, deliveryFailureReason );
        editTable.put( Field.DESTINATION_PORT, destinationPort );
        editTable.put( Field.DEST_ADDR_SUBUNIT, destAddrSubunit );
        editTable.put( Field.DEST_BEARER_TYPE, destBearerType );
        editTable.put( Field.DEST_NETWORK_TYPE, destNetworkType );
        editTable.put( Field.DEST_SUBADDRESS, destSubaddress );
        editTable.put( Field.DEST_TELEMATICS_ID, destTelematicsID );
        editTable.put( Field.DISPLAY_TIME, displayTime );
        editTable.put( Field.DPF_RESULT, dpfResult );
        editTable.put( Field.ITS_REPLY_TYPE, itsReplyType );
        editTable.put( Field.ITS_SESSION_INFO, itsSessionInfo );
        editTable.put( Field.LANGUAGE_INDICATOR, languageIndicator );
        editTable.put( Field.MESSAGE_PAYLOAD, messagePayload );
        editTable.put( Field.MESSAGE_STATE, messageState );
        editTable.put( Field.MORE_MESSAGES_TO_SEND, moreMessagesToSend );
        editTable.put( Field.MS_AVAILABILITY_STATUS, msAvailabilityStatus );
        editTable.put( Field.MS_MSG_WAIT_FACILITIES, msMsgWaitFacilities );
        editTable.put( Field.MS_VALIDITY, msValidity );
        editTable.put( Field.NETWORK_ERROR_CODE, networkErrorCode );
        editTable.put( Field.NUMBER_OF_MESSAGES, numberOfMessages );
        editTable.put( Field.PAYLOAD_TYPE, payloadType );
        editTable.put( Field.PRIVACY_INDICATOR, privacyIndicator );
        editTable.put( Field.QOS_TIME_TO_LIVE, qosTimeToLive );
        editTable.put( Field.RECEIPTED_MESSAGE_ID, receiptedMessageID );
        editTable.put( Field.SAR_MSG_REF_NUM, sarMsgRefNum );
        editTable.put( Field.SAR_SEGMENT_SEQNUM, sarSegmentSeqNum );
        editTable.put( Field.SAR_TOTAL_SEGMENTS, sarTotalSegments );
        editTable.put( Field.SC_INTERFACE_VERSION, scInterfaceVersion );
        editTable.put( Field.SET_DPF, setDPF );
        editTable.put( Field.SMS_SIGNAL, smsSignal );
        editTable.put( Field.SOURCE_ADDR_SUBUNIT, sourceAddrSubunit );
        editTable.put( Field.SOURCE_BEARER_TYPE, sourceBearerType );
        editTable.put( Field.SOURCE_NETWORK_TYPE, sourceNetworkType );
        editTable.put( Field.SOURCE_PORT, sourcePort );
        editTable.put( Field.SOURCE_SUBADDRESS, sourceSubaddress );
        editTable.put( Field.SOURCE_TELEMATICS_ID, sourceTelematicsID );
        editTable.put( Field.USER_MESSAGE_REFERENCE, userMessageReference );
        editTable.put( Field.USER_RESPONSE_CODE, userResponseCode );
        editTable.put( Field.USSD_SERVICE_OP, ussdServiceOp );
    }

    /**
     * Constructor
     * @param editFlag Allow this PDU to be edited?
     */
    public PDUOptionalPane( boolean editFlag )
    {
        setBorder( new TitledBorder( "Optional Fields" ) );

        setLayout( new GridBagLayout() );

        for( JComponent component: editTable.values() )
        {
            ((JTextComponent)component).setEditable( editFlag );
        }
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

    public String getAdditionalStatusInfoText()
    {
        if( additionalStatusInfoText.getText().length() == 0 )
        {
            return null;
        }
        return additionalStatusInfoText.getText();
    }


    public boolean getAlertOnMsgDelivery()
    {
        return alertOnMsgDelivery.getText().equalsIgnoreCase( "Y" );
    }


    public ByteBuffer getCallbackNum()
    {
        if( callbackNum.getText().length() == 0 )
        {
            return null;
        }
        return new ByteBuffer( callbackNum.getText().getBytes() );
    }


    public ByteBuffer getCallbackNumAtag()
    {
        if( callbackNumAtag.getText().length() == 0 )
        {
            return null;
        }
        return new ByteBuffer( callbackNumAtag.getText().getBytes() );
    }


    public Byte getCallbackNumPresInd()
    {
        if( callbackNumPresInd.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( callbackNumPresInd.getText() );
    }


    public Byte getDeliveryFailureReason()
    {
        if( deliveryFailureReason.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( deliveryFailureReason.getText() );
    }


    public Short getDestinationPort()
    {
        if( destinationPort.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( destinationPort.getText() );
    }


    public Byte getDestAddrSubunit()
    {
        if( destAddrSubunit.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( destAddrSubunit.getText() );
    }


    public Byte getDestBearerType()
    {
        if( destBearerType.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( destBearerType.getText() );
    }


    public Byte getDestNetworkType()
    {
        if( destNetworkType.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( destNetworkType.getText() );
    }


    public ByteBuffer getDestSubaddress()
    {
        if( destSubaddress.getText().length() == 0 )
        {
            return null;
        }
        return new ByteBuffer( destSubaddress.getText().getBytes() );
    }


    public Short getDestTelematicsID()
    {
        if( destTelematicsID.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( destTelematicsID.getText() );
    }


    public Byte getDisplayTime()
    {
        if( displayTime.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( displayTime.getText() );
    }


    public Byte getDpfResult()
    {
        if( dpfResult.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( dpfResult.getText() );
    }


    public Byte getItsReplyType()
    {
        if( itsReplyType.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( itsReplyType.getText() );
    }


    public Short getItsSessionInfo()
    {
        if( itsSessionInfo.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( itsSessionInfo.getText() );
    }


    public Byte getLanguageIndicator()
    {
        if( languageIndicator.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( languageIndicator.getText() );
    }


    public ByteBuffer getMessagePayload()
    {
        if( messagePayload.getText().length() == 0 )
        {
            return null;
        }
        ByteBuffer buf =  new ByteBuffer();
        try
        {
            buf.appendString( messagePayload.getText(), Data.ENC_UTF8 );
        }
        catch( UnsupportedEncodingException e )
        {
            // shouldn't happen
            e.printStackTrace();
            return null;
        }

        return buf;
    }


    public Byte getMessageState()
    {
        if( messageState.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( messageState.getText() );
    }


    public Byte getMoreMessagesToSend()
    {
        if( moreMessagesToSend.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( moreMessagesToSend.getText() );
    }


    public Byte getMsAvailabilityStatus()
    {
        if( msAvailabilityStatus.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( msAvailabilityStatus.getText() );
    }


    public Byte getMsMsgWaitFacilities()
    {
        if( msMsgWaitFacilities.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( msMsgWaitFacilities.getText() );
    }


    public Byte getMsValidity()
    {
        if( msValidity.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( msValidity.getText() );
    }


    public ByteBuffer getNetworkErrorCode()
    {
        if( networkErrorCode.getText().length() == 0 )
        {
            return null;
        }
        return new ByteBuffer( networkErrorCode.getText().getBytes() );
    }


    public Byte getNumberOfMessages()
    {
        if( numberOfMessages.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( numberOfMessages.getText() );
    }


    public Byte getPayloadType()
    {
        if( payloadType.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( payloadType.getText() );
    }


    public Byte getPrivacyIndicator()
    {
        if( privacyIndicator.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( privacyIndicator.getText() );
    }


    public Integer getQosTimeToLive()
    {
        return Integer.valueOf( qosTimeToLive.getText() );
    }


    public String getReceiptedMessageID()
    {
        if( receiptedMessageID.getText().length() == 0 )
        {
            return null;
        }
        return receiptedMessageID.getText();
    }


    public Short getSarMsgRefNum()
    {
        if( sarMsgRefNum.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( sarMsgRefNum.getText() );
    }


    public Short getSarSegmentSeqNum()
    {
        if( sarSegmentSeqNum.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( sarSegmentSeqNum.getText() );
    }


    public Short getSarTotalSegments()
    {
        if( sarTotalSegments.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( sarTotalSegments.getText() );
    }


    public Byte getScInterfaceVersion()
    {
        if( scInterfaceVersion.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( scInterfaceVersion.getText() );
    }


    public Byte getSetDPF()
    {
        if( setDPF.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( setDPF.getText() );
    }


    public Short getSmsSignal()
    {
        if( smsSignal.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( smsSignal.getText() );
    }


    public Byte getSourceAddrSubunit()
    {
        if( sourceAddrSubunit.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( sourceAddrSubunit.getText() );
    }


    public Byte getSourceBearerType()
    {
        if( sourceBearerType.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( sourceBearerType.getText() );
    }


    public Byte getSourceNetworkType()
    {
        if( sourceNetworkType.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( sourceNetworkType.getText() );
    }


    public Short getSourcePort()
    {
        if( sourcePort.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( sourcePort.getText() );
    }


    public ByteBuffer getSourceSubaddress()
    {
        if( sourceSubaddress.getText().length() == 0 )
        {
            return null;
        }
        return new ByteBuffer( sourceSubaddress.getText().getBytes() );
    }


    public Byte getSourceTelematicsID()
    {
        if( sourceTelematicsID.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( sourceTelematicsID.getText() );
    }


    public Short getUserMessageReference()
    {
        if( userMessageReference.getText().length() == 0 )
        {
            return null;
        }
        return Short.valueOf( userMessageReference.getText() );
    }


    public Byte getUserResponseCode()
    {
        if( userResponseCode.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( userResponseCode.getText() );
    }


    public Byte getUssdServiceOp()
    {
        if( ussdServiceOp.getText().length() == 0 )
        {
            return null;
        }
        return Byte.valueOf( ussdServiceOp.getText() );
    }


    public void setAdditionalStatusInfoText( String additionalStatusInfoText )
    {
        if( additionalStatusInfoText == null )
        {
            this.additionalStatusInfoText.setText( "" );
        }
        else
        {
            this.additionalStatusInfoText.setText( additionalStatusInfoText );
        }
    }


    public void setAlertOnMsgDelivery( boolean alertOnMsgDelivery )
    {
        if( alertOnMsgDelivery )
        {
            this.alertOnMsgDelivery.setText( "Y" );
        }
        else
        {
            this.alertOnMsgDelivery.setText( "" );
        }
    }


    public void setCallbackNum( String callbackNum )
    {
        if( callbackNum == null )
        {
            this.callbackNum.setText( "" );
        }
        else
        {
            this.callbackNum.setText( callbackNum );
        }
    }


    public void setCallbackNumAtag( String callbackNumAtag )
    {
        if( callbackNumAtag == null )
        {
            this.callbackNumAtag.setText( "" );
        }
        else
        {
            this.callbackNumAtag.setText( callbackNumAtag );
        }
    }


    public void setCallbackNumPresInd( Byte callbackNumPresInd )
    {
        if( callbackNumPresInd == null )
        {
            this.callbackNumPresInd.setText( "" );
        }
        else
        {
            this.callbackNumPresInd.setText( callbackNumPresInd.toString() );
        }
    }


    public void setDeliveryFailureReason( Byte deliveryFailureReason )
    {
        if( deliveryFailureReason == null )
        {
            this.deliveryFailureReason.setText( "" );
        }
        else
        {
            this.deliveryFailureReason.setText( deliveryFailureReason.toString() );
        }
    }


    public void setDestinationPort( Short destinationPort )
    {
        if( destinationPort == null )
        {
            this.destinationPort.setText( "" );
        }
        else
        {
            this.destinationPort.setText( destinationPort.toString() );
        }
    }


    public void setDestAddrSubunit( Byte destAddrSubunit )
    {
        if( destAddrSubunit == null )
        {
            this.destAddrSubunit.setText( "" );
        }
        else
        {
            this.destAddrSubunit.setText( destAddrSubunit.toString() );
        }
    }


    public void setDestBearerType( Byte destBearerType )
    {
        if( destBearerType == null )
        {
            this.destBearerType.setText( "" );
        }
        else
        {
            this.destBearerType.setText( destBearerType.toString() );
        }
    }


    public void setDestNetworkType( Byte destNetworkType )
    {
        if( destNetworkType == null )
        {
            this.destNetworkType.setText( "" );
        }
        else
        {
            this.destNetworkType.setText( destNetworkType.toString() );
        }
    }


    public void setDestSubaddress( String destSubaddress )
    {
        if( destSubaddress == null )
        {
            this.destSubaddress.setText( "" );
        }
        else
        {
            this.destSubaddress.setText( destSubaddress );
        }
    }


    public void setDestTelematicsID( Short destTelematicsID )
    {
        if( destTelematicsID == null )
        {
            this.destTelematicsID.setText( "" );
        }
        else
        {
            this.destTelematicsID.setText( destTelematicsID.toString() );
        }
    }


    public void setDisplayTime( Byte displayTime )
    {
        if( displayTime == null )
        {
            this.displayTime.setText( "" );
        }
        else
        {
            this.displayTime.setText( displayTime.toString() );
        }
    }


    public void setDpfResult( Byte dpfResult )
    {
        if( dpfResult == null )
        {
            this.dpfResult.setText( "" );
        }
        else
        {
            this.dpfResult.setText( dpfResult.toString() );
        }
    }


    public void setItsReplyType( Byte itsReplyType )
    {
        if( itsReplyType == null )
        {
            this.itsReplyType.setText( "" );
        }
        else
        {
            this.itsReplyType.setText( itsReplyType.toString() );
        }
    }


    public void setItsSessionInfo( Short itsSessionInfo )
    {
        if( itsSessionInfo == null )
        {
            this.itsSessionInfo.setText( "" );
        }
        else
        {
            this.itsSessionInfo.setText( itsSessionInfo.toString() );
        }
    }


    public void setLanguageIndicator( Byte languageIndicator )
    {
        if( languageIndicator == null )
        {
            this.languageIndicator.setText( "" );
        }
        else
        {
            this.languageIndicator.setText( languageIndicator.toString() );
        }
    }


    public void setMessagePayload( String messagePayload )
    {
        if( messagePayload == null )
        {
            this.messagePayload.setText( "" );
        }
        else
        {
            this.messagePayload.setText( messagePayload );
        }
    }


    public void setMessageState( Byte messageState )
    {
        if( messageState == null )
        {
            this.messageState.setText( "" );
        }
        else
        {
            this.messageState.setText( messageState.toString() );
        }
    }


    public void setMoreMessagesToSend( Byte moreMessagesToSend )
    {
        if( moreMessagesToSend == null )
        {
            this.moreMessagesToSend.setText( "" );
        }
        else
        {
            this.moreMessagesToSend.setText( moreMessagesToSend.toString() );
        }
    }


    public void setMsAvailabilityStatus( Byte msAvailabilityStatus )
    {
        if( msAvailabilityStatus == null )
        {
            this.msAvailabilityStatus.setText( "" );
        }
        else
        {
            this.msAvailabilityStatus.setText( msAvailabilityStatus.toString() );
        }
    }


    public void setMsMsgWaitFacilities( Byte msMsgWaitFacilities )
    {
        if( msMsgWaitFacilities == null )
        {
            this.msMsgWaitFacilities.setText( "" );
        }
        else
        {
            this.msMsgWaitFacilities.setText( msMsgWaitFacilities.toString() );
        }
    }


    public void setMsValidity( Byte msValidity )
    {
        if( msValidity == null )
        {
            this.msValidity.setText( "" );
        }
        else
        {
            this.msValidity.setText( msValidity.toString() );
        }
    }


    public void setNetworkErrorCode( String networkErrorCode )
    {
        if( networkErrorCode == null )
        {
            this.networkErrorCode.setText( "" );
        }
        else
        {
            this.networkErrorCode.setText( networkErrorCode );
        }
    }


    public void setNumberOfMessages( Byte numberOfMessages )
    {
        if( numberOfMessages == null )
        {
            this.numberOfMessages.setText( "" );
        }
        else
        {
            this.numberOfMessages.setText( numberOfMessages.toString() );
        }
    }


    public void setPayloadType( Byte payloadType )
    {
        if( payloadType == null )
        {
            this.payloadType.setText( "" );
        }
        else
        {
            this.payloadType.setText( payloadType.toString() );
        }
    }


    public void setPrivacyIndicator( Byte privacyIndicator )
    {
        if( privacyIndicator == null )
        {
            this.privacyIndicator.setText( "" );
        }
        else
        {
            this.privacyIndicator.setText( privacyIndicator.toString() );
        }
    }


    public void setQosTimeToLive( Integer qosTimeToLive )
    {
        if( qosTimeToLive == null )
        {
            this.qosTimeToLive.setText( "" );
        }
        else
        {
            this.qosTimeToLive.setText( qosTimeToLive.toString() );
        }
    }


    public void setReceiptedMessageID( String receiptedMessageID )
    {
        if( receiptedMessageID == null )
        {
            this.receiptedMessageID.setText( "" );
        }
        else
        {
            this.receiptedMessageID.setText( receiptedMessageID );
        }
    }


    public void setSarMsgRefNum( Short sarMsgRefNum )
    {
        if( sarMsgRefNum == null )
        {
            this.sarMsgRefNum.setText( "" );
        }
        else
        {
            this.sarMsgRefNum.setText( sarMsgRefNum.toString() );
        }
    }


    public void setSarSegmentSeqNum( Short sarSegmentSeqNum )
    {
        if( sarSegmentSeqNum == null )
        {
            this.sarSegmentSeqNum.setText( "" );
        }
        else
        {
            this.sarSegmentSeqNum.setText( sarSegmentSeqNum.toString() );
        }
    }


    public void setSarTotalSegments( Short sarTotalSegments )
    {
        if( sarTotalSegments == null )
        {
            this.sarTotalSegments.setText( "" );
        }
        else
        {
            this.sarTotalSegments.setText( sarTotalSegments.toString() );
        }
    }


    public void setScInterfaceVersion( Byte scInterfaceVersion )
    {
        if( scInterfaceVersion == null )
        {
            this.scInterfaceVersion.setText( "" );
        }
        else
        {
            this.scInterfaceVersion.setText( scInterfaceVersion.toString() );
        }
    }


    public void setSetDPF( Byte setDPF )
    {
        if( setDPF == null )
        {
            this.setDPF.setText( "" );
        }
        else
        {
            this.setDPF.setText( setDPF.toString() );
        }
    }


    public void setSmsSignal( Short smsSignal )
    {
        if( smsSignal == null )
        {
            this.smsSignal.setText( "" );
        }
        else
        {
            this.smsSignal.setText( smsSignal.toString() );
        }
    }


    public void setSourceAddrSubunit( Byte sourceAddrSubunit )
    {
        if( sourceAddrSubunit == null )
        {
            this.sourceAddrSubunit.setText( "" );
        }
        else
        {
            this.sourceAddrSubunit.setText( sourceAddrSubunit.toString() );
        }
    }


    public void setSourceBearerType( Byte sourceBearerType )
    {
        if( sourceBearerType == null )
        {
            this.sourceBearerType.setText( "" );
        }
        else
        {
            this.sourceBearerType.setText( sourceBearerType.toString() );
        }
    }


    public void setSourceNetworkType( Byte sourceNetworkType )
    {
        if( sourceNetworkType == null )
        {
            this.sourceNetworkType.setText( "" );
        }
        else
        {
            this.sourceNetworkType.setText( sourceNetworkType.toString() );
        }
    }


    public void setSourcePort( Short sourcePort )
    {
        if( sourcePort == null )
        {
            this.sourcePort.setText( "" );
        }
        else
        {
            this.sourcePort.setText( sourcePort.toString() );
        }
    }


    public void setSourceSubaddress( String sourceSubaddress )
    {
        if( sourceSubaddress == null )
        {
            this.sourceSubaddress.setText( "" );
        }
        else
        {
            this.sourceSubaddress.setText( sourceSubaddress );
        }
    }


    public void setSourceTelematicsID( Byte sourceTelematicsID )
    {
        if( sourceTelematicsID == null )
        {
            this.sourceTelematicsID.setText( "" );
        }
        else
        {
            this.sourceTelematicsID.setText( sourceTelematicsID.toString() );
        }
    }


    public void setUserMessageReference( Short userMessageReference )
    {
        if( userMessageReference == null )
        {
            this.userMessageReference.setText( "" );
        }
        else
        {
            this.userMessageReference.setText( userMessageReference.toString() );
        }
    }


    public void setUserResponseCode( Byte userResponseCode )
    {
        if( userResponseCode == null )
        {
            this.userResponseCode.setText( "" );
        }
        else
        {
            this.userResponseCode.setText( userResponseCode.toString() );
        }
    }


    public void setUssdServiceOp( Byte ussdServiceOp )
    {
        if( ussdServiceOp == null )
        {
            this.ussdServiceOp.setText( "" );
        }
        else
        {
            this.ussdServiceOp.setText( ussdServiceOp.toString() );
        }
    }
}
