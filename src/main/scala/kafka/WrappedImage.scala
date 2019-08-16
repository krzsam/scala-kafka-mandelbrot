package kafka

import java.awt.image.BufferedImage

class WrappedImage {
  private var img: BufferedImage = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_RGB )

  def init( sizeX: Int, sizeY: Int ): Unit = {
    img = new BufferedImage( sizeX, sizeY, BufferedImage.TYPE_INT_RGB )
  }

  def image = img
}
