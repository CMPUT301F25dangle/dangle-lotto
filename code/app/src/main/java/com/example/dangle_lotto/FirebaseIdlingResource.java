package com.example.dangle_lotto;

import androidx.test.espresso.IdlingResource;
import com.google.android.gms.tasks.Task;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An Espresso IdlingResource used to track pending Firebase operations.
 * <p>
 * Each async Firebase call should invoke {@link #increment()} when it begins
 * and {@link #decrement()} when it completes. Espresso waits until this
 * resource is idle before running UI assertions.
 *
 * @author Mahd Afzal
 * @version 1.0
 * @since 2025-11-30
 */
public class FirebaseIdlingResource implements IdlingResource {
    private ResourceCallback callback;
    private AtomicInteger counter = new AtomicInteger(0);

    public void increment() { counter.getAndIncrement(); }
    public void decrement() {
        int value = counter.decrementAndGet();
        if (value == 0 && callback != null) callback.onTransitionToIdle();
    }

    @Override
    public String getName() { return "FirebaseIdlingResource"; }

    @Override
    public boolean isIdleNow() { return counter.get() == 0; }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) { this.callback = callback; }
}
