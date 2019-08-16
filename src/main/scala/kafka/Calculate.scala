package kafka

import java.io.File

import javax.imageio.ImageIO
import org.apache.commons.math3.complex.Complex
import org.slf4j.{Logger, LoggerFactory}

object Calculate {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val BigNumber = new Complex( Double.MaxValue, Double.MaxValue )

  def calculateOne( c: Complex, iterations: Integer ) : Complex = {
    val zn = new Complex( 0, 0 )
    val range = 1 until iterations
    range.foldLeft( zn ) { (acc, item) =>
      acc.multiply( acc ).add( c )
    }
  }

  def processDataPoint( request: Message ): Message = {
    request.data match {
      case Some( DataPoint(posX, posY, c0, iterations) ) =>
        val result = calculateOne( c0, iterations )
        val resultAdjusted = if (result.isInfinite || result.isNaN) BigNumber else result
        //LOG.info( s"Calculating: (${posX},${posY}) , ${c0} -> ${resultAdjusted}")
        Message( Some( DataPoint(posX, posY, resultAdjusted, 0) ), None )

      case None =>
        // if it is a marker it is passed thru
        request
    }
  }

  def processResult( image: WrappedImage, result: Message ): Unit = {
    result.data match {
      case Some( DataPoint( posX, posY, cn, _ ) ) =>
        val colour = if( cn.isNaN ||  cn.isInfinite || cn.abs() > 10 ) 0 else 0xFFFFFF
        //LOG.info( s"${result.data.get.c} -> Setting image data: (${posX},${posY}) to ${colour}")
        image.image.setRGB( posX, posY, colour )

      case None =>
        result.marker match {
          case Some( Marker( begin, sizeX, sizeY ) ) =>
            if( begin ) {
              LOG.info( s"Received BEGIN marker, creating image of size ${sizeX} x ${sizeY}")
              image.init( sizeX, sizeY )
            } else {
              LOG.info( s"Received END marker, writing image to file")
              val now = System.currentTimeMillis()
              val newFile = s"image-${now}.png"
              LOG.info( s"Output file is: ${newFile}")
              ImageIO.write( image.image, "PNG", new File( newFile ));
            }

          case None =>
          // nothing
        }
    }
  }
}
