package com.github.nabezokodaikon

import akka.actor.{ Actor, ActorRef }
import akka.io.{ IO, Udp }
import com.github.nabezokodaikon.pcars1.TelemetryDataStructFactory.getJsonText
import com.typesafe.scalalogging.LazyLogging
import java.net.InetSocketAddress

object UdpListener {
  case class OutgoingValue(value: String)
}

class UdpListener(clientManager: ActorRef) extends Actor with LazyLogging {
  import UsingActor._
  import UdpListener._

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 5606))

  def receive = {
    case Udp.Bound(local) =>
      context.become(ready(sender()))
    case _ =>
      logger.warn("UdpListener received unknown message.")
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      val dataArray = data.toArray
      clientManager ! OutgoingValue(getJsonText(dataArray))
    // output2(dataArray)
    // confirm(data.toList)
    case Udp.Unbind =>
      logger.debug("UDP unbind.")
      socket ! Udp.Unbind
    case Udp.Unbound =>
      logger.debug("UDP unbound.")
      context.stop(self)
    case ActorDone =>
      println("UdpListener Done.")
      context.stop(self)
    case _ =>
      logger.warn("Received unknown message.")
  }

  def confirm(data: List[Byte]) = {
    import com.github.nabezokodaikon.pcars1.TelemetryDataStructFactory._
    import com.github.nabezokodaikon.pcars1.TelemetryDataConst._
    val frameInfo = createFrameInfo(data)
    if (frameInfo.frameType == TELEMETRY_DATA_FRAME_TYPE) {
      val telemetryData = createTelemetryData(data)
      // println(telemetryData.carStateData.brake)
      // println(telemetryData.carStateData.throttle.toString)
      // println(telemetryData.carStateData.clutch)
      // println(telemetryData.carStateData.steering)
      // println(telemetryData.carStateData.speed)
      // println(telemetryData.carStateData.gear)
      // println(telemetryData.carStateData.numGears)
      // println(telemetryData.carStateData.rpm)
    }
  }

  def output(data: Array[Byte]) = {
    import com.github.nabezokodaikon.util.FileUtil
    import java.util.Calendar
    import java.text.SimpleDateFormat
    import com.github.nabezokodaikon.pcars1._

    val c = Calendar.getInstance()
    val sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS")
    val time = sdf.format(c.getTime())
    val dir = FileUtil.currentDirectory

    val info = TelemetryDataStructFactory.createFrameInfo(data.toList)
    info.frameType match {
      case TelemetryDataConst.TELEMETRY_DATA_FRAME_TYPE =>
        val name = s"${dir}/testdata/pcars1/0_${time}.bin"
        FileUtil.writeBinary(name, data)
      case TelemetryDataConst.PARTICIPANT_INFO_STRINGS_FRAME_TYPE =>
        val name = s"${dir}/testdata/pcars1/1_${time}.bin"
        FileUtil.writeBinary(name, data)
      case TelemetryDataConst.PARTICIPANT_INFO_STRINGS_ADDITIONAL_FRAME_TYPE =>
        val name = s"${dir}/testdata/pcars1/2_${time}.bin"
        FileUtil.writeBinary(name, data)
    }
  }

  def output2(data: Array[Byte]) = {
    import com.github.nabezokodaikon.pcars2.UDPStreamerPacketHandlerType._
    import com.github.nabezokodaikon.pcars2.UDPDataReader.readPacketBase
    import com.github.nabezokodaikon.pcars2._
    import com.github.nabezokodaikon.util.FileUtil
    import java.util.Calendar
    import java.text.SimpleDateFormat

    val c = Calendar.getInstance()
    val sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS")
    val time = sdf.format(c.getTime())
    val dir = FileUtil.currentDirectory

    val (p, _) = readPacketBase(data.toList)
    p.packetType match {
      case CAR_PHYSICS =>
        val name = s"${dir}/testdata/pcars2/${CAR_PHYSICS}_${time}.bin"
        FileUtil.writeBinary(name, data)
      case RACE_DEFINITION =>
        val name = s"${dir}/testdata/pcars2/${RACE_DEFINITION}_${time}.bin"
        FileUtil.writeBinary(name, data)
      case PARTICIPANTS =>
        val name = s"${dir}/testdata/pcars2/${PARTICIPANTS}_${time}.bin"
        FileUtil.writeBinary(name, data)
      case TIMINGS =>
        val name = s"${dir}/testdata/pcars2/${TIMINGS}_${time}.bin"
        FileUtil.writeBinary(name, data)
      case GAME_STATE =>
        val name = s"${dir}/testdata/pcars2/${GAME_STATE}_${time}.bin"
        FileUtil.writeBinary(name, data)
      case WEATHER_STATE =>
        val name = s"${dir}/testdata/pcars2/${WEATHER_STATE}_${time}.bin"
        FileUtil.writeBinary(name, data)
      case VEHICLE_NAMES =>
        val name = s"${dir}/testdata/pcars2/${VEHICLE_NAMES}_${time}.bin"
        FileUtil.writeBinary(name, data)
      case TIME_STATS =>
        val name = s"${dir}/testdata/pcars2/${TIME_STATS}_${time}.bin"
        FileUtil.writeBinary(name, data)
      case PARTICIPANT_VEHICLE_NAMES => data.length match {
        case PacketSize.PARTICIPANT_VEHICLE_NAMES_DATA =>
          val name = s"${dir}/testdata/pcars2/${PARTICIPANT_VEHICLE_NAMES}_p_${time}.bin"
          FileUtil.writeBinary(name, data)
        case PacketSize.VEHICLE_CLASS_NAMES_DATA =>
          val name = s"${dir}/testdata/pcars2/${PARTICIPANT_VEHICLE_NAMES}_v_${time}.bin"
          FileUtil.writeBinary(name, data)
      }
      case _ =>
        println(s"Unknown packet type: ${p.packetType}")
    }
  }
}
