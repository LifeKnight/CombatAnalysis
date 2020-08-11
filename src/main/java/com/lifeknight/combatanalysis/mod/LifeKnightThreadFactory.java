package com.lifeknight.combatanalysis.mod;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class LifeKnightThreadFactory implements ThreadFactory
{
    private final AtomicInteger threadNumber;
    
    public LifeKnightThreadFactory() {
        this.threadNumber = new AtomicInteger(1);
    }
    
    @Override
    public Thread newThread(final Runnable r) {
        return new Thread(r, "LifeKnightThread" + this.threadNumber.getAndIncrement());
    }
}
