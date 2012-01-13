/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.gui;


import javax.swing.*;
import java.io.UnsupportedEncodingException;
import org.smpp.Data;
import org.smpp.pdu.*;
import org.smpp.pdu.tlv.TLV;
import org.smpp.pdu.tlv.TLVOctets;
import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;
import org.smpp.util.TerminatingZeroNotFoundException;


/**
 * A panel for displaying/editing PDUs.
 */
public class PDUPane extends JPanel
{
    /**
     * panel for header values    *
     */
    PDUHeaderPane headerPane;

    /**
     * panel for mandatory values    *
     */
    PDUMandatoryPane mandatoryPane;

    /**
     * panel for optional values    *
     */
    PDUOptionalPane optionalPane;

    /**
     * Constructor
     * @param pdu PDU to display/edit
     * @param editFlag Allow this PDU to be edited?
     *
     * If PDU is null and not editable then just display
     * the text "no PDU".
     */
    public PDUPane( PDU pdu, boolean editFlag )
    {
        if( pdu == null && !editFlag )
        {
            add( new JLabel( "NO PDU" ) );
            return;
        }

        setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );

        headerPane = new PDUHeaderPane( editFlag );
        add( headerPane );

        mandatoryPane = new PDUMandatoryPane( editFlag );
        add( new JScrollPane( mandatoryPane ) );

        optionalPane = new PDUOptionalPane( editFlag );
        add( new JScrollPane( optionalPane ) );

        headerPane.addPDUTypeListener( new PDUHeaderPane.PDUTypeListener()
            {
                public void notifyType( int type )
                {
                    mandatoryPane.setType( type );
                    optionalPane.setType( type );
                }
            } );

