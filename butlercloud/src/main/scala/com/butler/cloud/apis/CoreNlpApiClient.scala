package com.butler.cloud.apis

import akka.camel.javaapi.UntypedProducerActor
import com.butler.cloud.utils.Settings

class CoreNlpApiClient extends UntypedProducerActor {
  override def getEndpointUri(): String = Settings(context.system).urls.corenlpApi
}
