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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.ge.snowizard.core.IdWorker;
import com.ge.snowizard.exceptions.InvalidSystemClock;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class IdWorkerTest {
    private static final long workerMask = 0x000000000001F000L;
    private static final long datacenterMask = 0x00000000003E0000L;
    private static final long timestampMask = 0xFFFFFFFFFFC00000L;

    class EasyTimeWorker extends IdWorker {
        public List<Long> queue = Lists.newArrayList();

        public EasyTimeWorker(final Integer workerId, final Integer datacenterId) {
            super(workerId, datacenterId);
        }

        public void addTimestamp(final Long timestamp) {
            queue.add(timestamp);
        }

        public Long timeMaker() {
            return queue.remove(0);
        }

        @Override
        protected long timeGen() {
            return timeMaker();
        }
    }

    class WakingIdWorker extends EasyTimeWorker {
        public int slept = 0;

        public WakingIdWorker(final Integer workerId, final Integer datacenterId) {
            super(workerId, datacenterId);
        }

        @Override
        protected long tilNextMillis(final long lastTimestamp) {
            slept += 1;
            return super.tilNextMillis(lastTimestamp);
        }
    }

    class StaticTimeWorker extends IdWorker {
        public long time = 1L;

        public StaticTimeWorker(final Integer workerId,
                final Integer datacenterId) {
            super(workerId, datacenterId);
        }

        @Override
        protected long timeGen() {
            return time + twepoch;
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testInvalidWorkerId() {
        try {
            final Integer workerId = null;
            new IdWorker(workerId, 1);
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (NullPointerException e) {
        }

        try {
            new IdWorker(-1, 1);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
        }

        try {
            new IdWorker(32, 1);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testInvalidDatacenterId() {
        try {
            final Integer datacenterId = null;
            new IdWorker(1, datacenterId);
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (NullPointerException e) {
        }

        try {
            new IdWorker(1, -1);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
        }

        try {
            new IdWorker(1, 32);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGenerateId() throws Exception {
        final IdWorker worker = new IdWorker(1, 1);
        final Long id = worker.nextId();
        assertThat(id).isGreaterThan(0L);
    }

    @Test
    public void testAccurateTimestamp() throws Exception {
        final IdWorker worker = new IdWorker(1, 1);
        final Long time = System.currentTimeMillis();
        assertThat(worker.getTimestamp() - time).isLessThan(50L);
    }

    @Test
    public void testWorkerId() throws Exception {
        final IdWorker worker = new IdWorker(1, 1);
        assertThat(worker.getWorkerId()).isEqualTo(1);
    }

    @Test
    public void testDatacenterId() throws Exception {
        final IdWorker worker = new IdWorker(1, 1);
        assertThat(worker.getDatacenterId()).isEqualTo(1);
    }

    @Test
    public void testMaskWorkerId() throws Exception {
        final Integer workerId = 0x1F;
        final Integer datacenterId = 0;
        final IdWorker worker = new IdWorker(workerId, datacenterId);
        for (int i = 0; i < 1000; i++) {
            Long id = worker.nextId();
            assertThat((id & workerMask) >> 12).isEqualTo(Long.valueOf(workerId));
        }
    }

    @Test
    public void testMaskDatacenterId() throws Exception {
        final Integer workerId = 0;
        final Integer datacenterId = 0x1F;
        final IdWorker worker = new IdWorker(workerId, datacenterId);
        final Long id = worker.nextId();
        assertThat((id & datacenterMask) >> 17).isEqualTo(Long.valueOf(datacenterId));
    }

    @Test
    public void testMaskTimestamp() throws Exception {
        final EasyTimeWorker worker = new EasyTimeWorker(31, 31);
        for (int i = 0; i < 100; i++) {
            Long timestamp = System.currentTimeMillis();
            worker.addTimestamp(timestamp);
            Long id = worker.nextId();
            assertThat((id & timestampMask) >> 22).isEqualTo(timestamp - IdWorker.twepoch);
        }
    }

    @Test
    public void testRollOverSequenceId() throws Exception {
        final Integer workerId = 4;
        final Integer datacenterId = 4;
        final Long startSequence = 0xFFFFFF - 20L;
        final Long endSequence = 0xFFFFFF + 20L;
        final IdWorker worker = new IdWorker(workerId, datacenterId,
                startSequence);

        for (Long i = startSequence; i < endSequence; i++) {
            Long id = worker.nextId();
            assertThat((id & workerMask) >> 12).isEqualTo(Long.valueOf(workerId));
        }
    }

    @Test
    public void testIncreasingIds() throws Exception {
        final IdWorker worker = new IdWorker(1, 1);
        Long lastId = 0L;
        for (int i = 0; i < 100; i++) {
            Long id = worker.nextId();
            assertThat(id).isGreaterThan(lastId);
            lastId = id;
        }
    }

    @Test
    public void testMillionIds() throws Exception {
        final IdWorker worker = new IdWorker(31, 31);
        final Long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            worker.nextId();
        }
        final Long endTime = System.currentTimeMillis();
        System.out.println(String.format(
                "generated 1000000 ids in %d ms, or %,.0f ids/second",
                (endTime - startTime), 1000000000.0 / (endTime - startTime)));
    }

    @Test
    public void testSleep() throws Exception {
        final WakingIdWorker worker = new WakingIdWorker(1, 1);
        worker.addTimestamp(2L);
        worker.addTimestamp(2L);
        worker.addTimestamp(3L);

        worker.setSequence(4095L);
        worker.nextId();
        worker.setSequence(4095L);
        worker.nextId();

        assertThat(worker.slept).isEqualTo(1);
    }

    @Test
    public void testGenerateUniqueIds() throws Exception {
        final IdWorker worker = new IdWorker(31, 31);
        final Set<Long> ids = Sets.newHashSet();
        final int count = 2000000;
        for (int i = 0; i < count; i++) {
            Long id = worker.nextId();
            if (ids.contains(id)) {
                System.out.println(Long.toBinaryString(id));
            }
            else {
                ids.add(id);
            }
        }
        assertThat(ids.size()).isEqualTo(count);
    }

    @Test
    public void testGenerateIdsOver50Billion() throws Exception {
        final IdWorker worker = new IdWorker(0, 0);
        assertThat(worker.nextId()).isGreaterThan(50000000000L);
    }

    @Test
    public void testUniqueIdsBackwardsTime() throws Exception {
        final long sequenceMask = -1L ^ (-1L << 12);
        final StaticTimeWorker worker = new StaticTimeWorker(0, 0);

        // first we generate 2 ids with the same time, so that we get the sequqence to 1
        assertThat(worker.getSequence()).isEqualTo(0L);
        assertThat(worker.time).isEqualTo(1L);

        final Long id1 = worker.nextId();
        assertThat(id1 >> 22).isEqualTo(1L);
        assertThat(id1 & sequenceMask).isEqualTo(0L);

        assertThat(worker.getSequence()).isEqualTo(0L);
        assertThat(worker.time).isEqualTo(1L);

        final Long id2 = worker.nextId();
        assertThat(id2 >> 22).isEqualTo(1L);
        assertThat(id2 & sequenceMask).isEqualTo(1L);

        // then we set the time backwards
        worker.time = 0L;
        assertThat(worker.getSequence()).isEqualTo(1L);

        try {
            worker.nextId();
            failBecauseExceptionWasNotThrown(InvalidSystemClock.class);
        }
        catch (InvalidSystemClock ex) {
            assertThat(worker.getSequence()).isEqualTo(1L);
        }

        worker.time = 1L;
        final Long id3 = worker.nextId();
        assertThat(id3 >> 22).isEqualTo(1L);
        assertThat(id3 & sequenceMask).isEqualTo(2L);
    }

    @Test
    public void testValidUserAgent() throws Exception {
        final IdWorker worker = new IdWorker(1, 1);
        assertTrue(worker.isValidUserAgent("infra-dm"));
    }

    @Test
    public void testInvalidUserAgent() throws Exception {
        final IdWorker worker = new IdWorker(1, 1);
        assertFalse(worker.isValidUserAgent("1"));
        assertFalse(worker.isValidUserAgent("1asdf"));
    }
}
