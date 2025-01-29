package io.reactivex.rxjava3.core.clusters;

public class Cluster_464 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCallableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.schedulers.ImmediateThinSchedulerTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableBufferTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCallableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.schedulers.ImmediateThinSchedulerTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBufferTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_464() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fromCallable.evaluate();
            this._Benchmark_benchmark_0.payloads.fromCallableInvokesLazy.evaluate();
            this._Benchmark_benchmark_2.payloads.schedule.evaluate();
            this._Benchmark_benchmark_0.payloads.fromCallableTwice.evaluate();
            this._Benchmark_benchmark_4.payloads.timedSkipDoubleOnSubscribe.evaluate();
        }

   }

}