/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.batch;

import uk.org.youngman.smpp.test.SMPPEvent;
import org.smpp.Data;
import org.smpp.pdu.*;
import org.smpp.pdu.tlv.TLV;
import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;
import org.smpp.util.TerminatingZeroNotFoundException;

import java.io.*;
import java.util.Date;


public class BatchEventLog
{
    private Console console = System.console();
    private PrintWriter logWriter;


    BatchEventLog( File file )
    {
        try
        {
            logWriter = new PrintWriter( file );
        }
        catch( FileNotFoundException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    /**
     * Add an event to the Event Log
     * @param time Time of event
     * @param type Type of event
     * @param pdu  PDU associated with event (may be null)
     * @param message Message describing event details
     */
    void logEvent( Date time, SMPPEvent.EventType type, PDU pdu, String message )
    {
        StringBuilder line = new StringBuilder(
            SMPPEvent.TIMESTAMP_FORMAT.format( time ) ).
            append( " " ).append( message );
        logWriter.println( line );
        logWriter.flush();

        // Most IDEs don't support System.console().
        if( console == null )
        {
            System.out.println( line );
        }
        else
        {
            console.writer().println( line );
        }

        if( pdu != null )
        {
            logWriter.println( "    Sequence=" + pdu.getSequenceNumber() );
            logWriter.println( "    CommandStatus=" + pdu.getCommandStatus() );

            try
            {
                // Set mandatory values
                switch( pdu.getCommandId() )
                {
                case Data.BIND_RECEIVER:
                case Data.BIND_TRANSMITTER:
                case Data.BIND_TRANSCEIVER:
                    BindRequest request = (BindRequest)pdu;
                    logWriter.println( "    SystemID=" + request.getSystemId() );
                    logWriter.println( "    Password=" + request.getPassword() );
                    logWriter.println(
                        "    SystemType=" + request.getSystemType() );
                    logWriter.println( "    InterfaceVersion=" +
                                       request.getInterfaceVersion() );
                    if( request.getAddressRange().getAddressRange() != null )
                    {
                        logWriter.println(
                            "    AddrTON=" + request.getAddressRange().getTon() );
                        logWriter.println(
                            "    AddrNPI=" + request.getAddressRange().getNpi() );
                        logWriter.println(
                            "    AddressRange=" +
                            request.getAddressRange().getAddressRange() );
                    }
                    break;
                case Data.BIND_RECEIVER_RESP:
                case Data.BIND_TRANSMITTER_RESP:
                case Data.BIND_TRANSCEIVER_RESP:
                    BindResponse response = (BindResponse)pdu;
                    logWriter.println(
                        "    SystemID=" + response.getSystemId() );
                    break;
                case Data.OUTBIND:
                    Outbind outbind = (Outbind)pdu;
                    logWriter.println( "    SystemID=" + outbind.getSystemId() );
                    logWriter.println( "    Password=" + outbind.getPassword() );
                    break;
                case Data.UNBIND:
                case Data.UNBIND_RESP:
                case Data.GENERIC_NACK:
                    // No mandatory fields
                    break;
                case Data.QUERY_SM:
                    QuerySM querySM = (QuerySM)pdu;
                    logWriter.println(
                        "    MessageID=" + querySM.getMessageId() );
                    if( querySM.getSourceAddr().getAddress() != null )
                    {
                        logWriter.println(
                            "    SourceAddrTON=" + querySM.getSourceAddr().getTon() );
                        logWriter.println(
                            "    SourceAddrNPI=" + querySM.getSourceAddr().getNpi() );
                        logWriter.println(
                            "    SourceAddr=" +
                            querySM.getSourceAddr().getAddress() );
                    }
                    break;
                case Data.QUERY_SM_RESP:
                    QuerySMResp querySMResp = (QuerySMResp)pdu;
                    logWriter.println(
                        "    MessageID=" + querySMResp.getMessageId() );
                    logWriter.println(
                        "    FinalDate=" + querySMResp.getFinalDate() );
                    logWriter.println(
                        "    MessageState=" + querySMResp.getMessageState() );
                    logWriter.println(
                        "    ErrorCode=" + querySMResp.getErrorCode() );
                    break;
                case Data.SUBMIT_SM:
                    SubmitSM submitSM = (SubmitSM)pdu;
                    logWriter.println(
                        "    ServiceType=" + submitSM.getServiceType() );
                    if( submitSM.getSourceAddr().getAddress(  ) != null )
                    {
                        logWriter.println(
                            "    SourceAddrTON=" + submitSM.getSourceAddr().getTon() );
                        logWriter.println(
                            "    SourceAddrNPI=" + submitSM.getSourceAddr().getNpi() );
                        logWriter.println(
                            "    SourceAddr=" + submitSM.getSourceAddr().getAddress() );
                    }
                    if( submitSM.getDestAddr().getAddress() != null )
                    {
                        logWriter.println(
                            "    DestAddrTON=" + submitSM.getDestAddr().getTon() );
                        logWriter.println(
                            "    DestAddrNPI=" + submitSM.getDestAddr().getNpi() );
                        logWriter.println(
                            "    DestAddresses=" + submitSM.getDestAddr().getAddress() );
                    }
                    logWriter.println(
                        "    EsmClass=" + submitSM.getEsmClass() );
                    logWriter.println(
                        "    ProtocolID=" + submitSM.getProtocolId() );
                    logWriter.println(
                        "    PriorityFlag=" + submitSM.getPriorityFlag() );
                    logWriter.println( "    ScheduleDeliveryTime=" + submitSM.getScheduleDeliveryTime() );
                    logWriter.println( "    ValidityPeriod=" + submitSM.getValidityPeriod() );
                    logWriter.println( "    RegisteredDelivery=" + submitSM.getRegisteredDelivery() );
                    logWriter.println( "    ReplaceIfPresentFlag=" + submitSM.getReplaceIfPresentFlag() );
                    logWriter.println(
                        "    DataCoding=" + submitSM.getDataCoding() );
                    logWriter.println( "    SmDefaultMsgID=" + submitSM.getSmDefaultMsgId() );
                    logWriter.println(
                        "    ShortMessage=" + submitSM.getShortMessage( Data.ENC_UTF8) );
                    break;
                case Data.SUBMIT_SM_RESP:
                    SubmitSMResp submitSMResp = (SubmitSMResp)pdu;
                    logWriter.println(
                        "    MessageID=" + submitSMResp.getMessageId() );
                    break;
                case Data.SUBMIT_MULTI:
                    SubmitMultiSM submitMultiSM = (SubmitMultiSM)pdu;
                    logWriter.println( "    ServiceType=" + submitMultiSM.getServiceType() );
                    if( submitMultiSM.getSourceAddr().getAddress() != null )
                    {
                        logWriter.println(
                            "    SourceAddrTON=" + submitMultiSM.getSourceAddr().getTon() );
                        logWriter.println(
                            "    SourceAddrNPI=" + submitMultiSM.getSourceAddr().getNpi() );
                        logWriter.println(
                            "    SourceAddr=" + submitMultiSM.getSourceAddr().getAddress() );
                    }
                    for( int i=0; i<submitMultiSM.getNumberOfDests(); ++ i )
                    {
                        logWriter.println( "    DestAddrTON=" + submitMultiSM.getDestAddress( i ).getAddress().getTon() );
                        logWriter.println( "    DestAddrNPI=" + submitMultiSM.getDestAddress( i ).getAddress().getNpi() );
                        logWriter.println( "    DestAddresses=" + submitMultiSM.getDestAddress( i ).getAddress().getAddress() );
                    }
                    logWriter.println(
                        "    EsmClass=" + submitMultiSM.getEsmClass() );
                    logWriter.println(
                        "    ProtocolID=" + submitMultiSM.getProtocolId() );
                    logWriter.println( "    PriorityFlag=" + submitMultiSM.getPriorityFlag() );
                    logWriter.println(
                        "    ScheduleDeliveryTime=" + submitMultiSM
                        .getScheduleDeliveryTime() );
                    logWriter.println( "    ValidityPeriod=" + submitMultiSM.getValidityPeriod() );
                    logWriter.println( "    RegisteredDelivery=" + submitMultiSM.getRegisteredDelivery() );
                    logWriter.println(
                        "    ReplaceIfPresentFlag=" + submitMultiSM
                        .getReplaceIfPresentFlag() );
                    logWriter.println(
                        "    DataCoding=" + submitMultiSM.getDataCoding() );
                    logWriter.println( "    SmDefaultMsgID=" + submitMultiSM.getSmDefaultMsgId() );
                    logWriter.println( "    ShortMessage=" +
                                        submitMultiSM.getShortMessage( Data.ENC_UTF8 ) );
                    break;
                case Data.SUBMIT_MULTI_RESP:
                    SubmitMultiSMResp submitMultiSMResp = (SubmitMultiSMResp)pdu;
                    logWriter.println( "    MessageID=" + submitMultiSMResp.getMessageId() );
                    // TODO if we ever need it?
                    // submitMultiSMResp.addUnsuccessSME( ... );
                    throw new UnsupportedOperationException(
                        "SUBMIT_MULTI_RESP needs more work"  );
//                break;
                case Data.DELIVER_SM:
                    DeliverSM deliverSM = (DeliverSM)pdu;
                    logWriter.println(
                        "    ServiceType=" + deliverSM.getServiceType() );
                    if( deliverSM.getSourceAddr().getAddress() != null )
                    {
                        logWriter.println(
                            "    SourceAddrTON=" + deliverSM.getSourceAddr().getTon() );
                        logWriter.println(
                            "    SourceAddrNPI=" + deliverSM.getSourceAddr().getNpi() );
                        logWriter.println(
                            "    SourceAddr=" + deliverSM.getSourceAddr().getAddress() );
                    }
                    if( deliverSM.getDestAddr().getAddress() != null )
                    {
                        logWriter.println(
                            "    DestAddrTON=" + deliverSM.getDestAddr().getTon() );
                        logWriter.println(
                            "    DestAddrNPI=" + deliverSM.getDestAddr().getNpi() );
                        logWriter.println(
                            "    DestinationAddr=" + deliverSM.getDestAddr().getAddress() );
                    }
                    logWriter.println( "    EsmClass=" + deliverSM.getEsmClass() );
                    logWriter.println(
                        "    ProtocolID=" + deliverSM.getProtocolId() );
                    logWriter.println(
                        "    PriorityFlag=" + deliverSM.getPriorityFlag() );
                    logWriter.println( "    RegisteredDelivery=" +
                                       deliverSM.getRegisteredDelivery() );
                    logWriter.println(
                        "    DataCoding=" + deliverSM.getDataCoding() );
                    logWriter.println(
                        "    ShortMessage=" + deliverSM.getShortMessage( Data.ENC_UTF8 ) );
                    break;
                case Data.DELIVER_SM_RESP:
                    DeliverSMResp deliverSMResp = (DeliverSMResp)pdu;
                    logWriter.println( "    MessageID=" + deliverSMResp.getMessageId() );
                    break;
                case Data.REPLACE_SM:
                    ReplaceSM replaceSM = (ReplaceSM)pdu;
                    logWriter.println( "    MessageID=" + replaceSM.getMessageId() );
                    if( replaceSM.getSourceAddr().getAddress() != null )
                    {
                        logWriter.println(
                            "    SourceAddrTON=" + replaceSM.getSourceAddr().getTon() );
                        logWriter.println(
                            "    SourceAddrNPI=" + replaceSM.getSourceAddr().getNpi() );
                        logWriter.println(
                            "    SourceAddr=" + replaceSM.getSourceAddr().getAddress() );
                    }
                    logWriter.println( "    ScheduleDeliveryTime=" + replaceSM.getScheduleDeliveryTime() );
                    logWriter.println( "    ValidityPeriod=" + replaceSM.getValidityPeriod() );
                    logWriter.println( "    RegisteredDelivery=" + replaceSM.getRegisteredDelivery() );
                    logWriter.println( "    SmDefaultMsgID=" + replaceSM.getSmDefaultMsgId() );
                    logWriter.println( "    ShortMessage=" +
                                           replaceSM.getShortMessage( Data.ENC_UTF8 ) );
                    break;
                case Data.REPLACE_SM_RESP:
                    // No mandatory fields
                    break;
                case Data.CANCEL_SM:
                    CancelSM cancelSM = (CancelSM)pdu;
                    logWriter.println( "    ServiceType=" + cancelSM.getServiceType() );
                    logWriter.println( "    MessageID=" + cancelSM.getMessageId() );
                    if( cancelSM.getSourceAddr().getAddress() != null )
                    {
                        logWriter.println( "    SourceAddrTON=" + cancelSM.getSourceAddr().getTon() );
                        logWriter.println( "    SourceAddrNPI=" + cancelSM.getSourceAddr().getNpi() );
                        logWriter.println( "    SourceAddr=" + cancelSM.getSourceAddr().getAddress() );
                    }
                    if( cancelSM.getDestAddr().getAddress() != null )
                    {
                        logWriter.println( "    DestAddrTON=" + cancelSM.getDestAddr().getTon() );
                        logWriter.println( "    DestAddrNPI=" + cancelSM.getDestAddr().getNpi() );
                        logWriter.println( "    DestinationAddr=" + cancelSM.getDestAddr().getAddress() );
                    }
                    break;
                case Data.CANCEL_SM_RESP:
                case Data.ENQUIRE_LINK:
                case Data.ENQUIRE_LINK_RESP:
                    // No mandatory fields
                    break;
                case Data.ALERT_NOTIFICATION:
                    throw new UnsupportedOperationException(
                        "ALERT_NOTIFICATION needs more work"  );
//            AlertNotification alertNotification = (AlertNotification)pdu;
//            break;
                case Data.DATA_SM:
                    DataSM dataSM = (DataSM)pdu;
                    logWriter.println( "    ServiceType=" + dataSM.getServiceType() );
                    if( dataSM.getSourceAddr().getAddress() != null )
                    {
                        logWriter.println( "    SourceAddrTON=" + dataSM.getSourceAddr().getTon() );
                        logWriter.println( "    SourceAddrNPI=" + dataSM.getSourceAddr().getNpi() );
                        logWriter.println( "    SourceAddr=" + dataSM.getSourceAddr().getAddress() );
                    }
                    if( dataSM.getDestAddr().getAddress() != null )
                    {
                        logWriter.println( "    DestAddrTON=" + dataSM.getDestAddr().getTon() );
                        logWriter.println( "    DestAddrNPI=" + dataSM.getDestAddr().getNpi() );
                        logWriter.println( "    DestinationAddr=" + dataSM.getDestAddr().getAddress() );
                    }
                    logWriter.println( "    EsmClass=" + dataSM.getEsmClass() );
                    logWriter.println( "    RegisteredDelivery=" + dataSM.getRegisteredDelivery() );
                    logWriter.println( "    DataCoding=" + dataSM.getDataCoding() );
                    break;
                case Data.DATA_SM_RESP:
                    DataSMResp dataSMResp = (DataSMResp)pdu;
                    logWriter.println( "    MessageID=" + dataSMResp.getMessageId() );
                    break;
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
                    BindResponse response = (BindResponse)pdu;
                    if( response.hasScInterfaceVersion() )
                    {
                        logWriter.println( "    ScInterfaceVersion=" + response
                                           .getScInterfaceVersion() );
                    }
                    break;
                case Data.OUTBIND:
                case Data.UNBIND:
                case Data.UNBIND_RESP:
                case Data.GENERIC_NACK:
                case Data.QUERY_SM:
                case Data.QUERY_SM_RESP:
                    // No optional fields
                    break;
                case Data.SUBMIT_SM:
                    SubmitSM submitSM = (SubmitSM)pdu;
                    if( submitSM.hasUserMessageReference() )
                    {
                        logWriter.println( "    UserMessageReference=" + submitSM.getUserMessageReference() );
                    }
                    if( submitSM.hasSourcePort() )
                    {
                        logWriter.println( "    SourcePort=" + submitSM.getSourcePort() );
                    }
                    if( submitSM.hasSourceAddrSubunit() )
                    {
                        logWriter.println( "    SourceAddrSubunit=" + submitSM.getSourceAddrSubunit() );
                    }
                    if( submitSM.hasDestinationPort() )
                    {
                        logWriter.println( "    DestinationPort=" + submitSM.getDestinationPort() );
                    }
                    if( submitSM.hasDestAddrSubunit() )
                    {
                        logWriter.println( "    DestAddrSubunit=" + submitSM.getDestAddrSubunit() );
                    }
                    if( submitSM.hasSarMsgRefNum() )
                    {
                        logWriter.println( "    SarMsgRefNum=" + submitSM.getSarMsgRefNum() );
                    }
                    if( submitSM.hasSarTotalSegments() )
                    {
                        logWriter.println( "    SarTotalSegments=" + submitSM.getSarTotalSegments() );
                    }
                    if( submitSM.hasSarSegmentSeqnum() )
                    {
                        logWriter.println( "    SarSegmentSeqNum=" + submitSM.getSarSegmentSeqnum() );
                    }
                    if( submitSM.hasMoreMsgsToSend() )
                    {
                        logWriter.println( "    MoreMessagesToSend=" + submitSM.getMoreMsgsToSend() );
                    }
                    if( submitSM.hasPayloadType() )
                    {
                        logWriter.println( "    PayloadType=" + submitSM.getPayloadType() );
                    }
                    if( submitSM.hasMessagePayload() )
                    {
                        logWriter.println(
                            "    MessagePayload=" +
                            submitSM.getMessagePayload().removeString(
                                submitSM.getMessagePayload().length(), Data.ENC_UTF8 ) );
                    }
                    if( submitSM.hasPrivacyIndicator() )
                    {
                        logWriter.println( "    PrivacyIndicator=" + submitSM.getPrivacyIndicator() );
                    }
                    if( submitSM.hasCallbackNum() )
                    {
                        logWriter.println(
                            "    CallbackNum=" +
                            submitSM.callbackNum().removeString(
                                submitSM.callbackNum().length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitSM.hasCallbackNumPresInd() )
                    {
                        logWriter.println( "    CallbackNumPresInd=" + submitSM.getCallbackNumPresInd() );
                    }
                    if( submitSM.hasCallbackNumAtag() )
                    {
                        ByteBuffer buf = submitSM.getCallbackNumAtag();
                        byte encoding = buf.removeByte();
                        logWriter.println(
                            "    CallbackNumAtag: encoding=" + encoding +
                            ", tag=" + buf.removeString(
                                buf.length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitSM.hasSourceSubaddress() )
                    {
                        logWriter.println(
                            "    SourceSubaddress=" +
                            submitSM.getSourceSubaddress().removeString(
                                submitSM.getSourceSubaddress().length(),
                                Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitSM.hasDestSubaddress() )
                    {
                        logWriter.println(
                            "    DestSubaddress=" +
                            submitSM.getDestSubaddress().removeString(
                                submitSM.getDestSubaddress().length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitSM.hasUserResponseCode() )
                    {
                        logWriter.println( "    UserResponseCode=" + submitSM.getUserResponseCode() );
                    }
                    if( submitSM.hasDisplayTime() )
                    {
                        logWriter.println( "    DisplayTime=" + submitSM.getDisplayTime() );
                    }
                    if( submitSM.hasSmsSignal() )
                    {
                        logWriter.println( "    SmsSignal=" + submitSM.getSmsSignal() );
                    }
                    if( submitSM.hasMsValidity() )
                    {
                        logWriter.println( "    MsValidity=" + submitSM.getMsValidity() );
                    }
                    if( submitSM.hasMsMsgWaitFacilities() )
                    {
                        logWriter.println( "    MsMsgWaitFacilities=" + submitSM.getMsMsgWaitFacilities() );
                    }
                    if( submitSM.hasNumberOfMessages() )
                    {
                        logWriter.println( "    NumberOfMessages=" + submitSM.getNumberOfMessages() );
                    }
                    if( submitSM.hasAlertOnMsgDelivery() )
                    {
                        logWriter.println( "    AlertOnMsgDelivery=" + submitSM.getAlertOnMsgDelivery() );
                    }
                    if( submitSM.hasLanguageIndicator() )
                    {
                        logWriter.println( "    LanguageIndicator=" + submitSM.getLanguageIndicator() );
                    }
                    if( submitSM.hasItsReplyType() )
                    {
                        logWriter.println( "    ItsReplyType=" + submitSM.getItsReplyType() );
                    }
                    if( submitSM.hasItsSessionInfo() )
                    {
                        logWriter.println( "    ItsSessionInfo=" + submitSM.getItsSessionInfo() );
                    }
                    if( submitSM.hasUssdServiceOp() )
                    {
                        logWriter.println( "    UssdServiceOp=" + submitSM.getUssdServiceOp() );
                    }
                    break;
                case Data.SUBMIT_SM_RESP:
                    // No optional fields
                    break;
                case Data.SUBMIT_MULTI:
                    SubmitMultiSM submitMultiSM = (SubmitMultiSM)pdu;
                    if( submitMultiSM.hasUserMessageReference() )
                    {
                        logWriter.println( "    UserMessageReference=" + submitMultiSM.getUserMessageReference() );
                    }
                    if( submitMultiSM.hasSourcePort() )
                    {
                        logWriter.println( "    SourcePort=" + submitMultiSM.getSourcePort() );
                    }
                    if( submitMultiSM.hasSourceAddrSubunit() )
                    {
                        logWriter.println( "    SourceAddrSubunit=" + submitMultiSM.getSourceAddrSubunit() );
                    }
                    if( submitMultiSM.hasDestinationPort() )
                    {
                        logWriter.println( "    DestinationPort=" + submitMultiSM.getDestinationPort() );
                    }
                    if( submitMultiSM.hasDestAddrSubunit() )
                    {
                        logWriter.println( "    DestAddrSubunit=" + submitMultiSM.getDestAddrSubunit() );
                    }
                    if( submitMultiSM.hasSarMsgRefNum() )
                    {
                        logWriter.println( "    SarMsgRefNum=" + submitMultiSM.getSarMsgRefNum() );
                    }
                    if( submitMultiSM.hasSarTotalSegments() )
                    {
                        logWriter.println( "    SarTotalSegments=" + submitMultiSM.getSarTotalSegments() );
                    }
                    if( submitMultiSM.hasSarSegmentSeqnum() )
                    {
                        logWriter.println( "    SarSegmentSeqNum=" + submitMultiSM.getSarSegmentSeqnum() );
                    }
                    if( submitMultiSM.hasPayloadType() )
                    {
                        logWriter.println( "    PayloadType=" + submitMultiSM.getPayloadType() );
                    }
                    if( submitMultiSM.hasMessagePayload() )
                    {
                        logWriter.println(
                            submitMultiSM +
                            submitMultiSM.getMessagePayload().removeString(
                                submitMultiSM.getMessagePayload().length(),
                                Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitMultiSM.hasPrivacyIndicator() )
                    {
                        logWriter.println( "    PrivacyIndicator=" + submitMultiSM.getPrivacyIndicator() );
                    }
                    if( submitMultiSM.hasCallbackNum() )
                    {
                        logWriter.println(
                            "    CallbackNum=" +
                            submitMultiSM.callbackNum().removeString(
                                submitMultiSM.callbackNum().length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitMultiSM.hasCallbackNumPresInd() )
                    {
                        logWriter.println( "    CallbackNumPresInd=" + submitMultiSM.getCallbackNumPresInd() );
                    }
                    if( submitMultiSM.hasCallbackNumAtag() )
                    {
                        ByteBuffer buf = submitMultiSM.getCallbackNumAtag();
                        byte encoding = buf.removeByte();
                        logWriter.println(
                            "    CallbackNumAtag: encoding=" + encoding +
                            ", tag=" + buf.removeString(
                                buf.length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitMultiSM.hasSourceSubaddress() )
                    {
                        logWriter.println(
                            "    SourceSubaddress=" +
                            submitMultiSM.getSourceSubaddress().removeString(
                                submitMultiSM.getSourceSubaddress().length(),
                                Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitMultiSM.hasDestSubaddress() )
                    {
                        logWriter.println(
                            "    DestSubaddress=" +
                            submitMultiSM.getDestSubaddress().removeString(
                                submitMultiSM.getDestSubaddress().length(),
                                Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( submitMultiSM.hasDisplayTime() )
                    {
                        logWriter.println( "    DisplayTime=" + submitMultiSM.getDisplayTime() );
                    }
                    if( submitMultiSM.hasSmsSignal() )
                    {
                        logWriter.println( "    SmsSignal=" + submitMultiSM.getSmsSignal() );
                    }
                    if( submitMultiSM.hasMsValidity() )
                    {
                        logWriter.println( "    MsValidity=" + submitMultiSM.getMsValidity() );
                    }
                    if( submitMultiSM.hasMsMsgWaitFacilities() )
                    {
                        logWriter.println( "    MsMsgWaitFacilities=" + submitMultiSM.getMsMsgWaitFacilities() );
                    }
                    if( submitMultiSM.hasAlertOnMsgDelivery() )
                    {
                        logWriter.println( "    AlertOnMsgDelivery=" + submitMultiSM.getAlertOnMsgDelivery() );
                    }
                    if( submitMultiSM.hasLanguageIndicator() )
                    {
                        logWriter.println( "    LanguageIndicator=" + submitMultiSM.getLanguageIndicator() );
                    }
                    break;
                case Data.SUBMIT_MULTI_RESP:
                    // No optional fields
                    break;
                case Data.DELIVER_SM:
                    DeliverSM deliverSM = (DeliverSM)pdu;
                    if( deliverSM.hasUserMessageReference() )
                    {
                        logWriter.println( "    UserMessageReference=" + deliverSM.getUserMessageReference() );
                    }
                    if( deliverSM.hasSourcePort() )
                    {
                        logWriter.println( "    SourcePort=" + deliverSM.getSourcePort() );
                    }
                    if( deliverSM.hasDestinationPort() )
                    {
                        logWriter.println( "    DestinationPort=" + deliverSM.getDestinationPort() );
                    }
                    if( deliverSM.hasSarMsgRefNum() )
                    {
                        logWriter.println( "    SarMsgRefNum=" + deliverSM.getSarMsgRefNum() );
                    }
                    if( deliverSM.hasSarTotalSegments() )
                    {
                        logWriter.println( "    SarTotalSegments=" + deliverSM.getSarTotalSegments() );
                    }
                    if( deliverSM.hasSarSegmentSeqnum() )
                    {
                        logWriter.println( "    SarSegmentSeqNum=" + deliverSM.getSarSegmentSeqnum() );
                    }
                    if( deliverSM.hasUserResponseCode() )
                    {
                        logWriter.println( "    UserResponseCode=" + deliverSM.getUserResponseCode() );
                    }
                    if( deliverSM.hasPrivacyIndicator() )
                    {
                        logWriter.println( "    PrivacyIndicator=" + deliverSM.getPrivacyIndicator() );
                    }
                    if( deliverSM.hasPayloadType() )
                    {
                        logWriter.println( "    PayloadType=" + deliverSM.getPayloadType() );
                    }
                    if( deliverSM.hasMessagePayload() )
                    {
                        logWriter.println(
                            deliverSM +
                            deliverSM.getMessagePayload().removeString(
                                deliverSM.getMessagePayload().length(),
                                Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( deliverSM.hasCallbackNum() )
                    {
                        logWriter.println(
                            "    CallbackNum=" +
                            deliverSM.callbackNum().removeString(
                                deliverSM.callbackNum().length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( deliverSM.hasSourceSubaddress() )
                    {
                        logWriter.println(
                            "    SourceSubaddress=" +
                            deliverSM.getSourceSubaddress().removeString(
                                deliverSM.getSourceSubaddress().length(),
                                Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( deliverSM.hasDestSubaddress() )
                    {
                        logWriter.println(
                            "    DestSubaddress=" +
                            deliverSM.getDestSubaddress().removeString(
                                deliverSM.getDestSubaddress().length(),
                                Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( deliverSM.hasLanguageIndicator() )
                    {
                        logWriter.println( "    LanguageIndicator=" + deliverSM.getLanguageIndicator() );
                    }
                    if( deliverSM.hasItsSessionInfo() )
                    {
                        logWriter.println( "    ItsSessionInfo=" + deliverSM.getItsSessionInfo() );
                    }
                    if( deliverSM.hasNetworkErrorCode() )
                    {
                        ByteBuffer buf = deliverSM.getNetworkErrorCode();
                        logWriter.println(
                            "    NetworkErrorCode: type=" + buf.removeByte() + ", " +
                            "code=" + buf.removeShort() );
                    }
                    if( deliverSM.hasMessageState() )
                    {
                        logWriter.println( "    MessageState=" + deliverSM.getMessageState() );
                    }
                    if( deliverSM.hasReceiptedMessageId() )
                    {
                        logWriter.println( "    ReceiptedMessageID=" + deliverSM.getReceiptedMessageId() );
                    }
                    break;
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
//                    AlertNotification alertNotification = (AlertNotification)pdu;
//                    if( alertNotification.getMsAvailabilityStatus() != null )
//                    {
//                        throw new UnsupportedOperationException(
//                            "MS_AVAILABILITY_STATUS not implemented for ALERT_NOTIFICATION" );
//                    }
                    break;
                case Data.DATA_SM:
                {
                    DataSM dataSM = (DataSM)pdu;
                    if( dataSM.hasSourcePort() )
                    {
                        logWriter.println( "    SourcePort=" + dataSM.getSourcePort() );
                    }
                    if( dataSM.hasSourceAddrSubunit() )
                    {
                        logWriter.println( "    SourceAddrSubunit=" + dataSM.getSourceAddrSubunit() );
                    }
                    if( dataSM.hasSourceNetworkType() )
                    {
                        logWriter.println( "    SourceNetworkType=" + dataSM.getSourceNetworkType() );
                    }
                    if( dataSM.hasSourceBearerType() )
                    {
                        logWriter.println( "    SourceBearerType=" + dataSM.getSourceBearerType() );
                    }
                    if( dataSM.hasSourceTelematicsId() )
                    {
                        logWriter.println( "    SourceTelematicsID=" + dataSM.getSourceTelematicsId() );
                    }
                    if( dataSM.hasDestinationPort() )
                    {
                        logWriter.println( "    DestinationPort=" + dataSM.getDestinationPort() );
                    }
                    if( dataSM.hasDestAddrSubunit() )
                    {
                        logWriter.println( "    DestAddrSubunit=" + dataSM.getDestAddrSubunit() );
                    }
                    if( dataSM.hasDestNetworkType() )
                    {
                        logWriter.println( "    DestNetworkType=" + dataSM.getDestNetworkType() );
                    }
                    if( dataSM.hasDestBearerType() )
                    {
                        logWriter.println( "    DestBearerType=" + dataSM.getDestBearerType() );
                    }
                    if( dataSM.hasDestTelematicsId() )
                    {
                        logWriter.println( "    DestTelematicsID=" + dataSM.getDestTelematicsId() );
                    }
                    if( dataSM.hasSarMsgRefNum() )
                    {
                        logWriter.println( "    SarMsgRefNum=" + dataSM.getSarMsgRefNum() );
                    }
                    if( dataSM.hasSarTotalSegments() )
                    {
                        logWriter.println( "    SarTotalSegments=" + dataSM.getSarTotalSegments() );
                    }
                    if( dataSM.hasSarSegmentSeqnum() )
                    {
                        logWriter.println( "    SarSegmentSeqNum=" + dataSM.getSarSegmentSeqnum() );
                    }
                    if( dataSM.hasMoreMsgsToSend() )
                    {
                        logWriter.println( "    MoreMessagesToSend=" + dataSM.getMoreMsgsToSend() );
                    }
                    if( dataSM.hasQosTimeToLive() )
                    {
                        logWriter.println( "    QosTimeToLive=" + dataSM.getQosTimeToLive() );
                    }
                    if( dataSM.hasPayloadType() )
                    {
                        logWriter.println( "    PayloadType=" + dataSM.getPayloadType() );
                    }
                    if( dataSM.hasMessagePayload() )
                    {
                        logWriter.println(
                            dataSM +
                            dataSM.getMessagePayload().removeString(
                                dataSM.getMessagePayload().length(), Data.ENC_UTF8 ) );
                    }
                    if( dataSM.hasSetDpf() )
                    {
                        logWriter.println( "    SetDPF=" + dataSM.getSetDpf() );
                    }
                    if( dataSM.hasReceiptedMessageId() )
                    {
                        logWriter.println( "    ReceiptedMessageID=" + dataSM.getReceiptedMessageId() );
                    }
                    if( dataSM.hasMessageState() )
                    {
                        logWriter.println( "    MessageState=" + dataSM.getMessageState() );
                    }
                    if( dataSM.hasNetworkErrorCode() )
                    {
                        ByteBuffer buf = dataSM.getNetworkErrorCode();
                        logWriter.println(
                            "    NetworkErrorCode: type=" + buf.removeByte() + ", " +
                            "code=" + buf.removeShort() );
                    }
                    if( dataSM.hasUserMessageReference() )
                    {
                        logWriter.println( "    UserMessageReference=" + dataSM.getUserMessageReference() );
                    }
                    if( dataSM.hasPrivacyIndicator() )
                    {
                        logWriter.println( "    PrivacyIndicator=" + dataSM.getPrivacyIndicator() );
                    }
                    if( dataSM.hasCallbackNum() )
                    {
                        logWriter.println(
                            "    CallbackNum=" +
                            dataSM.callbackNum().removeString(
                                dataSM.callbackNum().length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( dataSM.hasCallbackNumPresInd() )
                    {
                        logWriter.println( "    CallbackNumPresInd=" + dataSM.getCallbackNumPresInd() );
                    }
                    if( dataSM.hasCallbackNumAtag() )
                    {
                        ByteBuffer buf = dataSM.getCallbackNumAtag();
                        byte encoding = buf.removeByte();
                        logWriter.println(
                            "    CallbackNumAtag: encoding=" + encoding +
                            ", tag=" + buf.removeString(
                                buf.length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( dataSM.hasSourceSubaddress() )
                    {
                        logWriter.println(
                            "    SourceSubaddress=" +
                            dataSM.getSourceSubaddress().removeString(
                                dataSM.getSourceSubaddress().length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( dataSM.hasDestSubaddress() )
                    {
                        logWriter.println(
                            "    DestSubaddress=" +
                            dataSM.getDestSubaddress().removeString(
                                dataSM.getDestSubaddress().length(), Data.ENC_ASCII ) ); // TODO: encodings
                    }
                    if( dataSM.hasUserResponseCode() )
                    {
                        logWriter.println( "    UserResponseCode=" + dataSM.getUserResponseCode() );
                    }
                    if( dataSM.hasDisplayTime() )
                    {
                        logWriter.println( "    DisplayTime=" + dataSM.getDisplayTime() );
                    }
                    if( dataSM.hasSmsSignal() )
                    {
                        logWriter.println( "    SmsSignal=" + dataSM.getSmsSignal() );
                    }
                    if( dataSM.hasMsValidity() )
                    {
                        logWriter.println( "    MsValidity=" + dataSM.getMsValidity() );
                    }
                    if( dataSM.hasMsMsgWaitFacilities() )
                    {
                        logWriter.println( "    MsMsgWaitFacilities=" + dataSM.getMsMsgWaitFacilities() );
                    }
                    if( dataSM.hasNumberOfMessages() )
                    {
                        logWriter.println( "    NumberOfMessages=" + dataSM.getNumberOfMessages() );
                    }
                    if( dataSM.getAlertOnMsgDelivery() )
                    {
                        logWriter.println( "Alert on messgae delivery is set" );
                    }
                    if( dataSM.hasLanguageIndicator() )
                    {
                        logWriter.println( "    LanguageIndicator=" + dataSM.getLanguageIndicator() );
                    }
                    if( dataSM.hasItsReplyType() )
                    {
                        logWriter.println( "    ItsReplyType=" + dataSM.getItsReplyType() );
                    }
                    if( dataSM.hasItsSessionInfo() )
                    {
                        logWriter.println( "    ItsSessionInfo=" + dataSM.getItsSessionInfo() );
                    }
                    break;
                }
                case Data.DATA_SM_RESP:
                {
                    DataSMResp dataSMResp = (DataSMResp)pdu;
                    if( dataSMResp.hasDeliveryFailureReason() )
                    {
                        logWriter.println( "    DeliveryFailureReason=" + dataSMResp.getDeliveryFailureReason() );
                    }
                    if( dataSMResp.hasNetworkErrorCode() )
                    {
                        ByteBuffer buf = dataSMResp.getNetworkErrorCode();
                        logWriter.println(
                            "    NetworkErrorCode: type=" + buf.removeByte() + ", " +
                            "code=" + buf.removeShort() );
                    }
                    if( dataSMResp.hasAdditionalStatusInfoText() )
                    {
                        logWriter.println( "    AdditionalStatusInfoText=" + dataSMResp.getAdditionalStatusInfoText() );
                    }
                    if( dataSMResp.hasDpfResult() )
                    {
                        logWriter.println( "    DpfResult=" + dataSMResp.getDpfResult() );
                    }
                    break;
                }
                }
            }
            catch( Exception ex )
            {
                logWriter.println( ex );
                ex.printStackTrace();
            }
        }
    }


    /**
     * Utility function to extract a 16 bit (Short) integer from a TLV field
     * @param tlv TLV containing required value
     * @return value extracted from TLV
     * @throws ValueNotSetException               No data in TLV
     * @throws NotEnoughDataInByteBufferException Length incorrect
     * @throws TerminatingZeroNotFoundException   Format error
     */
    private static short getShortFromTLV( TLV tlv )
        throws ValueNotSetException,
               NotEnoughDataInByteBufferException,
               TerminatingZeroNotFoundException
    {
        ByteBuffer data = tlv.getData();
        data.removeShort();  // Skip the tag
        data.removeShort();  // Skip the length
        return data.removeShort();
    }


    /**
     * Utility function to extract an octet string from a TLV field
     * @param tlv TLV containing required value
     * @return value extracted from TLV
     * @throws ValueNotSetException               No data in TLV
     * @throws NotEnoughDataInByteBufferException Length incorrect
     * @throws TerminatingZeroNotFoundException   Format error
     * @throws UnsupportedEncodingException       Requested an encoding that isn't available
     */
    private static String getOctetStringFromTLV( TLV tlv )
        throws ValueNotSetException,
               NotEnoughDataInByteBufferException,
               TerminatingZeroNotFoundException,
               UnsupportedEncodingException
    {
        ByteBuffer data = tlv.getData();
        data.removeShort();  // Skip the tag
        short len = data.removeShort();
        return data.removeString( len, Data.ENC_ASCII ); // TODO: encodings
    }
}
