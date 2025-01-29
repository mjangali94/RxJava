package io.reactivex.rxjava3.core.clusters;

public class Cluster_229 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.core.ConverterTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.core.TransformerTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.core.ConverterTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.core.TransformerTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_229() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.disposedObservable.evaluate();
            this._Benchmark_benchmark_1.payloads.usingObservableSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_2.payloads.singleConverterThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.takeLastWithNegativeCount.evaluate();
            this._Benchmark_benchmark_1.payloads.combineLatestIterableOneIsNull.evaluate();
            this._Benchmark_benchmark_1.payloads.combineLatestDelayErrorIterableOneIsNull.evaluate();
            this._Benchmark_benchmark_6.payloads.observableGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_6.payloads.singleGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_6.payloads.maybeGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_9.payloads.justConditional.evaluate();
            this._Benchmark_benchmark_10.payloads.repeatUntil.evaluate();
        }

   }

}