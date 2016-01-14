package com.butler.cloud.controllers

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import com.butler.cloud.databases.types.{NodeTypes, RelTypes}
import com.butler.cloud.gcm.handlers.GcmSender.SendMessage
import com.butler.cloud.gcm.requests.ClientMessageRequestTypes
import com.butler.cloud.models.ccs.ReceivedMessage
import com.butler.cloud.models.data.RequestData
import com.butler.cloud.models.payload.ClientMessage
import com.butler.cloud.models.social.FacebookAccount
import com.butler.cloud.testutils.Test
import com.butler.cloud.testutils.actors.ProxyActor
import com.butler.cloud.testutils.databases.{TestGraphDB, TestMongoDB}
import org.neo4j.graphdb.{Direction, Node}

class AuthRequestCtrlSpec(_system: ActorSystem) extends Test(_system) with TestMongoDB with TestGraphDB {

  def this() = this(ActorSystem("AuthRequestCtrlSpec"))

  val gcm_probe = TestProbe()

  override def beforeAll() = {
    mongoBeforeAll()
    graphBeforeAll()

    // Create an actor for /user/gcm
    system.actorOf(Props(classOf[ProxyActor], gcm_probe.ref), "gcm")
  }

  override def afterAll() {
    mongoAfterAll()
    graphAfterAll()
    TestKit.shutdownActorSystem(system)
  }

  "AuthRequestCtrl" must {

    val ctrl = system.actorOf(Props[AuthRequestCtrl], "AuthRequestCtrl")
    val facebookAccount = FacebookAccount("uid1", "FirstName", "LastName", "Name", "Gender", "http", "EN", 3, "Token")
    val requestData = RequestData(Some(facebookAccount), None, None, None, None, None, None)
    val clientMessage = ClientMessage("1", "uuid-1234", "testDeviceId", "token", ClientMessageRequestTypes.AuthRequest, requestData)

    "send a response back to the GCM router" in {
      clientMessage.receivedMessage = Some(ReceivedMessage("", "testMessageId", "testFrom", clientMessage, "testTo", "", "", "", ""))
      ctrl ! clientMessage
      gcm_probe.expectMsgClass(classOf[SendMessage])
    }

    "create nodes and relationships" in {
      withTx {
        var human: Option[Node] = None

        // Check human
        neo4j.findNodesByLabelAndProperty(NodeTypes.Human, "id", clientMessage.userId).each { iterator =>
          iterator.hasNext.shouldEqual(true)
          human = Some(iterator.next())
        }

        // Check FacebookAccount
        neo4j.findNodesByLabelAndProperty(NodeTypes.FacebookAccount, "id", facebookAccount.id).each { iterator =>
          iterator.hasNext.shouldEqual(true)

          human.get.getRelationships(RelTypes.ConnectedTo, Direction.OUTGOING).each { relIterator =>
            relIterator.hasNext.shouldEqual(true)
            relIterator.next().getEndNode.getId.shouldEqual(iterator.next().getId)
          }
        }

        // Check device
        neo4j.findNodesByLabelAndProperty(NodeTypes.Device, "id", clientMessage.deviceId).each { iterator =>
          iterator.hasNext.shouldEqual(true)

          human.get.getRelationships(RelTypes.PairedWith, Direction.OUTGOING).each { relIterator =>
            relIterator.hasNext.shouldEqual(true)
            relIterator.next().getEndNode.getId.shouldEqual(iterator.next().getId)
          }
        }
      }
    }

  }

}
