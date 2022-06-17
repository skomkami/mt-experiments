package pl.edu.agh.zio.pipeline.utils

import zio.stream.ZStream

object SeedScan {
  implicit class SeedScanOps[R, E, In](private val stream: ZStream[R, E, In]) {
    def seedScan[R1 <: R, E1 >: E, S](
      seed: In => S
    )(fn: (S, In) => S): ZStream[R1, E1, S] =
      stream
        .scan(Option.empty[S]) {
          case (Some(state), in) => Some(fn(state, in))
          case (_, in)           => Some(seed(in))
        }
        .collect {
          case Some(state) => state
        }
  }
}
