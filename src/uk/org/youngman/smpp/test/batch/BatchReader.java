/**
 * Original Author: Neil Youngman
 * Released under the GNU General Public License version 2.0 or later.
 */

package uk.org.youngman.smpp.test.batch;


import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.smpp.Data;
import org.smpp.pdu.*;
import org.smpp.pdu.tlv.TLV;
import org.smpp.pdu.tlv.TLVException;
import org.smpp.pdu.tlv.TLVOctets;
import org.smpp.util.ByteBuffer;


public class BatchReader
{
    private File batchFile;
    private int lastSeq = 0;

    /**
     * Main constructor
     * @param batchFile File containing instructions for this batch run (XML)
     *
     */
    public BatchReader( File batchFile )
    {
        this.batchFile = batchFile;
    }

    /**
     * Reads an interprets the batch file
     * @return Batch data
     */
    public Batch read()
    {
        Batch batch = new Batch();
        try
        {
            Document doc = xml2dom( batchFile );
            Element root = doc.getRootElement();
            List<Element> elements = root.getChildren();

            /**
             * Process each element of the batch file and add a suitable
             * element to the list.
             */
            for( Element element : elements )
            {
                if( element.getName().equalsIgnoreCase( "PDU" ) )
                {
                    PDU pdu = readPDU( element );
                    if( pdu == null )
                    {
                        return null;
                    }
                    else
                    {
                        batch.addPDU( pdu );
                    }
                }
                else if( element.getName().equalsIgnoreCase( "Connect" ) )
                {
                    batch.addConnection( readConnection( element ) );
                }
                else if( element.getName().equalsIgnoreCase( "Disconnect" ) )
                {
                    batch.addDisconnection( new Batch.DisconnectEvent() );
                }
                else if( element.getName().equalsIgnoreCase( "Pause" ) )
                {
                    String millis = element.getAttributeValue( "millis" );
                    if( millis == null || millis.length() == 0 )
                    {
                        System.err.println( "No millis=n attribute in <Pause>" );
                        throw new IllegalArgumentException( "No millis=n attribute in <Pause>" );
                    }
                    batch.addPause( Long.parseLong( millis ) );
                }
                else if( element.getName().equalsIgnoreCase( "Settings" ) )
                {
                    String auto = element.getAttributeValue( "autoResponse" );
                    if( auto != null && auto.length() > 0 )
                    {
                        String[] pdus = auto.split( "," );
                        batch.addAutoResponseSettings( pdus, true );
                    }
                    String noAuto = element.getAttributeValue( "noAutoResponse" );
                    if( noAuto != null && noAuto.length() > 0 )
                    {
                        String[] pdus = noAuto.split( "," );
                        batch.addAutoResponseSettings( pdus, false );
                    }
                }
                else
                {
                    System.err.println(
                        "Expected <PDU>, <Connection> or <Pause>: Found <" +
                        element.getName() + ">");
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }

        return batch;
    }


    private PDU readPDU( Element element )
        throws WrongLengthOfStringException, WrongDateFormatException,
        IntegerOutOfRangeException, TLVException
    {
        PDU pdu;
        assert( element.getName().equalsIgnoreCase( "Pdu" ) );

        Batch.CommandID type;
        try
        {
            type = Batch.CommandID.valueOf( element.getAttributeValue( "type" ) );
        }
        catch( IllegalArgumentException ex )
        {
            System.err.println( "Did not recognise CommandId \"" +
                                element.getAttributeValue( "type" )+ "\"");
            return null;
        }

        pdu = PDU.createPDU( type.getId() );

        String sequenceNumber = element.getChildTextTrim( "SequenceNumber" );
        if( sequenceNumber != null )
        {
            pdu.setSequenceNumber( Integer.parseInt( sequenceNumber ) );
        }
        else
        {
            pdu.setSequenceNumber( ++lastSeq );
        }
        String commandStatus = element.getChildTextTrim( "CommandStatus" );
        if( commandStatus == null )
        {
            pdu.setCommandStatus( 0 );
        }
        else
        {
            pdu.setCommandStatus( Integer.parseInt( commandStatus ) );
        }

        try
        {
            // Set mandatory values
            switch( type )
            {
            case BIND_RECEIVER:
            case BIND_TRANSMITTER:
            case BIND_TRANSCEIVER:
            {
                BindRequest request = (BindRequest)pdu;
                request.setSystemId( element.getChildTextTrim( "SystemId" ) );
                request.setPassword( element.getChildTextTrim( "Password" ) );
                request.setSystemType( element.getChildTextTrim( "SystemType" ) );

                String interfaceVersion =
                    element.getChildTextTrim( "InterfaceVersion" );
                if( interfaceVersion != null )
                {
                    request.setInterfaceVersion(
                        Byte.parseByte( interfaceVersion ) );

                }
                String addressRange =
                    element.getChildTextTrim( "AddressRange" );
                if( addressRange  != null )
                {
                    String addrTON =
                        element.getChildTextTrim( "AddrTON" );
                    if( addrTON == null )
                    {
                        addrTON = "0";
                    }
                    String addrNPI =
                        element.getChildTextTrim( "AddrNPI" );
                    if( addrNPI == null )
                    {
                        addrNPI = "0";
                    }
                    request.setAddressRange( Byte.parseByte( addrTON ),
                                             Byte.parseByte( addrNPI ),
                                             addressRange );
                }
                break;
            }
            case BIND_RECEIVER_RESP:
            case BIND_TRANSMITTER_RESP:
            case BIND_TRANSCEIVER_RESP:
            {
                BindResponse response = (BindResponse)pdu;
                response.setSystemId( element.getChildTextTrim( "SystemId" ) );
                break;
            }
            case OUTBIND:
            {
                Outbind outbind = (Outbind)pdu;
                outbind.setSystemId( element.getChildTextTrim( "SystemId" ) );

                outbind.setPassword( element.getChildTextTrim( "Password" ) );

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
                String messageID = element.getChildTextTrim( "MessageId" );
                if( messageID != null )
                {
                    querySM.setMessageId( messageID );
                }
                String sourceAddr =
                    element.getChildTextTrim( "SourceAddr" );
                if( sourceAddr != null )
                {
                    String addrTON =
                        element.getChildTextTrim( "AddrTON" );
                    if( addrTON == null )
                    {
                        addrTON = "0";
                    }
                    String addrNPI =
                        element.getChildTextTrim( "AddrNPI" );
                    if( addrNPI == null )
                    {
                        addrNPI = "0";
                    }
                    querySM.setSourceAddr( Byte.parseByte( addrTON ),
                                           Byte.parseByte( addrNPI ),
                                           sourceAddr );
                }
                break;
            }
            case QUERY_SM_RESP:
            {
                QuerySMResp querySMResp = (QuerySMResp)pdu;
                String messageID = element.getChildTextTrim( "MessageId" );
                if( messageID != null )
                {
                    querySMResp.setMessageId( messageID );
                }
                String finalDate = element.getChildTextTrim( "FinalDate" );
                if( finalDate != null )
                {
                    querySMResp.setFinalDate( finalDate );
                }
                String messageState = element.getChildTextTrim( "MessageState" );
                if( messageState != null )
                {
                    querySMResp.setMessageState(
                        Byte.parseByte( messageState ) );
                }
                String errorCode = element.getChildTextTrim( "ErrorCode" );
                if( errorCode != null )
                {
                    querySMResp.setErrorCode( Byte.parseByte( errorCode ) );
                }
                break;
            }
            case SUBMIT_SM:
            {
                SubmitSM submitSM = (SubmitSM)pdu;
                String serviceType = element.getChildTextTrim( "ServiceType" );
                if( serviceType != null )
                {
                    submitSM.setServiceType( serviceType );
                }
                String sourceAddr =
                    element.getChildTextTrim( "SourceAddr" );
                if( sourceAddr != null )
                {
                    String sourceAddrTON =
                        element.getChildTextTrim( "SourceAddrTON" );
                    if( sourceAddrTON == null )
                    {
                        sourceAddrTON = "0";
                    }
                    String sourceAddrNPI =
                        element.getChildTextTrim( "SourceAddrNPI" );
                    if( sourceAddrNPI == null )
                    {
                        sourceAddrNPI = "0";
                    }
                    submitSM.setSourceAddr( Byte.parseByte( sourceAddrTON ),
                                            Byte.parseByte( sourceAddrNPI ),
                                            sourceAddr );
                }
                String destAddr =
                    element.getChildTextTrim( "DestinationAddr" );
                if( destAddr != null )
                {
                    String destAddrTON =
                        element.getChildTextTrim( "DestAddrTON" );
                    if( destAddrTON == null )
                    {
                        destAddrTON = "0";
                    }
                    String destAddrNPI =
                        element.getChildTextTrim( "DestAddrNPI" );
                    if( destAddrNPI == null )
                    {
                        destAddrNPI = "0";
                    }
                    submitSM.setDestAddr( Byte.parseByte( destAddrTON ),
                                          Byte.parseByte( destAddrNPI ),
                                          destAddr );
                }
                String esmClass = element.getChildTextTrim( "EsmClass" );
                if( esmClass != null )
                {
                    submitSM.setEsmClass( Byte.parseByte( esmClass ) );
                }
                String protocolID = element.getChildTextTrim( "ProtocolId" );
                if( protocolID != null )
                {
                    submitSM.setProtocolId( Byte.parseByte( protocolID ) );
                }
                String priorityFlag = element.getChildTextTrim( "PriorityFlag" );
                if( priorityFlag != null )
                {
                    submitSM.setPriorityFlag( Byte.parseByte( priorityFlag ) );
                }
                String scheduleDeliveryTime = element.getChildTextTrim( "ScheduleDeliveryTime" );
                if( scheduleDeliveryTime != null )
                {
                    submitSM.setScheduleDeliveryTime( scheduleDeliveryTime );
                }
                String validityPeriod = element.getChildTextTrim( "ValidityPeriod" );
                if( validityPeriod != null )
                {
                    submitSM.setValidityPeriod( validityPeriod );
                }
                String registeredDelivery = element.getChildTextTrim( "RegisteredDelivery" );
                if( registeredDelivery != null )
                {
                    submitSM.setRegisteredDelivery(
                        Byte.parseByte( registeredDelivery ) );
                }
                String replaceIfPresentFlag = element.getChildTextTrim( "ReplaceIfPresentFlag" );
                if( replaceIfPresentFlag != null )
                {
                    submitSM.setReplaceIfPresentFlag(
                        Byte.parseByte( replaceIfPresentFlag ) );
                }
                String dataCoding = element.getChildTextTrim( "DataCoding" );
                if( dataCoding != null )
                {
                    submitSM.setDataCoding( Byte.parseByte( dataCoding ) );
                }
                String smDefaultMsgID = element.getChildTextTrim( "SmDefaultMsgId" );
                if( smDefaultMsgID != null )
                {
                    submitSM.setSmDefaultMsgId(
                        Byte.parseByte( smDefaultMsgID ) );
                }
                Element shortMessage = element.getChild( "ShortMessage" );
                if( shortMessage != null )
                {
                    Element userDataHeader = shortMessage.getChild( "UserDataHeader" );
                    short udhSeqRef = 0;
                    short udhSeqIndex = 0;
                    short udhSeqTotal =0 ;
                    if( userDataHeader != null &&
                        userDataHeader.getAttributeValue( "index" ) != null &&
                        userDataHeader.getAttributeValue( "total" ) != null )
                                {
                        if( userDataHeader.getAttributeValue( "ref" ) != null )
                        {
                            udhSeqRef = Short.parseShort(
                                userDataHeader.getAttributeValue( "ref" ), 10 );
                        }
                        udhSeqIndex = Short.parseShort(
                            userDataHeader.getAttributeValue( "index" ), 10 );
                        udhSeqTotal = Short.parseShort(
                            userDataHeader.getAttributeValue( "total" ), 10 );
                    }
                    StringBuilder text = new StringBuilder();
                    for( Object line: shortMessage.getChildren( "line" ) )
                    {
                        text.append( ((Element)line).getText() ).append( "\n" );
                    }
                    if( udhSeqIndex != 0 &&
                        udhSeqTotal != 0 &&
                        udhSeqIndex <= udhSeqTotal  )
                    {
                        ByteBuffer buffer = new ByteBuffer();
                        buffer.appendByte( (byte)5 );
                        buffer.appendByte( (byte)0 );
                        buffer.appendByte( (byte)3 );
                        buffer.appendByte( (byte)udhSeqRef );
                        buffer.appendByte( (byte)udhSeqTotal );
                        buffer.appendByte( (byte)udhSeqIndex );
                        buffer.appendString( text.toString(), Data.ENC_UTF8 );

                        submitSM.setShortMessageData( buffer );
                    }
                    else
                    {
                        submitSM.setShortMessage( text.toString(), Data.ENC_UTF8 );
                    }
                }
                break;
            }
            case SUBMIT_SM_RESP:
            {
                SubmitSMResp submitSMResp = (SubmitSMResp)pdu;
                String messageID = element.getChildTextTrim( "MessageId" );
                if( messageID != null )
                {
                    submitSMResp.setMessageId( messageID );
                }
                break;
            }
            case SUBMIT_MULTI:
            {
                SubmitMultiSM submitMultiSM = (SubmitMultiSM)pdu;
                String serviceType = element.getChildTextTrim( "ServiceType" );
                if( serviceType != null )
                {
                    submitMultiSM.setServiceType( serviceType );
                }
                String sourceAddr =
                    element.getChildTextTrim( "SourceAddr" );
                if( sourceAddr != null )
                {
                    String addrTON =
                        element.getChildTextTrim( "AddrTON" );
                    if( addrTON == null )
                    {
                        addrTON = "0";
                    }
                    String addrNPI =
                        element.getChildTextTrim( "AddrNPI" );
                    if( addrNPI == null )
                    {
                        addrNPI = "0";
                    }
                    submitMultiSM.setSourceAddr( Byte.parseByte( addrTON ),
                                                 Byte.parseByte( addrNPI ),
                                                 sourceAddr );
                }
                Element destinationAddresses =
                    element.getChild( "DestinationAddresses" );
                for( Object destination :
                        destinationAddresses.getChildren( "Destination" ) )
                {
                    String destAddr =
                        ((Element)destination).getChildTextTrim( "DestAddr" );
                    if( destAddr != null )
                    {
                        String destAddrTON =
                            element.getChildTextTrim( "DestAddrTON" );
                        if( destAddrTON == null )
                        {
                            destAddrTON = "0";
                        }
                        String destAddrNPI =
                            element.getChildTextTrim( "DestAddrNPI" );
                        if( destAddrNPI == null )
                        {
                            destAddrNPI = "0";
                        }
                        DestinationAddress dest = new DestinationAddress(
                            Byte.parseByte( destAddrTON ),
                            Byte.parseByte( destAddrNPI ),
                            destAddr );
                        submitMultiSM.addDestAddress( dest );
                    }
                }
                String esmClass = element.getChildTextTrim( "EsmClass" );
                if( esmClass != null )
                {
                    submitMultiSM.setEsmClass( Byte.parseByte( esmClass ) );
                }
                String protocolID = element.getChildTextTrim( "ProtocolId" );
                if( protocolID != null )
                {
                    submitMultiSM.setProtocolId( Byte.parseByte( protocolID ) );
                }
                String priorityFlag = element.getChildTextTrim( "PriorityFlag" );
                if( priorityFlag != null )
                {
                    submitMultiSM.setPriorityFlag(
                        Byte.parseByte( priorityFlag ) );
                }
                String scheduleDeliveryTime = element.getChildTextTrim( "ScheduleDeliveryTime" );
                if( scheduleDeliveryTime != null )
                {
                    submitMultiSM.setScheduleDeliveryTime( scheduleDeliveryTime );
                }
                String validityPeriod = element.getChildTextTrim( "ValidityPeriod" );
                if( validityPeriod != null )
                {
                    submitMultiSM.setValidityPeriod( validityPeriod );
                }
                String registeredDelivery = element.getChildTextTrim( "RegisteredDelivery" );
                if( registeredDelivery != null )
                {
                    submitMultiSM.setRegisteredDelivery(
                        Byte.parseByte( registeredDelivery ) );
                }
                String replaceIfPresentFlag = element.getChildTextTrim( "ReplaceIfPresentFlag" );
                if( replaceIfPresentFlag != null )
                {
                    submitMultiSM.setReplaceIfPresentFlag(
                        Byte.parseByte( replaceIfPresentFlag ) );
                }
                String dataCoding = element.getChildTextTrim( "DataCoding" );
                if( dataCoding != null )
                {
                    submitMultiSM.setDataCoding( Byte.parseByte( dataCoding ) );
                }
                String smDefaultMsgID = element.getChildTextTrim( "SmDefaultMsgId" );
                if( smDefaultMsgID != null )
                {
                    submitMultiSM.setSmDefaultMsgId(
                        Byte.parseByte( smDefaultMsgID ) );
                }
                Element shortMessage = element.getChild( "ShortMessage" );
                if( shortMessage != null )
                {
                    StringBuilder text = new StringBuilder();
                    for( Object line: shortMessage.getChildren( "line" ) )
                    {
                        text.append( ((Element)line).getText() ).append( "\n" );
                    }
                    submitMultiSM.setShortMessage( text.toString(), Data.ENC_UTF8 );
                }
                break;
            }
            case SUBMIT_MULTI_RESP:
            {
                SubmitMultiSMResp submitMultiSMResp = (SubmitMultiSMResp)pdu;
                String messageID = element.getChildTextTrim( "MessageId" );
                if( messageID != null )
                {
                    submitMultiSMResp.setMessageId( messageID );
                }
                // TODO if we ever need it?
                // submitMultiSMResp.addUnsuccessSME( ... );
                throw new UnsupportedOperationException(
                    "SUBMIT_MULTI_RESP needs more work"  );
//                break;
            }
            case DELIVER_SM:
            {
                DeliverSM deliverSM = (DeliverSM)pdu;
                String serviceType = element.getChildTextTrim( "ServiceType" );
                if( serviceType != null )
                {
                    deliverSM.setServiceType( serviceType );
                }
                String sourceAddr =
                    element.getChildTextTrim( "SourceAddr" );
                if( sourceAddr != null )
                {
                    String addrTON =
                        element.getChildTextTrim( "AddrTON" );
                    if( addrTON == null )
                    {
                        addrTON = "0";
                    }
                    String addrNPI =
                        element.getChildTextTrim( "AddrNPI" );
                    if( addrNPI == null )
                    {
                        addrNPI = "0";
                    }
                    deliverSM.setSourceAddr( Byte.parseByte( addrTON ),
                                             Byte.parseByte( addrNPI ),
                                             sourceAddr );
                }
                String destAddr =
                    element.getChildTextTrim( "DestinationAddr" );
                if( destAddr != null )
                {
                    String destAddrTON =
                        element.getChildTextTrim( "DestAddrTON" );
                    if( destAddrTON == null )
                    {
                        destAddrTON = "0";
                    }
                    String destAddrNPI =
                        element.getChildTextTrim( "DestAddrNPI" );
                    if( destAddrNPI == null )
                    {
                        destAddrNPI = "0";
                    }
                    deliverSM.setDestAddr( Byte.parseByte( destAddrTON ),
                                           Byte.parseByte( destAddrNPI ),
                                           destAddr );
                }
                String esmClass = element.getChildTextTrim( "EsmClass" );
                if( esmClass != null )
                {
                    deliverSM.setEsmClass( Byte.parseByte( esmClass ) );
                }
                String protocolID = element.getChildTextTrim( "ProtocolId" );
                if( protocolID != null )
                {
                    deliverSM.setProtocolId( Byte.parseByte( protocolID ) );
                }
                String priorityFlag = element.getChildTextTrim( "PriorityFlag" );
                if( priorityFlag != null )
                {
                    deliverSM.setPriorityFlag( Byte.parseByte( priorityFlag ) );
                }
                String registeredDelivery = element.getChildTextTrim( "RegisteredDelivery" );
                if( registeredDelivery != null )
                {
                    deliverSM.setRegisteredDelivery(
                        Byte.parseByte( registeredDelivery ) );
                }
                String dataCoding = element.getChildTextTrim( "DataCoding" );
                if( dataCoding != null )
                {
                    deliverSM.setDataCoding( Byte.parseByte( dataCoding ) );
                }
                Element shortMessage = element.getChild( "ShortMessage" );
                if( shortMessage != null )
                {
                    StringBuilder text = new StringBuilder();
                    for( Object line: shortMessage.getChildren( "line" ) )
                    {
                        text.append( ((Element)line).getText() ).append( "\n" );
                    }
                    deliverSM.setShortMessage( text.toString(), Data.ENC_UTF8 );
                }
                break;
            }
            case DELIVER_SM_RESP:
            {
                DeliverSMResp deliverSMResp = (DeliverSMResp)pdu;
                String messageID = element.getChildTextTrim( "MessageId" );
                if( messageID != null )
                {
                    deliverSMResp.setMessageId( messageID );
                }
                break;
            }
            case REPLACE_SM:
            {
                ReplaceSM replaceSM = (ReplaceSM)pdu;
                String messageID = element.getChildTextTrim( "MessageId" );
                if( messageID != null )
                {
                    replaceSM.setMessageId( messageID );
                }
                String sourceAddr =
                    element.getChildTextTrim( "SourceAddr" );
                if( sourceAddr != null )
                {
                    String addrTON =
                        element.getChildTextTrim( "AddrTON" );
                    if( addrTON == null )
                    {
                        addrTON = "0";
                    }
                    String addrNPI =
                        element.getChildTextTrim( "AddrNPI" );
                    if( addrNPI == null )
                    {
                        addrNPI = "0";
                    }
                    replaceSM.setSourceAddr( Byte.parseByte( addrTON ),
                                             Byte.parseByte( addrNPI ),
                                             sourceAddr );
                }
                String scheduleDeliveryTime = element.getChildTextTrim( "ScheduleDeliveryTime" );
                if( scheduleDeliveryTime != null )
                {
                    replaceSM.setScheduleDeliveryTime( scheduleDeliveryTime );
                }
                String validityPeriod = element.getChildTextTrim( "ValidityPeriod" );
                if( validityPeriod != null )
                {
                    replaceSM.setValidityPeriod( validityPeriod );
                }
                String registeredDelivery = element.getChildTextTrim( "RegisteredDelivery" );
                if( registeredDelivery != null )
                {
                    replaceSM.setRegisteredDelivery(
                        Byte.parseByte( registeredDelivery ) );
                }
                String smDefaultMsgID = element.getChildTextTrim( "SmDefaultMsgId" );
                if( smDefaultMsgID != null )
                {
                    replaceSM.setSmDefaultMsgId(
                        Byte.parseByte( smDefaultMsgID ) );
                }
                Element shortMessage = element.getChild( "ShortMessage" );
                if( shortMessage != null )
                {
                    StringBuilder text = new StringBuilder();
                    for( Object line: shortMessage.getChildren( "line" ) )
                    {
                        text.append( ((Element)line).getText() ).append( "\n" );
                    }
                    replaceSM.setShortMessage( text.toString(), Data.ENC_UTF8 );
                }
                break;
            }
            case REPLACE_SM_RESP:
                // No mandatory fields
                break;
            case CANCEL_SM:
            {
                CancelSM cancelSM = (CancelSM)pdu;
                String serviceType = element.getChildTextTrim( "ServiceType" );
                if( serviceType != null )
                {
                    cancelSM.setServiceType( serviceType );
                }
                String messageID = element.getChildTextTrim( "MessageId" );
                if( messageID != null )
                {
                    cancelSM.setMessageId( messageID );
                }
                String sourceAddr =
                    element.getChildTextTrim( "SourceAddr" );
                if( sourceAddr != null )
                {
                    String addrTON =
                        element.getChildTextTrim( "AddrTON" );
                    if( addrTON == null )
                    {
                        addrTON = "0";
                    }
                    String addrNPI =
                        element.getChildTextTrim( "AddrNPI" );
                    if( addrNPI == null )
                    {
                        addrNPI = "0";
                    }
                    cancelSM.setSourceAddr( Byte.parseByte( addrTON ),
                                            Byte.parseByte( addrNPI ),
                                            sourceAddr );
                }
                String destAddr =
                    element.getChildTextTrim( "DestinationAddr" );
                if( destAddr != null )
                {
                    String destAddrTON =
                        element.getChildTextTrim( "DestAddrTON" );
                    if( destAddrTON == null )
                    {
                        destAddrTON = "0";
                    }
                    String destAddrNPI =
                        element.getChildTextTrim( "DestAddrNPI" );
                    if( destAddrNPI == null )
                    {
                        destAddrNPI = "0";
                    }
                    cancelSM.setDestAddr( Byte.parseByte( destAddrTON ),
                                          Byte.parseByte( destAddrNPI ),
                                          destAddr );
                }
                break;
            }
            case CANCEL_SM_RESP:
            case ENQUIRE_LINK:
            case ENQUIRE_LINK_RESP:
                // No mandatory fields
                break;
            case ALERT_NOTIFICATION:
                throw new UnsupportedOperationException(
                    "ALERT_NOTIFICATION needs more work"  );
//            AlertNotification alertNotification = (AlertNotification)pdu;
//            alertNotification.setSourceAddr( sourceAddrTON,
//                                             sourceAddrNPI,
//                                             sourceAddr );
//            alertNotification.setEsmeAddr( esmeAddrTON,
//                                           EsmeAddrNPI,
//                                           EsmeAddr );
//            break;
            case DATA_SM:
            {
                DataSM dataSM = (DataSM)pdu;
                String serviceType = element.getChildTextTrim( "ServiceType" );
                if( serviceType != null )
                {
                    dataSM.setServiceType( serviceType );
                }
                String sourceAddr =
                    element.getChildTextTrim( "SourceAddr" );
                if( sourceAddr != null )
                {
                    String addrTON =
                        element.getChildTextTrim( "AddrTON" );
                    if( addrTON == null )
                    {
                        addrTON = "0";
                    }
                    String addrNPI =
                        element.getChildTextTrim( "AddrNPI" );
                    if( addrNPI == null )
                    {
                        addrNPI = "0";
                    }
                    dataSM.setSourceAddr( Byte.parseByte( addrTON ),
                                          Byte.parseByte( addrNPI ),
                                          sourceAddr );
                }
                String destAddr =
                    element.getChildTextTrim( "DestinationAddr" );
                if( destAddr != null )
                {
                    String destAddrTON =
                        element.getChildTextTrim( "DestAddrTON" );
                    if( destAddrTON == null )
                    {
                        destAddrTON = "0";
                    }
                    String destAddrNPI =
                        element.getChildTextTrim( "DestAddrNPI" );
                    if( destAddrNPI == null )
                    {
                        destAddrNPI = "0";
                    }
                    dataSM.setDestAddr( Byte.parseByte( destAddrTON ),
                                        Byte.parseByte( destAddrNPI ),
                                        destAddr );
                }
                String esmClass = element.getChildTextTrim( "EsmClass" );
                if( esmClass != null )
                {
                    dataSM.setEsmClass( Byte.parseByte( esmClass ) );
                }
                String registeredDelivery = element.getChildTextTrim( "RegisteredDelivery" );
                if( registeredDelivery != null )
                {
                    dataSM.setRegisteredDelivery(
                        Byte.parseByte( registeredDelivery ) );
                }
                String dataCoding = element.getChildTextTrim( "DataCoding" );
                if( dataCoding != null )
                {
                    dataSM.setDataCoding( Byte.parseByte( dataCoding ) );
                }
                break;
            }
            case DATA_SM_RESP:
            {
                DataSMResp dataSMResp = (DataSMResp)pdu;
                String messageID = element.getChildTextTrim( "MessageId" );
                if( messageID != null )
                {
                    dataSMResp.setMessageId( messageID );
                }
                break;
            }
            }

            // Set optional values
            switch( type )
            {
            case BIND_RECEIVER:
            case BIND_TRANSMITTER:
            case BIND_TRANSCEIVER:
                // No optional fields
                break;
            case BIND_RECEIVER_RESP:
            case BIND_TRANSMITTER_RESP:
            case BIND_TRANSCEIVER_RESP:
            {
                String scInterfaceVersion = element.getChildTextTrim( "ScInterfaceVersion" );
                if( scInterfaceVersion != null )
                {
                    BindResponse response = (BindResponse)pdu;
                    response.setScInterfaceVersion(
                            Byte.parseByte( scInterfaceVersion ) );

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
                String userMessageReference = element.getChildTextTrim( "UserMessageReference" );
                if( userMessageReference != null )
                {
                    submitSM.setUserMessageReference(
                        Short.parseShort( userMessageReference ) );
                }
                String sourcePort = element.getChildTextTrim( "SourcePort" );
                if( sourcePort != null )
                {
                    submitSM.setSourcePort( Short.parseShort( sourcePort ) );
                }
                String sourceAddrSubunit = element.getChildTextTrim( "SourceAddrSubunit" );
                if( sourceAddrSubunit != null )
                {
                    submitSM.setSourceAddrSubunit(
                        Byte.parseByte( sourceAddrSubunit ) );
                }
                String destinationPort = element.getChildTextTrim( "DestinationPort" );
                if( destinationPort != null )
                {
                    submitSM.setDestinationPort(
                        Short.parseShort( destinationPort ) );
                }
                String destAddrSubunit = element.getChildTextTrim( "DestAddrSubunit" );
                if( destAddrSubunit != null )
                {
                    submitSM.setDestAddrSubunit(
                        Byte.parseByte( destAddrSubunit ) );
                }
                String sarMsgRefNum = element.getChildTextTrim( "SarMsgRefNum" );
                if( sarMsgRefNum != null )
                {
                    submitSM.setSarMsgRefNum( Short.parseShort( sarMsgRefNum ) );
                }
                String sarTotalSegments = element.getChildTextTrim( "SarTotalSegments" );
                if( sarTotalSegments != null )
                {
                    submitSM.setSarTotalSegments(
                        Short.parseShort( sarTotalSegments ) );
                }
                String sarSegmentSeqNum = element.getChildTextTrim( "SarSegmentSeqnum" );
                if( sarSegmentSeqNum != null )
                {
                    submitSM.setSarSegmentSeqnum(
                        Short.parseShort( sarSegmentSeqNum ) );
                }
                String moreMessagesToSend = element.getChildTextTrim( "MoreMsgsToSend" );
                if( moreMessagesToSend != null )
                {
                    submitSM.setMoreMsgsToSend(
                        Byte.parseByte( moreMessagesToSend ) );
                }
                String payloadType = element.getChildTextTrim( "PayloadType" );
                if( payloadType != null )
                {
                    submitSM.setPayloadType( Byte.parseByte( payloadType ) );
                }
                Element messagePayload = element.getChild( "MessagePayload" );
                if( messagePayload != null )
                {
                    StringBuilder text = new StringBuilder();
                    for( Object line: messagePayload.getChildren( "line" ) )
                    {
                        text.append( ((Element)line).getText() ).append( "\n" );
                    }
                    ByteBuffer buffer = new ByteBuffer();
                    buffer.appendString( text.toString(), Data.ENC_UTF8 );
                    submitSM.setMessagePayload( buffer );
                }
                String privacyIndicator = element.getChildTextTrim( "PrivacyIndicator" );
                if( privacyIndicator != null )
                {
                    submitSM.setPrivacyIndicator(
                        Byte.parseByte( privacyIndicator ) );
                }
                Element callbackNum = element.getChild( "CallBackNum" );
                if( callbackNum != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.36
                    throw new UnsupportedOperationException(
                        "<CallBackNum> not implemented" );
                }
                String callbackNumPresInd = element.getChildTextTrim( "CallbackNumPresInd" );
                if( callbackNumPresInd != null )
                {
                    submitSM.setCallbackNumPresInd(
                        Byte.parseByte( callbackNumPresInd ) );
                }
                Element callbackNumATag = element.getChild( "CallBackNumATag" );
                if( callbackNumATag != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.38
                    throw new UnsupportedOperationException(
                        "<CallBackNumATag> not implemented" );
                }
                Element sourceSubaddress = element.getChild( "SourceSubaddress" );
                if( sourceSubaddress != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.15
                    throw new UnsupportedOperationException(
                        "<SourceSubaddress> not implemented" );
                }
                Element destSubaddress = element.getChild( "DestSubaddress" );
                if( destSubaddress != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.16
                    throw new UnsupportedOperationException(
                        "<DestSubaddress> not implemented" );
                }
                String userResponseCode = element.getChildTextTrim( "UserResponseCode" );
                if( userResponseCode != null )
                {
                    submitSM.setUserResponseCode(
                        Byte.parseByte( userResponseCode ) );
                }
                String displayTime = element.getChildTextTrim( "DisplayTime" );
                if( displayTime != null )
                {
                    submitSM.setDisplayTime( Byte.parseByte( displayTime ) );
                }
                String smsSignal = element.getChildTextTrim( "SmsSignal" );
                if( smsSignal != null )
                {
                    submitSM.setSmsSignal( Short.parseShort( smsSignal ) );
                }
                String msValidity = element.getChildTextTrim( "MsValidity" );
                if( msValidity != null )
                {
                    submitSM.setMsValidity( Byte.parseByte( msValidity ) );
                }
                String msMsgWaitFacilities = element.getChildTextTrim( "MsMsgWaitFacilities" );
                if( msMsgWaitFacilities != null )
                {
                    submitSM.setMsMsgWaitFacilities(
                        Byte.parseByte( msMsgWaitFacilities ) );
                }
                String numberOfMessages = element.getChildTextTrim( "NumberOfMessages" );
                if( numberOfMessages != null )
                {
                    submitSM.setNumberOfMessages(
                        Byte.parseByte( numberOfMessages ) );
                }
                String alertOnMsgDelivery =
                    element.getChildTextTrim( "AlertOnMsgDelivery" );
                if( alertOnMsgDelivery != null )
                {
                    submitSM.setAlertOnMsgDelivery(
                        alertOnMsgDelivery.toUpperCase().startsWith( "Y" ) );
                }
                String languageIndicator = element.getChildTextTrim( "LanguageIndicator" );
                if( languageIndicator != null )
                {
                    submitSM.setLanguageIndicator(
                        Byte.parseByte( languageIndicator ) );
                }
                String itsReplyType = element.getChildTextTrim( "ItsReplyType" );
                if( itsReplyType != null )
                {
                    submitSM.setItsReplyType( Byte.parseByte( itsReplyType ) );
                }
                String itsSessionInfo = element.getChildTextTrim( "ItsSessionInfo" );
                if( itsSessionInfo != null )
                {
                    submitSM.setItsSessionInfo(
                        Short.parseShort( itsSessionInfo ) );
                }
                String ussdServiceOp = element.getChildTextTrim( "UssdServiceOp" );
                if( ussdServiceOp != null )
                {
                    submitSM.setUssdServiceOp( Byte.parseByte( ussdServiceOp ) );
                }
                break;
            }
            case SUBMIT_SM_RESP:
                // No optional fields
                break;
            case SUBMIT_MULTI:
            {
                SubmitMultiSM submitMultiSM = (SubmitMultiSM)pdu;
                String userMessageReference =
                    element.getChildTextTrim( "UserMessageReference" );
                    if( userMessageReference != null )
                    {
                        submitMultiSM.setUserMessageReference(
                            Short.parseShort( userMessageReference ) );
                    }
                String sourcePort = element.getChildTextTrim( "SourcePort" );
                if( sourcePort != null )
                {
                    submitMultiSM.setSourcePort(
                        Short.parseShort( sourcePort ) );
                }
                String sourceAddrSubunit = element.getChildTextTrim( "SourceAddrSubunit" );
                if( sourceAddrSubunit != null )
                {
                    submitMultiSM.setSourceAddrSubunit(
                        Byte.parseByte( sourceAddrSubunit ) );
                }
                String destinationPort = element.getChildTextTrim( "DestinationPort" );
                if( destinationPort != null )
                {
                    submitMultiSM.setDestinationPort(
                        Short.parseShort( destinationPort ) );
                }
                String destAddrSubunit = element.getChildTextTrim( "DestAddrSubunit" );
                if( destAddrSubunit != null )
                {
                    submitMultiSM.setDestAddrSubunit(
                        Byte.parseByte( destAddrSubunit ) );
                }
                String sarMsgRefNum = element.getChildTextTrim( "SarMsgRefNum" );
                if( sarMsgRefNum != null )
                {
                    submitMultiSM.setSarMsgRefNum(
                        Short.parseShort( sarMsgRefNum ) );
                }
                String sarTotalSegments = element.getChildTextTrim( "SarTotalSegments" );
                if( sarTotalSegments != null )
                {
                    submitMultiSM.setSarTotalSegments(
                        Short.parseShort( sarTotalSegments ) );
                }
                String sarSegmentSeqNum = element.getChildTextTrim( "SarSegmentSeqnum" );
                if( sarSegmentSeqNum != null )
                {
                    submitMultiSM.setSarSegmentSeqnum(
                        Short.parseShort( sarSegmentSeqNum ) );
                }
                String payloadType = element.getChildTextTrim( "PayloadType" );
                if( payloadType != null )
                {
                    submitMultiSM.setPayloadType(
                        Byte.parseByte( payloadType ) );
                }
                Element messagePayload = element.getChild( "MessagePayload" );
                if( messagePayload != null )
                {
                    StringBuilder text = new StringBuilder();
                    for( Object line: messagePayload.getChildren( "line" ) )
                    {
                        text.append( ((Element)line).getText() ).append( "\n" );
                    }
                    ByteBuffer buffer = new ByteBuffer();
                    buffer.appendString( text.toString(), Data.ENC_UTF8 );
                    submitMultiSM.setMessagePayload( buffer );
                }
                String privacyIndicator = element.getChildTextTrim( "PrivacyIndicator" );
                if( privacyIndicator != null )
                {
                    submitMultiSM.setPrivacyIndicator(
                        Byte.parseByte( privacyIndicator ) );
                }
                Element callbackNum = element.getChild( "CallBackNum" );
                if( callbackNum != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.36
                    throw new UnsupportedOperationException(
                        "<CallBackNum> not implemented" );
                }
                String callbackNumPresInd = element.getChildTextTrim( "CallbackNumPresInd" );
                if( callbackNumPresInd != null )
                {
                    submitMultiSM.setCallbackNumPresInd(
                        Byte.parseByte( callbackNumPresInd ) );
                }
                Element callbackNumATag = element.getChild( "CallBackNumATag" );
                if( callbackNumATag != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.38
                    throw new UnsupportedOperationException(
                        "<CallBackNumATag> not implemented" );
                }
                Element sourceSubaddress = element.getChild( "SourceSubaddress" );
                if( sourceSubaddress != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.15
                    throw new UnsupportedOperationException(
                        "<SourceSubaddress> not implemented" );
                }
                Element destSubaddress = element.getChild( "DestSubaddress" );
                if( destSubaddress != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.16
                    throw new UnsupportedOperationException(
                        "<DestSubaddress> not implemented" );
                }
                String displayTime = element.getChildTextTrim( "DisplayTime" );
                if( displayTime != null )
                {
                    submitMultiSM.setDisplayTime(
                        Byte.parseByte( displayTime ) );
                }
                String smsSignal = element.getChildTextTrim( "SmsSignal" );
                if( smsSignal != null )
                {
                    submitMultiSM.setSmsSignal( Short.parseShort( smsSignal ) );
                }
                String msValidity = element.getChildTextTrim( "MsValidity" );
                if( msValidity != null )
                {
                    submitMultiSM.setMsValidity( Byte.parseByte( msValidity ) );
                }
                String msMsgWaitFacilities = element.getChildTextTrim( "MsMsgWaitFacilities" );
                if( msMsgWaitFacilities != null )
                {
                    submitMultiSM.setMsMsgWaitFacilities(
                        Byte.parseByte( msMsgWaitFacilities ) );
                }
                Element alertOnMsgDelivery =
                        element.getChild( "AlertOnMsgDelivery" );
                if( alertOnMsgDelivery != null )
                {
                    submitMultiSM.setAlertOnMsgDelivery(
                        alertOnMsgDelivery.getTextTrim().toUpperCase().startsWith( "Y" ) );
                }
                String languageIndicator = element.getChildTextTrim( "LanguageIndicator" );
                if( languageIndicator != null )
                {
                    submitMultiSM.setLanguageIndicator(
                        Byte.parseByte( languageIndicator ) );
                }
                break;
            }
            case SUBMIT_MULTI_RESP:
                // No optional fields
                break;
            case DELIVER_SM:
            {
                DeliverSM deliverSM = (DeliverSM)pdu;
                String userMessageReference = element.getChildTextTrim( "UserMessageReference" );
                if( userMessageReference != null )
                {
                    deliverSM.setUserMessageReference(
                        Short.parseShort( userMessageReference ) );
                }
                String sourcePort = element.getChildTextTrim( "SourcePort" );
                if( sourcePort != null )
                {
                    deliverSM.setSourcePort( Short.parseShort( sourcePort ) );
                }
                String destinationPort = element.getChildTextTrim( "DestinationPort" );
                if( destinationPort != null )
                {
                    deliverSM.setDestinationPort(
                        Short.parseShort( destinationPort ) );
                }
                String sarMsgRefNum = element.getChildTextTrim( "SarMsgRefNum" );
                if( sarMsgRefNum != null )
                {
                    deliverSM.setSarMsgRefNum(
                        Short.parseShort( sarMsgRefNum ) );
                }
                String sarTotalSegments = element.getChildTextTrim( "SarTotalSegments" );
                if( sarTotalSegments != null )
                {
                    deliverSM.setSarTotalSegments(
                        Short.parseShort( sarTotalSegments ) );
                }
                String sarSegmentSeqNum = element.getChildTextTrim( "SarSegmentSeqnum" );
                if( sarSegmentSeqNum != null )
                {
                    deliverSM.setSarSegmentSeqnum(
                        Short.parseShort( sarSegmentSeqNum ) );
                }
                String userResponseCode = element.getChildTextTrim( "UserResponseCode" );
                if( userResponseCode != null )
                {
                    deliverSM.setUserResponseCode(
                        Byte.parseByte( userResponseCode ) );
                }
                String privacyIndicator = element.getChildTextTrim( "PrivacyIndicator" );
                if( privacyIndicator != null )
                {
                    deliverSM.setPrivacyIndicator(
                        Byte.parseByte( privacyIndicator ) );
                }
                String payloadType = element.getChildTextTrim( "PayloadType" );
                if( payloadType != null )
                {
                    deliverSM.setPayloadType( Byte.parseByte( payloadType ) );
                }
                Element messagePayload = element.getChild( "MessagePayload" );
                if( messagePayload != null )
                {
                    StringBuilder text = new StringBuilder();
                    for( Object line: messagePayload.getChildren( "line" ) )
                    {
                        text.append( ((Element)line).getText() ).append( "\n" );
                    }
                    ByteBuffer buffer = new ByteBuffer();
                    buffer.appendString( text.toString(), Data.ENC_UTF8 );
                    deliverSM.setMessagePayload( buffer );
                }
                Element callbackNum = element.getChild( "CallBackNum" );
                if( callbackNum != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.36
                    throw new UnsupportedOperationException(
                        "<CallBackNum> not implemented" );
                }
                Element sourceSubaddress = element.getChild( "SourceSubaddress" );
                if( sourceSubaddress != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.15
                    throw new UnsupportedOperationException(
                        "<SourceSubaddress> not implemented" );
                }
                Element destSubaddress = element.getChild( "DestSubaddress" );
                if( destSubaddress != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.16
                    throw new UnsupportedOperationException(
                        "<DestSubaddress> not implemented" );
                }
                String languageIndicator = element.getChildTextTrim( "LanguageIndicator" );
                if( languageIndicator != null )
                {
                    deliverSM.setLanguageIndicator(
                        Byte.parseByte( languageIndicator ) );
                }
                String itsSessionInfo = element.getChildTextTrim( "ItsSessionInfo" );
                if( itsSessionInfo != null )
                {
                    deliverSM.setItsSessionInfo(
                        Short.parseShort( itsSessionInfo ) );
                }
                Element networkErrorCode = element.getChild( "NetworkErrorCode" );
                if( networkErrorCode != null )
                {
                    String networkType =
                        networkErrorCode.getAttributeValue( "NetworkType" );
                    ByteBuffer buffer = new ByteBuffer();
                    if( networkType == null || networkType.length() == 0 )
                    {
                        buffer.appendByte( (byte)0 );
                    }
                    else
                    {
                        buffer.appendByte( Byte.parseByte( networkType ) );
                    }
                    buffer.appendShort( Short.parseShort(
                            networkErrorCode.getTextTrim() ) );
                    deliverSM.setNetworkErrorCode( buffer );
                }
                String messageState = element.getChildTextTrim( "MessageState" );
                if( messageState != null )
                {
                    deliverSM.setMessageState( Byte.parseByte( messageState ) );
                }
                String receiptedMessageID = element.getChildTextTrim( "ReceiptedMessageId" );
                if( receiptedMessageID != null )
                {
                    deliverSM.setReceiptedMessageId( receiptedMessageID );
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
                String msAvailabilityStatus =
                    element.getChildTextTrim( "MsAvailabilityStatus" );
//                AlertNotification alertNotification = (AlertNotification)pdu;
                if( msAvailabilityStatus != null )
                {
                    throw new UnsupportedOperationException(
                        "MS_AVAILABILITY_STATUS not implemented for ALERT_NOTIFICATION" );
                }
                break;
            }
            case DATA_SM:
            {
                DataSM dataSM = (DataSM)pdu;
                String sourcePort = element.getChildTextTrim( "SourcePort" );
                if( sourcePort != null )
                {
                    dataSM.setSourcePort( Short.parseShort( sourcePort ) );
                }
                String sourceAddrSubunit = element.getChildTextTrim( "SourceAddrSubunit" );
                if( sourceAddrSubunit != null )
                {
                    dataSM.setSourceAddrSubunit(
                        Byte.parseByte( sourceAddrSubunit ) );
                }
                String sourceNetworkType = element.getChildTextTrim( "SourceNetworkType" );
                if( sourceNetworkType != null )
                {
                    dataSM.setSourceNetworkType(
                        Byte.parseByte( sourceNetworkType ) );
                }
                String sourceBearerType = element.getChildTextTrim( "SourceBearerType" );
                if( sourceBearerType != null )
                {
                    dataSM.setSourceBearerType(
                        Byte.parseByte( sourceBearerType ) );
                }
                String sourceTelematicsID = element.getChildTextTrim( "SourceTelematicsId" );
                if( sourceTelematicsID != null )
                {
                    dataSM.setSourceTelematicsId(
                        Byte.parseByte( sourceTelematicsID ) );
                }
                String destinationPort = element.getChildTextTrim( "DestinationPort" );
                if( destinationPort != null )
                {
                    dataSM.setDestinationPort(
                        Short.parseShort( destinationPort ) );
                }
                String destAddrSubunit = element.getChildTextTrim( "DestAddrSubunit" );
                if( destAddrSubunit != null )
                {
                    dataSM.setDestAddrSubunit(
                        Byte.parseByte( destAddrSubunit ) );
                }
                String destNetworkType = element.getChildTextTrim( "DestNetworkType" );
                if( destNetworkType != null )
                {
                    dataSM.setDestNetworkType(
                        Byte.parseByte( destNetworkType ) );
                }
                String destBearerType = element.getChildTextTrim( "DestBearerType" );
                if( destBearerType != null )
                {
                    dataSM.setDestBearerType( Byte.parseByte( destBearerType ) );
                }
                String destTelematicsID = element.getChildTextTrim( "DestTelematicsId" );
                if( destTelematicsID != null )
                {
                    dataSM.setDestTelematicsId(
                        Short.parseShort( destTelematicsID ) );
                }
                String sarMsgRefNum = element.getChildTextTrim( "SarMsgRefNum" );
                if( sarMsgRefNum != null )
                {
                    dataSM.setSarMsgRefNum( Short.parseShort( sarMsgRefNum ) );
                }
                String sarTotalSegments = element.getChildTextTrim( "SarTotalSegments" );
                if( sarTotalSegments != null )
                {
                    dataSM.setSarTotalSegments(
                        Short.parseShort( sarTotalSegments ) );
                }
                String sarSegmentSeqNum = element.getChildTextTrim( "SarSegmentSeqnum" );
                if( sarSegmentSeqNum != null )
                {
                    dataSM.setSarSegmentSeqnum(
                        Short.parseShort( sarSegmentSeqNum ) );
                }
                String moreMessagesToSend = element.getChildTextTrim( "MoreMsgsToSend" );
                if( moreMessagesToSend != null )
                {
                    dataSM.setMoreMsgsToSend(
                        Byte.parseByte( moreMessagesToSend ) );
                }
                String qosTimeToLive = element.getChildTextTrim( "QosTimeToLive" );
                if( qosTimeToLive != null )
                {
                    dataSM.setQosTimeToLive( Integer.parseInt( qosTimeToLive ) );
                }
                String payloadType = element.getChildTextTrim( "PayloadType" );
                if( payloadType != null )
                {
                    dataSM.setPayloadType( Byte.parseByte( payloadType ) );
                }
                Element messagePayload = element.getChild( "MessagePayload" );
                if( messagePayload != null )
                {
                    StringBuilder text = new StringBuilder();
                    for( Object line: messagePayload.getChildren( "line" ) )
                    {
                        text.append( ((Element)line).getText() ).append( "\n" );
                    }
                    ByteBuffer buffer = new ByteBuffer();
                    buffer.appendString( text.toString(), Data.ENC_UTF8 );
                    dataSM.setMessagePayload( buffer );
                }
                String setDPF = element.getChildTextTrim( "SetDpf" );
                if( setDPF != null )
                {
                    dataSM.setSetDpf( Byte.parseByte( setDPF ) );
                }
                String receiptedMessageID = element.getChildTextTrim( "ReceiptedMessageId" );
                if( receiptedMessageID != null )
                {
                    dataSM.setReceiptedMessageId( receiptedMessageID );
                }
                String messageState = element.getChildTextTrim( "MessageState" );
                if( messageState != null )
                {
                    dataSM.setMessageState( Byte.parseByte( messageState ) );
                }
                Element networkErrorCode = element.getChild( "NetworkErrorCode" );
                if( networkErrorCode != null )
                {
                    String networkType =
                        networkErrorCode.getAttributeValue( "NetworkType" );
                    ByteBuffer buffer = new ByteBuffer();
                    if( networkType == null || networkType.length() == 0 )
                    {
                        buffer.appendByte( (byte)0 );
                    }
                    else
                    {
                        buffer.appendByte( Byte.parseByte( networkType ) );
                    }
                    buffer.appendShort( Short.parseShort(
                            networkErrorCode.getTextTrim() ) );
                    dataSM.setNetworkErrorCode( buffer );
                }
                String userMessageReference = element.getChildTextTrim( "UserMessageReference" );
                if( userMessageReference != null )
                {
                    dataSM.setUserMessageReference(
                        Short.parseShort( userMessageReference ) );
                }
                String privacyIndicator = element.getChildTextTrim( "PrivacyIndicator" );
                if( privacyIndicator != null )
                {
                    dataSM.setPrivacyIndicator(
                        Byte.parseByte( privacyIndicator ) );
                }
                Element callbackNum = element.getChild( "CallBackNum" );
                if( callbackNum != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.36
                    throw new UnsupportedOperationException(
                        "<CallBackNum> not implemented" );
                }
                String callbackNumPresInd = element.getChildTextTrim( "CallbackNumPresInd" );
                if( callbackNumPresInd != null )
                {
                    dataSM.setCallbackNumPresInd(
                        Byte.parseByte( callbackNumPresInd ) );
                }
                Element callbackNumATag = element.getChild( "CallBackNumATag" );
                if( callbackNumATag != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.38
                    throw new UnsupportedOperationException(
                        "<CallBackNumATag> not implemented" );
                }
                Element sourceSubaddress = element.getChild( "SourceSubaddress" );
                if( sourceSubaddress != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.15
                    throw new UnsupportedOperationException(
                        "<SourceSubaddress> not implemented" );
                }
                Element destSubaddress = element.getChild( "DestSubaddress" );
                if( destSubaddress != null )
                {
                    // TODO: If we ever need this, SMPP v3.4 section 5.3.2.16
                    throw new UnsupportedOperationException(
                        "<DestSubaddress> not implemented" );
                }
                String userResponseCode = element.getChildTextTrim( "UserResponseCode" );
                if( userResponseCode != null )
                {
                    dataSM.setUserResponseCode(
                        Byte.parseByte( userResponseCode ) );
                }
                String displayTime = element.getChildTextTrim( "DisplayTime" );
                if( displayTime != null )
                {
                    dataSM.setDisplayTime( Byte.parseByte( displayTime ) );
                }
                String smsSignal = element.getChildTextTrim( "SmsSignal" );
                if( smsSignal != null )
                {
                    dataSM.setSmsSignal( Short.parseShort( smsSignal ) );
                }
                String msValidity = element.getChildTextTrim( "MsValidity" );
                if( msValidity != null )
                {
                    dataSM.setMsValidity( Byte.parseByte( msValidity ) );
                }
                String msMsgWaitFacilities = element.getChildTextTrim( "MsMsgWaitFacilities" );
                if( msMsgWaitFacilities != null )
                {
                    dataSM.setMsMsgWaitFacilities(
                        Byte.parseByte( msMsgWaitFacilities ) );
                }
                String numberOfMessages = element.getChildTextTrim( "NumberOfMessages" );
                if( numberOfMessages != null )
                {
                    dataSM.setNumberOfMessages(
                        Byte.parseByte( numberOfMessages ) );
                }
                String alertOnMsgDelivery =
                    element.getChildTextTrim( "AlertOnMsgDelivery" );
                if( alertOnMsgDelivery != null )
                {
                    dataSM.setAlertOnMsgDelivery(
                        alertOnMsgDelivery.toUpperCase().startsWith( "Y" ) );
                }
                String languageIndicator = element.getChildTextTrim( "LanguageIndicator" );
                if( languageIndicator != null )
                {
                    dataSM.setLanguageIndicator(
                        Byte.parseByte( languageIndicator ) );
                }
                String itsReplyType = element.getChildTextTrim( "ItsReplyType" );
                if( itsReplyType != null )
                {
                    dataSM.setItsReplyType( Byte.parseByte( itsReplyType ) );
                }
                String itsSessionInfo = element.getChildTextTrim( "ItsSessionInfo" );
                if( itsSessionInfo != null )
                {
                    dataSM.setItsSessionInfo(
                        Short.parseShort( itsSessionInfo ) );
                }
                break;
            }
            case DATA_SM_RESP:
            {
                DataSMResp dataSMResp = (DataSMResp)pdu;
                String deliveryFailureReason = element.getChildTextTrim( "DeliveryFailureReason" );
                if( deliveryFailureReason != null )
                {
                    dataSMResp.setDeliveryFailureReason(
                            Byte.parseByte( deliveryFailureReason ) );
                }
                Element networkErrorCode = element.getChild( "NetworkErrorCode" );
                if( networkErrorCode != null )
                {
                    String networkType =
                        networkErrorCode.getAttributeValue( "NetworkType" );
                    ByteBuffer buffer = new ByteBuffer();
                    if( networkType == null || networkType.length() == 0 )
                    {
                        buffer.appendByte( (byte)0 );
                    }
                    else
                    {
                        buffer.appendByte( Byte.parseByte( networkType ) );
                    }
                    buffer.appendShort( Short.parseShort(
                            networkErrorCode.getTextTrim() ) );
                    dataSMResp.setNetworkErrorCode( buffer );
                }
                String additionalStatusInfoText = element.getChildTextTrim( "AdditionalStatusInfoText" );
                if( additionalStatusInfoText != null )
                {
                    dataSMResp.setAdditionalStatusInfoText( additionalStatusInfoText );
                }
                String dpfResult = element.getChildTextTrim( "DpfResult" );
                if( dpfResult != null )
                {
                    dataSMResp.setDpfResult( Byte.parseByte( dpfResult ) );
                }
                break;
            }
            }
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
            return null;
        }

        return pdu;
    }



    private ConnectionEvent readConnection( Element element )
    {
        ConnectionEvent conn = new ConnectionEvent();
        Element server = element.getChild( "Server" );
        conn.setAddress( server.getTextTrim() );
        conn.setPort(
                Integer.parseInt( server.getAttributeValue( "port" ), 10 ) );

        return conn;
    }


    /**
     * Converts an XML file to a JDOM document
     * @param file XML file to parse
     * @throws JDOMException Exception parsing document
     * @throws IOException   IO error
     * @return JDOM document
     */
    private Document xml2dom( File file ) throws JDOMException, IOException
    {
        // Request document building without validation // TODO: validate
        SAXBuilder builder = new SAXBuilder(false);
        Document doc;
        doc = builder.build( file );

        return doc;
    }
}
