package com.asfoundation.wallet.billing.adyen;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import com.adyen.core.PaymentRequest;
import com.adyen.core.interfaces.DeletePreferredPaymentMethodListener;
import com.adyen.core.interfaces.PaymentDataCallback;
import com.adyen.core.interfaces.PaymentDetailsCallback;
import com.adyen.core.interfaces.PaymentMethodCallback;
import com.adyen.core.interfaces.PaymentRequestDetailsListener;
import com.adyen.core.interfaces.PaymentRequestListener;
import com.adyen.core.interfaces.UriCallback;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.PaymentRequestResult;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.Relay;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Adyen {

  private final Context context;
  private final Charset dataCharset;
  private final Scheduler scheduler;

  private volatile BehaviorRelay<AdyenPaymentStatus> status;
  private volatile PaymentRequest paymentRequest;
  private volatile DetailsStatus detailsStatus;
  private volatile PaymentStatus paymentStatus;
  private volatile PaymentType paymentType;

  public Adyen(Context context, Charset dataCharset, Scheduler scheduler,
      BehaviorRelay<AdyenPaymentStatus> paymentRequestStatus) {
    this.context = context;
    this.dataCharset = dataCharset;
    this.scheduler = scheduler;
    this.status = paymentRequestStatus;
  }

  public Single<String> getToken() {
    return getStatus().filter(status -> status.getToken() != null)
        .map(AdyenPaymentStatus::getToken)
        .firstOrError();
  }

  public Completable completePayment(String session) {
    return getStatus().firstOrError()
        .flatMapCompletable(status -> {
          if (status.getDataCallback() == null) {
            return Completable.error(
                new IllegalStateException("Not possible to create payment no callback available."));
          }
          status.getDataCallback()
              .completionWithPaymentData(session.getBytes(dataCharset));
          return Completable.complete();
        });
  }

  public Completable selectPaymentService(PaymentMethod service) {
    return getStatus().firstOrError()
        .flatMapCompletable(status -> {
          if (status.getServiceCallback() == null) {
            return Completable.error(new IllegalStateException(
                "Not possible to select payment service no callback available."));
          }
          status.getServiceCallback()
              .completionWithPaymentMethod(service);
          return Completable.complete();
        });
  }

  public Completable finishUri(Uri uri) {
    return getStatus().firstOrError()
        .flatMapCompletable(status -> {
          if (status.getUriCallback() == null) {
            return Completable.error(new IllegalStateException(
                "Not possible to select payment service no callback available."));
          }
          status.getUriCallback()
              .completionWithUri(uri);
          return Completable.complete();
        });
  }

  public Completable finishPayment(PaymentDetails details) {
    return getStatus().firstOrError()
        .flatMapCompletable(status -> {
          if (status.getDetailsCallback() == null) {
            return Completable.error(new IllegalStateException(
                "Not possible to finish payment with details no callback available."));
          }
          status.getDetailsCallback()
              .completionWithPaymentDetails(details);
          return Completable.complete();
        });
  }

  public Observable<PaymentRequestResult> getPaymentResult() {
    return getStatus().filter(status -> status.getResult() != null)
        .map(AdyenPaymentStatus::getResult);
  }

  public Observable<PaymentRequest> getPaymentRequest() {
    return getStatus().filter(status -> status.getPaymentRequest() != null)
        .map(adyenPaymentStatus -> adyenPaymentStatus.getPaymentRequest());
  }

  public Single<String> getRedirectUrl() {
    return getStatus().filter(status -> status.getRedirectUrl() != null)
        .map(AdyenPaymentStatus::getRedirectUrl)
        .firstOrError();
  }

  public Single<PaymentMethod> getPaymentMethod(PaymentType paymentType) {
    this.paymentType = paymentType;
    return getStatus().filter(status -> status.getServices() != null)
        .flatMap(status -> getPaymentMethod(status.getServices(), status.getRecurringServices(),
            paymentType))
        .firstOrError();
  }

  private Observable<PaymentMethod> getPaymentMethod(List<PaymentMethod> services,
      List<PaymentMethod> recurringServices, PaymentType paymentType) {
    return Observable.fromIterable(paymentType.getSubTypes())
        .concatMap(subType -> Observable.fromIterable(recurringServices)
            .concatWith(Observable.fromIterable(services))
            .filter(service -> subType.equals(service.getType())))
        .take(1);
  }

  private Observable<AdyenPaymentStatus> getStatus() {
    return status.subscribeOn(scheduler);
  }

  public void createNewPayment() {

    if (isOngoingPayment(paymentRequest)) {
      cancelPayment();
    }

    paymentStatus = new PaymentStatus(status);
    detailsStatus = new DetailsStatus(status, Collections.emptyList(), Collections.emptyList());
    paymentRequest = new PaymentRequest(context, paymentStatus, detailsStatus);
    paymentRequest.start();
  }

  private boolean isOngoingPayment(PaymentRequest paymentRequest) {
    return paymentRequest != null;
  }

  private void publish() {
    status.accept(AdyenPaymentStatus.from(paymentStatus, detailsStatus));
  }

  private void cancelPayment() {
    detailsStatus.clear();
    paymentStatus.clear();
    paymentRequest.cancel();
    paymentRequest = null;
  }

  public class PaymentStatus implements PaymentRequestListener {

    private Relay<AdyenPaymentStatus> status;
    private String token;
    private PaymentDataCallback dataCallback;
    private PaymentRequestResult result;

    public PaymentStatus(Relay<AdyenPaymentStatus> status) {
      this.status = status;
    }

    @Override public void onPaymentDataRequested(@NonNull PaymentRequest paymentRequest,
        @NonNull String token, @NonNull PaymentDataCallback paymentDataCallback) {
      this.token = token;
      this.dataCallback = paymentDataCallback;
      notifyStatus();
    }

    @Override public void onPaymentResult(@NonNull PaymentRequest paymentRequest,
        @NonNull PaymentRequestResult paymentRequestResult) {
      this.result = paymentRequestResult;
      Adyen.this.paymentRequest = paymentRequest;
      notifyStatus();
    }

    public String getToken() {
      return token;
    }

    public PaymentDataCallback getDataCallback() {
      return dataCallback;
    }

    public PaymentRequestResult getResult() {
      return result;
    }

    private void notifyStatus() {
      if (status != null) {
        publish();
      }
    }

    public void clear() {
      status = null;
      token = null;
      dataCallback = null;
      result = null;
    }
  }

  public class DetailsStatus
      implements PaymentRequestDetailsListener, DeletePreferredPaymentMethodListener {

    private Relay<AdyenPaymentStatus> status;
    private PaymentMethodCallback serviceCallback;
    private List<PaymentMethod> services;
    private List<PaymentMethod> recurringServices;
    private PaymentDetailsCallback detailsCallback;
    private PaymentRequest paymentRequest;
    private UriCallback uriCallback;
    private String redirectUrl;

    public DetailsStatus(Relay<AdyenPaymentStatus> status, List<PaymentMethod> services,
        List<PaymentMethod> recurringServices) {
      this.status = status;
      this.services = services;
      this.recurringServices = recurringServices;
    }

    @Override public void onPaymentMethodSelectionRequired(@NonNull PaymentRequest paymentRequest,
        @NonNull List<PaymentMethod> recurringServices, @NonNull List<PaymentMethod> otherServices,
        @NonNull PaymentMethodCallback paymentMethodCallback) {
      this.serviceCallback = paymentMethodCallback;
      this.recurringServices =
          recurringServices != null ? recurringServices : Collections.emptyList();
      this.services = otherServices != null ? otherServices : Collections.emptyList();
      notifyStatus();
    }

    @Override public void onRedirectRequired(@NonNull PaymentRequest paymentRequest,
        @NonNull String redirectUrl, @NonNull UriCallback uriCallback) {
      this.uriCallback = uriCallback;
      this.redirectUrl = redirectUrl;
      notifyStatus();
    }

    @Override public void onPaymentDetailsRequired(@NonNull PaymentRequest paymentRequest,
        @NonNull Collection<InputDetail> inputDetails,
        @NonNull PaymentDetailsCallback paymentDetailsCallback) {
      this.detailsCallback = paymentDetailsCallback;
      this.paymentRequest = paymentRequest;
      notifyStatus();
    }

    @Override public void onSuccess() {
      Log.d(this.getClass()
          .getSimpleName(), "onSuccess");
      for (String subtype : paymentType.getSubTypes()) {
        for (PaymentMethod service : services) {
          if (subtype.equals(service.getType())) {
            Log.d(this.getClass()
                    .getSimpleName(),
                "Subtype: " + subtype + " and service type: " + service.getType());
            detailsStatus.clear();
            paymentRequest = null;
            selectPaymentService(service).blockingAwait();
          }
        }
      }
      publish();
    }

    @Override public void onFail() {
      Log.d(this.getClass()
          .getSimpleName(), "onFail");
      notifyStatus();
    }

    public PaymentMethodCallback getServiceCallback() {
      return serviceCallback;
    }

    public List<PaymentMethod> getServices() {
      return services;
    }

    public PaymentDetailsCallback getDetailsCallback() {
      return detailsCallback;
    }

    public PaymentRequest getPaymentRequest() {
      return paymentRequest;
    }

    public List<PaymentMethod> getRecurringServices() {
      return recurringServices;
    }

    public UriCallback getUriCallback() {
      return uriCallback;
    }

    public String getRedirectUrl() {
      return redirectUrl;
    }

    private void notifyStatus() {
      if (status != null) {
        publish();
      }
    }

    public void clear() {
      status = null;
      serviceCallback = null;
      services = null;
      recurringServices = null;
      detailsCallback = null;
      paymentRequest = null;
      uriCallback = null;
      redirectUrl = null;
    }
  }

  public Completable deletePaymentMethod(PaymentMethod paymentMethod) {
    return Completable.fromRunnable(() -> detailsStatus.getPaymentRequest()
        .deletePreferredPaymentMethod(detailsStatus.getPaymentRequest()
            .getPaymentMethod(), detailsStatus));
  }
}
