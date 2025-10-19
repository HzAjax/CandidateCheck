package ru.volodin.loader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.volodin.loader.model.Person;
import ru.volodin.loader.model.Result;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ProcessingService.class);

    private final ConcurrentLinkedQueue<Person> queue = new ConcurrentLinkedQueue<>();
    private final ExecutorService pool;
    private final VerifyHubClient client;

    /**
     * @param threads количество потоков-воркеров
     * @param client  HTTP-клиент для отправки данных
     */
    public ProcessingService(int threads, VerifyHubClient client) {
        this.pool = Executors.newFixedThreadPool(threads);
        this.client = client;
    }

    /**
     * Параллельно обрабатывает список персон:
     * - добавляет их в очередь,
     * - запускает воркеров,
     * - для каждого элемента валидирует и отправляет наружу,
     * - считает статистику выполнения.
     *
     * @param persons входной список персон
     * @return агрегированные результаты (принято/успешно/ошибки/остаток)
     */
    public Result process(List<Person> persons) {
        queue.addAll(persons);

        AtomicInteger accepted  = new AtomicInteger(persons.size());
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger errors    = new AtomicInteger(0);

        int workers = ((ThreadPoolExecutor) pool).getCorePoolSize();
        CountDownLatch latch = new CountDownLatch(workers);

        for (int i = 0; i < workers; i++) {
            pool.submit(() -> {
                try {
                    for (;;) {
                        Person p = queue.poll();
                        if (p == null) break;
                        try {
                            VerifyHubClient.validate(p);
                            String reqId = UUID.randomUUID().toString();
                            client.send(p, reqId);
                            processed.incrementAndGet();
                        } catch (Exception e) {
                            log.error("Processing error", e);
                            errors.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        pool.shutdown();

        return new Result(accepted.get(), processed.get(), errors.get(), queue.size());
    }
}
