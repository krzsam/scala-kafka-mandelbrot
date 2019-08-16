package kafka.streaming

import kafka._
import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.scala.kstream.{KStream, Produced}
import org.apache.kafka.streams.scala.{Serdes, StreamsBuilder}
import org.slf4j.{Logger, LoggerFactory}

object ImageGenMain {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val topicIn = Topics.STREAM_RESPONSES

  val img = new WrappedImage()

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
    LOG.info( "Starting IMAGE GENERATION for STREAMING API")

    val options = nextOption( Map(), args.toList )

    LOG.info( s"Parsed parameters: ${options}")

    val kafkaUri =    options.getOrElse( 'kafka, "unknown" ).asInstanceOf[String]

    val props = KafkaUtil.kafkaProps( kafkaUri, "Mandelbrot.Image.Streaming")
    val stringSerde = Serdes.String
    implicit val consumed = Consumed.`with`( stringSerde, stringSerde )
    implicit val produced = Produced.`with`( stringSerde, stringSerde )

    val builder = new StreamsBuilder()
    val requests = builder.stream[String,String]( topicIn.topicName )

    val results: KStream[String, Unit] = requests.mapValues {
      recordValue =>
        val result = Serialization.read[Message]( recordValue )
        Calculate.processResult( img, result )
    }

    val streams = new KafkaStreams( builder.build(), props )
    streams.start()
  }
}
