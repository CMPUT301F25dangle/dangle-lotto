package com.example.dangle_lotto;

import androidx.annotation.Nullable;
import androidx.test.espresso.IdlingResource;
import com.google.android.gms.tasks.Task;

import java.util.HashSet;
import java.util.Set;

public class FirebaseIdlingResource implements IdlingResource {
    private ResourceCallback callback;
    private final Set<Task<?>> tasks = new HashSet<>();

    public void monitorTask(Task<?> task) {
        synchronized (tasks) {
            tasks.add(task);
        }

        task.addOnCompleteListener(t -> {
            synchronized (tasks) {
                tasks.remove(task);
                if (tasks.isEmpty() && callback != null) {
                    callback.onTransitionToIdle();
                }
            }
        });
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public boolean isIdleNow() {
        synchronized (tasks) {
            return tasks.isEmpty();
        }
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }
}
