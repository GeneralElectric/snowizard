/*
 * Copyright 2010-2012 Twitter, Inc.
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
package com.ge.snowizard.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.snowizard.exceptions.InvalidSystemClock;
import com.ge.snowizard.exceptions.InvalidUserAgentError;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

public class IdWorker {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IdWorker.class);
    private static final Pattern AGENT_PATTERN = Pattern
            .compile("([a-zA-Z][a-zA-Z0-9\\-]*)");

    public static final long TWEPOCH = 1288834974657L;

    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = -1L
            ^ (-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_BITS = 12L;

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS
            + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS
            + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

    private final Counter idsCounter = Metrics.newCounter(IdWorker.class,
            "ids_generated");
    private final Map<String, Counter> agentCounters = new ConcurrentHashMap<String, Counter>();
    private final Counter exceptionsCounter = Metrics.newCounter(
            IdWorker.class, "exceptions");
    private final int workerId;
    private final int datacenterId;

    private final AtomicLong lastTimestamp = new AtomicLong(-1L);
    private final AtomicLong sequence;

    /**
     * Constructor
     *
     * @param workerId
     *            Worker ID
     * @param datacenterId
     *            Datacenter ID
     */
    public IdWorker(final int workerId, final int datacenterId) {
        this(workerId, datacenterId, 0L);
    }

    /**
     * Constructor
     *
     * @param workerId
     *            Worker ID
     * @param datacenterId
     *            Datacenter ID
     * @param startSequence
     *            Starting sequence number
     */
    public IdWorker(final int workerId, final int datacenterId,
            final long startSequence) {

        checkNotNull(workerId);
        checkArgument(workerId >= 0, String.format(
                "worker Id can't be greater than %d or less than 0",
                MAX_WORKER_ID));
        checkArgument(workerId <= MAX_WORKER_ID, String.format(
                "worker Id can't be greater than %d or less than 0",
                MAX_WORKER_ID));

        checkNotNull(datacenterId);
        checkArgument(datacenterId >= 0, String.format(
                "datacenter Id can't be greater than %d or less than 0",
                MAX_DATACENTER_ID));
        checkArgument(datacenterId <= MAX_DATACENTER_ID, String.format(
                "datacenter Id can't be greater than %d or less than 0",
                MAX_DATACENTER_ID));

        checkNotNull(startSequence);

        this.workerId = workerId;
        this.datacenterId = datacenterId;

        LOGGER.info(
                "worker starting. timestamp left shift {}, datacenter id bits {}, worker id bits {}, sequence bits {}, workerid {}",
                TIMESTAMP_LEFT_SHIFT, DATACENTER_ID_BITS, WORKER_ID_BITS,
                SEQUENCE_BITS, workerId);

        sequence = new AtomicLong(startSequence);
    }

    /**
     * Get the next ID for a given user-agent
     *
     * @param agent
     *            User Agent
     * @return Generated ID
     * @throws InvalidUserAgentError
     *             When the user agent is invalid
     * @throws InvalidSystemClock
     *             When the system clock is moving backward
     */
    public long getId(final String agent) throws InvalidUserAgentError,
            InvalidSystemClock {
        if (!isValidUserAgent(agent)) {
            exceptionsCounter.inc();
            throw new InvalidUserAgentError();
        }

        final long id = nextId();
        genCounter(agent);

        return id;
    }

    /**
     * Return the worker ID
     *
     * @return Worker ID
     */
    public int getWorkerId() {
        return this.workerId;
    }

    /**
     * Return the data center ID
     *
     * @return Datacenter ID
     */
    public int getDatacenterId() {
        return this.datacenterId;
    }

    /**
     * Return the current system time in milliseconds.
     *
     * @return Current system time in milliseconds
     */
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Return the current sequence position
     *
     * @return Current sequence position
     */
    public long getSequence() {
        return sequence.get();
    }

    /**
     * Set the sequence to a given value
     *
     * @param value
     *            New sequence value
     */
    public void setSequence(final long value) {
        this.sequence.set(value);
    }

    /**
     * Get the next ID
     *
     * @return Next ID
     * @throws InvalidSystemClock
     *             When the clock is moving backward
     */
    public synchronized long nextId() throws InvalidSystemClock {
        long timestamp = timeGen();
        long curSequence = 0L;

        final long prevTimestamp = lastTimestamp.get();

        if (timestamp < prevTimestamp) {
            exceptionsCounter.inc();
            LOGGER.error(
                    "clock is moving backwards. Rejecting requests until {}",
                    prevTimestamp);
            throw new InvalidSystemClock(
                    String.format(
                            "Clock moved backwards. Refusing to generate id for %d milliseconds",
                            (prevTimestamp - timestamp)));
        }

        if (prevTimestamp == timestamp) {
            curSequence = sequence.incrementAndGet() & SEQUENCE_MASK;
            if (curSequence == 0) {
                timestamp = tilNextMillis(prevTimestamp);
            }
        } else {
            curSequence = 0L;
            sequence.set(0L);
        }

        lastTimestamp.set(timestamp);
        final long id = ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT) | curSequence;

        LOGGER.trace(
                "prevTimestamp = {}, timestamp = {}, sequence = {}, id = {}",
                prevTimestamp, timestamp, sequence, id);

        return id;
    }

    /**
     * Return the next time in milliseconds
     *
     * @param lastTimestamp
     *            Last timestamp
     * @return Next timestamp in milliseconds
     */
    protected long tilNextMillis(final long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * Generate a new timestamp (currently in milliseconds)
     *
     * @return current timestamp in milliseconds
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * Check whether the user agent is valid
     *
     * @param agent
     *            User-Agent
     * @return True if the user agent is valid
     */
    public boolean isValidUserAgent(final String agent) {
        final Matcher matcher = AGENT_PATTERN.matcher(agent);
        return matcher.matches();
    }

    /**
     * Update the counters for a given user agent
     *
     * @param agent
     *            User-Agent
     */
    protected void genCounter(final String agent) {
        idsCounter.inc();
        if (!agentCounters.containsKey(agent)) {
            agentCounters.put(agent, Metrics.newCounter(IdWorker.class,
                    "ids_generated_" + agent));
        }
        agentCounters.get(agent).inc();
    }
}
