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
#define _CRT_SECURE_NO_WARNINGS

#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <string.h>

#include "orcc_types.h"
#include "orcc_genetic.h"
#include "orcc_thread.h"
#include "orcc_scheduler.h"
#include "orcc_util.h"

///////////////////////////////////////////////////////////////////////////////
// Initializers
///////////////////////////////////////////////////////////////////////////////

/**
 * Initialize the given genetic structure.
 */
void genetic_init(struct genetic_s *genetic_info, int population_size,
		int generation_nb, double keep_ratio, double crossover_ratio,
		struct actor_s **actors, struct scheduler_s *schedulers, int actors_nb,
		int threads_nb, int use_ring_topology) {
	genetic_info->population_size = population_size;
	genetic_info->generation_nb = generation_nb;
	genetic_info->keep_ratio = keep_ratio;
	genetic_info->crossover_ratio = crossover_ratio;
	genetic_info->actors = actors;
	genetic_info->schedulers = schedulers;
	genetic_info->actors_nb = actors_nb;
	genetic_info->threads_nb = threads_nb;
	genetic_info->use_ring_topology = use_ring_topology;
}

/**
 * Initialize the given monitor structure.
 */
void monitor_init(struct monitor_s *monitoring, struct sync_s *sync,
		struct genetic_s *genetic_info) {
	monitoring->sync = sync;
	monitoring->genetic_info = genetic_info;
}

///////////////////////////////////////////////////////////////////////////////
// Various manipulations of structures
///////////////////////////////////////////////////////////////////////////////

/**
 * Compute a mapping from a given individual.
 */
static struct mapping_s* compute_mapping(individual *individual,
		struct genetic_s *genetic_info) {
	int i, j, k;
	struct mapping_s *mapping = allocate_mapping(genetic_info->threads_nb);

	for (i = 0; i < genetic_info->threads_nb; i++) {
		struct actor_s **actors_tmp = (struct actor_s**) malloc(
				genetic_info->actors_nb * sizeof(struct actor_s *));
		k = 0;

		for (j = 0; j < genetic_info->actors_nb; j++) {
			if (individual->genes[j]->mapped_core == i) {
				actors_tmp[k] = individual->genes[j]->actor;
				k++;
			}
		}

		mapping->partitions_of_actors[i] = (struct actor_s**) malloc(
				k * sizeof(struct actor_s*));
		memcpy(mapping->partitions_of_actors[i], actors_tmp,
				k * sizeof(struct actor_s*));
		mapping->partitions_size[i] = k;
		free(actors_tmp);
	}

	return mapping;
}

/**
 * Test the equality of two given individuals.
 */
static int individual_equal(individual* ind1, individual* ind2,
		struct genetic_s *genetic_info) {
	int i, equal = 1;
	for (i = 0; i < genetic_info->actors_nb && equal; i++) {
		equal = (ind1->genes[i] == ind2->genes[i]);
	}
	return equal;
}

/**
 * Check if a given individual is already contained in a given population.
 */
static int is_contained(individual* ind, population* pop, int size,
		struct genetic_s *genetic_info) {
	int i, contains = 0;
	for (i = 0; i < size && !contains; i++) {
		contains = individual_equal(ind, pop->individuals[i], genetic_info);
	}
	return contains;
}

/**
 * Clone a gene.
 */
static gene* copy_gene(gene *g) {
	gene *new_g = (gene*) malloc(sizeof(gene));
	new_g->actor = g->actor;
	new_g->mapped_core = g->mapped_core;
	return new_g;
}

/**
 * Clone an individual.
 */
static individual* copy_individual(individual *ind,
		struct genetic_s *genetic_info) {
	int i;
	individual *new_ind = (individual*) malloc(sizeof(individual));
	new_ind->genes = (gene**) malloc(genetic_info->actors_nb * sizeof(gene*));
	new_ind->fps = ind->fps;
	new_ind->old_fps = ind->old_fps;
	for (i = 0; i < genetic_info->actors_nb; i++) {
		new_ind->genes[i] = copy_gene(ind->genes[i]);
	}
	return new_ind;
}

/**
 * Compare the evaluations of two individuals.
 */
static int compare_individual_fps(void const *a, void const *b) {
	individual const **pi1 = (individual const**) a;
	individual const **pi2 = (individual const**) b;
	individual const *i1 = *pi1;
	individual const *i2 = *pi2;

	if (i2->fps < i1->fps) {
		return -1;
	} else if (i2->fps > i1->fps) {
		return 1;
	} else {
		return 0;
	}
}

