package org.observertc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

class CollectionChunkerTest {

    @Test
    void shouldChunkWithoutOverflow_1() {
        var source = this.generateSource(10, 10);
        var chunker = CollectionChunker.<String>builder()
                .setLimit(20)
                .setSizeFn(str -> str.length())
                .setCanOverflowFlag(false)
                .build();
        var iterated = 0;
        var maxChunkSize = 0;
        var maxChunkItemsLength = 0;
        for (var it = chunker.iterate(source); it.hasNext(); ++iterated) {
            var chunk = it.next();
            var itemsLength = chunk.stream().mapToInt(String::length).sum();
            maxChunkSize = Math.max(maxChunkSize, chunk.size());
            maxChunkItemsLength = Math.max(maxChunkItemsLength, itemsLength);
        }
        Assertions.assertTrue(1 == maxChunkSize);
        Assertions.assertTrue(10 == maxChunkItemsLength);
        Assertions.assertTrue(0 < iterated);
    }

    @Test
    void shouldChunkWithoutOverflow_2() {
        var source = this.generateSource(10, 10);
        var chunker = CollectionChunker.<String>builder()
                .setLimit(21)
                .setSizeFn(str -> str.length())
                .setCanOverflowFlag(false)
                .build();
        var iterated = 0;
        var maxChunkSize = 0;
        var maxChunkItemsLength = 0;
        for (var it = chunker.iterate(source); it.hasNext(); ++iterated) {
            var chunk = it.next();
            var itemsLength = chunk.stream().mapToInt(String::length).sum();
            maxChunkSize = Math.max(maxChunkSize, chunk.size());
            maxChunkItemsLength = Math.max(maxChunkItemsLength, itemsLength);
        }
        Assertions.assertTrue(2 == maxChunkSize);
        Assertions.assertTrue(20 == maxChunkItemsLength);
        Assertions.assertTrue(0 < iterated);
    }

    @Test
    void shouldChunkWithOverflow_1() {
        var source = this.generateSource(10, 10);
        var chunker = CollectionChunker.<String>builder()
                .setLimit(20)
                .setSizeFn(str -> str.length())
                .setCanOverflowFlag(true)
                .build();
        var iterated = 0;
        var maxChunkSize = 0;
        var maxChunkItemsLength = 0;
        for (var it = chunker.iterate(source); it.hasNext(); ++iterated) {
            var chunk = it.next();
            var itemsLength = chunk.stream().mapToInt(String::length).sum();
            maxChunkSize = Math.max(maxChunkSize, chunk.size());
            maxChunkItemsLength = Math.max(maxChunkItemsLength, itemsLength);
        }
        Assertions.assertTrue(2 == maxChunkSize);
        Assertions.assertTrue(20 == maxChunkItemsLength);
        Assertions.assertTrue(0 < iterated);
    }

    @Test
    void shouldChunkWithOverflow_2() {
        var source = this.generateSource(10, 10);
        var chunker = CollectionChunker.<String>builder()
                .setLimit(21)
                .setSizeFn(str -> str.length())
                .setCanOverflowFlag(true)
                .build();
        var iterated = 0;
        var maxChunkSize = 0;
        var maxChunkItemsLength = 0;
        for (var it = chunker.iterate(source); it.hasNext(); ++iterated) {
            var chunk = it.next();
            var itemsLength = chunk.stream().mapToInt(String::length).sum();
            maxChunkSize = Math.max(maxChunkSize, chunk.size());
            maxChunkItemsLength = Math.max(maxChunkItemsLength, itemsLength);
        }
        Assertions.assertTrue(3 == maxChunkSize);
        Assertions.assertTrue(21 < maxChunkItemsLength);
        Assertions.assertTrue(0 < iterated);
    }


    private Collection<String> generateSource(int collectionSize, int targetStringLength) {
        var result = new LinkedList<String>();
        var random = new Random();
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'

        for (var i = 0; i < collectionSize; ++i) {
            String item = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            result.add(item);
        }
        return result;
    }
}