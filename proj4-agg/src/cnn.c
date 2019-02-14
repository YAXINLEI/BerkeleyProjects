#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <assert.h>
#include <unistd.h>
#include <stdint.h>
#include <string.h>
#include "timestamp.c"

// Include SSE intrinsics
#if defined(_MSC_VER)
#include <intrin.h>
#elif defined(__GNUC__) && (defined(__x86_64__) || defined(__i386__))
#include <immintrin.h>
#include <x86intrin.h>
#endif

// Include OpenMP
#include <omp.h>

// Vol ------------------------------------------------------------------------

// Volumes are used to represent the activations (i.e., state) between the
// different layers of the CNN. They all have three dimensions. The inter-
// pretation of their content depends on the layer that produced them. Before
// the first iteration, the Volume holds the data of the image we want to
// classify (the depth are the three color dimensions). After the last stage
// of the CNN, the Volume holds the probabilities that an image is part of
// a specific category.

/*
 * Represents a three-dimensional array of numbers, and its size. The numbers
 * at (x,y,d) are stored in array w at location ((v->sx * y)+x)*v->depth+d.
 */

typedef struct vol {
  uint64_t sx,sy,depth;
  double* w;
} vol_t;

/*
 * Set the value at a specific entry of the array.
 */

static inline double get_vol(vol_t* v, int x, int y, int d) {
  return v->w[((v->sx * y)+x)*v->depth+d];
}

/*
 * Get the value at a specific entry of the array.
 */

static inline void set_vol(vol_t* v, int x, int y, int d, double val) {
  v->w[((v->sx * y)+x)*v->depth+d] = val;
}

/*
 * Allocate a new array with specific dimensions and default value v.
 */

static vol_t* make_vol(int sx, int sy, int d, double v) {
  vol_t* out = (vol_t*)malloc(sizeof(struct vol));
  out->w = (double*)malloc(sizeof(double)*(sx*sy*d));
  out->sx = sx;
  out->sy = sy;
  out->depth = d;

  for (int x = 0; x < sx; x++)
    for (int y = 0; y < sy; y++)
      for (int z = 0; z < d; z++)
        set_vol(out, x, y, z, v);

  return out;
}

/*
 * Copy the contents of one Volume to another (assuming same dimensions).
 */

static vol_t* copy_vol(vol_t* dest, vol_t* src) {
  for (int x = 0; x < dest->sx; x++)
    for (int y = 0; y < dest->sy; y++)
      for (int z = 0; z < dest->depth; z++)
        set_vol(dest, x, y, z, get_vol(src, x, y, z));
}

/*
 * Deallocate the array.
 */
void free_vol(vol_t* v) {
  free(v->w);
  free(v);
}

// A note about layers --------------------------------------------------------

/*
 * What follows are the different layers of the CNN. You will not have to
 * understand what these layers are actually doing. In general terms, each
 * layer performs a "forward" operation on a batch of inputs. During this
 * forward operation, the layer takes a set of input Volumes and transforms
 * them into a set of output Volumes (one output for each input). What differs
 * is the operation performed by each layer.
 *
 * In addition to the _forward function, each layer also provides a data
 * structure, holding (fixed) parameters for that layer, a make_ function to
 * allocate an instance of the layer with a particular set of parameters and
 * a load function to load training data for that layer from a file. Note that
 * you will not have to make any changes to any of these functions. The only
 * function you need to consider is the _forward function.
 */

// Convolutional Layer --------------------------------------------------------

typedef struct conv_layer {
  // required
  int out_depth;
  int sx;
  int in_depth;
  int in_sx;
  int in_sy;

  // optional
  int sy;
  int stride;
  int pad;
  double l1_decay_mul;
  double l2_decay_mul;

  // computed
  int out_sx;
  int out_sy;
  double bias;
  vol_t* biases;
  vol_t** filters;
} conv_layer_t;