/**
 * Create a constant individual with the given id.
 */
static individual* generate_constant_individual(struct genetic_s *genetic_info,
		int thread) {
	int i;
	individual* ind = (individual*) malloc(sizeof(individual));
	ind->genes = (gene**) malloc(genetic_info->actors_nb * sizeof(gene*));
	ind->fps = -1;
	ind->old_fps = -1;

	// Initialize genes randomly
	for (i = 0; i < genetic_info->actors_nb; i++) {
		ind->genes[i] = (gene*) malloc(sizeof(gene));
		ind->genes[i]->actor = genetic_info->actors[i];
		ind->genes[i]->mapped_core = thread;
	}

	return ind;
}

/**
 * Create a random individual.
 */
static individual* generate_random_individual(struct genetic_s *genetic_info) {
	int i;
	individual* ind = (individual*) malloc(sizeof(individual));
	ind->genes = (gene**) malloc(genetic_info->actors_nb * sizeof(gene*));
	ind->fps = -1;
	ind->old_fps = -1;

	// Initialize genes randomly
	for (i = 0; i < genetic_info->actors_nb; i++) {
		ind->genes[i] = (gene*) malloc(sizeof(gene));
		ind->genes[i]->actor = genetic_info->actors[i];
		ind->genes[i]->mapped_core = rand() % genetic_info->threads_nb;
	}

	return ind;
}

/**
 * Map some actors on available threads according to the given individual.
 */
static void map_actors_on_threads(individual *individual,
		struct genetic_s *genetic_info) {
	int i;
	struct mapping_s *mapping = compute_mapping(individual, genetic_info);

	for (i = 0; i < genetic_info->threads_nb; i++) {
		sched_reinit(&genetic_info->schedulers[i], mapping->partitions_size[i],
				mapping->partitions_of_actors[i],
				genetic_info->use_ring_topology, genetic_info->threads_nb);
	}

	delete_mapping(mapping, 0);
}

/**
 * Release the given population structure.
 */
static void destroy_population(population *pop, struct genetic_s *genetic_info) {
	int i, j;
	for (i = 0; i < genetic_info->population_size; i++) {
		for (j = 0; j < genetic_info->actors_nb; j++) {
			free(pop->individuals[i]->genes[j]);
		}
		free(pop->individuals[i]);
	}
	free(pop);
}

///////////////////////////////////////////////////////////////////////////////
// I/O functions
///////////////////////////////////////////////////////////////////////////////

/**
 * Allocate and read a parameter-sized table to clean processor cache.
 */
int clean_cache(int size) {
	int i, res = 0;
	int *table = (int*) malloc(size * sizeof(int));
	memset(table, 0, size);
	for (i = 0; i < size; i++) {
		res += table[i];
	}
	free(table);
	return res;
}

/**
 * Write the header of a mapping file.
 */
