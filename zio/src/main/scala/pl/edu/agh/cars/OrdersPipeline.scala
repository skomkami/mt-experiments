package pl.edu.agh.cars

import pl.edu.agh.cars.batcher.OrdersBatcher
import pl.edu.agh.cars.counter.OrdersCounter
import pl.edu.agh.cars.loader.OrdersLoader
import pl.edu.agh.cars.persistence.OrderBatchesPersistencePipe
import pl.edu.agh.cars.processor.OrdersProcessor
import pl.edu.agh.zio.pipeline.Pipeline

class OrdersPipeline
    extends Pipeline(
      List(
        OrdersLoader("orders.csv"),
        OrdersProcessor(),
        OrdersBatcher(),
        OrderBatchesPersistencePipe(),
        OrdersCounter()
      ),
    )
