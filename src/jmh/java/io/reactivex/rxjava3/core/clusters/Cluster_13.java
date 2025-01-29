package io.reactivex.rxjava3.core.clusters;

public class Cluster_13 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCompletableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableOnErrorXTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromCompletableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.core.TransformerTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.CompletableFromCompletionStageTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.core.ConverterTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableCreateTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSkipLastTest._Benchmark _Benchmark_benchmark_14;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCompletableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableOnErrorXTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromCompletableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.core.TransformerTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.jdk8.CompletableFromCompletionStageTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.core.ConverterTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.completable.CompletableCreateTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSkipLastTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_13() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.source.evaluate();
            this._Benchmark_benchmark_1.payloads.onErrorReturnEmpty.evaluate();
            this._Benchmark_benchmark_1.payloads.normalReturn.evaluate();
            this._Benchmark_benchmark_3.payloads.upstream.evaluate();
            this._Benchmark_benchmark_4.payloads.hidden.evaluate();
            this._Benchmark_benchmark_5.payloads.singleTransformerThrows.evaluate();
            this._Benchmark_benchmark_5.payloads.maybeTransformerThrows.evaluate();
            this._Benchmark_benchmark_7.payloads.dispose2.evaluate();
            this._Benchmark_benchmark_8.payloads.singleGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_8.payloads.observableGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_8.payloads.maybeGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_11.payloads.onCompleteThrows2.evaluate();
            this._Benchmark_benchmark_8.payloads.observableConverterThrows.evaluate();
            this._Benchmark_benchmark_8.payloads.maybeConverterThrows.evaluate();
            this._Benchmark_benchmark_14.payloads.skipLastWithNegativeCount.evaluate();
        }

   }

}