static void initialize_mapping_file() {
	// Open file
	FILE *mappingFile;
	if (output_genetic == NULL) {
		mappingFile = fopen("genetic_mapping.xcf", "w");
	} else {
		mappingFile = fopen(output_genetic, "w");
	}
	if (mappingFile == NULL) {
		perror("I/O error during opening of mapping file.");
	}

	// Write header
	fprintf(mappingFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	fprintf(mappingFile, "<!-- GENETIC ALGORITHM -->\n\n");

	// Close file
	fclose(mappingFile);
}

/**
 * Write a mapping in a file.
 */
static void write_mapping(population *pop, struct genetic_s *genetic_info) {
	int i, j, k;
	FILE *mappingFile;

	// Open file
	if (output_genetic == NULL) {
		mappingFile = fopen("genetic_mapping.xcf", "a");
	} else {
		mappingFile = fopen(output_genetic, "a");
	}
	if (mappingFile == NULL) {
		perror("I/O error during opening of mapping file.");
	}

	fprintf(mappingFile, "<!-- POPULATION N°%i -->\n", pop->generation_nb);

	for (i = 0; i < genetic_info->population_size; i++) {
		struct mapping_s* mapping = compute_mapping(pop->individuals[i],
				genetic_info);
		fprintf(mappingFile,
				"<Configuration pop-id=\"%i\" ind-id=\"%i\" fps=\"%f\">\n",
				pop->generation_nb, i, pop->individuals[i]->fps);
		fprintf(mappingFile, "\t<Partitioning>\n");

		for (j = 0; j < genetic_info->threads_nb; j++) {
			fprintf(mappingFile, "\t\t<Partition id=\"%i\">\n", j);
			for (k = 0; k < mapping->partitions_size[j]; k++) {
				fprintf(mappingFile, "\t\t\t<Instance id=\"%s\"/>\n",
						mapping->partitions_of_actors[j][k]->name);
			}
			fprintf(mappingFile, "\t\t</Partition>\n");
		}

		fprintf(mappingFile, "\t</Partitioning>\n");
		fprintf(mappingFile, "</Configuration>\n\n");

		delete_mapping(mapping, 1);
	}

	// Close file
	fclose(mappingFile);
}

/**
 * Print a mapping.
 */
static void print_mapping(individual *ind, struct genetic_s *genetic_info) {
	int i;
	printf("{");
	for (i = 0; i < genetic_info->actors_nb; i++) {
		printf("%i", ind->genes[i]->mapped_core);
		if (i != genetic_info->actors_nb - 1) {
			printf(", ");
		}
	}
	printf("}\n");
}

/**
 * Print the actor list of an individual.
 */
static void print_actor_list(individual *ind, struct genetic_s *genetic_info) {
	int i;
	printf("Actor list =\n");
	for (i = 0; i < genetic_info->actors_nb; i++) {
		printf("%i -> %s\n", i, ind->genes[i]->actor->name);
	}
	printf("\n\n");

}

///////////////////////////////////////////////////////////////////////////////
// Genetic functions
///////////////////////////////////////////////////////////////////////////////

/**
 * Select an individual from a population with a one-turn tournament.
 */
static individual* selection(population *pop, struct genetic_s *genetic_info) {
	individual* i1 = pop->individuals[rand() % genetic_info->population_size];
	individual* i2 = pop->individuals[rand() % genetic_info->population_size];
	if (i1->fps >= i2->fps) {
		return i1;
	} else {
		return i2;
	}
}

/**
 * Create two new individuals from crossover of two parents.
 */
static void crossover(individual **children, individual **parents,
		struct genetic_s *genetic_info) {
	int i, cut = rand() % (genetic_info->actors_nb - 1) + 1;

	children[0] = (individual*) malloc(sizeof(individual));
	children[1] = (individual*) malloc(sizeof(individual));
	children[0]->genes = (gene**) malloc(
			genetic_info->actors_nb * sizeof(gene*));
	children[1]->genes = (gene**) malloc(
			genetic_info->actors_nb * sizeof(gene*));
	children[0]->fps = -1;
	children[0]->old_fps = -1;
	children[1]->fps = -1;
	children[1]->old_fps = -1;

	for (i = 0; i < genetic_info->actors_nb; i++) {
		if (i < cut) {
			children[0]->genes[i] = copy_gene(parents[0]->genes[i]);
			children[1]->genes[i] = copy_gene(parents[1]->genes[i]);
		} else {
			children[0]->genes[i] = copy_gene(parents[1]->genes[i]);
			children[1]->genes[i] = copy_gene(parents[0]->genes[i]);
		}
	}
}

/**
 * Create a mutated individual from an original one.
 */
static individual* mutation(individual *original,
		struct genetic_s *genetic_info) {
	individual *mutated = (individual*) malloc(sizeof(individual));
	int i, mutated_index = rand() % genetic_info->actors_nb;

	mutated->genes = (gene**) malloc(genetic_info->actors_nb * sizeof(gene*));
	mutated->fps = -1;
	mutated->old_fps = -1;

	for (i = 0; i < genetic_info->actors_nb; i++) {
		mutated->genes[i] = copy_gene(original->genes[i]);
		if (i == mutated_index) {
			mutated->genes[i]->mapped_core = rand() % genetic_info->threads_nb;
		}
	}

	return mutated;
}

/**
 * Compute the next population.
 */
static population* compute_next_population(population *pop,
		struct genetic_s *genetic_info) {
	int i, free, last;

	// Allocate memory to store the new population
	population *next_pop = (population*) malloc(sizeof(population));
	individual **individuals = (individual**) malloc(
			genetic_info->population_size * sizeof(individual*));

	// Initialize population fields
	next_pop->individuals = individuals;
	next_pop->generation_nb = pop->generation_nb + 1;

	// Sort population by descending fps value
	qsort(pop->individuals, genetic_info->population_size, sizeof(individual*),
			compare_individual_fps);

	// Backup better individuals
	for (i = 0; i < genetic_info->population_size * genetic_info->keep_ratio; i++) {
		next_pop->individuals[i] = copy_individual(pop->individuals[i],
				genetic_info);
		next_pop->individuals[i]->old_fps = next_pop->individuals[i]->fps;
		next_pop->individuals[i]->fps = -1;
	}

	// Crossover
	free = genetic_info->population_size - i;
	last = ((int) (free * genetic_info->crossover_ratio)) + i;
	if ((last - free) % 2 == 1) {
		last = last - 1;
	}
	for (; i < last; i = i + 2) {
		individual *children[2];
		individual *parents[2];

		do {
			parents[0] = selection(pop, genetic_info);
			parents[1] = selection(pop, genetic_info);

			crossover(children, parents, genetic_info);
		} while (is_contained(children[0], next_pop, i, genetic_info)
				|| is_contained(children[1], next_pop, i, genetic_info));

		next_pop->individuals[i] = children[0];
		next_pop->individuals[i + 1] = children[1];
	}

	// Mutation
	for (; i < genetic_info->population_size; i++) {
		do {
			next_pop->individuals[i] = mutation(selection(pop, genetic_info),
					genetic_info);
		} while (is_contained(next_pop->individuals[i], next_pop, i,
				genetic_info));
	}

	destroy_population(pop, genetic_info);

	return next_pop;
}

/**
 * Create the first population.
 */
static population* initialize_population(struct genetic_s *genetic_info) {
	int i;

	// Allocate memory to store the new population
	population *pop = (population*) malloc(sizeof(population));
	individual **individuals = (individual**) malloc(
			genetic_info->population_size * sizeof(individual*));

	// Initialize population fields
	pop->individuals = individuals;
	pop->generation_nb = 0;

	// Initialize random function
	srand((unsigned) time(NULL));

	// Initialize first generation of individuals (constant and random)
	for (i = 0; (i < genetic_info->population_size) && (i
			< genetic_info->threads_nb); i++) {
		pop->individuals[i] = generate_constant_individual(genetic_info, i);
	}
	for (i = genetic_info->threads_nb; i < genetic_info->population_size; i++) {
		do {
			pop->individuals[i] = generate_random_individual(genetic_info);
		} while (is_contained(pop->individuals[i], pop, i, genetic_info));
	}

	return pop;
}

/**
 * Main routine of the genetic algorithm.
 */
void *monitor(void *data) {
	struct monitor_s *monitoring = (struct monitor_s *) data;
	int i, evalIndNb = 0;
	population *population;

	// Set native actor in genetic mode
	remove_fps_printing();
	source_active_genetic();

	// Initialize population
	printf("\nGenerate initial population...\n\n");
	population = initialize_population(monitoring->genetic_info);
	initialize_mapping_file();

	print_actor_list(population->individuals[evalIndNb],
			monitoring->genetic_info);
	map_actors_on_threads(population->individuals[evalIndNb],
			monitoring->genetic_info);

	while (1) {
		print_mapping(population->individuals[evalIndNb],
				monitoring->genetic_info);

		// Start timer and counter
		backup_partial_start_info();

		// wakeup all threads
		for (i = 0; i < monitoring->genetic_info->threads_nb; i++) {
			semaphore_set(monitoring->genetic_info->schedulers[i].sem_thread);
		}

		// wait threads synchro
		for (i = 0; i < monitoring->genetic_info->threads_nb; i++) {
			semaphore_wait(monitoring->sync->sem_monitor);
		}

		// Stop timer and counter
		backup_partial_end_info();

		population->individuals[evalIndNb]->fps = compute_partial_fps();

		// Print evaluation results
		printf("Evaluation of mapping %i = ", evalIndNb);
		if (is_timeout()) {
			printf("TIMEOUT");
		} else {
			printf("%f fps", population->individuals[evalIndNb]->fps);
		}
		if (population->individuals[evalIndNb]->old_fps == -1) {
			printf("\n");
		} else {
			printf(" (old = %f fps)\n",
					population->individuals[evalIndNb]->old_fps);
		}

		evalIndNb++;

		if (evalIndNb == monitoring->genetic_info->population_size) {
			if (population->generation_nb
					== monitoring->genetic_info->generation_nb - 1) {
				break;
			} else {
				write_mapping(population, monitoring->genetic_info);
				printf("\nCompute next generation...\n\n");
				population = compute_next_population(population,
						monitoring->genetic_info);
				evalIndNb = 0;
			}
		}
		map_actors_on_threads(population->individuals[evalIndNb],
				monitoring->genetic_info);

		source_close();
		clear_fifos();
		initialize_instances();
	}
	exit(0);

	return NULL;
}


