package kafka.streaming

import kafka.Calculate.processDataPoint
import kafka._
import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.commons.math3.complex.Complex
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.scala.kstream.{KStream, Produced}
import org.apache.kafka.streams.scala.{Serdes, StreamsBuilder}
import org.slf4j.{Logger, LoggerFactory}

object CalculationMain {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val topicIn = Topics.STREAM_REQUESTS
  val topicOut = Topics.STREAM_RESPONSES

  val BigNumber = new Complex( Double.MaxValue, Double.MaxValue )

  // for JSON deserialisation
  implicit val formats = DefaultFormats

  type OptionMap = Map[ Symbol, Any ]

  def nextOption( map: OptionMap, params: List[String] ): OptionMap = {
    params match {
      case "-k" :: kafkaUri :: tail =>
        nextOption( map + ( 'kafka -> kafkaUri ), tail )
      case Nil =>
        map
      case _ =>
        map
    }
  }

  def main(args: Array[String]) {
    LOG.info( "Starting Calculation for STREAMING API")

    val options = nextOption( Map(), args.toList )

    LOG.info( s"Parsed parameters: ${options}")

    val kafkaUri =    options.getOrElse( 'kafka, "unknown" ).asInstanceOf[String]

    val props = KafkaUtil.kafkaProps( kafkaUri, "Mandelbrot.Calculation.Streaming")
    val stringSerde = Serdes.String
    implicit val consumed = Consumed.`with`( stringSerde, stringSerde )
    implicit val produced = Produced.`with`( stringSerde, stringSerde )

    val builder = new StreamsBuilder()
    val requests = builder.stream[String,String]( topicIn.topicName )

    val results: KStream[String, String] = requests.mapValues {
      recordValue =>
        val request = Serialization.read[Message]( recordValue )
        val result = processDataPoint( request )
        Serialization.write( result )
    }

    results.to( topicOut.topicName )

    val streams = new KafkaStreams( builder.build(), props )
    streams.start()
  }
}
