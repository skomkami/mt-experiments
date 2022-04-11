package pl.edu.agh.cars

import pl.edu.agh.cars.laoder.OrdersLoader
import pl.edu.agh.cars.processor.OrdersProcessor
import pl.edu.agh.fs2.pipeline.Pipeline

object FS2OrdersPipe
    extends Pipeline(List(OrdersLoader("orders.csv"), OrdersProcessor()))
