/*
 * Copyright (c) 2010, IRISA
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
 *   * Neither the name of the IRISA nor the names of its
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
#ifndef GENETIC_H
#define GENETIC_H

struct monitor_s {
	struct sync_s *sync;
	struct genetic_s *genetic_info;
};

struct genetic_s {
	int population_size;
	int generation_nb;
	double keep_ratio;
	double crossover_ratio;
	struct actor_s **actors;
	struct scheduler_s *schedulers;
	int actors_nb;
	int threads_nb;
};

struct mapping_s {
	int *actors_per_threads;
	struct actor_s ***actors_mapping;
};

typedef struct gene_s {
	struct actor_s *actor;
	int mapped_core;
} gene;

typedef struct individual_s {
	gene **genes;
	float fps;
	float old_fps;
} individual;

typedef struct population_s {
	int generation_nb;
	individual **individuals;
} population;


void *monitor(void *data);
void genetic_init(struct genetic_s *genetic_info, int population_size, int generation_nb, double keep_ratio, double crossover_ratio, struct actor_s **actors, struct scheduler_s *schedulers, int actors_nb, int threads_nb);
void monitor_init(struct monitor_s *monitoring, struct sync_s *sync, struct genetic_s *genetic_info);
int clean_cache(int size);
		
extern float compute_partial_fps();
extern void backup_partial_start_info();
extern void backup_partial_end_info();
extern void active_fps_printing();
extern void clear_fifos();
extern void active_genetic();
extern void initialize_instances();
extern int is_timeout();

#endif