conv_layer_t* make_conv_layer(int in_sx, int in_sy, int in_depth,
                              int sx, int filters, int stride, int pad) {
  conv_layer_t* l = (conv_layer_t*)malloc(sizeof(conv_layer_t));

  // required
  l->out_depth = filters;
  l->sx = sx;
  l->in_depth = in_depth;
  l->in_sx = in_sx;
  l->in_sy = in_sy;
    
  // optional
  l->sy = l->sx;
  l->stride = stride;
  l->pad = pad;
  l->l1_decay_mul = 0.0;
  l->l2_decay_mul = 1.0;

  // computed
  l->out_sx = floor((l->in_sx + l->pad * 2 - l->sx) / l->stride + 1);
  l->out_sy = floor((l->in_sy + l->pad * 2 - l->sy) / l->stride + 1);

  l->filters = (vol_t**)malloc(sizeof(vol_t*)*filters);
  for (int i = 0; i < filters; i++) {
    l->filters[i] = make_vol(l->sx, l->sy, l->in_depth, 0.0);
  }

  l->bias = 0.0;
  l->biases = make_vol(1, 1, l->out_depth, l->bias);

  return l;
}





void conv_forward_3(conv_layer_t* l, vol_t** in, vol_t** out, int start, int end) {
  
  __m256d add_1 = _mm256_setzero_pd();
  __m256d add_2 = _mm256_setzero_pd();
  int y;
  int x;
  // #pragma omp parallel for private(add_1, add_2)
  for (int i = start; i <= end; i++) {
    vol_t* V = in[i];
    vol_t* A = out[i];
        
    // int V_sx = V->sx;
    // int V_sy = V->sy;
    // int V_depth = V->depth;

      for(int d = 0; d < 16; d++) {
      vol_t* f = l->filters[d];
      // int x = -l->pad;
      y = -2;
        for(int ay = 0; ay < 32; y++, ay++) {
          x = -2;
          for(int ax=0; ax < 32; x++, ax++) {
            double a = 0.0;
            for(int fy = 0; fy < 5; fy++) {
              int oy = y + fy;
              if(oy >= 0 && oy < 32){
                int V_sx_oy = 32 * oy;
              for(int fx = 0; fx < 5; fx++) {
                int ox = x + fx;
                if(ox >= 0 && ox < 32) {
                  a += f->w[(5 * fy + fx)*3] * V->w[(V_sx_oy + ox)*3];
                  a += f->w[(5 * fy + fx)*3+1] * V->w[(V_sx_oy + ox)*3+1];
                  a += f->w[(5 * fy + fx)*3+2] * V->w[(V_sx_oy + ox)*3+2];
                  }
                }
              }
            }
            a += l->biases->w[d];
            set_vol(A, ax, ay, d, a);
        }
      }
    }
  }
}






void conv_forward_16(conv_layer_t* l, vol_t** in, vol_t** out, int start, int end) {
  
  __m256d add_1 = _mm256_setzero_pd();
  __m256d add_2 = _mm256_setzero_pd();
  // #pragma omp parallel for private(add_1, add_2)
  for (int i = start; i <= end; i++) {
    vol_t* V = in[i];
    vol_t* A = out[i];
        
    // int V_sx = V->sx;
    // int V_sy = V->sy;
    int y;
    // int V_depth = V->depth;
      for(int d = 0; d < 20; d++) {
      vol_t* f = l->filters[d];
      // int x = -l->pad;
      y = -2;
      int x;
        double result[4];
        for(int ay = 0; ay < 16; y++, ay++) {
          int x = -2;
          for(int ax=0; ax < 16; x++, ax++) {
            double a = 0.0;
            __m256d sum = _mm256_setzero_pd();
            for(int fy = 0; fy < 5; fy++) {
              int oy = y + fy;
              if(oy >= 0 && oy < 16){
                int f_sx_fy = 5 * fy;
              for(int fx = 0; fx < 5; fx++) {
                int ox = x + fx;
                if(ox >=0 && ox < 16) {
                  double* loc_f = f->w + (f_sx_fy + fx)*16;
                  double* loc_V = V->w + ((16 * oy)+ox)*16;

                  add_1 = _mm256_loadu_pd(loc_f);
                  add_2 = _mm256_loadu_pd(loc_V);
                  sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum);

                  add_1 = _mm256_loadu_pd(loc_f+4);
                  add_2 = _mm256_loadu_pd(loc_V+4);
                  sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum);

                  add_1 = _mm256_loadu_pd(loc_f+8);
                  add_2 = _mm256_loadu_pd(loc_V+8);
                  sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum);

                  add_1 = _mm256_loadu_pd(loc_f+12);
                  add_2 = _mm256_loadu_pd(loc_V+12);
                  sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum);
                }
              }
            }
            }
            _mm256_store_pd(result, sum);
            a += result[0];
            a += result[1];
            a += result[2];
            a += result[3];    

            a += l->biases->w[d];
            set_vol(A, ax, ay, d, a);
          }
        }
      }
  }
}







