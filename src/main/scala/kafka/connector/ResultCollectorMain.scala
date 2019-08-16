package kafka.connector

import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration

import javax.imageio.ImageIO
import kafka.RequestGenerator.DataPoint
import kafka.{KafkaUtil, RequestGenerator, Topics}
import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.JavaConverters

object ResultCollectorMain {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val topicIn = Topics.CONNECTOR_RESPONSES

  var img: BufferedImage = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_RGB )

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
    LOG.info( "Starting Result Collector")

    val options = nextOption( Map(), args.toList )

    LOG.info( s"Parsed parameters: ${options}")

    val kafkaUri =    options.getOrElse( 'kafka, "unknown" ).asInstanceOf[String]

    val props = KafkaUtil.kafkaProps( kafkaUri, "Mandelbrot.Results.Connector")
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
        processResult( record.value(), record.key() )
    }
  }

  private def processResult( value: String, key: String ): Unit = {
    val result = Serialization.read[Any]( value )

    result match {
      case RequestGenerator.BEGIN_MARKER( sizeX, sizeY ) =>
        LOG.info( s"Received BEGIN marker, creating image of size ${sizeX} x ${sizeY}")
        img = new BufferedImage( sizeX, sizeY, BufferedImage.TYPE_INT_RGB )

      case RequestGenerator.END_MARKER =>
        LOG.info( s"Received END marker, writing image to file")
        val now = System.currentTimeMillis()
        val newFile = s"image-${now}.png"
        LOG.info( s"Output file is: ${newFile}")
        ImageIO.write( img, "PNG", new File( newFile ));

      case DataPoint( posX, posY, cn, iter ) =>
        val colour = if( cn.isNaN ||  cn.isInfinite ) 0 else 0xFFFFFF
        img.setRGB( posX, posY, colour )
    }
  }
}
