package pl.edu.agh.fs2.pipeline.utils

import fs2.Pull
import fs2.Stream

object SeedScan {
  implicit class SeedScanOps[F[_], In](private val stream: Stream[F, In]) {
    def seedScan[S](seed: In => S)(fn: (S, In) => S): Stream[F, S] = {
      def go(state: Option[S], s: Stream[F, In]): Pull[F, S, Unit] =
        s.pull.uncons1.flatMap {
          case Some((hd, tl)) =>
            val newState = state.map(fn(_, hd)).getOrElse(seed(hd))
            Pull.output1(newState) >> go(Some(newState), tl)
          case None => Pull.done
        }

      go(None, stream).stream
    }
  }
}
