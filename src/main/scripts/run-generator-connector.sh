java -cp scala-kafka-mandelbrot-assembly-0.1.jar \
    kafka.RequestGeneratorMain \
    -k localhost:9092 \
    -a connector \
    -tl -2.2,1.2 \
    -br 1.0,-1.2 \
    -sx 640 \
    -sy 480 \
    -i  128
