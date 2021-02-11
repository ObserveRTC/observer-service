//package org.observertc.webrtc.observer.evaluators.witholders;
//
//import io.reactivex.rxjava3.functions.Supplier;
//import org.observertc.webrtc.schemas.reports.Report;
//
//import java.util.Comparator;
//import java.util.Objects;
//import java.util.function.Function;
//
//public class EventValueHolder<T> implements Function<Report, Boolean>, Supplier<T>{
//
//    final Function<Report, T> extractor;
//    final Comparator<T> cmp;
//    private T last;
//    private T provided;
//
//    public EventValueHolder(Function<Report, T> extractor, Comparator<T> cmp) {
//        this.extractor = extractor;
//        this.cmp = cmp;
//    }
//
//    @Override
//    public Boolean apply(Report report) {
//        T value = this.extractor.apply(report);
//        if (Objects.isNull(value)) {
//            if (Objects.nonNull(this.last)) {
//                this.provided = this.last;
//                this.last = null;
//                return true;
//            }
//            this.provided = null;
//            return false;
//        }
//        if (Objects.isNull(this.last)) {
//            this.provided = this.last = value;
//            return false;
//        }
//        this.provided = this.last;
//        if (this.last.equals(value)) {
//            return false;
//        }
//        this.last = value;
//        return true;
//    }
//
//    @Override
//    public T get() throws Throwable {
//        return this.provided;
//    }
//}
