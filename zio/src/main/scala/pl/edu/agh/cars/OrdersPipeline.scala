package pl.edu.agh.cars

import pl.edu.agh.cars.batcher.OrdersBatcher
import pl.edu.agh.cars.counter.OrdersCounter
import pl.edu.agh.cars.loader.OrdersLoader
import pl.edu.agh.cars.persistence.OrderBatchesPersistencePipe
import pl.edu.agh.cars.processor.OrdersProcessor
import pl.edu.agh.zio.pipeline.Pipeline

class OrdersPipeline(inputFilePath: String, enabledPipes: Option[String] = None)
    extends Pipeline(
      List(
        OrdersLoader(inputFilePath),
        OrdersProcessor(),
        OrdersBatcher(),
        OrderBatchesPersistencePipe(),
        OrdersCounter()
      ).filter(f => enabledPipes.forall(_.split(",").contains(f.name))),
    )
