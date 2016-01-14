package com.butler.cloud.gcm

import com.butler.cloud.gcm.GcmPacketExtension.{ELEMENT_NAME, NAMESPACE}
import org.jivesoftware.smack.packet.{DefaultPacketExtension, Message, Packet}
import org.jivesoftware.smack.provider.PacketExtensionProvider
import org.jivesoftware.smack.util.{StringUtils, XmlStringBuilder}
import org.xmlpull.v1.XmlPullParser

object GcmPacketExtension {
  val ELEMENT_NAME = "gcm"
  val NAMESPACE = "google:mobile:data"

  def provider = new PacketExtensionProvider {
    def parseExtension(parser: XmlPullParser) = {
      new GcmPacketExtension(parser.nextText)
    }
  }
}

class GcmPacketExtension(json: String) extends DefaultPacketExtension(ELEMENT_NAME, NAMESPACE) {

  def getJson = json
  override def toXML: XmlStringBuilder = null

  def toPacket: Packet = {
    new Message {
      override def toXML: XmlStringBuilder = {
        val builder = new XmlStringBuilder
        builder.append("<message")

        if (getXmlns != null) builder.append(" xmlns=\"").append(getXmlns).append("\"")
        if (getLanguage != null) builder.append(" xml:lang=\"").append(getLanguage).append("\"")
        if (getPacketID != null) builder.append(" id=\"").append(getPacketID).append("\"")
        if (getTo != null) builder.append(" to=\"").append(StringUtils.escapeForXML(getTo)).append("\"")
        if (getFrom != null) builder.append(" from=\"").append(StringUtils.escapeForXML(getFrom)).append("\"")

        builder.append(">").append(GcmPacketExtension.this.toXML).append("</message>")
      }
    }
  }

}