void conv_forward_20(conv_layer_t* l, vol_t** in, vol_t** out, int start, int end) {

  __m256d add_1 = _mm256_setzero_pd();
  __m256d add_2 = _mm256_setzero_pd();
  for (int i = start; i <= end; i++) {
    vol_t* V = in[i];
    vol_t* A = out[i]; 
    // int V_sx = V->sx;
    // int V_sy = V->sy;
    int y;
    // int V_depth = V->depth;

      for(int d = 0; d < 20; d++) {
      vol_t* f = l->filters[d];
      // int x = -l->pad;
      y = -2;
      int x;
        double result[4];
        for(int ay = 0; ay < 8; y++, ay++) {
          int x = -2;
          for(int ax=0; ax < 8; x++, ax++) {
            double a = 0.0;
            __m256d sum = _mm256_setzero_pd();
            for(int fy = 0; fy < 5; fy++) {
              int oy = y + fy;
              if(oy >= 0 && oy < 8){
                int f_sx_fy = 5 * fy;
              for(int fx = 0; fx < 5; fx++) {
                int ox = x + fx;
                if(ox >=0 && ox < 8) {
                  double* loc_f = f->w + (f_sx_fy+fx)*20;
                  double* loc_V = V->w + ((8 * oy)+ox)*20;

                    add_1 = _mm256_loadu_pd(loc_f);
                    add_2 = _mm256_loadu_pd(loc_V);
                    sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum);

                    add_1 = _mm256_loadu_pd(loc_f+4);
                    add_2 = _mm256_loadu_pd(loc_V+4);
                    sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum);

                    add_1 = _mm256_loadu_pd(loc_f+8);
                    add_2 = _mm256_loadu_pd(loc_V+8);
                    sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum);

                    add_1 = _mm256_loadu_pd(loc_f+12);
                    add_2 = _mm256_loadu_pd(loc_V+12);
                    sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum);

                    add_1 = _mm256_loadu_pd(loc_f+16);
                    add_2 = _mm256_loadu_pd(loc_V+16);
                    sum = _mm256_add_pd(_mm256_mul_pd(add_1, add_2), sum); 
                  }
                }
              }
            }
            _mm256_store_pd(result, sum);

            a += result[0];
            a += result[1];
            a += result[2];
            a += result[3];  

            a += l->biases->w[d];
            set_vol(A, ax, ay, d, a);
          }
        }
      }
    }
}






void conv_load(conv_layer_t* l, const char* fn) {
  int sx, sy, depth, filters;

  FILE* fin = fopen(fn, "r");

  fscanf(fin, "%d %d %d %d", &sx, &sy, &depth, &filters);
  assert(sx == l->sx);
  assert(sy == l->sy);
  assert(depth == l->in_depth);
  assert(filters == l->out_depth);

  for(int d = 0; d < l->out_depth; d++)
    for (int x = 0; x < sx; x++)
      for (int y = 0; y < sy; y++)
        for (int z = 0; z < depth; z++) {
          double val;
          fscanf(fin, "%lf", &val);
          set_vol(l->filters[d], x, y, z, val);
        }

  for(int d = 0; d < l->out_depth; d++) {
    double val;
    fscanf(fin, "%lf", &val);
    set_vol(l->biases, 0, 0, d, val);
  }

  fclose(fin);
}

// Relu Layer -----------------------------------------------------------------

typedef struct relu_layer {
  // required
  int in_depth;
  int in_sx;
  int in_sy;

  // computed
  int out_depth;
  int out_sx;
  int out_sy;
} relu_layer_t;

relu_layer_t* make_relu_layer(int in_sx, int in_sy, int in_depth) {
  relu_layer_t* l = (relu_layer_t*)malloc(sizeof(relu_layer_t));

  // required
  l->in_depth = in_depth;
  l->in_sx = in_sx;
  l->in_sy = in_sy;

  // computed
  l->out_sx = l->in_sx;
  l->out_sy = l->in_sy;
  l->out_depth = l->in_depth;

  return l;
}

void relu_forward(relu_layer_t* l, vol_t** in, vol_t** out, int start, int end) {
  for (int j = start; j <= end; j++) {
    for (int i = 0; i < l->in_sx*l->in_sy*l->in_depth; i++) {
      out[j]->w[i] = (in[j]->w[i] < 0.0) ? 0.0 : in[j]->w[i];
    }
  }
}

