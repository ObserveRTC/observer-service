/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.observertc.webrtc.observer.connector.transformations;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class ReportObserver implements Observer<Report> {
    private static final Logger logger = LoggerFactory.getLogger(ReportObserver.class);

    public final PublishSubject<Report> initiatedCallReport = PublishSubject.create();
    public final PublishSubject<Report> finishedCallReport = PublishSubject.create();
    public final PublishSubject<Report> joinedPeerConnectionCallReport = PublishSubject.create();
    public final PublishSubject<Report> detachedPeerConnectionCallReport = PublishSubject.create();
    public final PublishSubject<Report> inboundRTPReport = PublishSubject.create();
    public final PublishSubject<Report> outboundRTPReport = PublishSubject.create();
    public final PublishSubject<Report> remoteInboundRTPReport = PublishSubject.create();
    public final PublishSubject<Report> trackReport = PublishSubject.create();
    public final PublishSubject<Report> mediaSourceReport = PublishSubject.create();
    public final PublishSubject<Report> userMediaErrorReport = PublishSubject.create();

    public final PublishSubject<Report> observerEventReport = PublishSubject.create();
    public final PublishSubject<Report> iceRemoteCandidateReport = PublishSubject.create();
    public final PublishSubject<Report> iceLocalCandidateReport = PublishSubject.create();
    public final PublishSubject<Report> iceCandidatePairReport = PublishSubject.create();
    public final PublishSubject<Report> unrecognizedReport = PublishSubject.create();

    private volatile boolean disposed = false;
    private AtomicBoolean completedHolder = new AtomicBoolean(false);
    private AtomicReference<Throwable> throwableHolder = new AtomicReference<>();
    private Disposable disposable;

    public ReportObserver() {

    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        this.disposable = d;
    }

    @Override
    public void onNext(@NonNull Report report) {
        if (this.disposed) {
            logger.warn("bytes received after disposale");
            return;
        }

        this.selectSubject(report);
    }

    @Override
    public void onError(@NonNull Throwable t) {
        if (Objects.isNull(this.disposable)) {
            RxJavaPlugins.onError(t);
            return;
        }
        if (disposable.isDisposed()) {
            RxJavaPlugins.onError(t);
            return;
        }
        if (!this.throwableHolder.compareAndSet(null, t)) {
            RxJavaPlugins.onError(t);
            return;
        }
        this.disposable.dispose();
    }

    @Override
    public void onComplete() {
        if (disposable.isDisposed()) {
            return;
        }
        this.completedHolder.compareAndSet(false, true);
        this.disposable.dispose();
    }

    private void selectSubject(Report report) {
        ReportType type = report.getType();
        if (Objects.isNull(type)) {
            this.processUnrecognizedReport(report);
            return;
        }
        switch (type) {
            case FINISHED_CALL:
                this.finishedCallReport.onNext(report);
                break;
            case JOINED_PEER_CONNECTION:
                this.joinedPeerConnectionCallReport.onNext(report);
                break;
            case INITIATED_CALL:
                this.initiatedCallReport.onNext(report);
                break;
            case DETACHED_PEER_CONNECTION:
                this.detachedPeerConnectionCallReport.onNext(report);
                break;
            case INBOUND_RTP:
                this.inboundRTPReport.onNext(report);
                break;
            case REMOTE_INBOUND_RTP:
                this.remoteInboundRTPReport.onNext(report);
                break;
            case OUTBOUND_RTP:
                this.outboundRTPReport.onNext(report);
                break;
            case MEDIA_SOURCE:
                this.mediaSourceReport.onNext(report);
                break;
            case ICE_CANDIDATE_PAIR:
                this.iceCandidatePairReport.onNext(report);
                break;
            case ICE_LOCAL_CANDIDATE:
                this.iceLocalCandidateReport.onNext(report);
                break;
            case ICE_REMOTE_CANDIDATE:
                this.iceRemoteCandidateReport.onNext(report);
                break;
            case USER_MEDIA_ERROR:
                this.userMediaErrorReport.onNext(report);
                break;
            case TRACK:
                this.trackReport.onNext(report);
                break;
            case OBSERVER_EVENT:
                this.observerEventReport.onNext(report);
                break;
            case EXTENSION:
            case UNKNOWN:
            default:
                this.processUnrecognizedReport(report);
                break;
        }
    }

    private void processUnrecognizedReport(Report report) {
        if (this.unrecognizedReport.hasObservers()) {
            this.unrecognizedReport.onNext(report);
        } else {
            logger.warn("An unrecognized error appeared {}", report);
        }
    }
}