/*
 * Copyright (c) 2009-2014, IETR/INSA of Rennes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

/**
 * Ring-buffer FIFO structure
 * Lock-free and cache-efficient implementation
 * Supports 1 producer - N consumers
 */
typedef struct {
    const int size;                         /** Size of the ringbuffer */
    const int nb_readers;                   /** Number of readers */
    T *contents;                            /** Buffer containing the FIFO's elements */
    index_s* reading;                       /** Reading positions */
    index_s writing;                        /** Writing position */
    volatile char padding[CACHELINE_SIZE];  /** Memory padding */
} FIFO_T(T);

static int FIFO_GET_NUM_TOKENS(T)(FIFO_T(T) *fifo, int reader_id) {
    return fifo->writing.index - fifo->reading[reader_id].index;
}

static int FIFO_GET_ROOM(T)(FIFO_T(T) *fifo, int num_readers) {
    int i;
    int num_tokens, max_num_tokens = 0;

    for (i = 0; i < num_readers; i++) {
        num_tokens = fifo->writing.index - fifo->reading[i].index;
        max_num_tokens = max_num_tokens > num_tokens ? max_num_tokens : num_tokens;
    }

    return fifo->size - max_num_tokens;
}