// Pool Layer -----------------------------------------------------------------

typedef struct pool_layer {
  // required
  int sx;
  int in_depth;
  int in_sx;
  int in_sy;

  // optional
  int sy;
  int stride;
  int pad;

  // computed
  int out_depth;
  int out_sx;
  int out_sy;
} pool_layer_t;

pool_layer_t* make_pool_layer(int in_sx, int in_sy, int in_depth,
                              int sx, int stride) {
  pool_layer_t* l = (pool_layer_t*)malloc(sizeof(pool_layer_t));

  // required
  l->sx = sx;
  l->in_depth = in_depth;
  l->in_sx = in_sx;
  l->in_sy = in_sy;

  // optional
  l->sy = l->sx;
  l->stride = stride;
  l->pad = 0;

  // computed
  l->out_depth = in_depth;
  l->out_sx = floor((l->in_sx + l->pad * 2 - l->sx) / l->stride + 1);
  l->out_sy = floor((l->in_sy + l->pad * 2 - l->sy) / l->stride + 1);

  return l;
}

void pool_forward(pool_layer_t* l, vol_t** in, vol_t** out, int start, int end) {
  for (int i = start; i <= end; i++) {
    vol_t* V = in[i];
    vol_t* A = out[i];
        
    int n=0;
    for(int d=0;d<l->out_depth;d++) {
      int x = -l->pad;
      int y = -l->pad;
      for(int ax=0; ax<l->out_sx; x+=l->stride,ax++) {
        y = -l->pad;
        for(int ay=0; ay<l->out_sy; y+=l->stride,ay++) {
  
          double a = -99999;
          for(int fx=0;fx<l->sx;fx++) {
            for(int fy=0;fy<l->sy;fy++) {
              int oy = y+fy;
              int ox = x+fx;
              if(oy>=0 && oy<V->sy && ox>=0 && ox<V->sx) {
                double v = get_vol(V, ox, oy, d);
                if(v > a) { a = v; }
              }
            }
          }
          n++;
          set_vol(A, ax, ay, d, a);
        }
      }
    }
  }
}

// FC Layer -------------------------------------------------------------------

typedef struct fc_layer {
  // required
  int out_depth;
  int in_depth;
  int in_sx;
  int in_sy;

  // optional
  double l1_decay_mul;
  double l2_decay_mul;

  // computed
  int out_sx;
  int out_sy;
  int num_inputs;
  double bias;
  vol_t* biases;
  vol_t** filters;
} fc_layer_t;

fc_layer_t* make_fc_layer(int in_sx, int in_sy, int in_depth,
                          int num_neurons) {
  fc_layer_t* l = (fc_layer_t*)malloc(sizeof(fc_layer_t));

  // required
  l->out_depth = num_neurons;
  l->in_depth = in_depth;
  l->in_sx = in_sx;
  l->in_sy = in_sy;
    
  // optional
  l->l1_decay_mul = 0.0;
  l->l2_decay_mul = 1.0;

  // computed
  l->num_inputs = l->in_sx * l->in_sy * l->in_depth;
  l->out_sx = 1;
  l->out_sy = 1;

  l->filters = (vol_t**)malloc(sizeof(vol_t*)*num_neurons);
  for (int i = 0; i < l->out_depth; i++) {
    l->filters[i] = make_vol(1, 1, l->num_inputs, 0.0);
  }

  l->bias = 0.0;
  l->biases = make_vol(1, 1, l->out_depth, l->bias);

  return l;
}

void fc_forward(fc_layer_t* l, vol_t** in, vol_t** out, int start, int end) {
  for (int j = start; j <= end; j++) {
    vol_t* V = in[j];
    vol_t* A = out[j];
        
    for(int i=0;i<l->out_depth;i++) {
      double a = 0.0;
      for(int d=0;d<l->num_inputs;d++) {
        a += V->w[d] * l->filters[i]->w[d];
      }
      a += l->biases->w[i];
      A->w[i] = a;
    }
  }
}