        if( pdu == null )
        {
            setPDUType( Data.BIND_TRANSMITTER );
        }
        else
        {
            setPDU( pdu );
        }
    }


    public PDU getPDU()
    {
        PDU pdu = PDU.createPDU( headerPane.getCommandID().getId() );
        pdu.setSequenceNumber( headerPane.getSequence() );
        pdu.setCommandStatus( headerPane.getCommandStatus().getStatus() );

        try
        {
            // Set mandatory values
            switch( headerPane.getCommandID() )
            {
            case BIND_RECEIVER:
            case BIND_TRANSMITTER:
            case BIND_TRANSCEIVER:
            {
                BindRequest request = (BindRequest)pdu;
                request.setSystemId( mandatoryPane.getSystemID() );
                request.setPassword( mandatoryPane.getPassword() );
                request.setSystemType( mandatoryPane.getSystemType() );
                request.setInterfaceVersion( mandatoryPane.getInterfaceVersion() );
                request.setAddressRange( mandatoryPane.getAddrTON(),
                                         mandatoryPane.getAddrNPI(),
                                         mandatoryPane.getAddressRange() );
                break;
            }
            case BIND_RECEIVER_RESP:
            case BIND_TRANSMITTER_RESP:
            case BIND_TRANSCEIVER_RESP:
            {
                BindResponse response = (BindResponse)pdu;
                response.setSystemId( mandatoryPane.getSystemID() );
                break;
            }
            case OUTBIND:
            {
                Outbind outbind = (Outbind)pdu;
                outbind.setSystemId( mandatoryPane.getSystemID() );
                outbind.setPassword( mandatoryPane.getPassword() );
                break;
            }
            case UNBIND:
            case UNBIND_RESP:
            case GENERIC_NACK:
                // No mandatory fields
                break;
            case QUERY_SM:
            {
                QuerySM querySM = (QuerySM)pdu;
                querySM.setMessageId( mandatoryPane.getMessageID() );
                querySM.setSourceAddr( mandatoryPane.getSourceAddrTON(),
                                       mandatoryPane.getSourceAddrNPI(),
                                       mandatoryPane.getSourceAddr() );
                break;
            }
            case QUERY_SM_RESP:
            {
                QuerySMResp querySMResp = (QuerySMResp)pdu;
                querySMResp.setMessageId( mandatoryPane.getMessageID() );
                querySMResp.setFinalDate( mandatoryPane.getFinalDate() );
                querySMResp.setMessageState( mandatoryPane.getMessageState() );
                querySMResp.setErrorCode( mandatoryPane.getErrorCode() );
                break;
            }
            case SUBMIT_SM:
            {
                SubmitSM submitSM = (SubmitSM)pdu;
                submitSM.setServiceType( mandatoryPane.getServiceType() );
                submitSM.setSourceAddr( mandatoryPane.getSourceAddrTON(),
                                        mandatoryPane.getSourceAddrNPI(),
                                        mandatoryPane.getSourceAddr() );
                submitSM.setDestAddr( mandatoryPane.getDestAddrTON(),
                                      mandatoryPane.getDestAddrNPI(),
                                      mandatoryPane.getDestinationAddr() );
                submitSM.setEsmClass( mandatoryPane.getEsmClass() );
                submitSM.setProtocolId( mandatoryPane.getProtocolID() );
                submitSM.setPriorityFlag( mandatoryPane.getPriorityFlag() );
                submitSM.setScheduleDeliveryTime( mandatoryPane.getScheduleDeliveryTime() );
                submitSM.setValidityPeriod( mandatoryPane.getValidityPeriod() );
                submitSM.setRegisteredDelivery( mandatoryPane.getRegisteredDelivery() );
                submitSM.setReplaceIfPresentFlag( mandatoryPane.getReplaceIfPresentFlag() );
                submitSM.setDataCoding( mandatoryPane.getDataCoding() );
                submitSM.setSmDefaultMsgId( mandatoryPane.getSmDefaultMsgID() );
                if( mandatoryPane.getUdhSeqRef() != 0 &&
                    mandatoryPane.getUdhSeqIndex() != 0 &&
                    mandatoryPane.getUdhSeqTotal() != 0 )
                {
                    ByteBuffer buffer = new ByteBuffer();
                    buffer.appendByte( (byte)5 );
                    buffer.appendByte( (byte)0 );
                    buffer.appendByte( (byte)3 );
                    buffer.appendByte( (byte)mandatoryPane.getUdhSeqRef() );
                    buffer.appendByte( (byte)mandatoryPane.getUdhSeqTotal() );
                    buffer.appendByte( (byte)mandatoryPane.getUdhSeqIndex() );
                    buffer.appendString(
                        mandatoryPane.getShortMessage() );

                    submitSM.setShortMessageData( buffer );
                }
                else
                {
                    submitSM.setShortMessage(
                        mandatoryPane.getShortMessage() );
                }
                break;
            }
            case SUBMIT_SM_RESP:
            {
                SubmitSMResp submitSMResp = (SubmitSMResp)pdu;
                submitSMResp.setMessageId( mandatoryPane.getMessageID() );
                break;
            }
            case SUBMIT_MULTI:
            {
                SubmitMultiSM submitMultiSM = (SubmitMultiSM)pdu;
                submitMultiSM.setServiceType( mandatoryPane.getServiceType() );
                submitMultiSM.setSourceAddr( mandatoryPane.getSourceAddrTON(),
                                             mandatoryPane.getSourceAddrNPI(),
                                             mandatoryPane.getSourceAddr() );
                for( String address : mandatoryPane.getDestinationAddr().split( "," ))
                {
                    address = address.trim();
                    if( address.length() > 0 )
                    {
                        DestinationAddress dest = new DestinationAddress(
                            mandatoryPane.getDestAddrTON(),
                            mandatoryPane.getDestAddrNPI(),
                            address );
                        submitMultiSM.addDestAddress( dest );
                    }
                }
                submitMultiSM.setEsmClass( mandatoryPane.getEsmClass() );
                submitMultiSM.setProtocolId( mandatoryPane.getProtocolID() );
                submitMultiSM.setPriorityFlag( mandatoryPane.getPriorityFlag() );
                submitMultiSM.setScheduleDeliveryTime( mandatoryPane.getScheduleDeliveryTime() );
                submitMultiSM.setValidityPeriod( mandatoryPane.getValidityPeriod() );
                submitMultiSM.setRegisteredDelivery( mandatoryPane.getRegisteredDelivery() );
                submitMultiSM.setReplaceIfPresentFlag( mandatoryPane.getReplaceIfPresentFlag() );
                submitMultiSM.setDataCoding( mandatoryPane.getDataCoding() );
                submitMultiSM.setSmDefaultMsgId( mandatoryPane.getSmDefaultMsgID() );
                submitMultiSM.setShortMessage(
                    mandatoryPane.getShortMessage() );
                break;
            }
            case SUBMIT_MULTI_RESP:
            {
                SubmitMultiSMResp submitMultiSMResp = (SubmitMultiSMResp)pdu;
                submitMultiSMResp.setMessageId( mandatoryPane.getMessageID() );
                // TODO if we ever need it?
                // submitMultiSMResp.addUnsuccessSME( ... );
                throw new UnsupportedOperationException(
                    "SUBMIT_MULTI_RESP needs more work"  );
//                break;
            }
            case DELIVER_SM:
            {
                DeliverSM deliverSM = (DeliverSM)pdu;
                deliverSM.setServiceType( mandatoryPane.getServiceType() );
                deliverSM.setSourceAddr( mandatoryPane.getSourceAddrTON(),
                                         mandatoryPane.getSourceAddrNPI(),
                                         mandatoryPane.getSourceAddr() );
                deliverSM.setDestAddr( mandatoryPane.getDestAddrTON(),
                                       mandatoryPane.getDestAddrNPI(),
                                       mandatoryPane.getDestinationAddr() );
                deliverSM.setEsmClass( mandatoryPane.getEsmClass() );
                deliverSM.setProtocolId( mandatoryPane.getProtocolID() );
                deliverSM.setPriorityFlag( mandatoryPane.getPriorityFlag() );
                deliverSM.setRegisteredDelivery( mandatoryPane.getRegisteredDelivery() );
                deliverSM.setDataCoding( mandatoryPane.getDataCoding() );
                deliverSM.setShortMessage(
                    mandatoryPane.getShortMessage() );
                break;
            }
            case DELIVER_SM_RESP:
            {
                DeliverSMResp deliverSMResp = (DeliverSMResp)pdu;
                deliverSMResp.setMessageId( mandatoryPane.getMessageID() );
                break;
            }
            case REPLACE_SM:
            {
                ReplaceSM replaceSM = (ReplaceSM)pdu;
                replaceSM.setMessageId( mandatoryPane.getMessageID() );
                replaceSM.setSourceAddr( mandatoryPane.getSourceAddrTON(),
                                         mandatoryPane.getSourceAddrNPI(),
                                         mandatoryPane.getSourceAddr() );
                replaceSM.setScheduleDeliveryTime( mandatoryPane.getScheduleDeliveryTime() );
                replaceSM.setValidityPeriod( mandatoryPane.getValidityPeriod() );
                replaceSM.setRegisteredDelivery( mandatoryPane.getRegisteredDelivery() );
                replaceSM.setSmDefaultMsgId( mandatoryPane.getSmDefaultMsgID() );
                replaceSM.setShortMessage(
                    mandatoryPane.getShortMessage() );
                break;
            }
            case REPLACE_SM_RESP:
            {
                // No mandatory fields
                break;
            }
            case CANCEL_SM:
            {
                CancelSM cancelSM = (CancelSM)pdu;
                cancelSM.setServiceType( mandatoryPane.getServiceType() );
                cancelSM.setMessageId( mandatoryPane.getMessageID() );
                cancelSM.setSourceAddr( mandatoryPane.getSourceAddrTON(),
                                        mandatoryPane.getSourceAddrNPI(),
                                        mandatoryPane.getSourceAddr() );
                cancelSM.setDestAddr( mandatoryPane.getDestAddrTON(),
                                      mandatoryPane.getDestAddrNPI(),
                                      mandatoryPane.getDestinationAddr() );
                break;
            }
            case CANCEL_SM_RESP:
            case ENQUIRE_LINK:
            case ENQUIRE_LINK_RESP:
                // No mandatory fields
                break;
            case ALERT_NOTIFICATION:
            {
                throw new UnsupportedOperationException(
                    "ALERT_NOTIFICATION needs more work"  );
//            AlertNotification alertNotification = (AlertNotification)pdu;
//            alertNotification.setSourceAddr( mandatoryPane.getSourceAddrTON(),
//                                             mandatoryPane.getSourceAddrNPI(),
//                                             mandatoryPane.getSourceAddr() );
//            alertNotification.setEsmeAddr( mandatoryPane.getEsmeAddrTON(),
//                                           mandatoryPane.getEsmeAddrNPI(),
//                                           mandatoryPane.getEsmeAddr() );
//            break;
            }
            case DATA_SM:
            {
                DataSM dataSM = (DataSM)pdu;
                dataSM.setServiceType( mandatoryPane.getServiceType() );
                dataSM.setSourceAddr( mandatoryPane.getSourceAddrTON(),
                                      mandatoryPane.getSourceAddrNPI(),
                                      mandatoryPane.getSourceAddr() );
                dataSM.setDestAddr( mandatoryPane.getDestAddrTON(),
                                    mandatoryPane.getDestAddrNPI(),
                                    mandatoryPane.getDestinationAddr() );
                dataSM.setEsmClass( mandatoryPane.getEsmClass() );
                dataSM.setRegisteredDelivery(
                    mandatoryPane.getRegisteredDelivery() );
                dataSM.setDataCoding( mandatoryPane.getDataCoding() );
                break;
            }
            case DATA_SM_RESP:
            {
                DataSMResp dataSMResp = (DataSMResp)pdu;
                dataSMResp.setMessageId( mandatoryPane.getMessageID() );
                break;
            }
            }

            // Set optional values
            switch( headerPane.getCommandID() )
            {
            case BIND_RECEIVER:
            case BIND_TRANSMITTER:
            case BIND_TRANSCEIVER:
                // No optional field
                break;
            case BIND_RECEIVER_RESP:
            case BIND_TRANSMITTER_RESP:
            case BIND_TRANSCEIVER_RESP:
            {
                if( optionalPane.getScInterfaceVersion() != null )
                {
                    BindResponse response = (BindResponse)pdu;
                    response.setScInterfaceVersion( optionalPane.getScInterfaceVersion() );
                }
                break;
            }
            case OUTBIND:
            case UNBIND:
            case UNBIND_RESP:
            case GENERIC_NACK:
            case QUERY_SM:
            case QUERY_SM_RESP:
                // No optional fields
                break;
            case SUBMIT_SM:
            {
                SubmitSM submitSM = (SubmitSM)pdu;
                if( optionalPane.getUserMessageReference() != null )
                {
                    submitSM.setUserMessageReference(
                        optionalPane.getUserMessageReference() );
                }
                if( optionalPane.getSourcePort() != null )
                {
                    submitSM.setSourcePort(
                        optionalPane.getSourcePort() );
                }
                if( optionalPane.getSourceAddrSubunit() != null )
                {
                    submitSM.setSourceAddrSubunit(
                        optionalPane.getSourceAddrSubunit() );
                }
                if( optionalPane.getDestinationPort() != null )
                {
                    submitSM.setDestinationPort(
                        optionalPane.getDestinationPort() );
                }
                if( optionalPane.getDestAddrSubunit() != null )
                {
                    submitSM.setDestAddrSubunit(
                        optionalPane.getDestAddrSubunit() );
                }
                if( optionalPane.getSarMsgRefNum() != null )
                {
                    submitSM.setSarMsgRefNum(
                        optionalPane.getSarMsgRefNum() );
                }
                if( optionalPane.getSarTotalSegments() != null )
                {
                    submitSM.setSarTotalSegments(
                        optionalPane.getSarTotalSegments() );
                }
                if( optionalPane.getSarSegmentSeqNum() != null )
                {
                    submitSM.setSarSegmentSeqnum(
                        optionalPane.getSarSegmentSeqNum() );
                }
                if( optionalPane.getMoreMessagesToSend() != null )
                {
                    submitSM.setMoreMsgsToSend(
                        optionalPane.getMoreMessagesToSend() );
                }
                if( optionalPane.getPayloadType() != null )
                {
                    submitSM.setPayloadType(
                        optionalPane.getPayloadType() );
                }
                if( optionalPane.getMessagePayload() != null )
                {
                    submitSM.setMessagePayload(
                        optionalPane.getMessagePayload() );
                }
                if( optionalPane.getPrivacyIndicator() != null )
                {
                    submitSM.setPrivacyIndicator(
                        optionalPane.getPrivacyIndicator() );
                }
                if( optionalPane.getCallbackNum() != null )
                {
                    submitSM.setCallbackNum(
                        optionalPane.getCallbackNum() );
                }
                if( optionalPane.getCallbackNumPresInd() != null )
                {
                    submitSM.setCallbackNumPresInd(
                        optionalPane.getCallbackNumPresInd() );
                }
                if( optionalPane.getCallbackNumAtag() != null )
                {
                    submitSM.setCallbackNumAtag(
                        optionalPane.getCallbackNumAtag() );
                }
                if( optionalPane.getSourceSubaddress() != null )
                {
                    submitSM.setSourceSubaddress(
                        optionalPane.getSourceSubaddress() );
                }
                if( optionalPane.getDestSubaddress() != null )
                {
                    submitSM.setDestSubaddress(
                        optionalPane.getDestSubaddress() );
                }
                if( optionalPane.getUserResponseCode() != null )
                {
                    submitSM.setUserResponseCode(
                        optionalPane.getUserResponseCode() );
                }
                if( optionalPane.getDisplayTime() != null )
                {
                    submitSM.setDisplayTime(
                        optionalPane.getDisplayTime() );
                }
                if( optionalPane.getSmsSignal() != null )
                {
                    submitSM.setSmsSignal(
                        optionalPane.getSmsSignal() );
                }
                if( optionalPane.getMsValidity() != null )
                {
                    submitSM.setMsValidity(
                        optionalPane.getMsValidity() );
                }
                if( optionalPane.getMsMsgWaitFacilities() != null )
                {
                    submitSM.setMsMsgWaitFacilities(
                        optionalPane.getMsMsgWaitFacilities() );
                }
                if( optionalPane.getNumberOfMessages() != null )
                {
                    submitSM.setNumberOfMessages(
                        optionalPane.getNumberOfMessages() );
                }
                if( optionalPane.getAlertOnMsgDelivery() )
                {
                    submitSM.setAlertOnMsgDelivery(
                        optionalPane.getAlertOnMsgDelivery() );
                }
                if( optionalPane.getLanguageIndicator() != null )
                {
                    submitSM.setLanguageIndicator(
                        optionalPane.getLanguageIndicator() );
                }
                if( optionalPane.getItsReplyType() != null )
                {
                    submitSM.setItsReplyType(
                        optionalPane.getItsReplyType() );
                }
                if( optionalPane.getItsSessionInfo() != null )
                {
                    submitSM.setItsSessionInfo(
                        optionalPane.getItsSessionInfo() );
                }
                if( optionalPane.getUssdServiceOp() != null )
                {
                    submitSM.setUssdServiceOp(
                        optionalPane.getUssdServiceOp() );
                }
                break;
            }
            case SUBMIT_SM_RESP:
                // No optional fields
                break;
            case SUBMIT_MULTI:
            {
                SubmitMultiSM submitMultiSM = (SubmitMultiSM)pdu;
                if( optionalPane.getUserMessageReference() != null )
                {
                    submitMultiSM.setUserMessageReference(
                        optionalPane.getUserMessageReference() );
                }
                if( optionalPane.getSourcePort() != null )
                {
                    submitMultiSM.setSourcePort(
                        optionalPane.getSourcePort() );
                }
                if( optionalPane.getSourceAddrSubunit() != null )
                {
                    submitMultiSM.setSourceAddrSubunit(
                        optionalPane.getSourceAddrSubunit() );
                }
                if( optionalPane.getDestinationPort() != null )
                {
                    submitMultiSM.setDestinationPort(
                        optionalPane.getDestinationPort() );
                }
                if( optionalPane.getDestAddrSubunit() != null )
                {
                    submitMultiSM.setDestAddrSubunit(
                        optionalPane.getDestAddrSubunit() );
                }
                if( optionalPane.getSarMsgRefNum() != null )
                {
                    submitMultiSM.setSarMsgRefNum(
                        optionalPane.getSarMsgRefNum() );
                }
                if( optionalPane.getSarTotalSegments() != null )
                {
                    submitMultiSM.setSarTotalSegments(
                        optionalPane.getSarTotalSegments() );
                }
                if( optionalPane.getSarSegmentSeqNum() != null )
                {
                    submitMultiSM.setSarSegmentSeqnum(
                        optionalPane.getSarSegmentSeqNum() );
                }
                if( optionalPane.getPayloadType() != null )
                {
                    submitMultiSM.setPayloadType(
                        optionalPane.getPayloadType() );
                }
                if( optionalPane.getMessagePayload() != null )
                {
                    submitMultiSM.setMessagePayload(
                        optionalPane.getMessagePayload() );
                }
                if( optionalPane.getPrivacyIndicator() != null )
                {
                    submitMultiSM.setPrivacyIndicator(
                        optionalPane.getPrivacyIndicator() );
                }
                if( optionalPane.getCallbackNum() != null )
                {
                    submitMultiSM.setCallbackNum(
                        optionalPane.getCallbackNum() );
                }
                if( optionalPane.getCallbackNumPresInd() != null )
                {
                    submitMultiSM.setCallbackNumPresInd(
                        optionalPane.getCallbackNumPresInd() );
                }
                if( optionalPane.getCallbackNumAtag() != null )
                {
                    submitMultiSM.setCallbackNumAtag(
                        optionalPane.getCallbackNumAtag() );
                }
                if( optionalPane.getSourceSubaddress() != null )
                {
                    submitMultiSM.setSourceSubaddress(
                        optionalPane.getSourceSubaddress() );
                }
                if( optionalPane.getDestSubaddress() != null )
                {
                    submitMultiSM.setDestSubaddress(
                        optionalPane.getDestSubaddress() );
                }
                if( optionalPane.getDisplayTime() != null )
                {
                    submitMultiSM.setDisplayTime(
                        optionalPane.getDisplayTime() );
                }
                if( optionalPane.getSmsSignal() != null )
                {
                    submitMultiSM.setSmsSignal(
                        optionalPane.getSmsSignal() );
                }
                if( optionalPane.getMsValidity() != null )
                {
                    submitMultiSM.setMsValidity(
                        optionalPane.getMsValidity() );
                }
                if( optionalPane.getMsMsgWaitFacilities() != null )
                {
                    submitMultiSM.setMsMsgWaitFacilities(
                        optionalPane.getMsMsgWaitFacilities() );
                }
                if( optionalPane.getAlertOnMsgDelivery() )
                {
                    submitMultiSM.setAlertOnMsgDelivery(
                        optionalPane.getAlertOnMsgDelivery() );
                }
                if( optionalPane.getLanguageIndicator() != null )
                {
                    submitMultiSM.setLanguageIndicator(
                        optionalPane.getLanguageIndicator() );
                }
                break;
            }
            case SUBMIT_MULTI_RESP:
                // No optional fields
                break;
            case DELIVER_SM:
            {
                DeliverSM deliverSM = (DeliverSM)pdu;
                if( optionalPane.getUserMessageReference() != null )
                {
                    deliverSM.setUserMessageReference(
                        optionalPane.getUserMessageReference() );
                }
                if( optionalPane.getSourcePort() != null )
                {
                    deliverSM.setSourcePort(
                        optionalPane.getSourcePort() );
                }
                if( optionalPane.getDestinationPort() != null )
                {
                    deliverSM.setDestinationPort(
                        optionalPane.getDestinationPort() );
                }
                if( optionalPane.getSarMsgRefNum() != null )
                {
                    deliverSM.setSarMsgRefNum(
                        optionalPane.getSarMsgRefNum() );
                }
                if( optionalPane.getSarTotalSegments() != null )
                {
                    deliverSM.setSarTotalSegments(
                        optionalPane.getSarTotalSegments() );
                }
                if( optionalPane.getSarSegmentSeqNum() != null )
                {
                    deliverSM.setSarSegmentSeqnum(
                        optionalPane.getSarSegmentSeqNum() );
                }
                if( optionalPane.getUserResponseCode() != null )
                {
                    deliverSM.setUserResponseCode(
                        optionalPane.getUserResponseCode() );
                }
                if( optionalPane.getPrivacyIndicator() != null )
                {
                    deliverSM.setPrivacyIndicator(
                        optionalPane.getPrivacyIndicator() );
                }
                if( optionalPane.getPayloadType() != null )
                {
                    deliverSM.setPayloadType(
                        optionalPane.getPayloadType() );
                }
                if( optionalPane.getMessagePayload() != null )
                {
                    deliverSM.setMessagePayload(
                        optionalPane.getMessagePayload() );
                }
                if( optionalPane.getCallbackNum() != null )
                {
                    deliverSM.setCallbackNum(
                        optionalPane.getCallbackNum() );
                }
                if( optionalPane.getSourceSubaddress() != null )
                {
                    deliverSM.setSourceSubaddress(
                        optionalPane.getSourceSubaddress() );
                }
                if( optionalPane.getDestSubaddress() != null )
                {
                    deliverSM.setDestSubaddress(
                        optionalPane.getDestSubaddress() );
                }
                if( optionalPane.getLanguageIndicator() != null )
                {
                    deliverSM.setLanguageIndicator(
                        optionalPane.getLanguageIndicator() );
                }
                if( optionalPane.getItsSessionInfo() != null )
                {
                    deliverSM.setItsSessionInfo(
                        optionalPane.getItsSessionInfo() );
                }
                if( optionalPane.getNetworkErrorCode() != null )
                {
                    deliverSM.setNetworkErrorCode(
                        optionalPane.getNetworkErrorCode() );
                }
                if( optionalPane.getMessageState() != null )
                {
                    deliverSM.setMessageState(
                        optionalPane.getMessageState() );
                }
                if( optionalPane.getReceiptedMessageID() != null )
                {
                    deliverSM.setReceiptedMessageId(
                        optionalPane.getReceiptedMessageID() );
                }
                break;
            }
            case DELIVER_SM_RESP:
            case REPLACE_SM:
            case REPLACE_SM_RESP:
            case CANCEL_SM:
            case CANCEL_SM_RESP:
            case ENQUIRE_LINK:
            case ENQUIRE_LINK_RESP:
                // No optional fields
                break;
            case ALERT_NOTIFICATION:
            {
//                AlertNotification alertNotification = (AlertNotification)pdu;
                if( optionalPane.getMsAvailabilityStatus() != null )
                {
                    throw new UnsupportedOperationException(
                        "MS_AVAILABILITY_STATUS not implemented for ALERT_NOTIFICATION" );
                }
                break;
            }
            case DATA_SM:
            {
                DataSM dataSM = (DataSM)pdu;
                if( optionalPane.getSourcePort() != null )
                {
                    dataSM.setSourcePort(
                        optionalPane.getSourcePort() );
                }
                if( optionalPane.getSourceAddrSubunit() != null )
                {
                    dataSM.setSourceAddrSubunit(
                        optionalPane.getSourceAddrSubunit() );
                }
                if( optionalPane.getSourceNetworkType() != null )
                {
                    dataSM.setSourceNetworkType(
                        optionalPane.getSourceNetworkType() );
                }
                if( optionalPane.getSourceBearerType() != null )
                {
                    dataSM.setSourceBearerType(
                        optionalPane.getSourceBearerType() );
                }
                if( optionalPane.getSourceTelematicsID() != null )
                {
                    dataSM.setSourceTelematicsId(
                        optionalPane.getSourceTelematicsID() );
                }
                if( optionalPane.getDestinationPort() != null )
                {
                    dataSM.setDestinationPort(
                        optionalPane.getDestinationPort() );
                }
                if( optionalPane.getDestAddrSubunit() != null )
                {
                    dataSM.setDestAddrSubunit(
                        optionalPane.getDestAddrSubunit() );
                }
                if( optionalPane.getDestNetworkType() != null )
                {
                    dataSM.setDestNetworkType(
                        optionalPane.getDestNetworkType() );
                }
                if( optionalPane.getDestBearerType() != null )
                {
                    dataSM.setDestBearerType(
                        optionalPane.getDestBearerType() );
                }
                if( optionalPane.getDestTelematicsID() != null )
                {
                    dataSM.setDestTelematicsId(
                        optionalPane.getDestTelematicsID() );
                }
                if( optionalPane.getSarMsgRefNum() != null )
                {
                    dataSM.setSarMsgRefNum(
                        optionalPane.getSarMsgRefNum() );
                }
                if( optionalPane.getSarTotalSegments() != null )
                {
                    dataSM.setSarTotalSegments(
                        optionalPane.getSarTotalSegments() );
                }
                if( optionalPane.getSarSegmentSeqNum() != null )
                {
                    dataSM.setSarSegmentSeqnum(
                        optionalPane.getSarSegmentSeqNum() );
                }
                if( optionalPane.getMoreMessagesToSend() != null )
                {
                    dataSM.setMoreMsgsToSend(
                        optionalPane.getMoreMessagesToSend() );
                }
                if( optionalPane.getQosTimeToLive() != null )
                {
                    dataSM.setQosTimeToLive(
                        optionalPane.getQosTimeToLive() );
                }
                if( optionalPane.getPayloadType() != null )
                {
                    dataSM.setPayloadType(
                        optionalPane.getPayloadType() );
                }
                if( optionalPane.getMessagePayload() != null )
                {
                    dataSM.setMessagePayload(
                        optionalPane.getMessagePayload() );
                }
                if( optionalPane.getSetDPF() != null )
                {
                    dataSM.setSetDpf( optionalPane.getSetDPF() );
                }
                if( optionalPane.getReceiptedMessageID() != null )
                {
                    dataSM.setReceiptedMessageId(
                        optionalPane.getReceiptedMessageID() );
                }
                if( optionalPane.getMessageState() != null )
                {
                    dataSM.setMessageState( optionalPane.getMessageState() );
                }
                if( optionalPane.getNetworkErrorCode() != null )
                {
                    dataSM.setNetworkErrorCode( optionalPane.getNetworkErrorCode() );
                }
                if( optionalPane.getUserMessageReference() != null )
                {
                    dataSM.setUserMessageReference(
                        optionalPane.getUserMessageReference() );
                }
                if( optionalPane.getPrivacyIndicator() != null )
                {
                    dataSM.setPrivacyIndicator(
                        optionalPane.getPrivacyIndicator() );
                }
                if( optionalPane.getCallbackNum() != null )
                {
                    dataSM.setCallbackNum(
                        optionalPane.getCallbackNum() );
                }
                if( optionalPane.getCallbackNumPresInd() != null )
                {
                    dataSM.setCallbackNumPresInd(
                        optionalPane.getCallbackNumPresInd() );
                }
                if( optionalPane.getCallbackNumAtag() != null )
                {
                    dataSM.setCallbackNumAtag(
                        optionalPane.getCallbackNumAtag() );
                }
                if( optionalPane.getSourceSubaddress() != null )
                {
                    dataSM.setSourceSubaddress(
                        optionalPane.getSourceSubaddress() );
                }
                if( optionalPane.getDestSubaddress() != null )
                {
                    dataSM.setDestSubaddress(
                        optionalPane.getDestSubaddress() );
                }
                if( optionalPane.getUserResponseCode() != null )
                {
                    dataSM.setUserResponseCode(
                        optionalPane.getUserResponseCode() );
                }
                if( optionalPane.getDisplayTime() != null )
                {
                    dataSM.setDisplayTime(
                        optionalPane.getDisplayTime() );
                }
                if( optionalPane.getSmsSignal() != null )
                {
                    dataSM.setSmsSignal(
                        optionalPane.getSmsSignal() );
                }
                if( optionalPane.getMsValidity() != null )
                {
                    dataSM.setMsValidity(
                        optionalPane.getMsValidity() );
                }
                if( optionalPane.getMsMsgWaitFacilities() != null )
                {
                    dataSM.setMsMsgWaitFacilities(
                        optionalPane.getMsMsgWaitFacilities() );
                }
                if( optionalPane.getNumberOfMessages() != null )
                {
                    dataSM.setNumberOfMessages(
                        optionalPane.getNumberOfMessages() );
                }
                if( optionalPane.getAlertOnMsgDelivery() )
                {
                    dataSM.setAlertOnMsgDelivery(
                        optionalPane.getAlertOnMsgDelivery() );
                }
                if( optionalPane.getLanguageIndicator() != null )
                {
                    dataSM.setLanguageIndicator(
                        optionalPane.getLanguageIndicator() );
                }
                if( optionalPane.getItsReplyType() != null )
                {
                    dataSM.setItsReplyType(
                        optionalPane.getItsReplyType() );
                }
                if( optionalPane.getItsSessionInfo() != null )
                {
                    dataSM.setItsSessionInfo(
                        optionalPane.getItsSessionInfo() );
                }
                break;
            }
            case DATA_SM_RESP:
            {
                DataSMResp dataSMResp = (DataSMResp)pdu;
                if( optionalPane.getDeliveryFailureReason() != null )
                {
                    dataSMResp.setDeliveryFailureReason(
                        optionalPane.getDeliveryFailureReason() );
                }
                if( optionalPane.getNetworkErrorCode() != null )
                {
                    dataSMResp.setNetworkErrorCode(
                        optionalPane.getNetworkErrorCode() );
                }
                if( optionalPane.getAdditionalStatusInfoText() != null )
                {
                    dataSMResp.setAdditionalStatusInfoText(
                        optionalPane.getAdditionalStatusInfoText() );
                }
                if( optionalPane.getDpfResult() != null )
                {
                    dataSMResp.setDpfResult( optionalPane.getDpfResult() );
                }
                break;
            }
            }
        }
        catch( Exception ex )
        {
            JOptionPane.showMessageDialog(
                this,
                "Caught an exception\n" + ex,
                "Exception",
                JOptionPane.ERROR_MESSAGE );
            System.err.println( ex );
            ex.printStackTrace();
            return null;
        }

        return pdu;
    }


    public void setPDU( final PDU pdu )
    {
        setPDUType( pdu.getCommandId() );
        headerPane.setSequence( pdu.getSequenceNumber() );
        headerPane.setCommandStatus( pdu.getCommandStatus() );

        try
        {
            // Set mandatory values
            switch( pdu.getCommandId() )
            {
            case Data.BIND_RECEIVER:
            case Data.BIND_TRANSMITTER:
            case Data.BIND_TRANSCEIVER:
            {
                BindRequest request = (BindRequest)pdu;
                mandatoryPane.setSystemID( request.getSystemId() );
                mandatoryPane.setPassword( request.getPassword() );
                mandatoryPane.setSystemType( request.getSystemType() );
                mandatoryPane.setInterfaceVersion( request.getInterfaceVersion() );
                mandatoryPane.setAddrTON( request.getAddressRange().getTon() );
                mandatoryPane.setAddrNPI( request.getAddressRange().getNpi() );
                mandatoryPane.setAddressRange( request.getAddressRange().getAddressRange() );
                break;
            }
            case Data.BIND_RECEIVER_RESP:
            case Data.BIND_TRANSMITTER_RESP:
            case Data.BIND_TRANSCEIVER_RESP:
            {
                BindResponse response = (BindResponse)pdu;
                mandatoryPane.setSystemID( response.getSystemId() );
                break;
            }
            case Data.OUTBIND:
            {
                Outbind outbind = (Outbind)pdu;
                mandatoryPane.setSystemID( outbind.getSystemId() );
                mandatoryPane.setPassword( outbind.getPassword() );
                break;
            }
            case Data.UNBIND:
            case Data.UNBIND_RESP:
            case Data.GENERIC_NACK:
                // No mandatory fields
                break;
            case Data.QUERY_SM:
            {
                QuerySM querySM = (QuerySM)pdu;
                mandatoryPane.setMessageID( querySM.getMessageId() );
                mandatoryPane.setSourceAddrTON( querySM.getSourceAddr().getTon() );
                mandatoryPane.setSourceAddrNPI( querySM.getSourceAddr().getNpi() );
                mandatoryPane.setSourceAddr( querySM.getSourceAddr().getAddress() );
                break;
            }
            case Data.QUERY_SM_RESP:
            {
                QuerySMResp querySMResp = (QuerySMResp)pdu;
                mandatoryPane.setMessageID( querySMResp.getMessageId() );
                mandatoryPane.setFinalDate( querySMResp.getFinalDate() );
                mandatoryPane.setMessageState( querySMResp.getMessageState() );
                mandatoryPane.setErrorCode( querySMResp.getErrorCode() );
                break;
            }
            case Data.SUBMIT_SM:
            {
                SubmitSM submitSM = (SubmitSM)pdu;
                mandatoryPane.setServiceType( submitSM.getServiceType() );
                mandatoryPane.setSourceAddrTON( submitSM.getSourceAddr().getTon() );
                mandatoryPane.setSourceAddrNPI( submitSM.getSourceAddr().getNpi() );
                mandatoryPane.setSourceAddr( submitSM.getSourceAddr().getAddress() );
                mandatoryPane.setDestAddrTON( submitSM.getDestAddr().getTon() );
                mandatoryPane.setDestAddrNPI( submitSM.getDestAddr().getNpi() );
                mandatoryPane.setDestAddresses( submitSM.getDestAddr().getAddress() );
                mandatoryPane.setEsmClass( submitSM.getEsmClass() );
                mandatoryPane.setProtocolID( submitSM.getProtocolId() );
                mandatoryPane.setPriorityFlag( submitSM.getPriorityFlag() );
                mandatoryPane.setScheduleDeliveryTime( submitSM.getScheduleDeliveryTime() );
                mandatoryPane.setValidityPeriod( submitSM.getValidityPeriod() );
                mandatoryPane.setRegisteredDelivery( submitSM.getRegisteredDelivery() );
                mandatoryPane.setReplaceIfPresentFlag( submitSM.getReplaceIfPresentFlag() );
                mandatoryPane.setDataCoding( submitSM.getDataCoding() );
                mandatoryPane.setSmDefaultMsgID( submitSM.getSmDefaultMsgId() );
                mandatoryPane.setShortMessage( submitSM.getShortMessage( Data.ENC_ASCII ) );
                break;
            }
            case Data.SUBMIT_SM_RESP:
            {
                SubmitSMResp submitSMResp = (SubmitSMResp)pdu;
                mandatoryPane.setMessageID( submitSMResp.getMessageId() );
                break;
            }
            case Data.SUBMIT_MULTI:
            {
                SubmitMultiSM submitMultiSM = (SubmitMultiSM)pdu;
                mandatoryPane.setServiceType( submitMultiSM.getServiceType() );
                mandatoryPane.setSourceAddrTON( submitMultiSM.getSourceAddr().getTon() );
                mandatoryPane.setSourceAddrNPI( submitMultiSM.getSourceAddr().getNpi() );
                mandatoryPane.setSourceAddr( submitMultiSM.getSourceAddr().getAddress() );
                for( int i=0; i<submitMultiSM.getNumberOfDests(); ++ i )
                {
                    mandatoryPane.setDestAddrTON(
                        submitMultiSM.getDestAddress(i).getAddress().getTon() );
                    mandatoryPane.setDestAddrNPI(
                        submitMultiSM.getDestAddress( i ).getAddress().getNpi() );
                    mandatoryPane.setDestAddresses(
                        submitMultiSM.getDestAddress(i).getAddress().getAddress() );
                }
                mandatoryPane.setEsmClass( submitMultiSM.getEsmClass() );
                mandatoryPane.setProtocolID( submitMultiSM.getProtocolId() );
                mandatoryPane.setPriorityFlag( submitMultiSM.getPriorityFlag() );
                mandatoryPane.setScheduleDeliveryTime( submitMultiSM.getScheduleDeliveryTime() );
                mandatoryPane.setValidityPeriod( submitMultiSM.getValidityPeriod() );
                mandatoryPane.setRegisteredDelivery( submitMultiSM.getRegisteredDelivery() );
                mandatoryPane.setReplaceIfPresentFlag( submitMultiSM.getReplaceIfPresentFlag() );
                mandatoryPane.setDataCoding( submitMultiSM.getDataCoding() );
                mandatoryPane.setSmDefaultMsgID( submitMultiSM.getSmDefaultMsgId() );
                mandatoryPane.setShortMessage( submitMultiSM.getShortMessage( Data.ENC_ASCII ) );
                break;
            }
            case Data.SUBMIT_MULTI_RESP:
            {
                SubmitMultiSMResp submitMultiSMResp = (SubmitMultiSMResp)pdu;
                mandatoryPane.setMessageID( submitMultiSMResp.getMessageId() );
                // TODO if we ever need it?
                // submitMultiSMResp.addUnsuccessSME( ... );
                throw new UnsupportedOperationException(
                    "SUBMIT_MULTI_RESP needs more work"  );
//                break;
            }
            case Data.DELIVER_SM:
            {
                DeliverSM deliverSM = (DeliverSM)pdu;
                mandatoryPane.setServiceType( deliverSM.getServiceType() );
                mandatoryPane.setSourceAddrTON( deliverSM.getSourceAddr().getTon() );
                mandatoryPane.setSourceAddrNPI( deliverSM.getSourceAddr().getNpi() );
                mandatoryPane.setSourceAddr(
                    deliverSM.getSourceAddr().getAddress() );
                mandatoryPane.setDestAddrTON( deliverSM.getDestAddr().getTon() );
                mandatoryPane.setDestAddrNPI( deliverSM.getDestAddr().getNpi() );
                mandatoryPane.setDestinationAddr(
                    deliverSM.getDestAddr().getAddress() );
                mandatoryPane.setEsmClass( deliverSM.getEsmClass() );
                mandatoryPane.setProtocolID( deliverSM.getProtocolId() );
                mandatoryPane.setPriorityFlag( deliverSM.getPriorityFlag() );
                mandatoryPane.setRegisteredDelivery( deliverSM.getRegisteredDelivery() );
                mandatoryPane.setDataCoding( deliverSM.getDataCoding() );
                mandatoryPane.setShortMessage(
                    deliverSM.getShortMessage( Data.ENC_ASCII ) );
                break;
            }
            case Data.DELIVER_SM_RESP:
            {
                DeliverSMResp deliverSMResp = (DeliverSMResp)pdu;
                mandatoryPane.setMessageID( deliverSMResp.getMessageId() );
                break;
            }
            case Data.REPLACE_SM:
            {
                ReplaceSM replaceSM = (ReplaceSM)pdu;
                mandatoryPane.setMessageID( replaceSM.getMessageId() );
                mandatoryPane.setSourceAddrTON( replaceSM.getSourceAddr().getTon() );
                mandatoryPane.setSourceAddrNPI( replaceSM.getSourceAddr().getNpi() );
                mandatoryPane.setSourceAddr( replaceSM.getSourceAddr().getAddress() );
                mandatoryPane.setScheduleDeliveryTime( replaceSM.getScheduleDeliveryTime() );
                mandatoryPane.setValidityPeriod( replaceSM.getValidityPeriod() );
                mandatoryPane.setRegisteredDelivery( replaceSM.getRegisteredDelivery() );
                mandatoryPane.setSmDefaultMsgID( replaceSM.getSmDefaultMsgId() );
                mandatoryPane.setShortMessage(
                    replaceSM.getShortMessage( Data.ENC_ASCII  ) );
                break;
            }
            case Data.REPLACE_SM_RESP:
                // No mandatory fields
                break;
            case Data.CANCEL_SM:
            {
                CancelSM cancelSM = (CancelSM)pdu;
                mandatoryPane.setServiceType( cancelSM.getServiceType() );
                mandatoryPane.setMessageID( cancelSM.getMessageId() );
                mandatoryPane.setSourceAddrTON( cancelSM.getSourceAddr().getTon() );
                mandatoryPane.setSourceAddrNPI( cancelSM.getSourceAddr().getNpi() );
                mandatoryPane.setSourceAddr( cancelSM.getSourceAddr().getAddress() );
                mandatoryPane.setDestAddrTON( cancelSM.getDestAddr().getTon() );
                mandatoryPane.setDestAddrNPI( cancelSM.getDestAddr().getNpi() );
                mandatoryPane.setDestinationAddr(
                    cancelSM.getDestAddr().getAddress() );
                break;
            }
            case Data.CANCEL_SM_RESP:
            case Data.ENQUIRE_LINK:
            case Data.ENQUIRE_LINK_RESP:
                // No mandatory fields
                break;
            case Data.ALERT_NOTIFICATION:
            {
                throw new UnsupportedOperationException(
                    "ALERT_NOTIFICATION needs more work"  );
//            AlertNotification alertNotification = (AlertNotification)pdu;
//            break;
            }
            case Data.DATA_SM:
            {
                DataSM dataSM = (DataSM)pdu;
                mandatoryPane.setServiceType( dataSM.getServiceType() );
                mandatoryPane.setSourceAddrTON( dataSM.getSourceAddr().getTon() );
                mandatoryPane.setSourceAddrNPI( dataSM.getSourceAddr().getNpi() );
                mandatoryPane.setSourceAddr( dataSM.getSourceAddr().getAddress() );
                mandatoryPane.setDestAddrTON( dataSM.getDestAddr().getTon() );
                mandatoryPane.setDestAddrNPI( dataSM.getDestAddr().getNpi() );
                mandatoryPane.setDestinationAddr(
                    dataSM.getDestAddr().getAddress() );
                mandatoryPane.setEsmClass( dataSM.getEsmClass() );
                mandatoryPane.setRegisteredDelivery(
                    dataSM.getRegisteredDelivery() );
                mandatoryPane.setDataCoding( dataSM.getDataCoding() );
                break;
            }
            case Data.DATA_SM_RESP:
            {
                DataSMResp dataSMResp = (DataSMResp)pdu;
                mandatoryPane.setMessageID( dataSMResp.getMessageId() );
                break;
            }
            }
            
            // Set optional values
            switch( pdu.getCommandId() )
            {
            case Data.BIND_RECEIVER:
            case Data.BIND_TRANSMITTER:
            case Data.BIND_TRANSCEIVER:
                // No optional field
                break;
            case Data.BIND_RECEIVER_RESP:
            case Data.BIND_TRANSMITTER_RESP:
            case Data.BIND_TRANSCEIVER_RESP:
            {
                BindResponse response = (BindResponse)pdu;
                if( response.hasScInterfaceVersion() )
                {
                    optionalPane.setScInterfaceVersion(
                        response.getScInterfaceVersion() );
                }
                break;
            }
            case Data.OUTBIND:
            case Data.UNBIND:
            case Data.UNBIND_RESP:
            case Data.GENERIC_NACK:
            case Data.QUERY_SM:
            case Data.QUERY_SM_RESP:
                // No optional fields
                break;
            case Data.SUBMIT_SM:
            {
                SubmitSM submitSM = (SubmitSM)pdu;
                if( submitSM.hasUserMessageReference() )
                {
                    optionalPane.setUserMessageReference(
                        submitSM.getUserMessageReference() );
                }
                if( submitSM.hasSourcePort() )
                {
                    optionalPane.setSourcePort(
                        submitSM.getSourcePort() );
                }
                if( submitSM.hasSourceAddrSubunit() )
                {
                    optionalPane.setSourceAddrSubunit(
                        submitSM.getSourceAddrSubunit() );
                }
                if( submitSM.hasDestinationPort() )
                {
                    optionalPane.setDestinationPort(
                        submitSM.getDestinationPort() );
                }
                if( submitSM.hasDestAddrSubunit() )
                {
                    optionalPane.setDestAddrSubunit(
                        submitSM.getDestAddrSubunit() );
                }
                if( submitSM.hasSarMsgRefNum() )
                {
                    optionalPane.setSarMsgRefNum(
                        submitSM.getSarMsgRefNum() );
                }
                if( submitSM.hasSarTotalSegments() )
                {
                    optionalPane.setSarTotalSegments(
                        submitSM.getSarTotalSegments() );
                }
                if( submitSM.hasSarSegmentSeqnum() )
                {
                    optionalPane.setSarSegmentSeqNum(
                        submitSM.getSarSegmentSeqnum() );
                }
                if( submitSM.hasMoreMsgsToSend() )
                {
                    optionalPane.setMoreMessagesToSend(
                        submitSM.getMoreMsgsToSend() );
                }
                if( submitSM.hasPayloadType() )
                {
                    optionalPane.setPayloadType(
                        submitSM.getPayloadType() );
                }
                if( submitSM.hasMessagePayload() )
                {
                    optionalPane.setMessagePayload(
                        submitSM.getMessagePayload().removeString(
                            submitSM.getMessagePayload().length(), Data.ENC_ASCII ) );
                }
                if( submitSM.hasPrivacyIndicator() )
                {
                    optionalPane.setPrivacyIndicator(
                        submitSM.getPrivacyIndicator() );
                }
                if( submitSM.hasCallbackNum() )
                {
                    optionalPane.setCallbackNum(
                        submitSM.callbackNum().removeString(
                            submitSM.callbackNum().length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( submitSM.hasCallbackNumPresInd() )
                {
                    optionalPane.setCallbackNumPresInd(
                        submitSM.getCallbackNumPresInd() );
                }
                if( submitSM.hasCallbackNumAtag() )
                {
                    ByteBuffer buf = submitSM.getCallbackNumAtag();
                    byte encoding = buf.removeByte();
                    optionalPane.setCallbackNumAtag(
                        Byte.toString( encoding ) +
                        ": " + buf.removeString(
                            buf.length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( submitSM.hasSourceSubaddress() )
                {
                    optionalPane.setSourceSubaddress(
                        submitSM.getSourceSubaddress().removeString(
                            submitSM.getSourceSubaddress().length(),
                            Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( submitSM.hasDestSubaddress() )
                {
                    optionalPane.setDestSubaddress(
                        submitSM.getDestSubaddress().removeString(
                            submitSM.getDestSubaddress().length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( submitSM.hasUserResponseCode() )
                {
                    optionalPane.setUserResponseCode(
                        submitSM.getUserResponseCode() );
                }
                if( submitSM.hasDisplayTime() )
                {
                    optionalPane.setDisplayTime(
                        submitSM.getDisplayTime() );
                }
                if( submitSM.hasSmsSignal() )
                {
                    optionalPane.setSmsSignal(
                        submitSM.getSmsSignal() );
                }
                if( submitSM.hasMsValidity() )
                {
                    optionalPane.setMsValidity(
                        submitSM.getMsValidity() );
                }
                if( submitSM.hasMsMsgWaitFacilities() )
                {
                    optionalPane.setMsMsgWaitFacilities(
                        submitSM.getMsMsgWaitFacilities() );
                }
                if( submitSM.hasNumberOfMessages() )
                {
                    optionalPane.setNumberOfMessages(
                        submitSM.getNumberOfMessages() );
                }
                if( submitSM.hasAlertOnMsgDelivery() )
                {
                    optionalPane.setAlertOnMsgDelivery(
                        submitSM.getAlertOnMsgDelivery() );
                }
                if( submitSM.hasLanguageIndicator() )
                {
                    optionalPane.setLanguageIndicator(
                        submitSM.getLanguageIndicator() );
                }
                if( submitSM.hasItsReplyType() )
                {
                    optionalPane.setItsReplyType(
                        submitSM.getItsReplyType() );
                }
                if( submitSM.hasItsSessionInfo() )
                {
                    optionalPane.setItsSessionInfo(
                        submitSM.getItsSessionInfo() );
                }
                if( submitSM.hasUssdServiceOp() )
                {
                    optionalPane.setUssdServiceOp(
                        submitSM.getUssdServiceOp() );
                }
                break;
            }
            case Data.SUBMIT_SM_RESP:
                // No optional fields
                break;
            case Data.SUBMIT_MULTI:
            {
                SubmitMultiSM submitMultiSM = (SubmitMultiSM)pdu;
                if( submitMultiSM.hasUserMessageReference() )
                {
                    optionalPane.setUserMessageReference(
                        submitMultiSM.getUserMessageReference() );
                }
                if( submitMultiSM.hasSourcePort() )
                {
                    optionalPane.setSourcePort(
                        submitMultiSM.getSourcePort() );
                }
                if( submitMultiSM.hasSourceAddrSubunit() )
                {
                    optionalPane.setSourceAddrSubunit(
                        submitMultiSM.getSourceAddrSubunit() );
                }
                if( submitMultiSM.hasDestinationPort() )
                {
                    optionalPane.setDestinationPort(
                        submitMultiSM.getDestinationPort() );
                }
                if( submitMultiSM.hasDestAddrSubunit() )
                {
                    optionalPane.setDestAddrSubunit(
                        submitMultiSM.getDestAddrSubunit() );
                }
                if( submitMultiSM.hasSarMsgRefNum() )
                {
                    optionalPane.setSarMsgRefNum(
                        submitMultiSM.getSarMsgRefNum() );
                }
                if( submitMultiSM.hasSarTotalSegments() )
                {
                    optionalPane.setSarTotalSegments(
                        submitMultiSM.getSarTotalSegments() );
                }
                if( submitMultiSM.hasSarSegmentSeqnum() )
                {
                    optionalPane.setSarSegmentSeqNum(
                        submitMultiSM.getSarSegmentSeqnum() );
                }
                if( submitMultiSM.hasPayloadType() )
                {
                    optionalPane.setPayloadType(
                        submitMultiSM.getPayloadType() );
                }
                if( submitMultiSM.hasMessagePayload() )
                {
                    optionalPane.setMessagePayload(
                        submitMultiSM.getMessagePayload().removeString(
                            submitMultiSM.getMessagePayload().length(), Data.ENC_ASCII ) );
                }
                if( submitMultiSM.hasPrivacyIndicator() )
                {
                    optionalPane.setPrivacyIndicator(
                        submitMultiSM.getPrivacyIndicator() );
                }
                if( submitMultiSM.hasCallbackNum() )
                {
                    optionalPane.setCallbackNum(
                        submitMultiSM.callbackNum().removeString(
                            submitMultiSM.callbackNum().length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( submitMultiSM.hasCallbackNumPresInd() )
                {
                    optionalPane.setCallbackNumPresInd(
                        submitMultiSM.getCallbackNumPresInd() );
                }
                if( submitMultiSM.hasCallbackNumAtag() )
                {
                    ByteBuffer buf = submitMultiSM.getCallbackNumAtag();
                    byte encoding = buf.removeByte();
                    optionalPane.setCallbackNumAtag(
                        Byte.toString( encoding ) +
                        ": " + buf.removeString(
                            buf.length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( submitMultiSM.hasSourceSubaddress() )
                {
                    optionalPane.setSourceSubaddress(
                        submitMultiSM.getSourceSubaddress().removeString(
                            submitMultiSM.getSourceSubaddress().length(),
                            Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( submitMultiSM.hasDestSubaddress() )
                {
                    optionalPane.setDestSubaddress(
                        submitMultiSM.getDestSubaddress().removeString(
                            submitMultiSM.getDestSubaddress().length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( submitMultiSM.hasDisplayTime() )
                {
                    optionalPane.setDisplayTime(
                        submitMultiSM.getDisplayTime() );
                }
                if( submitMultiSM.hasSmsSignal() )
                {
                    optionalPane.setSmsSignal(
                        submitMultiSM.getSmsSignal() );
                }
                if( submitMultiSM.hasMsValidity() )
                {
                    optionalPane.setMsValidity(
                        submitMultiSM.getMsValidity() );
                }
                if( submitMultiSM.hasMsMsgWaitFacilities() )
                {
                    optionalPane.setMsMsgWaitFacilities(
                        submitMultiSM.getMsMsgWaitFacilities() );
                }
                if( submitMultiSM.hasAlertOnMsgDelivery() )
                {
                    optionalPane.setAlertOnMsgDelivery(
                        submitMultiSM.getAlertOnMsgDelivery() );
                }
                if( submitMultiSM.hasLanguageIndicator() )
                {
                    optionalPane.setLanguageIndicator(
                        submitMultiSM.getLanguageIndicator() );
                }
                break;
            }
            case Data.SUBMIT_MULTI_RESP:
                // No optional fields
                break;
            case Data.DELIVER_SM:
            {
                DeliverSM deliverSM = (DeliverSM)pdu;
                if( deliverSM.hasUserMessageReference() )
                {
                    optionalPane.setUserMessageReference(
                        deliverSM.getUserMessageReference() );
                }
                if( deliverSM.hasSourcePort() )
                {
                    optionalPane.setSourcePort(
                        deliverSM.getSourcePort() );
                }
                if( deliverSM.hasDestinationPort() )
                {
                    optionalPane.setDestinationPort(
                        deliverSM.getDestinationPort() );
                }
                if( deliverSM.hasSarMsgRefNum() )
                {
                    optionalPane.setSarMsgRefNum(
                        deliverSM.getSarMsgRefNum() );
                }
                if( deliverSM.hasSarTotalSegments() )
                {
                    optionalPane.setSarTotalSegments(
                        deliverSM.getSarTotalSegments() );
                }
                if( deliverSM.hasSarSegmentSeqnum() )
                {
                    optionalPane.setSarSegmentSeqNum(
                        deliverSM.getSarSegmentSeqnum() );
                }
                if( deliverSM.hasUserResponseCode() )
                {
                    optionalPane.setUserResponseCode(
                        deliverSM.getUserResponseCode() );
                }
                if( deliverSM.hasPrivacyIndicator() )
                {
                    optionalPane.setPrivacyIndicator(
                        deliverSM.getPrivacyIndicator() );
                }
                if( deliverSM.hasPayloadType() )
                {
                    optionalPane.setPayloadType(
                        deliverSM.getPayloadType() );
                }
                if( deliverSM.hasMessagePayload() )
                {
                    optionalPane.setMessagePayload(
                        deliverSM.getMessagePayload().removeString(
                            deliverSM.getMessagePayload().length(), Data.ENC_ASCII ) );
                }
                if( deliverSM.hasCallbackNum() )
                {
                    optionalPane.setCallbackNum(
                        deliverSM.callbackNum().removeString(
                            deliverSM.callbackNum().length(), Data.ENC_ASCII ) );
                }
                if( deliverSM.hasSourceSubaddress() )
                {
                    optionalPane.setSourceSubaddress(
                        deliverSM.getSourceSubaddress().removeString(
                            deliverSM.getSourceSubaddress().length(),
                            Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( deliverSM.hasDestSubaddress() )
                {
                    optionalPane.setDestSubaddress(
                        deliverSM.getDestSubaddress().removeString(
                            deliverSM.getDestSubaddress().length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( deliverSM.hasLanguageIndicator() )
                {
                    optionalPane.setLanguageIndicator(
                        deliverSM.getLanguageIndicator() );
                }
                if( deliverSM.hasItsSessionInfo() )
                {
                    optionalPane.setItsSessionInfo(
                        deliverSM.getItsSessionInfo() );
                }
                if( deliverSM.hasNetworkErrorCode() )
                {
                    // defensive copy of network error code
                    ByteBuffer buf = new ByteBuffer(
                        deliverSM.getNetworkErrorCode().getBuffer() );
                    optionalPane.setNetworkErrorCode(
                        Byte.toString( buf.removeByte() ) + ", " +
                        Short.toString( buf.removeShort() ) );
                }
                if( deliverSM.hasMessageState() )
                {
                    optionalPane.setMessageState(
                        deliverSM.getMessageState() );
                }
                if( deliverSM.hasReceiptedMessageId() )
                {
                    optionalPane.setReceiptedMessageID(
                        deliverSM.getReceiptedMessageId() );
                }
                break;
            }
            case Data.DELIVER_SM_RESP:
            case Data.REPLACE_SM:
            case Data.REPLACE_SM_RESP:
            case Data.CANCEL_SM:
            case Data.CANCEL_SM_RESP:
            case Data.ENQUIRE_LINK:
            case Data.ENQUIRE_LINK_RESP:
                // No optional fields
                break;
            case Data.ALERT_NOTIFICATION:
            {
//                AlertNotification alertNotification = (AlertNotification)pdu;
                if( optionalPane.getMsAvailabilityStatus() != null )
                {
                    throw new UnsupportedOperationException(
                        "MS_AVAILABILITY_STATUS not implemented for ALERT_NOTIFICATION" );
                }
                break;
            }
            case Data.DATA_SM:
            {
                DataSM dataSM = (DataSM)pdu;
                if( dataSM.hasSourcePort() )
                {
                    optionalPane.setSourcePort(
                        dataSM.getSourcePort() );
                }
                if( dataSM.hasSourceAddrSubunit() )
                {
                    optionalPane.setSourceAddrSubunit(
                        dataSM.getSourceAddrSubunit() );
                }
                if( dataSM.hasSourceNetworkType() )
                {
                    optionalPane.setSourceNetworkType(
                        dataSM.getSourceNetworkType() );
                }
                if( dataSM.hasSourceBearerType() )
                {
                    optionalPane.setSourceBearerType(
                        dataSM.getSourceBearerType() );
                }
                if( dataSM.hasSourceTelematicsId() )
                {
                    optionalPane.setSourceTelematicsID(
                        dataSM.getSourceTelematicsId() );
                }
                if( dataSM.hasDestinationPort() )
                {
                    optionalPane.setDestinationPort( dataSM.getDestinationPort() );
                }
                if( dataSM.hasDestAddrSubunit() )
                {
                    optionalPane.setDestAddrSubunit( dataSM.getDestAddrSubunit() );
                }
                if( dataSM.hasDestNetworkType() )
                {
                    optionalPane.setDestNetworkType( dataSM.getDestNetworkType() );
                }
                if( dataSM.hasDestBearerType() )
                {
                    optionalPane.setDestBearerType( dataSM.getDestBearerType() );
                }
                if( dataSM.hasDestTelematicsId() )
                {
                    optionalPane.setDestTelematicsID(
                        dataSM.getDestTelematicsId() );
                }
                if( dataSM.hasSarMsgRefNum() )
                {
                    optionalPane.setSarMsgRefNum(
                        dataSM.getSarMsgRefNum() );
                }
                if( dataSM.hasSarTotalSegments() )
                {
                    optionalPane.setSarTotalSegments(
                        dataSM.getSarTotalSegments() );
                }
                if( dataSM.hasSarSegmentSeqnum() )
                {
                    optionalPane.setSarSegmentSeqNum(
                        dataSM.getSarSegmentSeqnum() );
                }
                if( dataSM.hasMoreMsgsToSend() )
                {
                    optionalPane.setMoreMessagesToSend(
                        dataSM.getMoreMsgsToSend() );
                }
                if( dataSM.hasQosTimeToLive() )
                {
                    optionalPane.setQosTimeToLive(
                        dataSM.getQosTimeToLive() );
                }
                if( dataSM.hasPayloadType() )
                {
                    optionalPane.setPayloadType(
                        dataSM.getPayloadType() );
                }
                if( dataSM.hasMessagePayload() )
                {
                    optionalPane.setMessagePayload(
                        dataSM.getMessagePayload().removeString(
                            dataSM.getMessagePayload().length(), Data.ENC_ASCII ) );
                }
                if( dataSM.hasSetDpf() )
                {
                    optionalPane.setSetDPF( dataSM.getSetDpf() );
                }
                if( dataSM.hasReceiptedMessageId() )
                {
                    optionalPane.setReceiptedMessageID(
                        dataSM.getReceiptedMessageId() );
                }
                if( dataSM.hasMessageState() )
                {
                    optionalPane.setMessageState( dataSM.getMessageState() );
                }
                if( dataSM.hasNetworkErrorCode() )
                {
                    ByteBuffer buf = dataSM.getNetworkErrorCode();
                    optionalPane.setNetworkErrorCode(
                        Byte.toString( buf.removeByte() ) + ", " +
                        Short.toString( buf.removeShort() ) );
                }
                if( dataSM.hasUserMessageReference() )
                {
                    optionalPane.setUserMessageReference(
                        dataSM.getUserMessageReference() );
                }
                if( dataSM.hasPrivacyIndicator() )
                {
                    optionalPane.setPrivacyIndicator(
                        dataSM.getPrivacyIndicator() );
                }
                if( dataSM.hasCallbackNum() )
                {
                    optionalPane.setCallbackNum(
                        dataSM.callbackNum().removeString(
                            dataSM.callbackNum().length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( dataSM.hasCallbackNumPresInd() )
                {
                    optionalPane.setCallbackNumPresInd(
                        dataSM.getCallbackNumPresInd() );
                }
                if( dataSM.hasCallbackNumAtag() )
                {
                    ByteBuffer buf = dataSM.getCallbackNumAtag();
                    byte encoding = buf.removeByte();
                    optionalPane.setCallbackNumAtag(
                        Byte.toString( encoding ) +
                        ": " + buf.removeString(
                            buf.length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( dataSM.hasSourceSubaddress() )
                {
                    optionalPane.setSourceSubaddress(
                        dataSM.getSourceSubaddress().removeString(
                            dataSM.getSourceSubaddress().length(),
                            Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( dataSM.hasDestSubaddress() )
                {
                    optionalPane.setDestSubaddress(
                        dataSM.getDestSubaddress().removeString(
                            dataSM.getDestSubaddress().length(), Data.ENC_ASCII ) ); // TODO: encodings
                }
                if( dataSM.hasUserResponseCode() )
                {
                    optionalPane.setUserResponseCode(
                        dataSM.getUserResponseCode() );
                }
                if( dataSM.hasDisplayTime() )
                {
                    optionalPane.setDisplayTime(
                        dataSM.getDisplayTime() );
                }
                if( dataSM.hasSmsSignal() )
                {
                    optionalPane.setSmsSignal(
                        dataSM.getSmsSignal() );
                }
                if( dataSM.hasMsValidity() )
                {
                    optionalPane.setMsValidity(
                        dataSM.getMsValidity() );
                }
                if( dataSM.hasMsMsgWaitFacilities() )
                {
                    optionalPane.setMsMsgWaitFacilities(
                        dataSM.getMsMsgWaitFacilities() );
                }
                if( dataSM.hasNumberOfMessages() )
                {
                    optionalPane.setNumberOfMessages(
                        dataSM.getNumberOfMessages() );
                }
                if( optionalPane.getAlertOnMsgDelivery() )
                {
                    dataSM.setAlertOnMsgDelivery(
                        optionalPane.getAlertOnMsgDelivery() );
                }
                if( dataSM.hasLanguageIndicator() )
                {
                    optionalPane.setLanguageIndicator(
                        dataSM.getLanguageIndicator() );
                }
                if( dataSM.hasItsReplyType() )
                {
                    optionalPane.setItsReplyType(
                        dataSM.getItsReplyType() );
                }
                if( dataSM.hasItsSessionInfo() )
                {
                    optionalPane.setItsSessionInfo(
                        dataSM.getItsSessionInfo() );
                }
                break;
            }
            case Data.DATA_SM_RESP:
            {
                DataSMResp dataSMResp = (DataSMResp)pdu;
                if( dataSMResp.hasDeliveryFailureReason() )
                {
                    optionalPane.setDeliveryFailureReason(
                        dataSMResp.getDeliveryFailureReason() );
                }
                if( dataSMResp.hasNetworkErrorCode() )
                {
                    ByteBuffer buf = dataSMResp.getNetworkErrorCode();
                    optionalPane.setNetworkErrorCode(
                        Byte.toString( buf.removeByte() ) + ", " +
                        Short.toString( buf.removeShort() ) );
                }
                if( dataSMResp.hasAdditionalStatusInfoText() )
                {
                    optionalPane.setAdditionalStatusInfoText(
                        dataSMResp.getAdditionalStatusInfoText() );
                }
                if( dataSMResp.hasDpfResult() )
                {
                    optionalPane.setDpfResult( dataSMResp.getDpfResult() );
                }
                break;
            }
            }
        }
        
        catch( Exception ex )
        {
            JOptionPane.showMessageDialog(
                this,
                "Caught an exception\n" + ex,
                "Exception",
                JOptionPane.ERROR_MESSAGE );
            System.err.println( ex );
            ex.printStackTrace();
        }
    }


    private static short getShortFromTLV( TLV tlv )
        throws ValueNotSetException, NotEnoughDataInByteBufferException,
        TerminatingZeroNotFoundException
    {
        ByteBuffer data = tlv.getData();
        data.removeShort();  // Skip the tag
        data.removeShort();  // Skip the length
        return data.removeShort();
    }


    private static String getOctetStringFromTLV( TLV tlv )
        throws ValueNotSetException, NotEnoughDataInByteBufferException,
        TerminatingZeroNotFoundException,
        UnsupportedEncodingException
    {
        ByteBuffer data = tlv.getData();
        data.removeShort();  // Skip the tag
        short len = data.removeShort();
        return data.removeString( len, Data.ENC_ASCII ); // TODO: encodings
    }


    void setPDUType( int type )
    {
        headerPane.setCommandId( type );
        optionalPane.setType( type );
        mandatoryPane.setType( type );

    }

    String getPDUType()
    {
        return headerPane.getCommandID().toString();
    }

    void incrementSeqNumber()
    {
        headerPane.incrementSeqNumber();
    }

}
