package kafka.connector

import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration

import javax.imageio.ImageIO
import kafka.RequestGenerator.DataPoint
import kafka.{KafkaUtil, RequestGenerator, Topics}
import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.JavaConverters

object ResultCollector {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val topicIn = Topics.CONNECTOR_RESPONSES

  var img: BufferedImage = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_RGB )

  // for JSON deserialisation
  implicit val formats = DefaultFormats

  def main(args: Array[String]) {
    LOG.info( "Starting Result Collector")

    val props = KafkaUtil.kafkaProps( "Mandelbrot.Results.Connector")
    val requestConsumer = new KafkaConsumer[String, String]( props )

    requestConsumer.subscribe( JavaConverters.asJavaCollection( List( topicIn.topicName )  ) )
    poller( requestConsumer )

    requestConsumer.close()
  }

  /*
  Poll for calculation results sent back from Consumer
  */
  @tailrec private def poller(resultConsumer: KafkaConsumer[String, String] ): Unit = {
    val msg: ConsumerRecords[String, String] = resultConsumer.poll( Duration.ofMinutes( 5 ))
    msg match {
      case recs: ConsumerRecords[String, String] =>
        LOG.info( s"Received message with records ${recs.count()}")
        processRecords( recs )
      case _ => /* nothing */
    }

    poller( resultConsumer )
  }

  private def processRecords(recs: ConsumerRecords[String, String] ): Unit = {
    val list: List[ConsumerRecord[String, String]] = JavaConverters.asScalaIterator( recs.iterator() ).toList

    list.foreach {
      rec: ConsumerRecord[String, String]  => processBatch( rec.value(), rec.key() )
    }
  }

  private def processBatch( value: String, key: String ): Unit = {
    val results = Serialization.read[List[Any]]( value )
    LOG.info( s"Received batch of results of size ${results.size}" )

    results.foreach{
      case RequestGenerator.BEGIN_MARKER( sizeX, sizeY ) =>
        LOG.info( s"Received BEGIN marker, createing image of size ${sizeX} x ${sizeY}")
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