void fc_load(fc_layer_t* l, const char* fn) {
  FILE* fin = fopen(fn, "r");

  int num_inputs;
  int out_depth;
  fscanf(fin, "%d %d", &num_inputs, &out_depth);
  assert(out_depth == l->out_depth);
  assert(num_inputs == l->num_inputs);

  for(int i = 0; i < l->out_depth; i++)
    for(int d = 0; d < l->num_inputs; d++) {
      double val;
      fscanf(fin, "%lf", &val);
      l->filters[i]->w[d] = val;
    }

  for(int i = 0; i < l->out_depth; i++) {
    double val;
    fscanf(fin, "%lf", &val);
    l->biases->w[i] = val;
  }

  fclose(fin);
}

// Softmax Layer --------------------------------------------------------------

// Maximum supported out_depth
#define MAX_ES 16

typedef struct softmax_layer {
  // required
  int in_depth;
  int in_sx;
  int in_sy;
  double* es; 

  // computed
  int out_depth;
  int out_sx;
  int out_sy;
} softmax_layer_t;

softmax_layer_t* make_softmax_layer(int in_sx, int in_sy, int in_depth) {
  softmax_layer_t* l = (softmax_layer_t*)malloc(sizeof(softmax_layer_t));

  // required
  l->in_depth = in_depth;
  l->in_sx = in_sx;
  l->in_sy = in_sy;

  // computed
  l->out_sx = 1;
  l->out_sy = 1;
  l->out_depth = l->in_sx * l->in_sy * l->in_depth;

  l->es = (double*)malloc(sizeof(double)*l->out_depth);

  return l;
}

void softmax_forward(softmax_layer_t* l, vol_t** in, vol_t** out, int start, int end) {
  double es[MAX_ES];

  for (int j = start; j <= end; j++) {
    vol_t* V = in[j];
    vol_t* A = out[j];
  
    // compute max activation
    double amax = V->w[0];
    for(int i=1;i<l->out_depth;i++) {
      if(V->w[i] > amax) amax = V->w[i];
    }
  
    // compute exponentials (carefully to not blow up)
    double esum = 0.0;
    for(int i=0;i<l->out_depth;i++) {
      double e = exp(V->w[i] - amax);
      esum += e;
      es[i] = e;
    }
  
    // normalize and output to sum to one
    for(int i=0;i<l->out_depth;i++) {
      es[i] /= esum;
      A->w[i] = es[i];
    }
  }
}

// Neural Network -------------------------------------------------------------

/*
 * This represents the CNN we will use in this project. It consists of 11
 * layers, which means that there are 12 volumes of data (where the first one
 * is the input data and the last one the classification result).
 */

#define LAYERS 11

typedef struct network {
  vol_t* v[LAYERS+1];
  conv_layer_t* l0;
  relu_layer_t* l1;
  pool_layer_t* l2;
  conv_layer_t* l3;
  relu_layer_t* l4;
  pool_layer_t* l5;
  conv_layer_t* l6;
  relu_layer_t* l7;
  pool_layer_t* l8;
  fc_layer_t* l9;
  softmax_layer_t* l10;
} network_t;

/*
 * Instantiate our specific CNN.
 */

network_t* make_network() {
  network_t* net = (network_t*)malloc(sizeof(network_t));
  net->v[0] = make_vol(32, 32, 3, 0.0);
  net->l0 = make_conv_layer(32, 32, 3, 5, 16, 1, 2);
  net->v[1] = make_vol(net->l0->out_sx, net->l0->out_sy, net->l0->out_depth, 0.0);
  net->l1 = make_relu_layer(net->v[1]->sx, net->v[1]->sy, net->v[1]->depth);
  net->v[2] = make_vol(net->l1->out_sx, net->l1->out_sy, net->l1->out_depth, 0.0);
  net->l2 = make_pool_layer(net->v[2]->sx, net->v[2]->sy, net->v[2]->depth, 2, 2);
  net->v[3] = make_vol(net->l2->out_sx, net->l2->out_sy, net->l2->out_depth, 0.0);
  net->l3 = make_conv_layer(net->v[3]->sx, net->v[3]->sy, net->v[3]->depth, 5, 20, 1, 2);
  net->v[4] = make_vol(net->l3->out_sx, net->l3->out_sy, net->l3->out_depth, 0.0);
  net->l4 = make_relu_layer(net->v[4]->sx, net->v[4]->sy, net->v[4]->depth);
  net->v[5] = make_vol(net->l4->out_sx, net->l4->out_sy, net->l4->out_depth, 0.0);
  net->l5 = make_pool_layer(net->v[5]->sx, net->v[5]->sy, net->v[5]->depth, 2, 2);
  net->v[6] = make_vol(net->l5->out_sx, net->l5->out_sy, net->l5->out_depth, 0.0);
  net->l6 = make_conv_layer(net->v[6]->sx, net->v[6]->sy, net->v[6]->depth, 5, 20, 1, 2);
  net->v[7] = make_vol(net->l6->out_sx, net->l6->out_sy, net->l6->out_depth, 0.0);
  net->l7 = make_relu_layer(net->v[7]->sx, net->v[7]->sy, net->v[7]->depth);
  net->v[8] = make_vol(net->l7->out_sx, net->l7->out_sy, net->l7->out_depth, 0.0);
  net->l8 = make_pool_layer(net->v[8]->sx, net->v[8]->sy, net->v[8]->depth, 2, 2);
  net->v[9] = make_vol(net->l8->out_sx, net->l8->out_sy, net->l8->out_depth, 0.0);
  net->l9 = make_fc_layer(net->v[9]->sx, net->v[9]->sy, net->v[9]->depth, 10);
  net->v[10] = make_vol(net->l9->out_sx, net->l9->out_sy, net->l9->out_depth, 0.0);
  net->l10 = make_softmax_layer(net->v[10]->sx, net->v[10]->sy, net->v[10]->depth);
  net->v[11] = make_vol(net->l10->out_sx, net->l10->out_sy, net->l10->out_depth, 0.0);
  return net;
}

