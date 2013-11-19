/*
 * Copyright (c) 2013, INSA of Rennes
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
 *   * Neither the name of the INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIREnum_inputsCT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#ifndef ORCC_DATAFLOW_H
#define ORCC_DATAFLOW_H

typedef struct scheduler_s scheduler_t;
typedef struct schedinfo_s schedinfo_t;

/*
 * Actors are the vertices of orcc Networks
 */
typedef struct actor_s {
    char *name;
    int group; /** id of his group. */
    void (*init_func)();
    void (*reinit_func)();
    void (*sched_func)(schedinfo_t *);
    int num_inputs; /** number of input ports */
    int num_outputs; /** number of output ports */
    int in_list; /** set to 1 when the actor is in the schedulable list. Used by add_schedulable to do the membership test in O(1). */
    int in_waiting; /** idem with the waiting list. */
    struct scheduler_s *sched; /** scheduler which execute this actor. */
    int processor_id; /** id of the processor core mapped to this actor. */
    int id;
    double workload; /** actor's workload gived by instrumention */
} actor_t;

/*
 * Connections are the edges of orcc Networks
 */
typedef struct connection_s {
    actor_t *src;
    actor_t *dst;
    double workload;
} connection_t;

/*
 * Orcc Networks are directed graphs
 */
typedef struct network_s {
    char *name;
    actor_t **actors;
    connection_t **connections;
    int nb_actors;
    int nb_connections;
} network_t;

/**
 * Find actor by its name in the given table.
 */
actor_t *find_actor_by_name(actor_t **actors, char *name, int nb_actors);

#endif