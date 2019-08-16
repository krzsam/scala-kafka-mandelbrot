package kafka.connector

import java.time.Duration

import kafka._
import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.JavaConverters

object ImageGenMain {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val topicIn = Topics.CONNECTOR_RESPONSES

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
    LOG.info( "Starting IMAGE GENERATION for CONNECTOR API")

    val options = nextOption( Map(), args.toList )

    LOG.info( s"Parsed parameters: ${options}")

    val kafkaUri =    options.getOrElse( 'kafka, "unknown" ).asInstanceOf[String]

    val props = KafkaUtil.kafkaProps( kafkaUri, "Mandelbrot.Image.Connector")
    val requestConsumer = new KafkaConsumer[String, String]( props )

    requestConsumer.subscribe( JavaConverters.asJavaCollection( List( topicIn.topicName )  ) )
    poller( requestConsumer )

    requestConsumer.close()
  }

  /*
  Poll for calculation results sent back from Consumer
  */
  @tailrec private def poller(resultConsumer: KafkaConsumer[String, String] ): Unit = {
    val records: ConsumerRecords[String, String] = resultConsumer.poll( Duration.ofMinutes( 5 ))
    processRecords( records )
    poller( resultConsumer )
  }

  private def processRecords(records: ConsumerRecords[String, String] ): Unit = {
    records.forEach {
      record =>
        val result = Serialization.read[Message]( record.value() )
        //LOG.info( s"Received result: ${result}")
        Calculate.processResult( img, result )
    }
  }
}
