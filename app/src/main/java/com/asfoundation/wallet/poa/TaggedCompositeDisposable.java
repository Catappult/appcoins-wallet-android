package com.asfoundation.wallet.poa;

import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaggedCompositeDisposable {
  private final HashMap<String, List<Disposable>> map;

  public TaggedCompositeDisposable(HashMap<String, List<Disposable>> map) {
    this.map = map;
  }

  public void add(String key, Disposable disposable) {
    List<Disposable> disposables = get(key);
    disposables.add(disposable);
    map.put(key, disposables);
  }

  public List<Disposable> get(String key) {
    List<Disposable> disposables = map.get(key);
    if (disposables == null) {
      return new ArrayList<>();
    } else {
      return new ArrayList<>(disposables);
    }
  }

  public void disposeAll() {
    for (Map.Entry<String, List<Disposable>> entry : map.entrySet()) {
      dispose(entry.getKey());
    }
  }

  private void dispose(Disposable disposable) {
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  public void dispose(String key) {
    for (Disposable disposable : map.get(key)) {
      dispose(disposable);
    }
  }
}
