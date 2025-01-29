package io.reactivex.rxjava3.core.clusters;

public class Cluster_534 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.util.EndConsumerHelperTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.disposables.DisposableTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.util.EndConsumerHelperTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.disposables.DisposableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_534() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_1.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_2.payloads.crashDropsOnNextsConditional.evaluate();
            this._Benchmark_benchmark_3.payloads.validateDisposable.evaluate();
            this._Benchmark_benchmark_4.payloads.setOnceTwice.evaluate();
        }

   }

}