/* 
 * Free our specific CNN.
 */

void free_network(network_t* net) {
  for (int i = 0; i < LAYERS+1; i++)
    free_vol(net->v[i]);

  free(net->l0);
  free(net->l1);
  free(net->l2);
  free(net->l3);
  free(net->l4);
  free(net->l5);
  free(net->l6);
  free(net->l7);
  free(net->l8);
  free(net->l9);
  free(net->l10);

  free(net);
}

/*
 * We organize data as "batches" of volumes. Each batch consists of a number of samples,
 * each of which contains a volume for every intermediate layer. Say we have L layers
 * and a set of N input images. Then batch[l][n] contains the volume at layer l for
 * input image n.
 *
 * By using batches, we can process multiple images at once in each run of the forward
 * functions of the different layers.
 */

typedef vol_t** batch_t;

/*
 * This function allocates a new batch for the network old_net with size images.
 */

batch_t* make_batch(network_t* old_net, int size) {
  batch_t* out = (batch_t*)malloc(sizeof(vol_t**)*(LAYERS+1));
  for (int i = 0; i < LAYERS+1; i++) {
    out[i] = (vol_t**)malloc(sizeof(vol_t*)*size);
    for (int j = 0; j < size; j++) {
      out[i][j] = make_vol(old_net->v[i]->sx, old_net->v[i]->sy, old_net->v[i]->depth, 0.0);
    }
  }

  return out;
}

/*
 * Free a previously allocated batch.
 */

void free_batch(batch_t* v, int size) {
  for (int i = 0; i < LAYERS+1; i++) {
    for (int j = 0; j < size; j++) {
      free_vol(v[i][j]);
    }
    free(v[i]);
  }
  free(v);
}

/*
 * Apply our network to a specific batch of inputs. The batch has to be given
 * as input to v and start/end are the first and the last image in that batch
 * to process (start and end are inclusive).
 */

uint64_t t_start = 0;
uint64_t t_final = 0;
uint64_t t1 = 0;
uint64_t t0 = 0;
uint64_t t_conv1 = 0;
uint64_t t_relu1 = 0;
uint64_t t_pool1 = 0;
uint64_t t_conv2 = 0;
uint64_t t_relu2 = 0;
uint64_t t_pool2 = 0;
uint64_t t_conv3 = 0;
uint64_t t_relu3 = 0;
uint64_t t_pool3 = 0;
uint64_t t_fc = 0;
uint64_t t_softmax = 0;
uint64_t t_total = 0;


