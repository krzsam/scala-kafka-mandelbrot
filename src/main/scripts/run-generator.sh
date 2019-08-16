java -cp scala-kafka-mandelbrot_2.12-0.1.jar:scala-kafka-mandelbrot-assembly-0.1.jar kafka.RequestGeneratorMain \
    -k 10.128.0.2:9092 \
    -a connector \
    -tl -2.2,1.2 \
    -br 1.0,-1.2 \
    -sx 800 \
    -sy 600 \
    -i  128
