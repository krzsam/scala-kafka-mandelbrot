package kafka.connector

import java.time.Duration

import kafka.RequestGenerator.DataPoint
import kafka.{Calculate, KafkaUtil, RequestGenerator, Topics}
import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.commons.math3.complex.Complex
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.JavaConverters

object CalculationMain {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val topicIn = Topics.CONNECTOR_REQUESTS
  val topicOut = Topics.CONNECTOR_RESPONSES

  val BigNumber = new Complex( Double.MaxValue, Double.MaxValue )

  // for JSON deserialisation
  implicit val formats = DefaultFormats

  def main(args: Array[String]) {
    LOG.info( "Starting Calculation Service for CONNECTOR API topics")

    val props = KafkaUtil.kafkaProps( "Mandelbrot.Calculation.Connector")
    val requestConsumer = new KafkaConsumer[String, String]( props )
    val resultProducer: KafkaProducer[String, String] = new KafkaProducer[String, String]( props )

    requestConsumer.subscribe( JavaConverters.asJavaCollection( List( topicIn.topicName )  ) )

    poller( requestConsumer, resultProducer )

    requestConsumer.close()
    resultProducer.close()
  }

  /*
  Poll for calculation requests sent from Producer
  */
  @tailrec private def poller(consumer: KafkaConsumer[String, String], producer: KafkaProducer[String, String] ) {
    val msg: ConsumerRecords[String, String] = consumer.poll( Duration.ofMinutes( 5 ))
    msg match {
      case recs: ConsumerRecords[String, String] =>
        LOG.info( s"Received message with records ${recs.count()}")
        processRecords( recs, producer )
      case _ => /* nothing */
    }
    poller( consumer, producer )
  }

  private def processRecords(recs: ConsumerRecords[String, String], producer: KafkaProducer[String, String] ): Unit = {
    val list: List[ConsumerRecord[String, String]] = JavaConverters.asScalaIterator( recs.iterator() ).toList

    val results = list.flatMap(
      record => processBatch( record.value(), record.partition() )
    )

    if( results.size > 0 )
      sendResults( producer, results, list(0).key() )
  }

  private def processBatch( value: String, partition: Int ): Seq[Any] = {
    val requests = Serialization.read[List[Any]]( value )

    requests.map {
      case begin @ RequestGenerator.BEGIN_MARKER =>
        begin

      case end @ RequestGenerator.END_MARKER =>
        end

      case DataPoint(posX, posY, c0, iterations) =>
        val result = Calculate.calculateOne( c0, iterations )
        val resultAdjusted = if (result.isInfinite || result.isNaN) BigNumber else result
        DataPoint(posX, posY, resultAdjusted, 0)

      case _ => DataPoint(0, 0, BigNumber, 0)
    }
  }

  private def sendResults(resultProducer: KafkaProducer[String, String], results: Seq[Any], key: String ): Unit = {
    LOG.info( s"Sending back ${results.size} results")
    val jsonStr = Serialization.write(results)
    val record = new ProducerRecord[String, String](topicOut.topicName, key, jsonStr)
    resultProducer.send(record)
  }
}