void net_forward(network_t* net, batch_t* v, int start, int end) {

  t_start = timestamp_us();
  conv_forward_3(net->l0, v[0], v[1], start, end);
  t1 = timestamp_us();
  t_conv1 += t1 - t_start;

  t0 = timestamp_us();
  relu_forward(net->l1, v[1], v[2], start, end);
  t1 = timestamp_us();
  t_relu1 += t1 - t0;

  t0 = timestamp_us();
  pool_forward(net->l2, v[2], v[3], start, end);
  t1 = timestamp_us();
  t_pool1 += t1 - t0;

  t0 = timestamp_us();
  conv_forward_16(net->l3, v[3], v[4], start, end);
  t1 = timestamp_us();
  t_conv2 += t1 - t0;

  t0 = timestamp_us();
  relu_forward(net->l4, v[4], v[5], start, end);
  t1 = timestamp_us();
  t_relu2 += t1 - t0;

  t0 = timestamp_us();
  pool_forward(net->l5, v[5], v[6], start, end);
  t1 = timestamp_us();
  t_pool2 += t1 - t0;

  t0 = timestamp_us();
  conv_forward_20(net->l6, v[6], v[7], start, end);
  t1 = timestamp_us();
  t_conv3 += t1 - t0;

  t0 = timestamp_us();
  relu_forward(net->l7, v[7], v[8], start, end);
  t1 = timestamp_us();
  t_relu3 += t1 - t0;

  t0 = timestamp_us();
  pool_forward(net->l8, v[8], v[9], start, end);
  t1 = timestamp_us();
  t_pool3 += t1 - t0;

  t0 = timestamp_us();
  fc_forward(net->l9, v[9], v[10], start, end);
  t1 = timestamp_us();
  t_fc += t1 - t0;

  t0 = timestamp_us();
  softmax_forward(net->l10, v[10], v[11], start, end);
  t_final = timestamp_us();
  t_softmax += t_final - t0;


  t_total += t_final - t_start;
}

/*
 * Putting everything together: Take a set of n input images as 3-dimensional
 * Volumes and process them using the CNN in batches of 1. Then look at the
 * output (which is a set of 10 labels, each of which tells us the likelihood
 * of a specific category) and classify the image as a cat iff the likelihood
 * of "cat" is larger than 50%. Writes the cat likelihood for all images into
 * an output array (0 = definitely no cat, 1 = definitely cat).
 */

#define CAT_LABEL 3
void net_classify_cats(network_t* net, vol_t** input, double* output, int n) {
  #pragma omp parallel
  {
    batch_t* batch = make_batch(net, 8);

    #pragma omp for
    for (int i = 0; i < n; i+=8) {
      copy_vol(batch[0][0], input[i]);
      copy_vol(batch[0][1], input[i+1]);
      copy_vol(batch[0][2], input[i+2]);
      copy_vol(batch[0][3], input[i+3]);
      copy_vol(batch[0][4], input[i+4]);
      copy_vol(batch[0][5], input[i+5]);
      copy_vol(batch[0][6], input[i+6]);
      copy_vol(batch[0][7], input[i+7]);
      net_forward(net, batch, 0, 7);
      output[i] = batch[11][0]->w[CAT_LABEL]; 
      output[i+1] = batch[11][1]->w[CAT_LABEL]; 
      output[i+2] = batch[11][2]->w[CAT_LABEL]; 
      output[i+3] = batch[11][3]->w[CAT_LABEL]; 
      output[i+4] = batch[11][4]->w[CAT_LABEL]; 
      output[i+5] = batch[11][5]->w[CAT_LABEL]; 
      output[i+6] = batch[11][6]->w[CAT_LABEL]; 
      output[i+7] = batch[11][7]->w[CAT_LABEL]; 
    }
    free_batch(batch, 8);
  }
  // printf("conv level 1 %lu\n", t_conv1);
  // printf("conv level 2 %lu\n", t_conv2);
  // printf("conv level 3 %lu\n", t_conv3);
  // printf("relu level 1 %lu\n", t_relu1);
  // printf("relu level 2 %lu\n", t_relu2);
  // printf("relu level 3 %lu\n", t_relu3);
  // printf("pool level 1 %lu\n", t_pool1);
  // printf("pool level 2 %lu\n", t_pool2);
  // printf("pool level 3 %lu\n", t_pool3);
  // printf("fc level 1 %lu\n", t_fc);
  // printf("softmax level 1 %lu\n", t_softmax);
  // printf("total time %lu\n", t_total);
}

// IGNORE EVERYTHING BELOW THIS POINT -----------------------------------------

// Including C files in other C files is very bad style and should be avoided
// in any real application. We do it here since we want everything that you
// may edit to be in one file, without having to fix the interfaces between
// the different components of the system.

#include "util.c"
#include "main.c"
