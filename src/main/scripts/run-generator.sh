java -cp scala-kafka-mandelbrot-assembly-0.1.jar kafka.RequestGeneratorMain \
    -k 10.128.0.2:9092
    -a connector
    -tl -1.0,1.2
    -br 1.0,-1.2
    -sx 128
    -sy 128
    -i  128