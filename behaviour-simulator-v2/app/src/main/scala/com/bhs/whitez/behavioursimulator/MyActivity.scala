package com.bhs.whitez.behavioursimulator

import android.util.Log
import android.widget.LinearLayout
import com.jjoe64.graphview.GraphView.GraphViewData
import com.jjoe64.graphview.{GraphViewDataInterface, GraphViewSeries, LineGraphView}
import net.liftweb.json._
import org.scaloid.common.SActivity

import scala.io.Source

class MyActivity extends SActivity {

  onCreate {
    setContentView(R.layout.activity_my)

    // Generate data
    var data = Array[GraphViewDataInterface]()
    var v: Double = 0

    for (i <- List.range(0, 100)) {
      v = 3 + Math.random() * 30
      data :+= new GraphViewData(i, v)
    }

    // Add view
    val graphView = new LineGraphView(this, "GraphViewDemo")
    graphView.addSeries(new GraphViewSeries(data))
    graphView.setViewPort(0, 23.9)
    graphView.setScrollable(true)
    graphView.setScalable(true)
    find[LinearLayout](R.id.container_my).addView(graphView)

    // Read input
    val input1 = getFile(R.raw.input_1)

    for (event <- input1) {
      Log.wtf("BHS", s"type: ${event.eventType} timestamp: ${event.timestamp}")
    }
  }

  implicit val formats = DefaultFormats

  def getFile(id: Int) =
    parse(Source.fromInputStream(getResources.openRawResource(id)).mkString).children.map {
      obj => Event((obj \ "type").asInstanceOf[JString].values, (obj \ "timestamp").asInstanceOf[JInt].values.toLong)
    }

  case class Event(eventType: String, timestamp: Long)

}
